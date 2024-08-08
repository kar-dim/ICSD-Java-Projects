package domain;
//Nikolaos Katsiopis icsd13076
//Dimitrios Karatzas icsd13072

import java.io.Serializable;

public record Message (String message, String token, String hmac) implements Serializable  {
    @Override
    public String toString() {
        return message + token;
    }
}
