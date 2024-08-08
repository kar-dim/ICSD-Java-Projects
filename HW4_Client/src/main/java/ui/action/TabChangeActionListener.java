package ui.action;

import domain.Announcement;
import domain.Message;
import ui.TableModel;
import util.Session;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;

import static util.MessageType.EDIT_DELETE;
import static util.MessageType.EDIT_DELETE_FAIL;
import static util.NetworkOperations.*;

public class TabChangeActionListener extends ActionBase {
    private final List<Announcement> userAnnouncements;
    private final List<JCheckBox> deleteAnnouncementCheck;
    private final JPanel deleteEditRecordsPanel;
    private final JButton deleteAnnouncementsBtn;
    private final JButton updateAnnouncementsBtn;

    public TabChangeActionListener(JTabbedPane tabs, List<Announcement> userAnnouncements, List<JCheckBox> deleteAnnouncementCheck, JPanel deleteEditRecordsPanel, JButton deleteAnnouncementsBtn, JButton updateAnnouncementsBtn) {
        super(tabs);
        this.userAnnouncements = userAnnouncements;
        this.deleteAnnouncementCheck = deleteAnnouncementCheck;
        this.deleteEditRecordsPanel = deleteEditRecordsPanel;
        this.deleteAnnouncementsBtn = deleteAnnouncementsBtn;
        this.updateAnnouncementsBtn = updateAnnouncementsBtn;
    }

    @Override
    public void doAction() {
        if (tabs.getSelectedIndex() == 4) {
            try {
                initializeConnection();
                writeMessage(new Message(EDIT_DELETE, Session.getLoggedInUser().username()));
                Message msg = readMessage();
                if (msg.message().equals(EDIT_DELETE_FAIL)) {
                    deleteEditRecordsPanel.removeAll();
                    JOptionPane.showMessageDialog(tabs, "Error trying to access data from server, or you don't have any announcements", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                //πήραμε τη λίστα με τις ανακοινώσεις και γίνεται δέσμευση μνήμης στα στοιχεία ανάλογα το μέγεθος
                userAnnouncements.clear();
                userAnnouncements.addAll(msg.listToSend());
                deleteAnnouncementCheck.clear();
                //χρειαζόμαστε το table model για να γράψουμε στο table τις γραμμές
                var announcementsTableModel = new TableModel(userAnnouncements.size(), 3);
                JTable table = new JTable();
                table.setModel(announcementsTableModel);

                Object[][] row_data = new Object[userAnnouncements.size()][3];
                for (int i = 0; i < userAnnouncements.size(); i++) {
                    deleteAnnouncementCheck.add(new JCheckBox());
                    row_data[i][0] = false;
                    row_data[i][1] = userAnnouncements.get(i).getAnnouncement();
                    row_data[i][2] = userAnnouncements.get(i).getLastEditDate();
                    announcementsTableModel.setValueAt(row_data[i][0], i, 0);
                    announcementsTableModel.setValueAt(row_data[i][1], i, 1);
                    announcementsTableModel.setValueAt(row_data[i][2], i, 2);
                    announcementsTableModel.addTableModelListener(event -> {
                        int row = event.getFirstRow();
                        int col = event.getColumn();
                        TableModel source = (TableModel) event.getSource();
                        if (col == 0) {
                            boolean markForDeletion = (Boolean) source.getValueAt(row, col);
                            deleteAnnouncementCheck.get(row).setSelected(markForDeletion);
                        } else if (col == 1) {
                            String newAnnouncement = (String) source.getValueAt(row, col);
                            userAnnouncements.get(row).setAnnouncement(newAnnouncement);
                            userAnnouncements.get(row).setLastEditDate(LocalDate.now());
                        }
                    });
                }

                //αφού τελειώσουμε με τον κώδικα για τη μεταφορά δεδομένων και τους ελέγχους, πρέπει να φτιάξουμκε και το layout
                deleteEditRecordsPanel.removeAll();
                deleteEditRecordsPanel.setLayout(new GridLayout(2, 1));
                JScrollPane jsp = new JScrollPane(table);

                deleteEditRecordsPanel.add(jsp);

                JPanel buttons = new JPanel();
                buttons.setLayout(new FlowLayout());
                buttons.add(deleteAnnouncementsBtn);
                buttons.add(updateAnnouncementsBtn);

                deleteEditRecordsPanel.add(buttons);

                tabs.revalidate();
                tabs.repaint();
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                closeConnection();
            }
        }
    }
}
