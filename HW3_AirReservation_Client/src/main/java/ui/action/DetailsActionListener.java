package ui.action;

import util.Session;

import javax.swing.*;
import java.rmi.RemoteException;
import java.util.logging.Level;

public class DetailsActionListener extends ActionBase {
    private final String name;
    private final String lastName;
    private final int id;
    public DetailsActionListener(JTabbedPane tabs, String name, String lastName, int id) {
        super(tabs);
        this.name = name;
        this.lastName = lastName;
        this.id = id;
    }

    @Override
    public void doAction() {
        try {
            String result = Session.getAirReservation().displayReservationData(name, lastName, id);
            //θα εμφανίσουμε το αποτέλεσμα σε ένα νέο παράθυρο
            JPanel displayPanel = new JPanel();
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
    }
}
