package ui.action;

import javax.swing.*;
import java.awt.GridLayout;

import static util.Constants.EXPENSES_FILE_NAME;
import static util.Constants.INCOME_FILE_NAME;
import static util.EncryptionUtils.decryptRecordsFromFile;

public class PublishActionListener extends ActionBase {
    private final String publishDate;
    public PublishActionListener(JTabbedPane tabs, String publishDate1) {
        super(tabs);
        this.publishDate = publishDate1;
    }
    @Override
    public boolean doAction() {
        //παίρνουμε το μήνα
        int monthSelected;
        try {
            monthSelected = Integer.parseInt(publishDate);
            if (monthSelected < 1 || monthSelected > 12)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(tabs, "Wrong monthSelected number, must be 1-12, example: 1=January, 2=February", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Εμφάνιση στο UI
        JPanel recordsPanel = new JPanel();
        recordsPanel.setLayout(new GridLayout(1, 2));
        recordsPanel.add(new JScrollPane(createResultsTextArea(true, monthSelected)));
        recordsPanel.add(new JScrollPane(createResultsTextArea(false, monthSelected)));

        JFrame frame = new JFrame("Results");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        frame.add(recordsPanel);
        frame.pack();
        return true;
    }

    private JTextArea createResultsTextArea(boolean isExpense, int monthSelected) {
        String headLine = "Your " + (isExpense ? "Expenses" : "Income") + " for this month Selected\n\n";
        JTextArea textArea = new JTextArea();
        textArea.setText(headLine);
        textArea.append(decryptRecordsFromFile(privateKey, isExpense ? EXPENSES_FILE_NAME : INCOME_FILE_NAME, monthSelected));
        textArea.setEditable(false);
        return textArea;
    }
}
