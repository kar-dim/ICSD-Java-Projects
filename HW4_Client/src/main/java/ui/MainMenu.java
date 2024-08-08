package ui;

/* icsd13072 Karatzas Dimitris
   icsd13096 Lazaros Apostolos*/

import domain.Announcement;
import ui.action.*;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class MainMenu extends JPanel {
    private final JTabbedPane tabs;
    private JTextArea newAnnouncementText;
    private final JPanel deleteEditRecordsPanel;
    private JTextField registerUsernameField, loginUsernameField, registerLastNameField, registerNameField, viewStartDate, viewEndDate;
    private JPasswordField registerPasswordField, loginPasswordField;
    private JButton registerBtn, loginBtn, viewAnnouncementsBtn, createAnnouncementBtn, deleteAnnouncementsBtn, updateAnnouncementsBtn;
    private final List<Announcement> userAnnouncements = new ArrayList<>();
    private final List<JCheckBox> deleteAnnouncementCheck = new ArrayList<>();
    public MainMenu() {
        super(new GridLayout(1, 1));
        deleteEditRecordsPanel = new JPanel();
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab("Register", createRegisterPanel());
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("View Announcements", createViewPanel());
        tabs.addTab("Insert announcement", createInsertPanel());
        tabs.addTab("Edit/Delete Your Announcements", createEditDeletePanel());
        tabs.setEnabledAt(3, false);
        tabs.setEnabledAt(4, false);
        add(tabs);
        addActionListeners();
    }

    private JPanel createRegisterPanel() {
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridLayout(5, 2));

        registerPanel.add(new JLabel("Username:"));
        registerUsernameField = new JTextField(20);
        registerPanel.add(registerUsernameField);

        registerPanel.add(new JLabel("Password:"));
        registerPasswordField = new JPasswordField(20);
        registerPanel.add(registerPasswordField);

        registerPanel.add(new JLabel("Name:"));
        registerNameField = new JTextField(20);
        registerPanel.add(registerNameField);

        registerPanel.add(new JLabel("Last Name:"));
        registerLastNameField = new JTextField(20);
        registerPanel.add(registerLastNameField);

        registerBtn = new JButton("Register");
        registerPanel.add(registerBtn);
        return registerPanel;
    }

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2));

        loginPanel.add(new JLabel("Username:"));
        loginUsernameField = new JTextField(20);
        loginPanel.add(loginUsernameField);

        loginPanel.add(new JLabel("Password:"));
        loginPasswordField = new JPasswordField(20);
        loginPanel.add(loginPasswordField);

        loginBtn = new JButton("Login");
        loginPanel.add(loginBtn);
        return loginPanel;
    }

    private JPanel createViewPanel() {
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new GridLayout(3, 2));

        viewPanel.add(new JLabel("Search Dates from (dd-MM-yyyy only):"));
        viewStartDate = new JTextField(20);
        viewPanel.add(viewStartDate);

        viewPanel.add(new JLabel("Search Dates to (dd-MM-yyy only):"));
        viewEndDate = new JTextField(20);
        viewPanel.add(viewEndDate);

        //προσθήκη του κουμπιού
        viewAnnouncementsBtn = new JButton("Search");
        viewPanel.add(viewAnnouncementsBtn);
        return viewPanel;
    }

    private JPanel createInsertPanel() {
        JPanel insertPanel = new JPanel();
        insertPanel.setLayout(new GridLayout(2, 1));

        //Στο πεδίο αυτό θα γραφτεί η ανακοίνωση
        newAnnouncementText = new JTextArea(7, 57);
        JScrollPane scroll = new JScrollPane(newAnnouncementText);
        insertPanel.add(scroll, BorderLayout.CENTER);

        createAnnouncementBtn = new JButton("Create");
        //panel για το κουμπί ώστε να μη πιάνει τη μισή οθόνη
        JPanel button_panel = new JPanel();
        button_panel.setLayout(new BorderLayout());
        button_panel.add(createAnnouncementBtn, BorderLayout.SOUTH);

        insertPanel.add(button_panel);
        return insertPanel;
    }

    private JPanel createEditDeletePanel() {
        deleteAnnouncementsBtn = new JButton("Delete");
        updateAnnouncementsBtn = new JButton("Update");
        //δεν μπηκαν τα κουμπια στο πανελ ακομα, ειναι empty
        return deleteEditRecordsPanel;
    }

    private void addActionListeners() {
        tabs.addChangeListener(e -> new TabChangeActionListener(
                tabs, userAnnouncements, deleteAnnouncementCheck, deleteEditRecordsPanel, deleteAnnouncementsBtn, updateAnnouncementsBtn).doAction());

        deleteAnnouncementsBtn.addActionListener(e -> new DeleteAnnouncementsActionListener(
                tabs, deleteAnnouncementCheck, userAnnouncements, loginUsernameField.getText(), new String(loginPasswordField.getPassword())).doAction());

        updateAnnouncementsBtn.addActionListener(e -> new UpdateAnnouncementsActionListener(
                tabs, loginUsernameField.getText(), new String(loginPasswordField.getPassword()),userAnnouncements).doAction());

        registerBtn.addActionListener(e -> new RegisterActionListener(
                tabs, registerUsernameField.getText(), registerNameField.getText(), registerLastNameField.getText(), registerPasswordField.getPassword()).doAction());

        loginBtn.addActionListener(e -> new LoginActionListener(
                tabs, loginUsernameField.getText(), loginPasswordField.getPassword()).doAction());

        viewAnnouncementsBtn.addActionListener(e -> new ViewAnnouncementsActionListener(
                tabs, viewStartDate.getText(), viewEndDate.getText()).doAction());

        createAnnouncementBtn.addActionListener(e -> new CreateAnnouncementActionListener(
                tabs, newAnnouncementText.getText(), loginUsernameField.getText()).doAction());
    }
}
