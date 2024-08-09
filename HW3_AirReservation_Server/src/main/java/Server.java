/* Dimitris Karatzas icsd13072
   Apostolos Lazaros icsd13096
 */

import domain.Flight;
import domain.Reservation;
import domain.Seat;
import remote.AirReservation;
import remote.ReserveStep;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Server extends UnicastRemoteObject implements AirReservation {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private final List<Flight> flightList = new ArrayList<>();

    public Server() throws RemoteException {
        super(0);
        try {
            final String flightsFilePath = "HW3_AirReservation_Server/flights.dat";
            Flight temp;
            if (new File(flightsFilePath).exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(flightsFilePath))) {
                    //διαβάζουμε όλο το αρχείο μέχρι να φτάσουμε στο τέλος, αν είναι άδειο δεν αλλάζει κάτι, απλώς η λίστα είναι άδεια
                    while ((temp = (Flight) ois.readObject()) != null)
                        flightList.add(temp);
                    return;
                }
            }
            new File(flightsFilePath).createNewFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(flightsFilePath));
                 ObjectInputStream ois = new ObjectInputStream(new FileInputStream(flightsFilePath))) {
                oos.writeObject(new Flight(1, "Athens", "Prague", 120, LocalDateTime.parse("10-10-2016 16:30", dateFormatter), List.of(new Reservation("Name1", "Lname1", List.of(new Seat(true, 2), new Seat(true, 51), new Seat(true, 100))))));
                oos.writeObject(new Flight(2, "New York", "Athens", 160, LocalDateTime.parse("12-10-2016 18:00", dateFormatter), List.of(new Reservation("Name1", "Lname1", List.of(new Seat(true, 24))))));
                oos.writeObject(new Flight(3, "Thessaloniki", "Berlin", 120, LocalDateTime.parse("10-05-2017 18:00", dateFormatter), List.of(new Reservation("Name2", "Lname2", List.of(new Seat(true, 17), new Seat(true, 18))))));
                //διαβάζουμε το αρχείο που μόλις γράψαμε (για επαλήθευση οτι γράφτηκαν σωστά)
                while ((temp = (Flight) ois.readObject()) != null)
                    flightList.add(temp);
            }
        } catch (EOFException ignored) {
        } catch (IOException | DateTimeParseException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws Exception {
        LOGGER.log(Level.INFO, "RMI server start");
        try {
            LocateRegistry.createRegistry(1099);
            LOGGER.log(Level.INFO, "RMI registry created.");
        } catch (RemoteException ignored) {
        }

        Server server = new Server();
        Naming.rebind("//localhost/ReservationService", server);
        LOGGER.log(Level.INFO, "Server bound in registry");
    }

    @Override
    public String checkAvailability(String start, String destination, LocalDate date) throws RemoteException {
        String result = flightList.stream()
                .filter(value -> value.from().equalsIgnoreCase(start) && value.destination().equalsIgnoreCase(destination) && value.date().toLocalDate().equals(date))
                .map(Flight::displayFlightData)
                .collect(Collectors.joining());
        return result.isEmpty() ? "Nothing found!" : result;
    }

    @Override
    public Integer[] reserve(int id, ReserveStep reserveStep, List<Integer> seatsList, List<String> names) throws
            RemoteException {
        switch (reserveStep) {
            case FIRST -> {
                Optional<Flight> flight = flightList.stream()
                        .filter(f -> f.id() == id)
                        .findFirst();
                if (flight.isPresent())
                    return flight.get().getNonReservedSeats();
            }
            /* TODO TIMER */
            case SECOND -> {
            }
            case THIRD -> {
                synchronized (this) {
                    flightList.stream()
                            .filter(flight -> flight.id() == id)
                            .forEach(flight -> flight.addReservation(names.get(0), names.get(1), seatsList));
                }
            }
        }
        return null;
    }

    @Override
    public String displayReservationData(String name, String lname, int id) throws RemoteException {
        String result = flightList.stream()
                .filter(flight -> flight.id() == id)
                .flatMap(flight -> flight.reservations().stream()
                        .filter(reservation -> reservation.passengerName().equalsIgnoreCase(name) && reservation.passengerLastName().equalsIgnoreCase(lname))
                        .map(reservation -> flight + reservation.toString() + "\n"))
                .collect(Collectors.joining());
        return result.isEmpty() ? "No results found!" : result;
    }
}
