package util;

import java.security.PrivateKey;
import java.security.PublicKey;

import static util.Constants.PRIVATE_KEY_PATH;
import static util.Constants.PUBLIC_KEY_PATH;
import static util.FileUtils.getKeyFromFile;

public class Session {
    private static String loggedInUser = "";
    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    public static PublicKey getPublicKey() {
        return publicKey;
    }

    public static void setPublicKey(PublicKey publicKey) {
        Session.publicKey = publicKey;
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void setPrivateKey(PrivateKey privateKey) {
        Session.privateKey = privateKey;
    }
    public static String getLoggedInUser(){
        return loggedInUser == null ? "" : loggedInUser;
    }
    public static void setLoggedInUser(String userName){
        loggedInUser = userName;
    }

    public static void setUpKeys() {
        setPrivateKey((PrivateKey) getKeyFromFile(PRIVATE_KEY_PATH));
        setPublicKey((PublicKey) getKeyFromFile(PUBLIC_KEY_PATH));
    }
}
