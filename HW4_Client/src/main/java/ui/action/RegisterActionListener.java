package ui.action;

import domain.Message;
import domain.User;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.IOException;
import java.util.logging.Level;

import static util.MessageType.REGISTER;
import static util.MessageType.REGISTER_OK;
import static util.NetworkOperations.*;

public class RegisterActionListener extends ActionBase {
    private final String userName;
    private final String name;
    private final String lastName;
    private final char[] password;

    public RegisterActionListener(JTabbedPane tabs, String userName, String name, String lastName, char[] password) {
        super(tabs);
        this.userName = userName;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
    }

    @Override
    public void doAction() {
        try {
            if (userName.isEmpty() || name.isEmpty() || lastName.isEmpty() || password == null || password.length < 6) {
                JOptionPane.showMessageDialog(tabs, "Could not make your account, please check your input data, password must be greater than 6 characters long", "Error registering", JOptionPane.ERROR_MESSAGE);
                return;
            }
            initializeConnection();
            writeMessage(new Message(REGISTER, new User(userName, new String(password), name, lastName)));
            Message msg = readMessage();
            if (msg.message().equals(REGISTER_OK)) {
                JOptionPane.showMessageDialog(tabs, "Successfully registered! Please select the Login tab to login", "Success", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(tabs, "Could not make your account, maybe there is already a user with this username", "Error registering", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            closeConnection();
        }
    }
}
