package ui;

/* Dimitris Karatzas icsd13072
   Apostolos Lazaros icsd13096
 */

import ui.action.CheckActionListener;
import ui.action.CreateActionListener;
import ui.action.DetailsActionListener;

import javax.swing.*;
import java.awt.GridLayout;
public class MainMenu extends JPanel {
    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
    private JTextField check_tf_dep, check_tf_dest, check_tf_date, display_tf_name, display_tf_id, display_tf_lname, reserve_tf_id;
    private JButton checkBtn, createBtn, reservationDetailsBtn, clearCheck, clearDisplay, clearReserve;

    public MainMenu() {
        super(new GridLayout(1, 1));
        initializePanels();
    }

    private void initializePanels() {
        //δημιουργία GUI
        tabs.addTab("Check availability", createAvailabilityPanel());
        tabs.addTab("Reserve ticket", createReservePanel());
        tabs.addTab("Display reservation", createDisplayPanel());
        addActionListeners();
        add(tabs);
    }

    //οι παρακάτω 3 μέθοδοι απλώς δημιουργούν τα γραφικά στοιχεία της εφαρμογής, για κάθε ενέργεια έχουμε ένα αντίστοιχο πανελ
    //με αντίστοιχά textfields, labels και κουμπιά
    private JPanel createAvailabilityPanel() {
        JPanel availabilityPanel = new JPanel();
        availabilityPanel.setLayout(new GridLayout(4, 2));

        availabilityPanel.add(new JLabel("Type Departure City:"));
        check_tf_dep = new JTextField(25);
        availabilityPanel.add(check_tf_dep);

        availabilityPanel.add(new JLabel("Type Destination City:"));
        check_tf_dest = new JTextField(25);
        availabilityPanel.add(check_tf_dest);

        availabilityPanel.add(new JLabel("Date (dd-MM-yyyy):"));
        check_tf_date = new JTextField(25);
        availabilityPanel.add(check_tf_date);

        checkBtn = new JButton("Check");
        availabilityPanel.add(checkBtn);
        clearCheck = new JButton("Clear data");
        availabilityPanel.add(clearCheck);

        return availabilityPanel;
    }

    private JPanel createReservePanel() {
        JPanel reservePanel = new JPanel();
        reservePanel.setLayout(new GridLayout(2, 2));

        reservePanel.add(new JLabel("Enter Flight ID:"));
        reserve_tf_id = new JTextField(3);
        reservePanel.add(reserve_tf_id);

        createBtn = new JButton("Reserve");
        reservePanel.add(createBtn);
        clearReserve = new JButton("Clear");
        reservePanel.add(clearReserve);
        return reservePanel;
    }

    private JPanel createDisplayPanel() {
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridLayout(4, 2));

        displayPanel.add(new JLabel("Your Name:"));
        display_tf_name = new JTextField(25);
        displayPanel.add(display_tf_name);

        displayPanel.add(new JLabel("Your Last Name:"));
        display_tf_lname = new JTextField(25);
        displayPanel.add(display_tf_lname);

        displayPanel.add(new JLabel("Flight ID:"));
        display_tf_id = new JTextField(3);
        displayPanel.add(display_tf_id);

        reservationDetailsBtn = new JButton("Display");
        displayPanel.add(reservationDetailsBtn);
        clearDisplay = new JButton("Clear");
        displayPanel.add(clearDisplay);
        return displayPanel;
    }

    //μέθοδος που βάζει listeners για τα κουμπιά
    private void addActionListeners() {

        checkBtn.addActionListener(e ->
                new CheckActionListener(tabs, check_tf_date.getText(), check_tf_dep.getText(), check_tf_dest.getText()).doAction());

        createBtn.addActionListener(e -> new CreateActionListener(tabs, Integer.parseInt(reserve_tf_id.getText())).doAction());

        reservationDetailsBtn.addActionListener(e -> new DetailsActionListener(
                tabs, display_tf_name.getText(), display_tf_lname.getText(), Integer.parseInt(display_tf_id.getText())).doAction());

        clearCheck.addActionListener(e -> {
            check_tf_dest.setText("");
            check_tf_dep.setText("");
            check_tf_date.setText("");
        });

        clearDisplay.addActionListener(e -> {
            display_tf_name.setText("");
            display_tf_lname.setText("");
            display_tf_id.setText("");
        });

        clearReserve.addActionListener(e -> reserve_tf_id.setText(""));
    }
}
