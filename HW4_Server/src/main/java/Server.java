/* icsd13072 Karatzas Dimitris
   icsd13096 Lazaros Apostolos*/
//η κλάση του server, χρειαζόμαστε νήματα διότι θα εξυπηρετούνται πολλοί πελάτες ταυτόχρονα

import domain.Announcement;
import domain.Message;
import domain.User;
import util.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class Server implements Runnable {

    private final Socket sock;
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final List<User> logged_in = new ArrayList<>();
    private static final List<Announcement> announcements = new ArrayList<>();
    private static final List<User> users = new ArrayList<>(Arrays.asList(
            new User("admin1", "admin1", "admin_a", "admin_aa"),
            new User("admin2", "admin2", "admin_b", "admin_bb")));

    public Server(Socket soc) {
        sock = soc;
    }

    //η main όταν τρέξει αυτόματα θα δημιουργεί συνέχεια Threads για κάθε accept της serversocket
    //έτσι δε θα περιμένει να τελειώσει κάποια accept αλλά θα τρέχουν παράλληλα πολλά threads
    public static void main(String[] args) {
        try (ServerSocket sc = new ServerSocket(5555)) { //5555 port
            while (true) {
                Socket sock = sc.accept();
                new Thread(new Server(sock)).start();
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Can't make the connection with the client", ioe);
        }
    }

    //κάθε νήμα θα εκτελεί τον παρακάτω κώδικα, δηλαδή για κάθε σύνδεση σε client (σε κάθε accept δηλαδή, όπως
    //φαίνεται και από την main μέθοδο) θα τρέχει η run() η οποία κάνει όλη τη δουλεία του server
    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());

            /* εδώ ξεκινάει η μεταφορά ωφέλιμων δεδομένων μεταξύ client και server */
            while (true) {
                Message startMsg = (Message) ois.readObject();
                writeMessage(oos, new Message(startMsg.getMessage().equals(START) ? STARTED : BAD_FORMAT));
                final Message msg = (Message) ois.readObject();
                switch (msg.getMessage()) {
                    case MessageType.REGISTER -> {
                        //έλεγχος αν ο χρήστης είναι ήδη εγγεγραμμένος, αν η λίστα με τους χρήστες είναι άδεια τότε απλά τον βάζουμε (δηλαδή δε θα μπει στο loop και το if (exists==false) θα τον βαλει
                        boolean exists = users.stream()
                                .anyMatch(user -> user.getUsername().equals(msg.getUser().getUsername()));
                         if (!exists) {
                            synchronized (this) {
                                users.add(new User(msg.getUser()));
                            }
                        }
                        writeMessage(oos, new Message(exists ? REGISTER_FAIL : REGISTER_OK));
                    }

                    case MessageType.LOGIN -> {
                        Optional<User> userExists = users.stream()
                                .filter(user -> user.getUsername().equals(msg.getUser().getUsername())).findFirst();
                        boolean isLoggedIn = userExists.isPresent() && logged_in.stream()
                                .anyMatch(user -> user.getUsername().equals(msg.getUser().getUsername()));

                        //αν δεν υπάρχει στη λίστα στέλνουμε στον client μήνυμα ότι δεν έχει κάνει register, ή αν είναι ήδη logged in (άλλη λίστα αυτή)
                        if (userExists.isEmpty() || isLoggedIn) {
                            writeMessage(oos, new Message(LOGIN_FAIL));
                        }
                        //έλεγχος του password
                        if (userExists.isPresent() && !isLoggedIn) {
                            if (userExists.get().getPassword().equals(msg.getUser().getPassword())) {
                                synchronized (this) {
                                    logged_in.add(new User(msg.getUser()));
                                }
                                writeMessage(oos, new Message(LOGIN_OK));
                            } //αν ο κωδικός δεν είναι σωστός, τότε μήνυμα λάθους
                            else {
                                writeMessage(oos, new Message(LOGIN_FAIL));
                            }
                        }
                    }

                    case MessageType.CREATE -> {
                        //εδώ δε χρειάζεται έλεγχος για το αν ο χρήστης υπάρχει
                        try {
                            synchronized (this) {
                                announcements.add(msg.getAnnouncement());
                            }
                            writeMessage(oos, new Message(CREATE_OK));
                        } catch (Exception e) {
                            writeMessage(oos, new Message(CREATE_FAIL));
                        }
                    }

                    case MessageType.EDIT_DELETE -> {
                        List<Announcement> listToSend = announcements.stream()
                                .filter(announcement -> announcement.getAuthor().equals(msg.getUser().getUsername()) || msg.getUser().getUsername().equals("admin1") || msg.getUser().getUsername().equals("admin2"))
                                .toList();
                        //αν η λίστα με τις ανακοινώσεις του χρήστη είναι άδεια σημαίνει πως δεν έχει βάλει ακόμα κάποια ανακοίνωση, οπότε μήνυμα λάθους
                        //πίσω στον client
                        if (listToSend.isEmpty()) {
                            writeMessage(oos, new Message(EDIT_DELETE_FAIL));
                        } else {
                            writeMessage(oos, new Message(EDIT_DELETE_OK, listToSend));
                        }
                    }

                    case MessageType.EDIT -> modifyAnnouncements(msg, EDIT_OK, EDIT_FAIL, oos);

                    case MessageType.DELETE -> modifyAnnouncements(msg, DELETE_OK, DELETE_FAIL, oos);

                    case MessageType.VIEW -> {
                        List<Announcement> listToSend = announcements.stream()
                                .filter(announcement -> (announcement.getLastEditDate().isAfter(msg.getFirstDate()) && (announcement.getLastEditDate().isBefore(msg.getSecondDate()))))
                                .toList();
                        //άδεια=δε βρέθηκε τίποτα, οπότε λάθος
                        if (listToSend.isEmpty())
                            writeMessage(oos, new Message(VIEW_FAIL));
                        else
                            writeMessage(oos, new Message(VIEW_OK, listToSend));
                    }

                    case MessageType.END -> {
                        //αν στείλει ο client "END" τότε αποσυνδέσουμε τον χρήστη
                        synchronized (this) {
                            logged_in.removeIf(user -> user.getUsername().equals(msg.getUser().getUsername()) && (user.getPassword().equals(msg.getUser().getPassword())));
                        }
                        connectionCleanup(sock, ois, oos);
                        return;
                    }

                    default -> {
                        writeMessage(oos, new Message(MessageType.BAD_FORMAT));
                        connectionCleanup(sock, ois, oos);
                        return;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void modifyAnnouncements(Message msg, String successMessage, String failureMessage, ObjectOutputStream oos) throws IOException {
        try {
            synchronized (this) {
                announcements.removeIf(announcement -> announcement.getAuthor().equals(msg.getUser().getUsername()));
                announcements.addAll(msg.getAnnouncements());
            }
            writeMessage(oos, new Message(successMessage));
        } catch (Exception e) {
            writeMessage(oos, new Message(failureMessage));
        }
    }

    private void writeMessage(ObjectOutputStream oos, Message message) throws IOException {
        oos.writeObject(message);
        oos.flush();
    }

    private void connectionCleanup(Socket sock, ObjectInputStream ois, ObjectOutputStream oos) throws IOException {
        sock.close();
        oos.close();
        ois.close();
    }
}
