/* icsd13072 Karatzas Dimitris
   icsd13096 Lazaros Apostolos*/
//απλή κλάση η οποία περιέχει τη main μέθοδο του project και δημιουργεί το frame στο οποίο θα μπει το GUI της κλάσης ui.MainMenu

import domain.Message;
import ui.MainMenu;
import util.Session;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.END;
import static util.NetworkOperations.initializeConnection;
import static util.NetworkOperations.writeMessage;

public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    public static void main(String[] args) {
        JFrame frame = new JFrame("Reservations");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.add(new MainMenu(), BorderLayout.CENTER);
        frame.pack(); //να πιάνει όσο χώρο χρειάζεται
        frame.setResizable(false);
        //όταν κλείσει το παράθυρο βάζουμε listener για να σταλεί στον server το μήνυμα END
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    initializeConnection();
                    writeMessage(new Message(END, Session.getLoggedInUser()));
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
