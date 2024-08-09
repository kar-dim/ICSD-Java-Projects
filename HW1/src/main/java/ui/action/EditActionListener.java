package ui.action;

import domain.Expense;
import domain.Income;
import domain.Transaction;
import util.Session;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static util.Constants.*;
import static util.EncryptionUtils.decrypt;

public class EditActionListener extends ActionBase {
    private final String editDateText;
    private final String transactionOperation;
    private LocalDate date;

    public EditActionListener(JTabbedPane tabs, String editDateText, String transactionOperation) {
        super(tabs);
        this.editDateText = editDateText;
        this.transactionOperation = transactionOperation;
    }

    @Override
    public boolean doAction() {
        try {
            date = LocalDate.parse(editDateText, dateFormatter);
            File keyfile = new File(Session.getLoggedInUser() + ENCRYPTED_AES_KEY_FILE_NAME);
            SecretKey originalKey;
            if (keyfile.exists()) {
                byte[] decryptedKeyBytes = decrypt(Files.readAllBytes(keyfile.toPath()), privateKey);
                originalKey = new SecretKeySpec(decryptedKeyBytes, 0, decryptedKeyBytes.length, "AES");
            } else {
                throw new FileNotFoundException();
            }
            //ανάλογα την επιλογή του χρήστη, θα ψάξουμε και θα εμφανίσουμε τα κατάλληλα records
            doEditOperation(transactionOperation.equals("Expense"), originalKey);
            return true;
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(tabs, "Wrong date format, must be in dd-MM-yyyy, example: 10-10-2010", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private void doEditOperation(boolean isExpense, SecretKey originalKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, originalKey);
        String fileName = (isExpense ? EXPENSES_FILE_NAME : INCOME_FILE_NAME);
        String loggedInUsernamePath = Session.getLoggedInUser();
        if (!new File(loggedInUsernamePath + fileName).exists()) {
            JOptionPane.showMessageDialog(tabs, "No records found, please select \"Insert record\" tab to insert a record", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //οι λίστες που θα έχουν τις καταχωρήσεις που έχουν την ίδια ημερομηνία
        List<Transaction> recordsMatched = new ArrayList<>();
        List<Transaction> recordsNotMatched = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(loggedInUsernamePath + fileName))) {
            Transaction record;
            while (true) {
                SealedObject sealed = (SealedObject) ois.readObject();
                record = isExpense ? (Expense) sealed.getObject(cipher) : (Income) sealed.getObject(cipher);
                if (record.getTransactionDate().equals(date))
                    recordsMatched.add(record); //προσθήκη στη λίστα μόνο αν οι ημερομηνίες είναι ίδιες, αλλιώς προσθήκη στην άλλη λίστα
                else
                    recordsNotMatched.add(record);
            }
        } catch (EOFException ignored) {
        }
        if (recordsMatched.isEmpty()) {
            JOptionPane.showMessageDialog(tabs, "Could not find any records, please check your date", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel editRecordsPanel = new JPanel();
        editRecordsPanel.setLayout(new GridLayout(recordsMatched.size() + 1, 1, 1, 5)); //+1 γραμμή για κουμπί (2η παράμετρος, οι άλλες 2 είναι για τα κενά ανάμεσα στα στοιχεία)
        //λίστες με fields/textareas με βάση τον αριθμό των στοιχείων
        JTextField[] matchedRecordsDate = new JTextField[recordsMatched.size()];
        JTextField[] matchedRecordsValue = new JTextField[recordsMatched.size()];
        JTextArea[] matchedRecordsDescription = new JTextArea[recordsMatched.size()];
        for (int i = 0; i < recordsMatched.size(); i++) {

            matchedRecordsDate[i] = new JTextField(25);
            matchedRecordsDate[i].setText(date.getDayOfMonth() + "-" + date.getMonthValue() + "-" + date.getYear());
            matchedRecordsValue[i] = new JTextField(25);
            matchedRecordsValue[i].setText(Double.toString(recordsMatched.get(i).getValue()));
            matchedRecordsDescription[i] = new JTextArea();
            matchedRecordsDescription[i].setText(recordsMatched.get(i).getDescription());

            JPanel oneRecord = new JPanel();
            oneRecord.setLayout(new GridLayout(1, 6));
            oneRecord.add(new JLabel("Date:"));
            oneRecord.add(matchedRecordsDate[i]);
            oneRecord.add(new JLabel("Value:"));
            oneRecord.add(matchedRecordsValue[i]);
            oneRecord.add(new JLabel("Description:"));
            oneRecord.add(matchedRecordsDescription[i]);

            editRecordsPanel.add(oneRecord);
        }
        //αφού δημιουργηθούν όλες οι παραπάνω εγγραφές, πρέπει να βάλουμε και το κουμπί στο τέλος
        JPanel editRecordsButtonPanel = new JPanel();
        JButton editRecordsBtn = new JButton("OK");
        editRecordsButtonPanel.add(editRecordsBtn);
        editRecordsPanel.add(editRecordsButtonPanel);
        //δημιουργία νέου παραθύρου
        JFrame frame = new JFrame("Edit");
        frame.add(editRecordsPanel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();

        //γράψιμο στο αρχείο όταν πατηθεί το ΟΚ κουμπι
        editRecordsBtn.addActionListener(e -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //κλείσιμο το παράθυρο δε το χρειαζόμαστε πλέον
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(loggedInUsernamePath + EXPENSES_FILE_NAME))) {
                cipher.init(Cipher.ENCRYPT_MODE, originalKey); //θέτουμε λειτουργία encrypt
                for (Transaction recordNotMatched : recordsNotMatched)
                    oos.writeObject(new SealedObject(recordNotMatched, cipher));
                for (int i = 0; i < recordsMatched.size(); i++) {
                    recordsMatched.get(i).setDescription(matchedRecordsDescription[i].getText());
                    recordsMatched.get(i).setTransactionDate(LocalDate.parse(matchedRecordsDate[i].getText(), dateFormatter));
                    recordsMatched.get(i).setValue(Double.parseDouble(matchedRecordsValue[i].getText()));
                    oos.writeObject(new SealedObject(recordsMatched.get(i), cipher));
                }
                JOptionPane.showMessageDialog(tabs, "Successfully updated your transactions", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (DateTimeParseException dt) {
                JOptionPane.showMessageDialog(tabs, "Wrong date format, must be in dd-MM-yyyy, example: 10-10-2010", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        });
    }
}
