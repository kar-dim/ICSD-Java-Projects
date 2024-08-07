//Dimitrios Karatzas icsd13072

import crypto.ClientCryptoDH;
import crypto.ClientCryptoRSA;
import crypto.ClientCryptoStS;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import util.NetworkOperations;

import java.security.Security;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final NetworkOperations network = new NetworkOperations();
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        System.setProperty("javax.net.ssl.trustStore", "HW2_MultiKAP_Client/keystores/truststoreCL2");
        String keyStoreName = "HW2_MultiKAP_Client/keystores/keystoreCL2";
        char[] keyStorePass = "password2".toCharArray();
        String trustStoreName = "HW2_MultiKAP_Client/keystores/truststoreCL2";
        char[] trustStorePass = "password2".toCharArray();
        Scanner scan = new Scanner(System.in);

        try {
            network.initializeConnectionAsClient();
            //εδώ ο client επιλέγει ποιον αλγόριθμο θέλει να χρησιμοποιήσει
            while (true) {
                LOGGER.log(Level.INFO, "Select a key-exchange method\n1: Encapsulation\n2: Diffie-Hellman\n3: StS Protocol");
                int choice = scan.nextInt();
                if (choice == 1) {
                    network.writeUTF(START_RSA);
                    new ClientCryptoRSA(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass).performKeyExchange();
                    break;
                } else if (choice == 2) {
                    network.writeUTF(START_DH);
                    new ClientCryptoDH(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass).performKeyExchange();
                    break;
                } else if (choice == 3) {
                    network.writeUTF(START_STS);
                    new ClientCryptoStS(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass).performKeyExchange();
                    break;
                } else {
                    LOGGER.log(Level.SEVERE, "Wrong choice, please type 1,2 or 3");
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            network.closeConnection();
        }
    }
}
