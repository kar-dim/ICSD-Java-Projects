//Nikolaos Katsiopis icsd13076
//Dimitrios Karatzas icsd13072

import crypto.ServerCryptoDH;
import crypto.ServerCryptoRSA;
import crypto.ServerCryptoStS;
import exception.UnknownProtocolCommandException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import util.NetworkOperations;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Security;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class ServerMultiKAP {

    private static final Logger LOGGER = Logger.getLogger(ServerMultiKAP.class.getName());

    private static final NetworkOperations network = new NetworkOperations();

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        Properties properties = new Properties();
        try  (FileInputStream fis = new FileInputStream("HW2_MultiKAP_Server/config.properties")) {
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

            LOGGER.log(Level.INFO, "Waiting for connection....");
            network.initializeConnection();
            LOGGER.log(Level.INFO, "Client: " + network.getSock().getInetAddress().getHostName() + " connected!");
            String choice = network.readUTF();
            switch (choice) {
                case START_RSA ->
                        new ServerCryptoRSA(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass).performKeyExchange();
                case START_DH ->
                        new ServerCryptoDH(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass).performKeyExchange();
                case START_STS ->
                        new ServerCryptoStS(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass).performKeyExchange();
                default ->
                        throw new UnknownProtocolCommandException("Unknown protocol sent from client...\nTerminating program....");
            }
        } catch (IOException | UnknownProtocolCommandException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            network.closeConnection();
        }
    }
}
