package util;//Dimitrios Karatzas icsd13072
//Nikolaos Katsiopis icsd13076

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class TokenGenerator {
    public String generateToken() {
        Random random = new SecureRandom();
        char[] buf = new char[21];
        for (int i = 0; i < buf.length; ++i) {
            buf[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }
    private final char[] symbols;
    public TokenGenerator() {
        this.symbols = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" +  "abcdefghijklmnopqrstuvwxyz"+ "0123456789").toCharArray();
    }
}
