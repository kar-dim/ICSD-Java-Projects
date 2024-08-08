package ui.action;

import domain.Message;
import domain.User;
import util.Session;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.IOException;
import java.util.logging.Level;

import static util.MessageType.LOGIN;
import static util.MessageType.LOGIN_OK;
import static util.NetworkOperations.*;

public class LoginActionListener extends ActionBase {
    private final String userName;
    private final char[] password;

    public LoginActionListener(JTabbedPane tabs, String userName, char[] password) {
        super(tabs);
        this.userName = userName;
        this.password = password;
    }

    @Override
    public void doAction() {
        try {
            //έλεγχος κενών τιμών
            if (userName.isEmpty() || password == null || password.length < 6) {
                JOptionPane.showMessageDialog(tabs, "Cannot Login, please check your data", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            initializeConnection();
            //γράψιμο στο stream
            writeMessage(new Message(LOGIN, new User(userName, new String(password))));

            //έλεγχος μηνύματος
            Message msg = readMessage();
            if (msg.message().equals(LOGIN_OK)) {
                tabs.setEnabledAt(3, true);
                tabs.setEnabledAt(4, true);
                JOptionPane.showMessageDialog(tabs, "Welcome back", "Success", JOptionPane.INFORMATION_MESSAGE);
                //κρατάμε τον logged_in user αν επιτύχει το login
                Session.setLoggedInUser(new User(userName, new String(password)));
                return;
            }
            JOptionPane.showMessageDialog(tabs, "Could not login, maybe there is already a user with this username or your password is incorrect", "Error in login", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            closeConnection();
        }
    }
}
