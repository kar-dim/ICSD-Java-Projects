package ui.action;

import domain.Announcement;
import domain.Message;
import domain.User;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import static util.MessageType.EDIT;
import static util.MessageType.EDIT_FAIL;
import static util.NetworkOperations.*;

public class UpdateAnnouncementsActionListener extends ActionBase {
    private final String userName;
    private final String password;
    private final List<Announcement> userAnnouncements;
    public UpdateAnnouncementsActionListener(JTabbedPane tabs, String userName, String password, List<Announcement> userAnnouncements) {
        super(tabs);
        this.userName = userName;
        this.password = password;
        this.userAnnouncements = userAnnouncements;
    }

    @Override
    public void doAction() {
        try {
            initializeConnection();
            writeMessage(new Message(EDIT, new User(userName,password),userAnnouncements));
            Message msg = readMessage();
            if (!msg.message().equals(EDIT_FAIL)) {
                JOptionPane.showMessageDialog(tabs, "Successfully updated your announcements", "Success", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(tabs, "Failed to update your announcements", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            closeConnection();
        }
    }
}
