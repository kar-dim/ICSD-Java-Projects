/* Dimitris Karatzas icsd13072
   Nikolaos Katsiopis icsd13076
   Christos Papakostas icsd13143
 */

package domain;

import java.io.Serializable;
import java.time.LocalDate;
public class Income extends Transaction implements Serializable{
    public Income(double value, String description, LocalDate date) {
        super(value, description, date);
    }
    @Override
    public String toString() {
       return "Type: Income\nTransaction Date: "+ this.date.toString() + "\nValue: " + this.value + "\nDescription: "+ this.description;
    }
    
}
