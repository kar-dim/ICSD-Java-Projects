package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.Constants.*;

public class FileUtils {
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

    //μέθοδος που ελέγχει αν τα κλειδιά υπάρχουν
    public static boolean areKeysPresent() {
        return new File(PRIVATE_KEY_PATH).exists() && new File(PUBLIC_KEY_PATH).exists();
    }

    //έλεγχος αν υπάρχει το αρχείο "users.bin"
    public static boolean isUsersFilePresent() {
        return new File(USERS_FILE_PATH).exists();
    }

    //δημιουργία του αρχείου χρηστών
    public static void createUsersFile() {
        try {
            File usersFile = new File(USERS_FILE_PATH);
            if (usersFile.getParentFile() != null)
                usersFile.getParentFile().mkdirs();
            usersFile.createNewFile();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static Key getKeyFromFile(String name) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(name))) {
            return name.equals(PRIVATE_KEY_PATH) ? (PrivateKey) ois.readObject() : (PublicKey) ois.readObject();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String removeSpecialCharacters(String str) {
        return str.replaceAll("[<>:/\\\\|?*]", "_");
    }
}
