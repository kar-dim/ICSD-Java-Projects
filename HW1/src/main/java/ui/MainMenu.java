/* Dimitris Karatzas icsd13072
   Nikolaos Katsiopis icsd13076
   Christos Papakostas icsd13143
 */

import domain.Expense;
import domain.Income;
import domain.Transaction;
import domain.User;
import exception.PasswordTooShortException;
import exception.UserExistsException;
import util.AppendableObjectOutputStream;
import util.Constants;

import static util.Constants.*;
import static util.EncryptionUtils.*;
import static util.FileUtils.*;
import static util.FileUtils.removeSpecialCharacters;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.security.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainMenu extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(MainMenu.class.getName());
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final JTabbedPane tabs;
    private JTextField registerUsernameText, registerNameText, registerLastNameText, loginUsernameText, createDateText, createValueText, publishDateText, editDateText;
    private JPasswordField registerPassText, loginPassText;
    private JTextArea createRecordText;
    private JRadioButton createIncome;
    private JRadioButton createExpense;
    private JRadioButton editIncome;
    private JRadioButton editExpense;
    private JButton registerBtn, loginBtn, publishBtn, createRecordBtn, editBtn, edit2_b, edit3_b, clearRegisterDetailsBtn, clearLoginDetailsBtn;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private static String loggedInUsernamePath; //το (σχετικό) μονοπάτι προς τον φάκελο, στην ουσία είναι της μορφής "./username_fixed/" ώπου fixed το εξηγούμε πιο κάτω στο register

    public MainMenu() {
        super(new GridLayout(1, 1));
        if (!areKeysPresent())
            generateKeys();
        publicKey = (PublicKey) getKeyFromFile(Constants.PUBLIC_KEY_PATH);
        privateKey = (PrivateKey) getKeyFromFile(Constants.PRIVATE_KEY_PATH);
        //δημιουργία tabs
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab("Register", createRegisterPanel());
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("Insert record", createInsertPanel());
        tabs.addTab("Edit record", createEditPanel());
        tabs.addTab("Publish Report", createPublishPanel());

        //αρχικά μόνο register επιτρέπεται και login
        tabs.setEnabledAt(0, true);
        tabs.setEnabledAt(1, true);
        tabs.setEnabledAt(2, false);
        tabs.setEnabledAt(3, false);
        tabs.setEnabledAt(4, false);

        //προσθήκη listeners για τα κουμπιά και radio buttons
        setListeners();
        add(tabs);
    }

    //μεθόδοι που φτιάχνουν τα στοιχεία του κάθε tab, μόνο το γραφικό κομμάτι, η λειτουργικότητα δημιουργείται μετά την εκτέλεση των παρακάτω "create*Panel()" συναρτήσεων
    //εδώ απλώς δημιουργούμε τα Labels, text fields κτλ
    private JPanel createRegisterPanel() {
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridLayout(5, 2));

        //labels και textfields
        registerPanel.add(new JLabel("Username:"));
        registerUsernameText = new JTextField(25);
        registerPanel.add(registerUsernameText);
        registerPanel.add(new JLabel("Password:"));
        registerPassText = new JPasswordField(25);
        registerPanel.add(registerPassText);
        registerPanel.add(new JLabel("Name:"));
        registerNameText = new JTextField(25);
        registerPanel.add(registerNameText);
        registerPanel.add(new JLabel("Last name:"));
        registerLastNameText = new JTextField(25);
        registerPanel.add(registerLastNameText);

        registerBtn = new JButton("Register");
        clearRegisterDetailsBtn = new JButton("Clear Data");

        registerPanel.add(registerBtn);
        registerPanel.add(clearRegisterDetailsBtn);
        return registerPanel;
    }

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Username:"));
        loginUsernameText = new JTextField(25);
        loginPanel.add(loginUsernameText);
        loginPanel.add(new JLabel("Password:"));
        loginPassText = new JPasswordField(25);
        loginPanel.add(loginPassText);

        loginBtn = new JButton("Login");
        clearLoginDetailsBtn = new JButton("Clear Data");
        loginPanel.add(loginBtn);
        loginPanel.add(clearLoginDetailsBtn);

        return loginPanel;
    }

    private JPanel createInsertPanel() {
        JPanel insertPanel = new JPanel();
        insertPanel.setLayout(new GridLayout(5, 1));

        JPanel upper = new JPanel();
        upper.setLayout(new GridLayout(1, 2));
        upper.add(new JLabel("Transaction Date: (dd-MM-yyyy:"));
        createDateText = new JTextField(25);
        upper.add(createDateText);

        JPanel upper2 = new JPanel();
        upper2.setLayout(new GridLayout(1, 2));
        upper2.add(new JLabel("Value:"));
        createValueText = new JTextField(25);
        upper2.add(createValueText);

        JPanel med = new JPanel();
        med.setLayout(new GridLayout(1, 2));
        med.add(new JLabel("Transaction description:"));
        createRecordText = new JTextArea();
        JScrollPane scroll = new JScrollPane(createRecordText);
        med.add(scroll);

        JPanel lower = new JPanel();
        lower.setLayout(new GridLayout(1, 2));
        ButtonGroup createRecordGroup = new ButtonGroup();
        createIncome = new JRadioButton("Income");
        createExpense = new JRadioButton("Expense");
        createIncome.setSelected(true); //να μην υπάρχει περίπτωση να μη επιλεχθεί τίποτα, οπότε εξ ορισμού επιλέγουμε Income
        createRecordGroup.add(createIncome);
        createRecordGroup.add(createExpense);
        lower.add(createIncome);
        lower.add(createExpense);

        JPanel buttonPanel = new JPanel();
        createRecordBtn = new JButton("Create Transaction");
        buttonPanel.add(createRecordBtn);

        insertPanel.add(upper);
        insertPanel.add(upper2);
        insertPanel.add(med);
        insertPanel.add(lower);
        insertPanel.add(buttonPanel);
        return insertPanel;
    }

    private JPanel createEditPanel() {
        JPanel editPanel = new JPanel();
        editPanel.setLayout(new FlowLayout());

        JPanel top = new JPanel();
        top.setLayout(new FlowLayout());
        top.add(new JLabel("Type the date (dd-MM-yyyy):"));
        editDateText = new JTextField(10);
        top.add(editDateText);

        JPanel med = new JPanel();
        med.setLayout(new FlowLayout());
        ButtonGroup editecordGroup = new ButtonGroup();
        editIncome = new JRadioButton("Income");
        editExpense = new JRadioButton("Expense");
        editecordGroup.add(editIncome);
        editecordGroup.add(editExpense);
        editIncome.setSelected(true); //να μην υπάρχει περίπτωση να μη επιλεχθεί τίποτα, οπότε εξ ορισμού επιλέγουμε Income
        med.add(editIncome);
        med.add(editExpense);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        editBtn = new JButton("Edit");
        buttonsPanel.add(editBtn);

        editPanel.add(top);
        editPanel.add(med);
        editPanel.add(buttonsPanel);

        return editPanel;
    }

    private JPanel createPublishPanel() {
        JPanel publishPanel = new JPanel();
        publishPanel.setLayout(new FlowLayout());

        publishPanel.add(new JLabel("Type the month number(1-12"));
        publishDateText = new JTextField(2);
        publishPanel.add(publishDateText);

        publishBtn = new JButton("Publish");
        publishPanel.add(publishBtn);
        return publishPanel;
    }

    //μέθοδος που θέτει τους διάφορους listeners 
    private void setListeners() {
        //όταν πατηθεί το register, θα γίνει το hash του κωδικού με βάση τον αλγόριθμο sha256
        registerBtn.addActionListener(e -> {
            if (!isUsersFilePresent())
                createUsersFile();
            try {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Constants.USERS_FILE_PATH))) {
                    User userFromFile;
                    while ((userFromFile = (User) ois.readObject()) != null) {
                        if (userFromFile.getUsername().equals(registerUsernameText.getText()))
                            throw new UserExistsException();
                    }
                } catch (EOFException ignored) {
                }

                //αν ο κωδικός είναι μικρότερος από 6 χαρακτήρες τότε σφάλμα
                if (new String(registerPassText.getPassword()).length() < 6) {
                    throw new PasswordTooShortException();
                }
                //πρόσθεση του salt στον κωδικό και έπειτε hash με sha256. Ο κωδικος θα γινει encrypt
                String salt = createSalt();
                String saltedPassword = new String(registerPassText.getPassword()) + salt;
                byte[] encryptedHashedPassword = encrypt(SHA256Hash(saltedPassword), publicKey);

                //τωρα θα δημιουρήσουμε τον φάκελο για αυτόν τον χρήστη
                String userPath = "HW1/" + removeSpecialCharacters(registerUsernameText.getText());
                boolean isNewUserPathExists = new File(userPath).mkdir();
                //αν επιτύχει η δημιουργία του φακέλου τότε θέτουμε το όνομα του σε μια ιδιότητα του αντικειμένου user
                if (isNewUserPathExists) {
                    //θα κρυπτογραφήσουμε ένα νέο συμμετρικό κλειδί (AES) για τον χρήστη
                    byte[] encryptedAesKey = encryptNewKey(publicKey);
                    //αποθήκευση στο αρχείο (και αν υπάρχει overwrite) encrypted.aeskey
                    if (new File(userPath + "/" + ENCRYPTED_AES_KEY_FILE_NAME).createNewFile()) {
                        //γράψιμο τα bytes στο αρχείο encrypted.aeskey
                        FileOutputStream fos = new FileOutputStream(userPath + "/" + ENCRYPTED_AES_KEY_FILE_NAME);
                        fos.write(encryptedAesKey);
                        fos.flush();
                        //γράψιμο στο αρχείο users.bin τα στοιχεία του χρήστη
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Constants.USERS_FILE_PATH));
                        oos.writeObject(new User(registerUsernameText.getText(), encryptedHashedPassword, registerNameText.getText(), registerLastNameText.getText(), salt));
                        oos.flush();
                        JOptionPane.showMessageDialog(tabs, "Successfully registered! Please select the Login tab to login", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        });
        //listener για το login, έλεγχος username και password, αυθεντικοποίηση χρήστη
        loginBtn.addActionListener(e -> {
            if (!isUsersFilePresent())
                createUsersFile();
            boolean userFound = false;
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Constants.USERS_FILE_PATH));
                User userFromFile;
                while ((userFromFile = (User) ois.readObject()) != null) {
                    if (userFromFile.getUsername().equals(loginUsernameText.getText())) {
                        String currentPasswordToCheck = new String(loginPassText.getPassword()) + userFromFile.getSalt();
                        String decryptedPassFromFile = new String(decrypt(userFromFile.getPassword(), privateKey));
                        //hash με τον αλγόριθμο sha256 για έλεγχο
                        if (decryptedPassFromFile.equals(SHA256Hash(currentPasswordToCheck))) {
                            userFound = true;
                            loggedInUsernamePath = "HW1/" + removeSpecialCharacters(loginUsernameText.getText());
                            if (!verifyUserFilesSignature(loggedInUsernamePath, publicKey)) {
                                JOptionPane.showMessageDialog(tabs, "Critical Error! Could not verify your data integrity, someone may have edited your files, or your files may be corrupted", "Warning", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            //επιτυχές Login
                            setupUiAfterLogin();
                        } else {
                            //λάθος κωδικός
                            JOptionPane.showMessageDialog(tabs, "Cannot login, wrong password", "Failure", JOptionPane.ERROR_MESSAGE);
                            userFound = true;
                        }
                    }
                }

            } catch (EOFException eofe) {
                //αν δε βρεθεί χρήστης με το συγκεκριμένο username τότε λάθος
                if (!userFound)
                    JOptionPane.showMessageDialog(tabs, "No username found with the username specified", "Error", JOptionPane.ERROR_MESSAGE);
                //αν δε βρεθεί η υπογραφή
            } catch (NoSuchFileException nsfe) {
                setupUiAfterLogin(); //δεν υπαρχει signature file, it's ok! (first login πχ)
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });

        publishBtn.addActionListener(e -> {
            //παίρνουμε το μήνα
            int monthSelected;
            try {
                monthSelected = Integer.parseInt(publishDateText.getText());
                if (monthSelected < 1 || monthSelected > 12)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(tabs, "Wrong monthSelected number, must be 1-12, example: 1=January, 2=February", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //Εμφάνιση στο UI
            JTextArea expensesTextArea = new JTextArea();
            expensesTextArea.setText("Your Expenses for this monthSelected\n\n");
            expensesTextArea.append(decryptRecordsFromFile(privateKey, loggedInUsernamePath, EXPENSES_FILE_NAME, monthSelected));
            expensesTextArea.setEditable(false);

            JTextArea incomeTextArea = new JTextArea();
            incomeTextArea.setText("Your Income for this monthSelected\n\n");
            incomeTextArea.append(decryptRecordsFromFile(privateKey, loggedInUsernamePath, INCOME_FILE_NAME, monthSelected));
            incomeTextArea.setEditable(false);

            JPanel recordsPanel = new JPanel();
            recordsPanel.setLayout(new GridLayout(1, 2));
            recordsPanel.add(new JScrollPane(expensesTextArea));
            recordsPanel.add(new JScrollPane(incomeTextArea));

            JFrame frame = new JFrame("Results");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
            frame.add(recordsPanel);
            frame.pack();
        });

        /* TODO REFACTOR THIS */
        editBtn.addActionListener(e -> {
            boolean expensesfile = new File(loggedInUsernamePath + EXPENSES_FILE_NAME).exists();
            boolean incomefile = new File(loggedInUsernamePath + INCOME_FILE_NAME).exists();
            try {
                LocalDate date = LocalDate.parse(editDateText.getText(), dateFormatter);
                File keyfile = new File(loggedInUsernamePath + ENCRYPTED_AES_KEY_FILE_NAME);
                SecretKey originalKey;
                if (keyfile.exists()) {
                    byte[] decryptedKeyBytes = decrypt(Files.readAllBytes(keyfile.toPath()), privateKey);
                    originalKey = new SecretKeySpec(decryptedKeyBytes, 0, decryptedKeyBytes.length, "AES");
                } else {
                    throw new FileNotFoundException();
                }

                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, originalKey);

                //οι λίστες που θα έχουν τις καταχωρήσεις που έχουν την ίδια ημερομηνία
                List<Expense> e_list = new ArrayList<>();
                List<Income> i_list = new ArrayList<>();
                //οι λίστες που θα έχουν τις καταχωρήσεις που δε θα έχουν την ίδια ημερομηνία, χρειάζονται διότι αφού κάνουμε overwrite το αρχείο
                //πρέπει να ξαναγράψουμε τα αντικείμενα αυτά πάλι αλλιώς θα χαθούν
                List<Expense> ne_list = new ArrayList<>();
                List<Income> ni_list = new ArrayList<>();
                //ανάλογα την επιλογή του χρήστη, θα ψάξουμε και θα εμφανίσουμε τα κατάλληλα records
                if (getTransactionOperation().equals("Expense"))

                {
                    if (!expensesfile) {
                        //αν δεν υπάρχει τα αρχείο δεν μπορεί να γίνει τροποποίηση
                        JOptionPane.showMessageDialog(tabs, "No expenses records found, please select \"Insert record\" tab to insert a record", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    //αλλιώς αν υπάρχει παίρνουμε τις εγγραφές, θέλουμε και άλλον έναν έλεγχο, αν υπάρχει το αρχείο αλλά είναι άδειο
                    try {
                        //κάθε εγγραφή έχει κρυπτογραφηθεί, οπότε απλώς την αποκρυπτογραφούμε με βάση το privatekey
                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(loggedInUsernamePath + EXPENSES_FILE_NAME));
                        SealedObject sealed;
                        Expense expense;
                        while (true) {
                            sealed = (SealedObject) ois.readObject();
                            expense = (Expense) sealed.getObject(cipher);
                            if (expense.getTransactionDate().equals(date)) {
                                e_list.add(expense); //προσθήκη στη λίστα μόνο αν οι ημερομηνίες είναι ίδιες, αλλιώς προσθήκη στην άλλη λίστα
                            } else {
                                ne_list.add(expense);
                            }
                        }
                    } catch (EOFException eofe) {
                    }
                    if (e_list.isEmpty()) {
                        JOptionPane.showMessageDialog(tabs, "Could not find any records, please check your date", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        //μετά το EOF συνεχίζεται η ροή εδώ, οπότε αφού έχουμε τη λίστα με τις δαπάνες, θα δημιουργήσουμε το GUI στο οποίο θα εμφανίζονται όλες οι δαπάνες
                        JPanel edit_gui = new JPanel();
                        //λίστες με fields/textareas με βάση τον αριθμό των στοιχείων
                        JTextField[] date_tf_edit = new JTextField[e_list.size()];
                        JTextField[] value_tf_edit = new JTextField[e_list.size()];
                        JTextArea[] text_edit = new JTextArea[e_list.size()];

                        edit_gui.setLayout(new GridLayout(e_list.size() + 1, 1, 1, 5)); //+1 γραμμή για κουμπί (2η παράμετρος, οι άλλες 2 είναι για τα κενά ανάμεσα στα στοιχεία)
                        for (int i = 0; i < e_list.size(); i++) {
                            //προσθήκη των γραφικών στοιχείων σε ένα πανελ το οποίο αντιστοιχεί σε μια record, στην επόμενη επανάληψη πάλι δημιουργούμε
                            //τα ίδια πεδία και θέτουμε αντίστοιχα τις τιμές
                            JPanel one_record = new JPanel();
                            one_record.setLayout(new GridLayout(1, 6));
                            one_record.add(new JLabel("Date:"));
                            date_tf_edit[i] = new JTextField(25);
                            //μετατροπή του Date σε string της μορφής dd-MM-yyyy
                            date_tf_edit[i].setText(date.getDayOfMonth() + "-" + date.getMonthValue() + "-" + date.getYear()); //θέτουμε την τιμή που είχε
                            one_record.add(date_tf_edit[i]);

                            one_record.add(new JLabel("Value:"));
                            value_tf_edit[i] = new JTextField(25);
                            value_tf_edit[i].setText(Double.toString(e_list.get(i).getValue())); //θέτουμε την τιμή που είχε
                            one_record.add(value_tf_edit[i]);

                            one_record.add(new JLabel("Description:"));
                            text_edit[i] = new JTextArea();
                            text_edit[i].setText(e_list.get(i).getDescription());
                            one_record.add(text_edit[i]);

                            edit_gui.add(one_record);
                        }
                        //αφού δημιουργηθούν όλες οι παραπάνω εγγραφές, πρέπει να βάλουμε και το κουμπί στο τέλος
                        JPanel button_panel = new JPanel();
                        edit2_b = new JButton("OK");
                        button_panel.add(edit2_b);
                        edit_gui.add(button_panel);
                        //δημιουργία νέου παραθύρου
                        JFrame frame = new JFrame("Edit");
                        frame.add(edit_gui);
                        frame.setVisible(true);
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        frame.pack();
                        //τώρα για να τροποποιηθεί η λίστα, πρέπει ο χρήστης να πατήσει το κουμπί edit2_b του παραθύρου αυτουνού, οπότε
                        //στον listener θα γίνει η τελική τροποποίηση
                        edit2_b.addActionListener(e12 -> {
                            try {
                                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //κλείσιμο το παράθυρο δε το χρειαζόμαστε πλέον
                                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(loggedInUsernamePath + EXPENSES_FILE_NAME));
                                //επειδή στην επιλογή αυτή (τροποποίηση) ο χρήστης δεν αφαιρεί ή δημιουργεί νέες καταχωρήσεις, το μέγεθος θα είναι ίσο με το μέγεθος των 2 λιστών
                                //πρώτα θα βάλουμε τις μη αλλαγμένες καταχωρίσεις

                                cipher.init(Cipher.ENCRYPT_MODE, originalKey); //θέτουμε λειτουργία encrypt

                                for (Expense exp : ne_list) {
                                    oos.writeObject(new SealedObject(exp, cipher));
                                }
                                oos.flush();
                                //έπειτα τις αλλαγμένες, πρώτα όμως πρέπει να ενημερώσουμε τη λίστα με τις νέες αλλαγές του χρήστη

                                for (int i = 0; i < e_list.size(); i++) {
                                    e_list.get(i).setDescription(text_edit[i].getText());

                                    LocalDate date_to_add = LocalDate.parse(date_tf_edit[i].getText(), dateFormatter);
                                    e_list.get(i).setTransactionDate(date_to_add);

                                    e_list.get(i).setValue(Double.parseDouble(value_tf_edit[i].getText()));
                                    //γράψιμο στο stream το κρυπτογραφημένο αντικείμενο
                                    oos.writeObject(new SealedObject(e_list.get(i), cipher));
                                }
                                oos.flush();
                                oos.close();
                                //αν δε πιάσουν τα exception  σημαίνει πως γράφτηκαν σωστά τα αντικείμενα
                                JOptionPane.showMessageDialog(tabs, "Successfully updated your transactions", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } catch (IOException | IllegalBlockSizeException | InvalidKeyException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            } catch (DateTimeParseException ex) {
                                JOptionPane.showMessageDialog(tabs, "Wrong date format, must be in dd-MM-yyyy, example: 10-10-2010", "Error", JOptionPane.ERROR_MESSAGE);
                            }

                        });
                    }

                }





                //ακριβώς η ίδια διαδικασία και για την επιλογή εσόδων
                else {
                    if (!incomefile) {
                        //αν δεν υπάρχει τα αρχείο δεν μπορεί να γίνει τροποποίηση
                        JOptionPane.showMessageDialog(tabs, "No income records found, please select \"Insert record\" tab to insert a record", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    //αλλιώς αν υπάρχει παίρνουμε τις εγγραφές, θέλουμε και άλλον έναν έλεγχο, αν υπάρχει το αρχείο αλλά είναι άδειο
                    try {
                        //κάθε εγγραφή έχει κρυπτογραφηθεί, οπότε απλώς την αποκρυπτογραφούμε με βάση το privatekey
                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(loggedInUsernamePath + INCOME_FILE_NAME));
                        SealedObject sealed;
                        Income income;
                        while (true) {
                            sealed = (SealedObject) ois.readObject();
                            income = (Income) sealed.getObject(cipher);
                            if (income.getTransactionDate().equals(date)) {
                                i_list.add(income); //προσθήκη στη λίστα μόνο αν οι ημερομηνίες είναι ίδιες, αλλιώς τίποτα
                            } else {
                                ni_list.add(income);
                            }
                        }
                    } catch (EOFException eofe) {
                    }
                    if (i_list.isEmpty()) {
                        JOptionPane.showMessageDialog(tabs, "Could not find any records, please check your date", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        //μετά το EOF συνεχίζεται η ροή εδώ, οπότε αφού έχουμε τη λίστα με τις δαπάνες, θα δημιουργήσουμε το GUI στο οποίο θα εμφανίζονται όλα τα έσοδα
                        JPanel edit_gui = new JPanel();
                        //λίστες με fields/textareas με βάση τον αριθμό των στοιχείων
                        JTextField[] date_tf_edit = new JTextField[i_list.size()];
                        JTextField[] value_tf_edit = new JTextField[i_list.size()];
                        JTextArea[] text_edit = new JTextArea[i_list.size()];

                        edit_gui.setLayout(new GridLayout(i_list.size() + 1, 1, 1, 5)); //+1 γραμμή για κουμπί (2η παράμετρος, οι άλλες 2 είναι για τα κενά ανάμεσα στα στοιχεία)
                        for (int i = 0; i < i_list.size(); i++) {
                            //προσθήκη των γραφικών στοιχείων σε ένα πανελ το οποίο αντιστοιχεί σε μια record, στην επόμενη επανάληψη πάλι δημιουργούμε
                            //τα ίδια πεδία και θέτουμε αντίστοιχα τις τιμές
                            JPanel one_record = new JPanel();
                            one_record.setLayout(new GridLayout(1, 6));
                            one_record.add(new JLabel("Date:"));
                            date_tf_edit[i] = new JTextField(25);
                            //μετατροπή του Date σε string της μορφής dd-MM-yyyy
                            date_tf_edit[i].setText(date.getDayOfMonth() + "-" + date.getMonthValue() + "-" + date.getYear()); //θέτουμε την τιμή που είχε
                            one_record.add(date_tf_edit[i]);

                            one_record.add(new JLabel("Value:"));
                            value_tf_edit[i] = new JTextField(25);
                            value_tf_edit[i].setText(Double.toString(i_list.get(i).getValue())); //θέτουμε την τιμή που είχε
                            one_record.add(value_tf_edit[i]);

                            one_record.add(new JLabel("Description:"));
                            text_edit[i] = new JTextArea(10, 20);
                            text_edit[i].setText(i_list.get(i).getDescription());
                            one_record.add(text_edit[i]);

                            edit_gui.add(one_record);
                        }
                        //αφού δημιουργηθούν όλες οι παραπάνω εγγραφές, πρέπει να βάλουμε και το κουμπί στο τέλος
                        JPanel button_panel = new JPanel();
                        edit3_b = new JButton("OK");
                        button_panel.add(edit2_b);
                        edit_gui.add(button_panel);
                        //δημιουργία νέου παραθύρου
                        JFrame frame = new JFrame("Edit");
                        frame.add(edit_gui);
                        frame.setVisible(true);
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        frame.pack();
                        //τώρα για να τροποποιηθεί η λίστα, πρέπει ο χρήστης να πατήσει το κουμπί edit3_b του παραθύρου αυτουνού, οπότε
                        //στον listener θα γίνει η τελική τροποποίηση (γράψιμο στο αρχείο)
                        edit3_b.addActionListener(e1 -> {
                            try {
                                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(loggedInUsernamePath + INCOME_FILE_NAME));
                                //επειδή στην επιλογή αυτή (τροποποίηση) ο χρήστης δεν αφαιρεί ή δημιουργεί νέες καταχωρήσεις, το μέγεθος θα είναι ίσο με το μέγεθος των 2 λιστών
                                //πρώτα θα βάλουμε τις μη αλλαγμένες καταχωρίσεις

                                cipher.init(Cipher.ENCRYPT_MODE, originalKey); //θέτουμε λειτουργία encrypt

                                //πρώτα γράφουμε τις μη πειραγμένες
                                for (Income inc : ni_list) {
                                    oos.writeObject(new SealedObject(inc, cipher));
                                }
                                oos.flush();
                                //έπειτα τις αλλαγμένες, πρώτα όμως πρέπει να ενημερώσουμε τη λίστα με τις νέες αλλαγές του χρήστη

                                for (int i = 0; i < i_list.size(); i++) {
                                    i_list.get(i).setDescription(text_edit[i].getText());

                                    LocalDate date_to_add = LocalDate.parse(date_tf_edit[i].getText(), dateFormatter);
                                    i_list.get(i).setTransactionDate(date_to_add);

                                    i_list.get(i).setValue(Double.parseDouble(value_tf_edit[i].getText()));
                                    //γράψιμο στο stream το κρυπτογραφημένο αντικείμενο
                                    oos.writeObject(new SealedObject(i_list.get(i), cipher));
                                }
                                oos.flush();
                                //αν δε πιάσουν τα exception σημαίνει πως γράφτηκαν σωστά τα αντικείμενα
                                JOptionPane.showMessageDialog(tabs, "Successfully updated your transactions", "Success", JOptionPane.INFORMATION_MESSAGE);
                                oos.close();
                            } catch (IOException | IllegalBlockSizeException | InvalidKeyException ex) {
                                Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (DateTimeParseException ex) {
                                JOptionPane.showMessageDialog(tabs, "Wrong date format, must be in dd-MM-yyyy, example: 10-10-2010", "Error", JOptionPane.ERROR_MESSAGE);
                            }

                        });
                    }
                }





            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(tabs, "Wrong date format, must be in dd-MM-yyyy, example: 10-10-2010", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });

        //όταν πατηθεί το κουμπί για δημιουργία μιας αναφοράς
        createRecordBtn.addActionListener(e -> {
            try {
                if (createDateText.getText().isEmpty() || createValueText.getText().isEmpty() || createRecordText.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(tabs, "Some fields are empty, please check your data", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                //αφού κανένα δεν είναι empty τώρα, ελέγχουμε αν η ημερομηνία είναι όντως σε σωστή μορφή καθώς επίσης και η τιμή να είναι αριθμός
                LocalDate date = LocalDate.parse(createDateText.getText(), dateFormatter);
                double value = Double.parseDouble(createValueText.getText());
                appendRecordToFile(getTransactionOperation().equals("Expense"), value, date);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(tabs, "Wrong date format, must be in dd-MM-yyyy, example: 10-10-2010", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(tabs, "Cannot find encryption key!", "Critical error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(tabs, "Value must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });

        //καθαρισμός των πεδίων
        clearLoginDetailsBtn.addActionListener(e -> {
            loginUsernameText.setText("");
            loginPassText.setText("");
        });

        clearRegisterDetailsBtn.addActionListener(e -> {
            registerUsernameText.setText("");
            registerPassText.setText("");
            registerNameText.setText("");
            registerLastNameText.setText("");
        });
    }

    /* TODO REFACTOR THIS */
    private void appendRecordToFile(boolean isExpense, double recordValue, LocalDate recordDate) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        File keyfile = new File(loggedInUsernamePath + ENCRYPTED_AES_KEY_FILE_NAME);
        String fileToWrite = isExpense ? EXPENSES_FILE_NAME : INCOME_FILE_NAME;
        if (keyfile.exists()) {
            //το κρυπτογραφημένο AES key έχει ακριβώς 256bytes (RSA-2048 κρυπτογραφεί σε 256 bytes)
            byte[] aesEncrypted = Files.readAllBytes(keyfile.toPath());
            Cipher cipherDecrypt = Cipher.getInstance("RSA");
            cipherDecrypt.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decodedKeyBytes = cipherDecrypt.doFinal(aesEncrypted);
            SecretKey originalKey = new SecretKeySpec(decodedKeyBytes, 0, decodedKeyBytes.length, "AES");
            Transaction record = isExpense ? new Expense(recordValue, createRecordText.getText(), recordDate) : new Income(recordValue, createRecordText.getText(), recordDate);
            Cipher cipherEncrypt = Cipher.getInstance("AES");
            cipherEncrypt.init(Cipher.ENCRYPT_MODE, originalKey);
            SealedObject sealed = new SealedObject(record, cipherEncrypt);
            boolean fileExists = new File(loggedInUsernamePath + fileToWrite).exists();
            new File(loggedInUsernamePath + fileToWrite).createNewFile();
            try (ObjectOutputStream oos = fileExists ?
                    new AppendableObjectOutputStream(new FileOutputStream(loggedInUsernamePath + fileToWrite, true)) :
                    new ObjectOutputStream(new FileOutputStream(loggedInUsernamePath + fileToWrite))) {
                oos.writeObject(sealed);
                oos.flush();
            }
            //αν δε πεταχτεί κάποιο exception Η εκτέλεση θα συνεχιστεί εδώ οπότε σημαίνει πως γράφτηκε σωστά
            JOptionPane.showMessageDialog(tabs, "Successfully added your record to the file", "Success", JOptionPane.INFORMATION_MESSAGE);

        } else {
            throw new FileNotFoundException();
        }
    }

    //επιστρέφει τη διαδρομή του loggedin χρήστη, θα χρησιμοποιηθεί από την Main κλάση για να πάρει τον logged in χρήστη
    public static String getLoggedInUsernamePath() {
        return loggedInUsernamePath;
    }

    private String getTransactionOperation() {
        if (createIncome.isSelected() || editIncome.isSelected())
            return "Income";
        else if (createExpense.isSelected() || editExpense.isSelected())
            return "Expense";
        return "Income";
    }

    private void setupUiAfterLogin() {
        //(απ)ενεργοποίηση UI elements αν επιτύχει το login
        tabs.setEnabledAt(2, true);
        tabs.setEnabledAt(3, true);
        tabs.setEnabledAt(4, true);
        loginBtn.setEnabled(false);
        clearLoginDetailsBtn.setEnabled(false);
        loginUsernameText.setEnabled(false);
        loginPassText.setEnabled(false);
        tabs.setEnabledAt(1, false);
        JOptionPane.showMessageDialog(tabs, "Successfully logged in", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}

