package util;

import java.security.SecureRandom;
import java.util.Random;

public class TokenGenerator {
    private static final char[] symbols = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" +  "abcdefghijklmnopqrstuvwxyz"+ "0123456789").toCharArray();

    public String generateToken() {
        Random random = new SecureRandom();
        char[] buf = new char[21];
        for (int i = 0; i < buf.length; ++i)
            buf[i] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

}
