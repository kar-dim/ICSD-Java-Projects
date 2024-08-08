package crypto;
//Nikolaos Katsiopis icsd13076
//Dimitrios Karatzas icsd13072

import util.NetworkOperations;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import java.security.PublicKey;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerCryptoDH extends CryptoDH {
    private static final Logger LOGGER = Logger.getLogger(ServerCryptoDH.class.getName());
    public ServerCryptoDH(NetworkOperations network, String keyStoreName, char[] keyStorePass, String trustStoreName, char[] trustStorePass, String keyAlias, char[] keyPass) {
        super(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass);
        generateDHKeys();
        createIV();
    }
    @Override
    public void performKeyExchange() {
        try {
            //αρχή επικοινωνίας, ανταλλαγη public keys
            publicDHExtKey = (PublicKey) network.readObject();
            network.writeObject(publicDHKey);
            network.writeObject(Base64.getEncoder().encodeToString(iv.getIV()));
            //Με βάση τον DH αλγοριθμο, πρέπει να παραχθεί το SecretKey με βάση το public Κευ που δέχτηκε
            generateDHSecretKey();
            //ο αλγόριθμος έχει τελειώσει
            symmetricKey = new SecretKeySpec(dhSecretKey, "AES");
            //αρχικοποίηση του hmac
            initializeHMAC();
            //πρώτα λαμβάνουμε το session ID
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);
            var sealedObject = (SealedObject) network.readObject();
            String token = (String) sealedObject.getObject(cipher);
            //παραδειγμα συνομιλιας
            communicateSecurely(token);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
