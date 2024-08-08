package ui.action;

import domain.Announcement;
import domain.Message;
import domain.User;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.IntStream;

import static util.MessageType.DELETE;
import static util.MessageType.DELETE_FAIL;
import static util.NetworkOperations.*;

public class DeleteAnnouncementsActionListener extends ActionBase {
    private final List<JCheckBox>deleteAnnouncementCheck;
    private final List<Announcement> userAnnouncements;
    private final String userName;
    private final String password;

    public DeleteAnnouncementsActionListener(JTabbedPane tabs, List<JCheckBox>deleteAnnouncementCheck, List<Announcement> userAnnouncements, String userName, String password) {
        super(tabs);
        this.deleteAnnouncementCheck = deleteAnnouncementCheck;
        this.userAnnouncements = userAnnouncements;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public void doAction() {
        try {
            initializeConnection();
            //κρατάμε τις ανακοινώσεις που δεν έχουν επιλεχτεί από τον χρήστη για διαγραφή
            List<Announcement> userKeep = IntStream.range(0, deleteAnnouncementCheck.size())
                    .filter(index -> !deleteAnnouncementCheck.get(index).isSelected())
                    .mapToObj(userAnnouncements::get)
                    .toList();
            writeMessage(new Message(DELETE, new User(userName, password), userKeep));
            Message msg = readMessage();
            //έλεγχος
            if (msg.message().equals(DELETE_FAIL)) {
                JOptionPane.showMessageDialog(tabs, "Failed to delete your announcements", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            //not fail, update userAnnouncements
            userAnnouncements.clear();
            userAnnouncements.addAll(userKeep);
            JOptionPane.showMessageDialog(tabs, "Successfully deleted your announcements", "Success", JOptionPane.INFORMATION_MESSAGE);
            tabs.setSelectedIndex(0);
            tabs.setSelectedIndex(4); //refresh table
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            closeConnection();
        }
    }
}
