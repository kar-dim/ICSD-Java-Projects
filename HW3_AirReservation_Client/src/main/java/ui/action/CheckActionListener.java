package ui.action;

import util.Session;

import javax.swing.*;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;

public class CheckActionListener extends ActionBase {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final String checkDate;
    private final String departure;
    private final String destination;
    public CheckActionListener(JTabbedPane tabs, String checkDate, String departure, String destination) {
        super(tabs);
        this.checkDate = checkDate;
        this.departure = departure;
        this.destination = destination;
    }

    @Override
    public void doAction() {
        try {
            LocalDate date = LocalDate.parse(checkDate, dateFormatter);
            JFrame searchResults = new JFrame("Search results");
            searchResults.setVisible(true);
            JPanel searchPanel = new JPanel();
            JTextArea textArea = new JTextArea(20, 40);
            textArea.setEditable(false); //εφόσον αρχικοποιηθεί, δεν επιτρέπουμε την αλλαγή του αφού είναι read only
            JScrollPane scrollPane = new JScrollPane(textArea);

            textArea.append(Session.getAirReservation().checkAvailability(departure, destination, date));

            searchPanel.add(scrollPane);
            searchResults.add(searchPanel);
            searchResults.pack();

        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(tabs, "Date must be in format: dd-MM-yyyy", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
