package ui;

/* icsd13072 Karatzas Dimitris
   icsd13096 Lazaros Apostolos*/

import domain.Announcement;
import domain.Message;
import domain.User;
import util.NetworkOperations;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class MainMenu extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(MainMenu.class.getName());
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final JTabbedPane tabs;
    private JTextArea newAnnouncementText;
    private JCheckBox[] deleteAnnouncementCheck; //checkboxes για επιλογή αν θα διαγραφτεί η ανακοίνωση
    private JPanel del_ins_panel;
    private JTable table;
    private JTextField username_tf, username_tf_l, lname_tf, name_tf, date_1_tf, date_2_tf;
    private JPasswordField password_pf, password_pf_l;
    private JButton register_b, login_b, view_b, create_b, delete_b, update_b;
    private List<Announcement> userAnnouncements;
    private TableModel announcementsTableModel = new TableModel(0, 3);
    private final NetworkOperations network;
    public static User logged_in;

    public MainMenu() {
        super(new GridLayout(1, 1));
        del_ins_panel = new JPanel();
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab("Register", createRegisterPanel());
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("View Announcements", createViewPanel());
        tabs.addTab("Insert announcement", createInsertPanel());
        tabs.addTab("Edit/Delete Your Announcements", createEditDeletePanel());
        tabs.setEnabledAt(3, false);
        tabs.setEnabledAt(4, false);
        add(tabs);
        network = new NetworkOperations();
        addActionListeners();

    }

    private JPanel createRegisterPanel() {
        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new GridLayout(5, 2));

        jPanel1.add(new JLabel("Username:"));
        username_tf = new JTextField(20);
        jPanel1.add(username_tf);

        jPanel1.add(new JLabel("Password:"));
        password_pf = new JPasswordField(20);
        jPanel1.add(password_pf);

        jPanel1.add(new JLabel("Name:"));
        name_tf = new JTextField(20);
        jPanel1.add(name_tf);

        jPanel1.add(new JLabel("Last Name:"));
        lname_tf = new JTextField(20);
        jPanel1.add(lname_tf);

        register_b = new JButton("Register");
        jPanel1.add(register_b);
        return jPanel1;
    }

    private JPanel createLoginPanel() {
        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout(new GridLayout(3, 2));

        jPanel2.add(new JLabel("Username:"));
        username_tf_l = new JTextField(20);
        jPanel2.add(username_tf_l);

        jPanel2.add(new JLabel("Password:"));
        password_pf_l = new JPasswordField(20);
        jPanel2.add(password_pf_l);

        login_b = new JButton("Login");
        jPanel2.add(login_b);
        return jPanel2;
    }

    private JPanel createViewPanel() {
        JPanel jPanel3 = new JPanel();
        jPanel3.setLayout(new GridLayout(3, 2));

        jPanel3.add(new JLabel("Search Dates from (dd-MM-yyyy only):"));
        date_1_tf = new JTextField(20);
        jPanel3.add(date_1_tf);

        jPanel3.add(new JLabel("Search Dates to (dd-MM-yyy only):"));
        date_2_tf = new JTextField(20);
        jPanel3.add(date_2_tf);

        //προσθήκη του κουμπιού
        view_b = new JButton("Search");
        jPanel3.add(view_b);
        return jPanel3;
    }

    private JPanel createInsertPanel() {
        JPanel jPanel4 = new JPanel();
        jPanel4.setLayout(new GridLayout(2, 1));

        //Στο πεδίο αυτό θα γραφτεί η ανακοίνωση
        newAnnouncementText = new JTextArea(7, 57);
        JScrollPane scroll = new JScrollPane(newAnnouncementText);
        jPanel4.add(scroll, BorderLayout.CENTER);

        create_b = new JButton("Create");
        //panel για το κουμπί ώστε να μη πιάνει τη μισή οθόνη
        JPanel button_panel = new JPanel();
        button_panel.setLayout(new BorderLayout());
        button_panel.add(create_b, BorderLayout.SOUTH);

        jPanel4.add(button_panel);
        return jPanel4;
    }

    private JPanel createEditDeletePanel() {
        del_ins_panel = new JPanel();
        delete_b = new JButton("Delete");
        update_b = new JButton("Update");
        return del_ins_panel;
    }

    private void addActionListeners() {
        //listener για tab τροποποίησης ή διαγραφής ανακοίνωσης, θα πάρουμε τις ανακοινώσεις που έχει ο συγκεκριμένος χρήστης και θα 
        //τις βάλουμε στο table, στη συνέχεια θα βάλουμε ένα scrollpane στο οποίο θα μπούν οι ανακοινώσεις σε μορφή πίνακα (JTable)
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 4) {
                //αν επιλεχθεί το tab αυτό, ξεκινάμε κανονικά όπως με τα κουμπιά, αρχικά sockets/streams κτλ
                //λίστα που έχει μέσα ποια indexes των ανακοινώσεων θα διαγραφούν
                try {
                    network.initializeConnection();
                    network.writeMessage(new Message(EDIT_DELETE, logged_in.getUsername()));
                    Message msg = (Message) network.getOis().readObject(); //διάβασμα της απάντησης
                    if (msg.getMessage().equals(EDIT_DELETE_FAIL)) {
                        JOptionPane.showMessageDialog(tabs, "Error trying to access data from server, or you don't have any announcements", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    //πήραμε τη λίστα με τις ανακοινώσεις και γίνεται δέσμευση μνήμης στα στοιχεία ανάλογα το μέγεθος
                    userAnnouncements = new ArrayList<>(msg.getAnnouncements());
                    int size = userAnnouncements.size();
                    deleteAnnouncementCheck = new JCheckBox[size]; //αν επιλεχθει μια ανακοινωση σημαινει πως ο χρηστης θελει να διαγραφει
                    //χρειαζόμαστε το table model για να γράψουμε στο table τις γραμμές
                    announcementsTableModel = new TableModel(size, 3);
                    table = new JTable();
                    table.setModel(announcementsTableModel);
                    //θέτουμε false στο να ειναι τροποποιήσιμα τα πεδία
                    //για κάθε γραμμή βάζουμε την ανακοίνωση και την ημερομηνία τελευταίας τροποποίησης
                    Object[][] row_data = new Object[size][3];
                    for (int i = 0; i < size; i++) {
                        deleteAnnouncementCheck[i] = new JCheckBox();
                        row_data[i][0] = false;
                        row_data[i][1] = userAnnouncements.get(i).getAnnouncement();
                        row_data[i][2] = userAnnouncements.get(i).getLastEditDate();
                        announcementsTableModel.setValueAt(row_data[i][0], i, 0);
                        announcementsTableModel.setValueAt(row_data[i][1], i, 1);
                        announcementsTableModel.setValueAt(row_data[i][2], i, 2);
                        announcementsTableModel.addTableModelListener(event -> {
                            int row = event.getFirstRow();
                            int col = event.getColumn();
                            TableModel source = (TableModel) event.getSource();
                            if (col == 0) {
                                boolean markForDeletion = (Boolean) source.getValueAt(row, col);
                                deleteAnnouncementCheck[row].setSelected(markForDeletion);
                            } else if (col == 1) {
                                String newAnnouncement = (String) source.getValueAt(row, col);
                                userAnnouncements.get(row).setAnnouncement(newAnnouncement);
                                userAnnouncements.get(row).setLastEditDate(LocalDate.now());
                            }
                        });
                    }

                    //αφού τελειώσουμε με τον κώδικα για τη μεταφορά δεδομένων και τους ελέγχους, πρέπει να φτιάξουμκε και το layout
                    //του del_ins_panel
                    del_ins_panel.removeAll();
                    del_ins_panel.setLayout(new GridLayout(2, 1));
                    JScrollPane jsp = new JScrollPane(table);

                    del_ins_panel.add(jsp);

                    JPanel buttons = new JPanel();
                    buttons.setLayout(new FlowLayout());
                    buttons.add(delete_b);
                    buttons.add(update_b);

                    del_ins_panel.add(buttons);

                    tabs.revalidate();
                    tabs.repaint();

                } catch (IOException | ClassNotFoundException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                } finally {
                    network.closeConnection();
                }
            }
        });

        delete_b.addActionListener(e -> {
            try {
                network.initializeConnection();
                //ενημερώνουμε την λίστα και έπειτα θα τη γράψουμε στο socket ώστε να ενημερωθεί στον server
                for (int i = deleteAnnouncementCheck.length - 1; i >= 0; i--) {
                    if (deleteAnnouncementCheck[i].isSelected())
                        userAnnouncements.remove(i);
                }
                //γράψιμο της λίστας
                network.writeMessage(new Message(DELETE, userAnnouncements, new User(username_tf_l.getText(), new String(password_pf_l.getPassword()))));
                //διάβασμα αν γράφτηκε σωστά
                Message msg = (Message) network.getOis().readObject();
                //έλεγχος
                if (msg.getMessage().equals(DELETE_FAIL)) {
                    JOptionPane.showMessageDialog(tabs, "Failed to delete your announcements", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(tabs, "Successfully deleted your announcements", "Success", JOptionPane.INFORMATION_MESSAGE);
                    tabs.setSelectedIndex(0);
                    tabs.setSelectedIndex(4); //refresh table
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                network.closeConnection();
            }
        });

        update_b.addActionListener(e -> {
            try {
                network.initializeConnection();
                network.writeMessage(new Message(EDIT, userAnnouncements, new User(username_tf_l.getText(), new String(password_pf_l.getPassword()))));
                //διάβασμα αν γράφτηκε σωστά
                Message msg = (Message) network.getOis().readObject();
                //έλεγχος
                if (msg.getMessage().equals(EDIT_FAIL)) {
                    JOptionPane.showMessageDialog(tabs, "Failed to update your announcements", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(tabs, "Successfully updated your announcements", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                network.closeConnection();
            }
        });

        register_b.addActionListener(e -> {
            try {
                //έλεγχος αν υπάρχει έστω και μια κενή τιμή , αν υπάρχει τότε nullpointer exception και μήνυμα λάθους
                //επίσης ελέγχουμε και αν το μήκος του κωδικού είναι τουλάχιστον 6
                if (username_tf.getText().isEmpty() || name_tf.getText().isEmpty() || lname_tf.getText().isEmpty() || username_tf.getText().isEmpty() || password_pf.getPassword().length < 6 || password_pf.getPassword() == null) {
                    JOptionPane.showMessageDialog(tabs, "Could not make your account, please check your input data, password must be greater than 6 characters long", "Error registering", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                network.initializeConnection();
                //αν όλα πάνε καλά μέχρι στιγμής τότε στέλνουμε τον νέο χρήστη στον server καλώντας τον κατάλληλο constructor του Message
                network.writeMessage(new Message(REGISTER, new User(username_tf.getText(), new String(password_pf.getPassword()), name_tf.getText(), lname_tf.getText())));
                //διάβασμα της απάντησης του server
                Message msg = (Message) network.getOis().readObject();
                if (msg.getMessage().equals(REGISTER_OK)) {
                    JOptionPane.showMessageDialog(tabs, "Successfully registered! Please select the Login tab to login", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(tabs, "Could not make your account, maybe there is already a user with this username", "Error registering", JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                network.closeConnection();
            }
        });

        //όταν πατηθεί το κουμπί του login
        login_b.addActionListener(e -> {
            try {
                //έλεγχος κενών τιμών
                if (username_tf_l.getText().isEmpty() || password_pf_l.getPassword().length < 6 || password_pf_l.getPassword() == null) {
                    JOptionPane.showMessageDialog(tabs, "Cannot Login, please check your data", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                network.initializeConnection();
                //γράψιμο στο stream
                network.writeMessage(new Message(LOGIN, new User(username_tf_l.getText(), new String(password_pf_l.getPassword()))));

                //έλεγχος μηνύματος
                Message msg = (Message) network.getOis().readObject();
                if (msg.getMessage().equals(LOGIN_OK)) {
                    //αν το login επιτύχει τότε ενεργοποίηση των tabs τροποποίησης/διαγραφής και εισαγωγής
                    tabs.setEnabledAt(3, true);
                    tabs.setEnabledAt(4, true);
                    JOptionPane.showMessageDialog(tabs, "Welcome back", "Success", JOptionPane.INFORMATION_MESSAGE);
                    //κρατάμε τον logged_in user αν επιτύχει το login
                    logged_in = new User(username_tf_l.getText(), new String(password_pf_l.getPassword()));
                } else {
                    //εφόσον τα πεδία δεν είναι κενά (και εφόσον σταλούν επιτυχημένα στο stream, δηλαδή δεν έχουμε IOException, αλλά ο server στείλει LOGIN FAIL τότε
                    //σημαίνει πως υπάρχει ήδη λογαριασμός με αυτό το username
                    JOptionPane.showMessageDialog(tabs, "Could not login, maybe there is already a user with this username or your password is incorrect", "Error in login", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                network.closeConnection();
            }
        });

        view_b.addActionListener(e -> {
            try {
                //παίρνουμε τα Dates από τον client
                LocalDate dateStart = LocalDate.parse(date_1_tf.getText(), dateFormatter);
                LocalDate dateEnd = LocalDate.parse(date_2_tf.getText(), dateFormatter);

                if (dateStart.isAfter(dateEnd)) {
                    JOptionPane.showMessageDialog(tabs, "Could not search announcements, maybe the first date is after the second?", "Error searching data", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                network.initializeConnection();
                //γράφουμε στο socket τις ημερομηνίες και το μήνυμα (δε χρειάζεται να στείλουμε τον χρήστη αφού ο καθένας μπορεί να κάνει view)
                network.writeMessage(new Message(VIEW, dateStart, dateEnd));
                //έλεγχος μηνύματος
                Message msg = (Message) network.getOis().readObject();
                if (msg.getMessage().equals(VIEW_OK)) {

                    //αρχικοποίηση της λίστας με βάση τη λίστα που έστειλε ο σερβερ
                    ArrayList<Announcement> list = new ArrayList<>(msg.getAnnouncements());

                    JFrame jf = new JFrame("Search results");
                    jf.setVisible(true);

                    JPanel panel = new JPanel();
                    JTextArea jta = new JTextArea(20, 40);
                    jta.setEditable(false); //εφόσον αρχικοποιηθεί, δεν επιτρέπουμε την αλλαγή του αφού είναι read only
                    JScrollPane jsp = new JScrollPane(jta);

                    //προσθήκη των ανακοινώσεων στο textarea
                    for (Announcement announcement : list) {
                        jta.append(announcement.toString());
                    }
                    panel.add(jsp);
                    //προσθήκη όλου του πανελ στο frame
                    jf.add(panel);
                    jf.pack();
                } else {
                    //εφόσον τα πεδία δεν είναι κενά (και εφόσον σταλούν επιτυχημένα στο stream, δηλαδή δεν έχουμε IOException, αλλά ο server στείλει VIEW FAIL
                    //σημαίνει πως δε βρέθηκε κάποια ανακοίνωση σε αυτό το εύρος ημερομηνιών
                    JOptionPane.showMessageDialog(tabs, "Could not find any announcements", "Nothing found", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ClassNotFoundException | IOException ex) {
                JOptionPane.showMessageDialog(tabs, "Could not search announcements", "Error searching data", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(tabs, "Could not search announcements, make sure your dates are in format dd-MM-yyyy", "Error searching data", JOptionPane.ERROR_MESSAGE);
            } finally {
                network.closeConnection();
            }
        });

        create_b.addActionListener(e -> {
            try {
                //έλεγχος αν η τιμή είναι κενή
                if (newAnnouncementText.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(tabs, "Could not create your announcement, please check your details", "Error inserting data", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                network.initializeConnection();
                //στέλνουμε μήνυμα στο stream για εισαγωγή
                network.writeMessage(new Message(CREATE, new Announcement(newAnnouncementText.getText(), username_tf_l.getText(), LocalDate.now())));
                //έλεγχος
                Message msg = (Message) network.getOis().readObject();
                if (msg.getMessage().equals(CREATE_OK)) {
                    JOptionPane.showMessageDialog(tabs, "Successfully added your announcement", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(tabs, "Could not create your announcement", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(tabs, "Could not create announcement, server error", "Error inserting data", JOptionPane.ERROR_MESSAGE);
            } finally {
                network.closeConnection();
            }
        });
    }
}
