/* Dimitris Karatzas icsd13072
   Apostolos Lazaros icsd13096
*/

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public interface AirReservation extends Remote {
    String checkAvailability(String start, String dest, LocalDate date) throws RemoteException;
    Integer[] reserve(int id, final int mode, List<Integer> seatsList, List<String> names) throws RemoteException;
    String displayReservationData( String name, String lname, int id) throws RemoteException;
}
