package ui.action;

import domain.User;
import util.Constants;
import util.Session;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.logging.Level;

import static util.EncryptionUtils.*;
import static util.FileUtils.*;

public class LoginActionListener extends ActionBase {
    private final String loginUserName;
    private final char[] loginPass;

    public LoginActionListener(JTabbedPane tabs, String loginUserName, char[] loginPass) {
        super(tabs);
        this.loginUserName = loginUserName;
        this.loginPass = loginPass;
    }

    @Override
    public boolean doAction() {
        if (!isUsersFilePresent())
            createUsersFile();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Constants.USERS_FILE_PATH))) {
            User userFromFile;
            while ((userFromFile = (User) ois.readObject()) != null) {
                if (userFromFile.getUsername().equals(loginUserName)) {
                    String currentPasswordToCheck = new String(loginPass) + userFromFile.getSalt();
                    String decryptedPassFromFile = new String(decrypt(userFromFile.getPassword(), privateKey), StandardCharsets.UTF_8);
                    //hash με τον αλγόριθμο sha256 για έλεγχο
                    if (decryptedPassFromFile.equals(SHA256Hash(currentPasswordToCheck))) {
                        Session.setLoggedInUser("HW1/" + removeSpecialCharacters(loginUserName) + "/");
                        if (!verifyUserFilesSignature(publicKey)) {
                            JOptionPane.showMessageDialog(tabs, "Critical Error! Could not verify your data integrity, someone may have edited your files, or your files may be corrupted", "Warning", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        //επιτυχές Login
                        return true;
                    } else {
                        //λάθος κωδικός
                        JOptionPane.showMessageDialog(tabs, "Cannot login, wrong password", "Failure", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
            }

        } catch (EOFException eofe) {
            //αν δε βρεθεί χρήστης με το συγκεκριμένο username τότε λάθος
            JOptionPane.showMessageDialog(tabs, "No username found with the username specified", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NoSuchFileException nsfe) {
            return true; //δεν υπαρχει signature file, επιτρέπουμε login, οχι απαραίτητα σφάλμα (first login πχ)
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
