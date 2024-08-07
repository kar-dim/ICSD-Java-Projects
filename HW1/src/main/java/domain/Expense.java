/* Dimitris Karatzas icsd13072
   Nikolaos Katsiopis icsd13076
   Christos Papakostas icsd13143
 */

package sec3.domain;

import java.io.Serializable;
import java.time.LocalDate;

public class Expense extends Transaction implements Serializable{

    public Expense(double value, String description, LocalDate date) {
        super(value, description, date);
    }
    @Override
    public String toString() {
       return "Type: Expense\nTransaction Date: " + this.date.toString() + "\nValue: " + this.value + "\nDescription: " + this.description;
    }
    
}
