package domain;
/* Dimitris Karatzas icsd13072
   Apostolos Lazaros icsd13096
*/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public record Reservation (String passengerName, String passengerLastName, List<Seat> seats) implements Serializable {
    public Reservation {
        seats = new ArrayList<>(seats);
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
