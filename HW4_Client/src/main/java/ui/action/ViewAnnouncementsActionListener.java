package ui.action;

import domain.Announcement;
import domain.Message;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static util.MessageType.VIEW;
import static util.MessageType.VIEW_OK;
import static util.NetworkOperations.*;

public class ViewAnnouncementsActionListener extends ActionBase {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final String viewStartDate;
    private final String viewEndDate;
    public ViewAnnouncementsActionListener(JTabbedPane tabs, String viewStartDate, String viewEndDate) {
        super(tabs);
        this.viewStartDate = viewStartDate;
        this.viewEndDate = viewEndDate;
    }

    @Override
    public void doAction() {
        try {
            //παίρνουμε τα Dates από τον client
            LocalDate dateStart = LocalDate.parse(viewStartDate, dateFormatter);
            LocalDate dateEnd = LocalDate.parse(viewEndDate, dateFormatter);

            if (dateStart.isAfter(dateEnd)) {
                JOptionPane.showMessageDialog(tabs, "Could not search announcements, maybe the first date is after the second?", "Error searching data", JOptionPane.ERROR_MESSAGE);
                return;
            }
            initializeConnection();
            //γράφουμε στο socket τις ημερομηνίες και το μήνυμα (δε χρειάζεται να στείλουμε τον χρήστη αφού ο καθένας μπορεί να κάνει view)
            writeMessage(new Message(VIEW, dateStart, dateEnd));
            //έλεγχος μηνύματος
            Message msg = readMessage();
            if (msg.message().equals(VIEW_OK)) {
                //αρχικοποίηση της λίστας με βάση τη λίστα που έστειλε ο σερβερ
                List<Announcement> list = new ArrayList<>(msg.listToSend());

                JFrame jf = new JFrame("Search results");
                jf.setVisible(true);

                JPanel panel = new JPanel();
                JTextArea jta = new JTextArea(20, 40);
                jta.setEditable(false); //εφόσον αρχικοποιηθεί, δεν επιτρέπουμε την αλλαγή του αφού είναι read only
                JScrollPane jsp = new JScrollPane(jta);

                //προσθήκη των ανακοινώσεων στο textarea
                for (Announcement announcement : list) {
                    jta.append(announcement.toString());
                }
                panel.add(jsp);
                //προσθήκη όλου του πανελ στο frame
                jf.add(panel);
                jf.pack();
            } else {
                JOptionPane.showMessageDialog(tabs, "Could not find any announcements", "Nothing found", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ClassNotFoundException | IOException ex) {
            JOptionPane.showMessageDialog(tabs, "Could not search announcements", "Error searching data", JOptionPane.ERROR_MESSAGE);
            closeConnection();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(tabs, "Could not search announcements, make sure your dates are in format dd-MM-yyyy", "Error searching data", JOptionPane.ERROR_MESSAGE);
            closeConnection();
        }
    }
}
