package domain;
/* icsd13072 Karatzas Dimitris
   icsd13096 Lazaros Apostolos*/

import java.io.Serializable;

public record User(String username, String password, String name, String lastName) implements Serializable {
    public User(User other) {
        this(other.username, other.password, other.name, other.lastName);
    }
    public User(String username, String password) {
        this(username, password, null, null);
    }
}