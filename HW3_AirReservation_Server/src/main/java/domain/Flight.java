package domain;/* Dimitris Karatzas icsd13072
   Apostolos Lazaros icsd13096
 */

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Flight(
        int id, List<Reservation> reservations, List<Seat> seats, String from, String destination, double cost, LocalDateTime date) implements Serializable {
    private static final int MAX_SEATS_CAPACITY = 250;

    public Flight(int id, String from, String destination, double cost, LocalDateTime dateTime, List<Reservation> reservs) {
        this(id, new ArrayList<>(reservs), initializeSeats(), from, destination, cost, dateTime);
        reservations.stream()
                .flatMap(reservation -> reservation.seats().stream())
                .forEach(seat -> seats.get(seat.getSeatId()).setReserved(true));
    }
    private static List<Seat> initializeSeats() {
        return IntStream.range(0, MAX_SEATS_CAPACITY)
                .mapToObj(i -> new Seat(false, i))
                .collect(Collectors.toList());
    }

    //προσθήκη κράτησηςς
    public void addReservation(String name, String lname, List<Integer> seatsProvided) {
        reservations.add(new Reservation(name, lname, seats));
        seatsProvided.forEach(seatNo -> seats.get(seatNo).setReserved(true));
    }

    //επιστρέφει τον πίνακα με τις θέσεις που δεν είναι δεσμευμένες
    public Integer[] getNonReservedSeats() {
        return seats.stream()
                .filter(seat -> !seat.isReserved())
                .map(Seat::getSeatId)
                .sorted()
                .toArray(Integer[]::new);
    }

    //toString() -> εμφανίζει τα πάντα σχετικά με τη κλάση, ενώ η displayFlightData() εμφανίζει μόνο όσα ζητούνται (για το 1η επιλογή "έλεγχος διαθεσιμότητας" )
    @Override
    public String toString() {
        return "ID: " + id + "\nStart city: " + from + "\nDestination city: " + destination + "\nSeats Capacity: " + MAX_SEATS_CAPACITY + "\nTicket Cost: " + cost + "\nDate: " + date + "\n";
    }

    //εδώ υπολογίζονται και οι διαθέσιμες θέσεις, απλώς μετράμε τις θέσεις του is_reserved πίνακα που έχουν τιμή false
    public String displayFlightData() {
        long availableSeats = seats.stream()
                .filter(seat -> !seat.isReserved())
                .count();
        return String.format("ID: %s\nAvailable Seats: %d\nTicket Cost: %s\nTime: %s\n", id, availableSeats, cost, String.format("%02d:%02d", date.getHour(), date.getMinute()));
    }
}
