/* Dimitris Karatzas icsd13072
   Apostolos Lazaros icsd13096
 */

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//απλή κλάση που αναπαριστά μια πτήση
public class Flight implements Serializable {

    private final int MAX_SEATS_CAPACITY = 250; //η τιμή αυτή δείχνει το μέγιστο όριο χωρητικότητας για κάθε πτήση. Κανονικά είναι μεταβλητή αλλά έτσι θα χρειαζόμασταν μια άλλη
    //κλάση για αεροπλάνο που να θέταμε εκεί τη συγκεκριμένη χωρητικότητα αλλά θεωρήσαμε πως δεν είναι απαραίτητο
    private int id;
    private final List<Reservation> reservations; //η δομή που κρατάει τις κρατήσεις για τη συγκεκριμένη πτήση
    private String from;
    private String destination;
    private final List<Seat> seats;
    private double cost;
    private LocalDateTime dateTime;

    public Flight(int id, String from, String destination, double cost, LocalDateTime dateTime, List<Reservation> reservs) {
        reservations = new ArrayList<>(reservs);
        seats = new ArrayList<>(MAX_SEATS_CAPACITY);
        for (int i = 0; i < MAX_SEATS_CAPACITY; i++)
            seats.add(new Seat(false, i));

        for (Reservation reservation : reservations) {
            for (Seat seat : reservation.getReservatedSeats()) {
                seats.get(seat.getSeatId()).setReserved(true);
            }
        }
        this.id = id;
        this.from = from;
        this.destination = destination;
        this.cost = cost;
        this.dateTime = dateTime;
    }

    //getters
    public int getId() {
        return this.id;
    }

    public String getFrom() {
        return this.from;
    }

    public String getDestination() {
        return this.destination;
    }

    public double getCost() {
        return this.cost;
    }

    public LocalDateTime getDate() {
        return this.dateTime;
    }

    public List<Reservation> getReservations() {
        return this.reservations;
    }

    //setters
    public void setId(int id) {
        this.id = id;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setDestination(String dest) {
        this.destination = dest;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setDate(LocalDateTime date) {
        this.dateTime = date;
    }

    //προσθήκη κράτησης, θέτουμε σε κάθε θέση της κράτησης TRUE για τη συγκεκριμένη πτήση
    // π.χ θεσεις πτησης: [οχι,οχι,οχι,οχι...] και θεσεις κρατησης:[0,2,3] αρα οι θεσεις πτησεις διαμορφωνονται ως εξης -> [ναι,οχι,ναι,ναι,οχι...]
    public void addReservation(String name, String lname, List<Seat> seatsProvided) {
        reservations.add(new Reservation(name, lname, seats));
        for (Seat seat : seatsProvided)
            seats.get(seat.getSeatId()).setReserved(seat.isReserved());
    }

    //επιστρέφει τον πίνακα με τις θέσεις που δεν είναι δεσμευμένες
    public List<Seat> getNonReservedSeats() {
        return seats.stream().filter(seat -> !seat.isReserved()).sorted().toList();
    }

    //toString() -> εμφανίζει τα πάντα σχετικά με τη κλάση, ενώ η displayFlightData() εμφανίζει μόνο όσα ζητούνται (για το 1η επιλογή "έλεγχος διαθεσιμότητας" )
    @Override
    public String toString() {
        return "ID: " + id + "\nStart city: " + from + "\nDestination city: " + destination + "\nSeats Capacity: " +MAX_SEATS_CAPACITY + "\nTicket Cost: " + cost + "\nDate: " + dateTime + "\n";
    }

    //εδώ υπολογίζονται και οι διαθέσιμες θέσεις, απλώς μετράμε τις θέσεις του is_reserved πίνακα που έχουν τιμή false
    public String displayFlightData() {
        long availableSeats = seats.stream().filter(seat -> !seat.isReserved()).count();
        int hours = dateTime.getHour();
        int minutes = dateTime.getMinute();
        //επειδή τα getHours() και getMinutes() αν η τιμή είναι 0 επιστρέφουν 0, εμείς θέλουμε η ώρα να φαίνεται με 2 ψηφία (π.χ 18:00 και όχι 18:0 ή 00:30 και όχι 0:30)
        if (hours == 0 && minutes != 0) {
            return "ID: " + id + "\nAvailable Seats: " + availableSeats + "\nTicket Cost: " + cost + "\nTime: " + "0" + hours + ":" + minutes + "\n";
        } else if (hours == 0) {
            return "ID: " + id + "\nAvailable Seats: " + availableSeats + "\nTicket Cost: " + cost + "\nTime: " + "0" + hours + ":" + "0" + minutes + "\n";
        } else if (minutes == 0) {
            return "ID: " + id + "\nAvailable Seats: " + availableSeats + "\nTicket Cost: " + cost + "\nTime: " + hours + ":" + "0" + minutes + "\n";
        } else {
            return "ID: " + id + "\nAvailable Seats: " + availableSeats + "\nTicket Cost: " + cost + "\nTime: " + hours + ":" + minutes + "\n";
        }
    }
}
