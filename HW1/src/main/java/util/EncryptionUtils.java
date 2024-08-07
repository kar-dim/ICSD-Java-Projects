package util;

import domain.Expense;
import domain.Income;
import domain.Transaction;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.Constants.*;

public class EncryptionUtils {

    private static final Logger LOGGER = Logger.getLogger(EncryptionUtils.class.getName());

    //Μέθοδος που κρυπτογραφεί ένα string με βάση το public key
    public static byte[] encrypt(String text, PublicKey key) throws Exception {
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(text.getBytes());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    //αποκρυπτοράφηση byte[] δεδομένων με βάση το private key
    public static byte[] decrypt(byte[] text, PrivateKey key) throws Exception {
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(text);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    public static byte[] encryptNewKey(PublicKey publicKey) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey key = keyGenerator.generateKey();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(key.getEncoded());
    }

    private static boolean updateSignatureWithFileData(Signature sign) throws Exception {
        String loggedInUsernamePath = Session.getLoggedInUser();
        File expensesFile = new File(loggedInUsernamePath + EXPENSES_FILE_NAME);
        File incomeFIle = new File(loggedInUsernamePath + INCOME_FILE_NAME);
        if (expensesFile.exists() && incomeFIle.exists()) {
            sign.update(Files.readAllBytes(expensesFile.toPath()));
            sign.update(Files.readAllBytes(incomeFIle.toPath()));
        } else if (expensesFile.exists()) {
            sign.update(Files.readAllBytes(expensesFile.toPath()));
        } else if (incomeFIle.exists()) {
            sign.update(Files.readAllBytes(incomeFIle.toPath()));
        } else {
            return false;
        }
        return true;
    }

    public static boolean verifyUserFilesSignature(PublicKey publicKey) throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        byte[] signBytesOnFile = Files.readAllBytes(new File(Session.getLoggedInUser() + SIGNATURE_FILE_NAME).toPath());
        //αν δεν υπαρχουν αρχεια, δεν υπογραφουμε
        if (!updateSignatureWithFileData(sign))
            return true;
        return sign.verify(signBytesOnFile);
    }

    public static byte[] signUserFiles() throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA"); //αντικείμενο υπογραφής
        sign.initSign(Session.getPrivateKey());
        //αν δεν υπαρχουν αρχεια, δεν επαληθευουμε την υπογραφη
        if (!updateSignatureWithFileData(sign))
            return null;
        return sign.sign();
    }

    //συνάρτηση που δημιουργεί τα κλειδιά
    public static void generateKeys() {
        try {
            //RSA-2048
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair key = keyGen.generateKeyPair();

            File privateKeyFile = new File(PRIVATE_KEY_PATH);
            File publicKeyFile = new File(PUBLIC_KEY_PATH);

            // δημιουργία των αρχείων που θα κρατήσουν τα κλειδιά
            //δεν έχουμε βάλει ακόμα τα κλειδιά, απλώς δημιυργούμε τα αρχεία
            if (privateKeyFile.getParentFile() != null)
                privateKeyFile.getParentFile().mkdirs();
            privateKeyFile.createNewFile();
            if (publicKeyFile.getParentFile() != null)
                publicKeyFile.getParentFile().mkdirs();
            publicKeyFile.createNewFile();

            try (ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(publicKeyFile))) {
                publicKeyOS.writeObject(key.getPublic());
            }
            try (ObjectOutputStream privateKeyOS = new ObjectOutputStream(new FileOutputStream(privateKeyFile))) {
                privateKeyOS.writeObject(key.getPrivate());
            }

        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    public static String createSalt() {
        SecureRandom random = new SecureRandom();
        //20 random bytes (salt)
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String SHA256Hash(String str) throws NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(str.getBytes());
        for (byte oneByte : messageDigest.digest())
            sb.append(Integer.toString((oneByte & 0xff) + 0x100, 16).substring(1));
        return sb.toString();
    }

    public static String decryptRecordsFromFile(PrivateKey privateKey, String fileName, int month) {
        String loggedInUsernamePath = Session.getLoggedInUser();
        boolean recordFile = new File(loggedInUsernamePath + fileName).exists();
        boolean isExpense = fileName.equals(EXPENSES_FILE_NAME);
        //αν δεν υπάρχει τότε δεν εμφανίζουμε κάποιο αποτέλεσμα
        if (!recordFile)
            return "Nothing found!";
        double total = 0;
        StringBuilder sb = new StringBuilder();
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            File keyFile = new File(loggedInUsernamePath + ENCRYPTED_AES_KEY_FILE_NAME);
            SecretKey decryptedKey;
            if (keyFile.exists()) {
                byte[] decodedKeyBytes = cipher.doFinal(Files.readAllBytes(keyFile.toPath()));
                decryptedKey = new SecretKeySpec(decodedKeyBytes, 0, decodedKeyBytes.length, "AES");
            } else {
                throw new FileNotFoundException();
            }
            cipher.init(Cipher.DECRYPT_MODE, decryptedKey);
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(loggedInUsernamePath + fileName))) {
                Transaction record;
                SealedObject sealed;
                while ((sealed = (SealedObject) ois.readObject()) != null) {
                    record = isExpense ? (Expense) sealed.getObject(cipher) : (Income) sealed.getObject(cipher);
                    if (month == record.getMonth()) {
                        sb.append(record).append("\n\n");
                        total += record.getValue();
                    }
                }
            }

        } catch (EOFException eofe) { //αν φτάσει στο τέλος του αρχείου δεν κάνουμε κάτι
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        sb.append("Total Value: ").append(total);
        return sb.toString();
    }
}
