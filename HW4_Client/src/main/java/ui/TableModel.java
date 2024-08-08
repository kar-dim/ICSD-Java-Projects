package ui;
/* icsd13072 Karatzas Dimitris
   icsd13096 Lazaros Apostolos*/

import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;

public class TableModel extends DefaultTableModel {
    public TableModel(int rows, int cols) { // constructor
        super(rows, cols);
        this.setColumnIdentifiers(new String[]{"Check to delete", "Announcement", "Last Edit Date"});
    }

    @Override
    //μόνο τα checkboxes μπορούμε να αλλάζουμε καθώς επίσης και το όνομα της ανακοίνωσης (για το Update)
    public boolean isCellEditable(int row, int column) {
        return (column == 0 || column == 1);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class cl = String.class;
        if (columnIndex == 0)
            cl = Boolean.class;
        else if (columnIndex == 3) {
            cl = LocalDate.class;
        }
        return cl;
    }
}