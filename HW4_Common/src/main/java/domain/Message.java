package domain;
/* icsd13072 Karatzas Dimitris
   icsd13096 Lazaros Apostolos*/

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//η κλάση για το μήνυμα, αντικείμενα αυτού του τύπου μεταφέρονται μόνο από client σε server και αντίστροφα
public record Message(
        String message, User user, Announcement announcement, List<Announcement> listToSend, LocalDate startDate, LocalDate endDate) implements Serializable {

    public Message(String msg) {
        this(msg, null, null, null, null, null);
    }
    public Message(String msg, String userName) {
        this(msg, new User(userName, null, null, null));
    }

    public Message(String msg, User usr) {
        this(msg, new User(usr), null, null, null, null);
    }

    public Message(String msg, List<Announcement> list) {
        this(msg, null, null, new ArrayList<>(list), null, null);
    }

    public Message(String msg, Announcement announce) {
        this(msg, null, new Announcement(announce), null, null, null);
    }

    public Message(String msg, User usr, List<Announcement> list) {
        this(msg, usr, null, new ArrayList<>(list), null, null);
    }

    public Message(String msg, LocalDate startDate, LocalDate endDate) {
        this(msg, null, null, null, startDate, endDate);
    }
}
