package ui.action;

import domain.User;
import exception.PasswordTooShortException;
import exception.UserExistsException;
import util.Constants;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.*;
import java.security.PublicKey;
import java.util.logging.Level;

import static util.Constants.ENCRYPTED_AES_KEY_FILE_NAME;
import static util.EncryptionUtils.*;
import static util.FileUtils.*;

public class RegisterActionListener extends ActionBase {
    private final String registerUsername;
    private final String registerName;
    private final String registerLastName;
    private final char[] registerPass;

    public RegisterActionListener(JTabbedPane tabs, String registerUsername, String registerName, String registerLastName, char[] registerPass) {
        super(tabs);
        this.registerUsername = registerUsername;
        this.registerName = registerName;
        this.registerLastName = registerLastName;
        this.registerPass = registerPass;
    }

    @Override
    public boolean doAction() {
        if (!isUsersFilePresent())
            createUsersFile();
        try {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Constants.USERS_FILE_PATH))) {
                User userFromFile;
                while ((userFromFile = (User) ois.readObject()) != null) {
                    if (userFromFile.getUsername().equals(registerUsername))
                        throw new UserExistsException();
                }
            } catch (EOFException ignored) {
            }

            //αν ο κωδικός είναι μικρότερος από 6 χαρακτήρες τότε σφάλμα
            if (new String(registerPass).length() < 6) {
                throw new PasswordTooShortException();
            }
            //πρόσθεση του salt στον κωδικό και έπειτε hash με sha256. Ο κωδικος θα γινει encrypt
            String salt = createSalt();
            String saltedPassword = new String(registerPass) + salt;
            byte[] encryptedHashedPassword = encrypt(SHA256Hash(saltedPassword), publicKey);

            //τωρα θα δημιουρήσουμε τον φάκελο για αυτόν τον χρήστη
            String userPath = "HW1/" + removeSpecialCharacters(registerUsername);
            boolean isNewUserPathExists = new File(userPath).mkdir();
            //αν επιτύχει η δημιουργία του φακέλου τότε θέτουμε το όνομα του σε μια ιδιότητα του αντικειμένου user
            if (isNewUserPathExists) {
                //θα κρυπτογραφήσουμε ένα νέο συμμετρικό κλειδί (AES) για τον χρήστη
                byte[] encryptedAesKey = encryptNewKey(publicKey);
                //αποθήκευση στο αρχείο (και αν υπάρχει overwrite) encrypted.aeskey
                if (new File(userPath + "/" + ENCRYPTED_AES_KEY_FILE_NAME).createNewFile()) {
                    //γράψιμο τα bytes στο αρχείο encrypted.aeskey
                    try (FileOutputStream fos = new FileOutputStream(userPath + "/" + ENCRYPTED_AES_KEY_FILE_NAME);
                         ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Constants.USERS_FILE_PATH))) {
                        fos.write(encryptedAesKey);
                        oos.writeObject(new User(registerUsername, encryptedHashedPassword, registerName, registerLastName, salt));
                        JOptionPane.showMessageDialog(tabs, "Successfully registered! Please select the Login tab to login", "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    }
                } else {
                    JOptionPane.showMessageDialog(tabs, "Cannot register, application error", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } //αν δε μπορεί να δημιουργήσει το φάκελο σημαίνει πς υπάρχει ήδη και άρα δε μπορεί να γίνει register
            else {
                throw new UserExistsException();
            }

        } catch (UserExistsException uee) {
            JOptionPane.showMessageDialog(tabs, "Could not create account,there is already a user with this username", "Error creating account", JOptionPane.ERROR_MESSAGE);
        } catch (PasswordTooShortException pts) {
            JOptionPane.showMessageDialog(tabs, "Could not create account,password too short, must be greater than 5 characters", "Error creating account", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(tabs, "Cannot register, application error", "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
