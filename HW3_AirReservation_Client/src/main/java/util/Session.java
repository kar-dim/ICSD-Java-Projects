package util;

import remote.AirReservation;

import java.rmi.Naming;

public class Session {
    private static AirReservation airReservation;
    public static void initializeRmiLookup(String lookUpName) throws Exception {
        airReservation = (AirReservation) Naming.lookup(lookUpName);
    }
    public static AirReservation getAirReservation() {
        return airReservation;
    }
}
