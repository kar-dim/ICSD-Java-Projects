package domain;/* icsd13072 Karatzas Dimitris
   icsd13096 Lazaros Apostolos*/

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Announcement implements Serializable {
    private String announcement; //η ανακοίνωση είναι αυτό το string
    private String author; //ποιός έκανε την ανακοίνωση, username
    private LocalDate lastEdit;

    public Announcement(String announce, String auth, LocalDate dat) {
        announcement = announce;
        author = auth;
        lastEdit = dat;
    }

    public Announcement(Announcement announce) {
        announcement = announce.getAnnouncement();
        author = announce.getAuthor();
        lastEdit = announce.getLastEditDate();
    }

    //setters και getters
    public void setAnnouncement(String announce) {
        announcement = announce;
    }

    public void setAuthor(String auth) {
        author = auth;
    }

    public String getAuthor() {
        return author;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public LocalDate getLastEditDate() {
        return lastEdit;
    }

    public void setLastEditDate(LocalDate date) {
        lastEdit = date;
    }

    @Override
    public String toString() {
        return "Author: " + author + "\nAnnouncement: " + announcement + "\nLast Edit: " + lastEdit.toString() + "\n\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Announcement that = (Announcement) o;
        return Objects.equals(announcement, that.announcement) && Objects.equals(author, that.author) && Objects.equals(lastEdit, that.lastEdit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(announcement, author, lastEdit);
    }
}
