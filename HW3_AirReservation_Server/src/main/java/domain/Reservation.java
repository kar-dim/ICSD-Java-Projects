package domain;/* Dimitris Karatzas icsd13072
   Apostolos Lazaros icsd13096
*/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

//κλάση που αναπαριστά μια κράτηση
public class Reservation implements Serializable {
    private final String passengerName;
    private final String passengerLastName;
    private final List<Seat> seats; //seats -> οι θέσεις που δεσμεύει ο συγκεκριμένος πελάτης (μπορεί ο ίδιος να δεσμεύσει παραπάνω από 1 θέση)

    //εδώ έχουμε κάνει μια παραδοχή: Ο πελάτης δεσμεύει τις υπόλοιπες θέσεις (πέρα από τη δικιά του) στο δικό του όνομα και όχι στο όνομα των πελατών
    //που θα καθίσουν σε αυτές
    public Reservation(String name, String lastName, List<Seat> seats) {
        passengerName = name;
        passengerLastName = lastName;
        this.seats = new ArrayList<>(seats);
    }

    //getters
    public String getPassengerName() {
        return passengerName;
    }

    public String getPassengerLastName() {
        return this.passengerLastName;
    }

    public List<Seat> getReservatedSeats() {
        return seats;
    }

    //εμφανίζει τα δεδομένα της συγκεκριμένης κράτησης, στην ουσία τις θέσεις που έχει δεσμεύσει ο χρήστης
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (Seat seat : seats.stream().filter(Seat::isReserved).toList())
            joiner.add(String.valueOf(seat.getSeatId()));
        return "Your reserved seats: " + joiner;
    }
}
