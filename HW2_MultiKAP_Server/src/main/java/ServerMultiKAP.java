//Nikolaos Katsiopis icsd13076
//Dimitrios Karatzas icsd13072

import crypto.ServerCryptoDH;
import crypto.ServerCryptoRSA;
import crypto.ServerCryptoStS;
import exception.UnknownProtocolCommandException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import util.NetworkOperations;

import java.io.IOException;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final NetworkOperations network = new NetworkOperations();
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        System.setProperty("javax.net.ssl.trustStore", "HW2_MultiKAP_Server/keystores/truststoreCL1");
        String keyStoreName = "HW2_MultiKAP_Server/keystores/keystoreCL1";
        char[] keyStorePass = "password1".toCharArray();
        String trustStoreName = "HW2_MultiKAP_Server/keystores/truststoreCL1";
        char[] trustStorePass = "password1".toCharArray();
        try {
            LOGGER.log(Level.INFO, "Waiting for connection....");
            network.initializeConnection();
            LOGGER.log(Level.INFO, "Client: " + network.getSock().getInetAddress().getHostName() + " connected!");
            String choice = network.readUTF();
            switch (choice) {
                case START_RSA -> new ServerCryptoRSA(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass).performKeyExchange();
                case START_DH -> new ServerCryptoDH(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass).performKeyExchange();
                case START_STS -> new ServerCryptoStS(network,keyStoreName, keyStorePass, trustStoreName, trustStorePass).performKeyExchange();
                default -> throw new UnknownProtocolCommandException("Unknown protocol sent from client...\nTerminating program....");
            }
        } catch (IOException | UnknownProtocolCommandException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            network.closeConnection();
        }
    }
}
