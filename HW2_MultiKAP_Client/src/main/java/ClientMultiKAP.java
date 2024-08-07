//Dimitrios Karatzas icsd13072

import crypto.ClientCryptoDH;
import crypto.ClientCryptoRSA;
import crypto.ClientCryptoStS;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import util.NetworkOperations;

import java.io.FileInputStream;
import java.security.Security;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class ClientMultiKAP {
    private static final Logger LOGGER = Logger.getLogger(ClientMultiKAP.class.getName());

    private static final NetworkOperations network = new NetworkOperations();

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        Properties properties = new Properties();
        try  (FileInputStream fis = new FileInputStream("HW2_MultiKAP_Client/config.properties")) {
            properties.load(fis);
            //set system properties
            System.setProperty("jdk.crypto.KeyAgreement.legacyKDF", properties.getProperty("jdk.crypto.KeyAgreement.legacyKDF"));
            System.setProperty("javax.net.ssl.trustStore", properties.getProperty("javax.net.ssl.trustStore"));

            //load keystore paths/passwords and key aliases/passwords
            String keyStoreName = properties.getProperty("keyStoreName");
            char[] keyStorePass = properties.getProperty("keyStorePass").toCharArray();
            String trustStoreName = properties.getProperty("trustStoreName");
            char[] trustStorePass = properties.getProperty("trustStorePass").toCharArray();
            String keyAlias = properties.getProperty("keyAlias");
            char[] keyPass = properties.getProperty("keyPass").toCharArray();

            Scanner scan = new Scanner(System.in);
            network.initializeConnectionAsClient();
            //εδώ ο client επιλέγει ποιον αλγόριθμο θέλει να χρησιμοποιήσει
            while (true) {
                LOGGER.log(Level.INFO, "Select a key-exchange method\n1: Encapsulation\n2: Diffie-Hellman\n3: StS Protocol");
                int choice = scan.nextInt();
                if (choice == 1) {
                    network.writeUTF(START_RSA);
                    new ClientCryptoRSA(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass).performKeyExchange();
                    break;
                } else if (choice == 2) {
                    network.writeUTF(START_DH);
                    new ClientCryptoDH(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass).performKeyExchange();
                    break;
                } else if (choice == 3) {
                    network.writeUTF(START_STS);
                    new ClientCryptoStS(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass).performKeyExchange();
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
