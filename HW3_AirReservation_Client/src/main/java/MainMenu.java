/* Dimitris Karatzas icsd13072
   Apostolos Lazaros icsd13096
 */
//η βασική κλάση η οποία υλοποιεί το GUI και κάνει όλες τις ενέργειες από τη μεριά του Client

import remote.AirReservation;
import remote.ReserveStep;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
public class MainMenu extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(MainMenu.class.getName());
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final AirReservation airReservation;
    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
    private JTextField check_tf_dep, check_tf_dest, check_tf_date, display_tf_name, display_tf_id, display_tf_lname, reserve_tf_id;
    private JButton checkBtn, createBtn, reservationDetailsBtn, clearCheck, clearDisplay, clearReserve;

    public MainMenu() throws Exception {
        super(new GridLayout(1, 1));
        airReservation = (AirReservation) Naming.lookup("//localhost/ReservationService");
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
        //όταν πατηθεί το κουμπί για έλεγχο
        checkBtn.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(check_tf_date.getText(), dateFormatter);
                JFrame searchResults = new JFrame("Search results");
                searchResults.setVisible(true);
                JPanel searchPanel = new JPanel();
                JTextArea textArea = new JTextArea(20, 40);
                textArea.setEditable(false); //εφόσον αρχικοποιηθεί, δεν επιτρέπουμε την αλλαγή του αφού είναι read only
                JScrollPane scrollPane = new JScrollPane(textArea);

                textArea.append(airReservation.checkAvailability(check_tf_dep.getText(), check_tf_dest.getText(), date));

                searchPanel.add(scrollPane);
                searchResults.add(searchPanel);
                searchResults.pack();

            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(tabs, "Date must be in format: dd-MM-yyyy", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        createBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(reserve_tf_id.getText()); //μπορεί να είναι οτιδήποτε (ακόμα και string, οπότε έλεγχος για το NumberFormatException (λάθος μετατροπή)

                Integer[] listOfNonReservedSeats = airReservation.reserve(id, ReserveStep.FIRST, null, null);
                if (listOfNonReservedSeats == null){
                    JOptionPane.showMessageDialog(tabs, "No flight data, check Flight ID", "Error ", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                else if (listOfNonReservedSeats.length == 0){
                    JOptionPane.showMessageDialog(tabs, "No available seats are available for this flight", "Error ", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JList<Integer> reservedSeats = new JList<>(listOfNonReservedSeats);
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                reservedSeats.setFont(new Font("Arial", Font.BOLD, 20));
                reservedSeats.setFixedCellHeight(38);
                reservedSeats.setFixedCellWidth(38);
                reservedSeats.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                reservedSeats.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                reservedSeats.setVisibleRowCount(8);
                panel.add(reservedSeats);

                //αφού βάλαμε τη λίστα θα βάλουμε και το κουμπί
                JButton seatsConfirmBtn = new JButton("OK");
                panel.add(seatsConfirmBtn, BorderLayout.SOUTH);
                //εδώ θα εμφανιστούν (σε νέο παράθυρο) οι διαθέσιμες θέσεις σε checkboxes, ο χρήστης θα επιλέξει κάποιες θέσεις και στη συνέχεια
                //ο server θα του πει να γράψει τα στοιχεία του για να ολοκληρώσει την διαδικασία (με 2 λεπτά όριο)
                JFrame frame = new JFrame("Select seat numbers to reserve, Ctrl+Click for multiple selection");
                frame.add(panel);
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //δε θέλουμε να κλείσει η εφαρμογή αν κλείσει το νέο παράθυρο αλλά μόνο το παράθυρο
                frame.pack();

                //listener για το κουμπί το οποίο δείχνει ποιές θέσεις θέλει να δεσμεύσει ο client
                seatsConfirmBtn.addActionListener(x -> {
                    //τώρα ο client στέλνει στον σερβερ, καλώντας την ίδια μέθοδο με παράμετρο=1
                    //η 3η παράμετρος είναι τα στοιχεία που επέλεξε ο χρήστης
                    try {
                        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //κλείνουμε το παράθυρο όταν πατηθεί το ΟΚ
                        //παίρνουμε τις θέσεις που επέλεξε ο χρήστης από τη λίστα και τις στέλνουμε στον σερβερ
                        //από τη στιγμή που ο σερβερ θα απαντήσει έχουμε 2 λεπτά να κάνουμε τη νέα καταχώριση
                        List<Integer> seatsList = reservedSeats.getSelectedValuesList();
                        //αν δε στείλει τίποτα ο χρήστης πετάμε exception
                        if (seatsList.isEmpty()) {
                            JOptionPane.showMessageDialog(tabs, "Please select your seat numbers", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        airReservation.reserve(id, ReserveStep.SECOND, seatsList, null);
                        //θα δημιουργηθεί ένα JFrame πάλι στο οποίο θα γράψουμε τα στοιχεία μας, επειδή για να γίνει η καταχώριση πρέπει να πατηθεί το κουμπί,
                        //ο χρήστης μπορεί να ακυρώσει τη διαδικασία χωρίς να συμβεί κάτι στα δεδομένα του σερβερ, πρέπει όμως να απελευθερωθούν οι θέσεις που επέλεξε

                        JPanel reservationPanel = new JPanel();
                        reservationPanel.setLayout(new GridLayout(3, 2));
                        reservationPanel.add(new JLabel("Name:"));
                        JTextField nameField = new JTextField(25);
                        reservationPanel.add(nameField);

                        reservationPanel.add(new JLabel("Last Name:"));
                        JTextField lastNameField = new JTextField(25);
                        reservationPanel.add(lastNameField);

                        JButton doReservationBtn = new JButton("OK");
                        JButton clearFieldsBtn = new JButton("Clear Data");
                        reservationPanel.add(doReservationBtn);
                        reservationPanel.add(clearFieldsBtn);

                        JFrame newReservationFrame = new JFrame("Please insert your details");
                        newReservationFrame.add(reservationPanel);
                        newReservationFrame.setVisible(true);
                        newReservationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        newReservationFrame.pack();

                        //καθαρισμός
                        clearFieldsBtn.addActionListener(y -> {
                            nameField.setText("");
                            lastNameField.setText("");
                        });

                        //μόλις πατηθεί το ΟΚ, η καταχώριση θα γίνει επιτυχώς στον σερβερ και οι αντίστοιχες θέσεις για τη συγκεκριμένη πτήση στον σερβερ θα έχουν τιμή true
                        //δηλαδή θα δηλωθούν ότι είναι δεσμευμένες
                        doReservationBtn.addActionListener(z -> {
                            try {
                                newReservationFrame.dispatchEvent(new WindowEvent(newReservationFrame, WindowEvent.WINDOW_CLOSING)); //κλείνουμε το παράθυρο όταν πατηθεί το ΟΚ
                                if (nameField.getText().isEmpty() || lastNameField.getText().isEmpty()) {
                                    JOptionPane.showMessageDialog(tabs, "Name or Last Name can't be empty", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                List<String> names = Arrays.asList(nameField.getText(),lastNameField.getText());
                                airReservation.reserve(id, ReserveStep.THIRD, seatsList, names); //προσθήκη της κράτησης
                                JOptionPane.showMessageDialog(tabs, "Successfully added your reservation", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } catch (RemoteException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                        });
                    } catch (RemoteException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                        //αν η λίστα είναι κενή απλώς γράφουμε στον χρήστη πως δεν επέλεξε κάτι (σφάλμα)
                    }
                });
            } catch (RemoteException ex) {
                Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        reservationDetailsBtn.addActionListener(e -> {
            try {
                String result = airReservation.displayReservationData(display_tf_name.getText(), display_tf_lname.getText(), Integer.parseInt(display_tf_id.getText()));
                //θα εμφανίσουμε το αποτέλεσμα σε ένα νέο παράθυρο
                JPanel displayPanel = new JPanel();
                //JTextArea που θα περιέχει το αποτέλεσμα της αναζήτησης του σερβερ
                JTextArea displayResultsArea = new JTextArea(20, 40);
                displayResultsArea.setEditable(false); //εφόσον αρχικοποιηθεί, δεν επιτρέπουμε την αλλαγή του αφού είναι read only
                displayResultsArea.setText(result);
                displayPanel.add(new JScrollPane(displayResultsArea));
                JFrame displayFrame = new JFrame("Your reservations details");
                displayFrame.add(displayPanel);
                displayFrame.setVisible(true);
                displayFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                displayFrame.pack();
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(tabs, "ID must be number", "Error ", JOptionPane.ERROR_MESSAGE);
            }
        });

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
