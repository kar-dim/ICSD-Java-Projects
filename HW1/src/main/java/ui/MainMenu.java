package ui;
/* Dimitris Karatzas icsd13072
   Nikolaos Katsiopis icsd13076
   Christos Papakostas icsd13143
 */

import ui.action.*;
import util.Session;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MainMenu extends JPanel {
    private final JTabbedPane tabs;
    private JTextField registerUsernameText, registerNameText, registerLastNameText, loginUsernameText, createDateText, createValueText, publishDateText, editDateText;
    private JPasswordField registerPassText, loginPassText;
    private JTextArea createRecordText;
    private JRadioButton createIncome, createExpense, editIncome, editExpense;
    private JButton registerBtn, loginBtn, publishBtn, createRecordBtn, editBtn, clearRegisterDetailsBtn, clearLoginDetailsBtn;

    public MainMenu() {
        super(new GridLayout(1, 1));
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
        registerBtn.addActionListener(e -> new RegisterActionListener(
                tabs, registerUsernameText.getText(), registerNameText.getText(), registerLastNameText.getText(), registerPassText.getPassword()).doAction()
        );

        loginBtn.addActionListener(e -> {
            if (new LoginActionListener(tabs, loginUsernameText.getText(), loginPassText.getPassword()).doAction())
                setupUiAfterLogin();
        });

        publishBtn.addActionListener(e -> new PublishActionListener(
                tabs, publishDateText.getText()).doAction());

        editBtn.addActionListener(e -> new EditActionListener(
                tabs, editDateText.getText(), getTransactionOperation()).doAction());

        //όταν πατηθεί το κουμπί για δημιουργία μιας αναφοράς
        createRecordBtn.addActionListener(e -> new CreateRecordListener(
                tabs, createRecordText.getText(), createDateText.getText(), createValueText.getText(), getTransactionOperation()).doAction());

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

