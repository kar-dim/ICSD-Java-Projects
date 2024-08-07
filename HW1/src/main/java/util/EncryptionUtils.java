package sec3.util;

import sec3.MainMenu;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EncryptionUtils {

    //μέθοδος που ελέγχει αν τα κλειδιά υπάρχουν
    public static boolean areKeysPresent() {
        return new File("HW1/private.key").exists() && new File("HW1/public.key").exists();
    }

    //Μέθοδος που κρυπτογραφεί ένα string με βάση το public key
    public static byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            // κρυπτογράφηση
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, e);
        }
        return cipherText;
    }

    //αποκρυπτοράφηση byte[] δεδομένων με βάση το private key
    public static String decrypt(byte[] text, PrivateKey key) {
        byte[] decryptedText = null;
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            //αποκρυπτογράφηση
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = cipher.doFinal(text);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new String(decryptedText);
    }

    public static Key getKey(String name) {
        ObjectInputStream ois = null;
        Key key = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(name));
            key = name.equals("HW1/private.key") ? (PrivateKey) ois.readObject() : (PublicKey) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
        return key;
    }

    //έλεγχος αν υπάρχει το αρχείο "users.bin"
    public static boolean isUsersFilePresent() {
        return (new File("HW1/users.bin").exists());
    }

    //δημιουργία του αρχείου χρηστών
    public static void createUsersFile() {
        try {
            File users_file = new File("HW1/users.bin");
            if (users_file.getParentFile() != null) {
                users_file.getParentFile().mkdirs();
            }
            users_file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //συνάρτηση που δημιουργεί τα κλειδιά
    public static void generateKeys() {
        try {
            //RSA-2048
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            //παίρνουμε το ζεύγος private/public keys
            final KeyPair key = keyGen.generateKeyPair();

            File privateKeyFile = new File("HW1/private.key");
            File publicKeyFile = new File("HW1/public.key");

            // δημιουργία των αρχείων που θα κρατήσουν τα κλειδιά
            //δεν έχουμε βάλει ακόμα τα κλειδιά, απλώς δημιυργούμε τα αρχεία
            if (privateKeyFile.getParentFile() != null) {
                privateKeyFile.getParentFile().mkdirs();
            }
            privateKeyFile.createNewFile();

            if (publicKeyFile.getParentFile() != null) {
                publicKeyFile.getParentFile().mkdirs();
            }
            publicKeyFile.createNewFile();

            try ( // σώζουμε στα αρχεία τα κλειδιά
                  ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(publicKeyFile))) {
                publicKeyOS.writeObject(key.getPublic());
            }

            try (ObjectOutputStream privateKeyOS = new ObjectOutputStream(new FileOutputStream(privateKeyFile))) {
                privateKeyOS.writeObject(key.getPrivate());
            }

        } catch (NoSuchAlgorithmException | IOException e) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
