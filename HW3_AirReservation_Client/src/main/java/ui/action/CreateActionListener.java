package ui.action;

import remote.ReserveStep;
import util.Session;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class CreateActionListener extends ActionBase {
    private final int id;

    public CreateActionListener(JTabbedPane tabs, int id) {
        super(tabs);
        this.id = id;
    }

    @Override
    public void doAction() {
        try {
            Integer[] listOfNonReservedSeats = Session.getAirReservation().reserve(id, ReserveStep.FIRST, null, null);
            if (listOfNonReservedSeats == null) {
                JOptionPane.showMessageDialog(tabs, "No flight data, check Flight ID", "Error ", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (listOfNonReservedSeats.length == 0) {
                JOptionPane.showMessageDialog(tabs, "No available seats are available for this flight", "Error ", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JList<Integer> reservedSeats = new JList<>(listOfNonReservedSeats);

            reservedSeats.setFont(new Font("Arial", Font.BOLD, 20));
            reservedSeats.setFixedCellHeight(38);
            reservedSeats.setFixedCellWidth(38);
            reservedSeats.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            reservedSeats.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            reservedSeats.setVisibleRowCount(8);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(reservedSeats);

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
                try {
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //κλείνουμε το παράθυρο όταν πατηθεί το ΟΚ
                    List<Integer> seatsList = reservedSeats.getSelectedValuesList();
                    if (seatsList.isEmpty()) {
                        JOptionPane.showMessageDialog(tabs, "Please select your seat numbers", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Session.getAirReservation().reserve(id, ReserveStep.SECOND, seatsList, null);

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
                            List<String> names = Arrays.asList(nameField.getText(), lastNameField.getText());
                            Session.getAirReservation().reserve(id, ReserveStep.THIRD, seatsList, names); //προσθήκη της κράτησης
                            JOptionPane.showMessageDialog(tabs, "Successfully added your reservation", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (RemoteException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    });
                } catch (RemoteException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            });
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
