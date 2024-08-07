package crypto;
//Nikolaos Katsiopis icsd13076
//Dimitrios Karatzas icsd13072

import util.NetworkOperations;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SealedObject;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CryptoDH extends CryptoBase {
    private static final Logger LOGGER = Logger.getLogger(CryptoDH.class.getName());
    public CryptoDH(NetworkOperations network, String keyStoreName, char[] keyStorePass, String trustStoreName, char[] trustStorePass) {
        super(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass);
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
            //ο αλγόριθμος έχει τελειώσει, μπορούμε να στέλνουμε μηνύματα στο δίκτυο
            //αρχικοποίηση του Cipher για κρυπτογράφηση με AES
            symmetricKey = new SecretKeySpec(sec, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            //αρχικοποίηση του hmac
            initializeHMAC();
            //πρώτα λαμβάνουμε το session ID
            cipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);
            SealedObject seal = (SealedObject) network.readObject();
            String token = (String) seal.getObject(cipher);
            //παραδειγμα συνομιλιας
            communicateSecurely(token);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
