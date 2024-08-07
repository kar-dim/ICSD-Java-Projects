/* Dimitris Karatzas icsd13072
   Nikolaos Katsiopis icsd13076
   Christos Papakostas icsd13143
 */
package sec3.domain;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class Transaction implements Serializable {
    protected double value;
    protected String description;
    protected LocalDate date;
    public Transaction(double value, String description, LocalDate date){
        this.value=value;
        this.description=description;
        this.date=date;
    }

    public LocalDate getTransactionDate(){
        return this.date;
    }
    public String getDescription(){
        return this.description;
    }
    public double getValue(){
        return this.value;
    }
    public int getMonth(){
        return this.date.getMonthValue();
    }
    public void setTransactionDate(LocalDate date){
        this.date=date;
    }
    public void setDescription(String desc){
        this.description=desc;
    }
    public void setValue(double value){
        this.value=value;
    }
    @Override
    public abstract String toString();
}
