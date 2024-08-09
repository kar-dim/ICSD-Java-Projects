package crypto;//Dimitrios Karatzas icsd13072

import util.NetworkOperations;
import util.TokenGenerator;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.PublicKey;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientCryptoDH extends CryptoDH {
    private static final Logger LOGGER = Logger.getLogger(ClientCryptoDH.class.getName());

    public ClientCryptoDH(NetworkOperations network, String keyStoreName, char[] keyStorePass, String trustStoreName, char[] trustStorePass, String keyAlias, char[] keyPass) {
        super(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass);
        generateDHKeys();
    }

    /*πρωτόκολλο
       SERVER                    CLIENT
         <-------PublicDHKey---------
         -------PublicDHKey--------->
         <-Encrypted Session Token-

         while(true){ //παράδειγμα είναι αυτό
              <--μηνυμα--
              --μηνυμα-->
           }

      */
    @Override
    public void performKeyExchange() {
        //Initiator -> κάνει connect σε socket
        try {
            //αρχή επικοινωνίας, αρχικά ο Initiator στέλνει το public DH key
            network.writeObject(publicDHKey);
            //στη συνέχεια λαμβάνει το public DH του Receiver και τα IV parameters
            publicDHExtKey = (PublicKey) network.readObject();
            iv = new IvParameterSpec(Base64.getDecoder().decode((String) network.readObject()));
            //Με βάση τον DH αλγοριθμο, πρέπει να παραχθεί το SecretKey με βάση το public Κευ που δέχτηκε
            generateDHSecretKey();
            //αρχικοποίηση του Cipher για κρυπτογράφηση με AES
            symmetricKey = new SecretKeySpec(dhSecretKey, "AES");
            //αρχικοποίηση του hmac
            initializeHMAC();
            //πρώτα στέλνουμε το SESSION TOKEN
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, iv);
            String token = new TokenGenerator().generateToken();
            network.writeObject(new SealedObject(token, cipher));
            //παραδειγμα συνομιλιας
            communicateSecurelyAsClient(token);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
