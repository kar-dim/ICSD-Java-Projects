package ui.action;

import domain.Announcement;
import domain.Message;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.Level;

import static util.MessageType.CREATE;
import static util.MessageType.CREATE_OK;
import static util.NetworkOperations.*;

public class CreateAnnouncementActionListener extends ActionBase {
    private final String newAnnouncement;
    private final String username;
    public CreateAnnouncementActionListener(JTabbedPane tabs, String newAnnouncement, String username) {
        super(tabs);
        this.newAnnouncement = newAnnouncement;
        this.username = username;
    }

    @Override
    public void doAction() {
        try {
            //έλεγχος αν η τιμή είναι κενή
            if (newAnnouncement.isEmpty()) {
                JOptionPane.showMessageDialog(tabs, "Could not create your announcement, please check your details", "Error inserting data", JOptionPane.ERROR_MESSAGE);
                return;
            }
            initializeConnection();
            //στέλνουμε μήνυμα στο stream για εισαγωγή
            writeMessage(new Message(CREATE, new Announcement(newAnnouncement, username, LocalDate.now())));
            //έλεγχος
            Message msg = readMessage();
            if (msg.message().equals(CREATE_OK)) {
                JOptionPane.showMessageDialog(tabs, "Successfully added your announcement", "Success", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(tabs, "Could not create your announcement", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(tabs, "Could not create announcement, server error", "Error inserting data", JOptionPane.ERROR_MESSAGE);
            closeConnection();
        }
    }
}
