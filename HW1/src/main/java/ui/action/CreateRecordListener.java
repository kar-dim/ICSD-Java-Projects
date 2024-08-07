package ui.action;

import domain.Expense;
import domain.Income;
import domain.Transaction;
import util.AppendableObjectOutputStream;
import util.Session;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;

import static util.Constants.*;
import static util.EncryptionUtils.decrypt;

public class CreateRecordListener extends ActionBase {
    private final String createRecordDescription;
    private final String createDate;
    private final String createValue;
    private final String transactionOperation;
    public CreateRecordListener(JTabbedPane tabs, String createRecordDescription, String createDate, String createValue, String transactionOperation) {
        super(tabs);
        this.createRecordDescription = createRecordDescription;
        this.createDate = createDate;
        this.createValue = createValue;
        this.transactionOperation = transactionOperation;
    }
    @Override
    public boolean doAction() {
        try {
            if (createDate.isEmpty() || createValue.isEmpty() || createRecordDescription.isEmpty()) {
                JOptionPane.showMessageDialog(tabs, "Some fields are empty, please check your data", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            //αφού κανένα δεν είναι empty τώρα, ελέγχουμε αν η ημερομηνία είναι όντως σε σωστή μορφή καθώς επίσης και η τιμή να είναι αριθμός
            LocalDate date = LocalDate.parse(createDate, dateFormatter);
            double value = Double.parseDouble(createValue);
            appendRecordToFile(transactionOperation.equals("Expense"), value, date);
            return true;
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(tabs, "Wrong date format, must be in dd-MM-yyyy, example: 10-10-2010", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(tabs, "Cannot find encryption key!", "Critical error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(tabs, "Value must be a number", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private void appendRecordToFile(boolean isExpense, double recordValue, LocalDate recordDate) throws Exception {
        File keyFile = new File(Session.getLoggedInUser() + "/" + ENCRYPTED_AES_KEY_FILE_NAME);
        String fileToWrite = Session.getLoggedInUser() + (isExpense ? EXPENSES_FILE_NAME : INCOME_FILE_NAME);
        if (keyFile.exists()) {
            //το κρυπτογραφημένο AES key έχει ακριβώς 256bytes (RSA-2048 κρυπτογραφεί σε 256 bytes)
            byte[] decodedKeyBytes = decrypt(Files.readAllBytes(keyFile.toPath()), privateKey);
            SecretKey originalKey = new SecretKeySpec(decodedKeyBytes, 0, decodedKeyBytes.length, "AES");
            Transaction record = isExpense ? new Expense(recordValue, createRecordDescription, recordDate) : new Income(recordValue, createRecordDescription, recordDate);
            Cipher cipherEncrypt = Cipher.getInstance("AES");
            cipherEncrypt.init(Cipher.ENCRYPT_MODE, originalKey);
            SealedObject sealed = new SealedObject(record, cipherEncrypt);
            boolean fileExists = new File(fileToWrite).exists();
            new File(fileToWrite).createNewFile();
            try (ObjectOutputStream oos = fileExists ?
                    new AppendableObjectOutputStream(new FileOutputStream(fileToWrite, true)) :
                    new ObjectOutputStream(new FileOutputStream(fileToWrite))) {
                oos.writeObject(sealed);
            }
            JOptionPane.showMessageDialog(tabs, "Successfully added your record to the file", "Success", JOptionPane.INFORMATION_MESSAGE);

        } else {
            throw new FileNotFoundException();
        }
    }
}
