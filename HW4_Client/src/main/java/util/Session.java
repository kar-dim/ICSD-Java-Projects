package util;

import domain.User;

public class Session {
    private static User loggedInUser;
    public static User getLoggedInUser(){
        return loggedInUser;
    }
    public static void setLoggedInUser(User user){
        loggedInUser = user;
    }
}
