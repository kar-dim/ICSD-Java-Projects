package crypto;//Nikolaos Katsiopis icsd13076
//Dimitrios Karatzas icsd13072

import exception.ConnectionNotSafeException;
import exception.UnknownProtocolCommandException;
import util.NetworkOperations;
import util.TokenGenerator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class ServerCryptoRSA extends CryptoBase {
    private static final Logger LOGGER = Logger.getLogger(ServerCryptoRSA.class.getName());

    public ServerCryptoRSA(NetworkOperations network, String keyStoreName, char[] keyStorePass, String trustStoreName, char[] trustStorePass, String keyAlias, char[] keyPass) {
        super(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass);
        createIV(); //παράγουμε τυχαία τα IV parameters τα οποία θα χρησιμοποιηθούν στο CBC (AES Block Cipher)
    }

    //Πρωτόκολλο
    /*
    SERVER                    CLIENT
       <-------StartSession------
       -----------OK------------>
       <------Certificate--------
       -------CertReceived------>
       -------Certificate------->
       <------CertReceived-------
       -----Encrypted AES Key--->
       -Encrypted token(με AES)->
    
        while(true){ //παράδειγμα είναι αυτό 
           <--μηνυμα--
           --μηνυμα-->
        }
     */
    @Override
    public void performKeyExchange() {
        try {
            //διαβασμα του "startsession"
            if (!network.readUTF().equals(START_SESSION)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }
            //στελνουμε "ΟΚ"
            network.writeUTF(OK);
            //λαμβάνουμε το certificate
            var cerReceived = (X509Certificate) network.readObject();
            //Στέλνουμε ACK οτι το λάβαμε
            network.writeUTF(CERT_RECEIVED);
            //Validate το certificate
            if (!checkReceivedCertificate(cerReceived)) {
                throw new ConnectionNotSafeException("The certificate can't be verified!\n");
            }
            //extract public key από το certificate που λαμβάνει
            PublicKey receivedPublicKey = cerReceived.getPublicKey();

            //διάβασμα από το αρχείο
            try (FileInputStream fis = new FileInputStream("HW2_MultiKAP_Server/certificates/client1signed.cer")) {
                var cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(fis);
                network.writeObject(cert); //εδώ στέλνουμε το certificate
            }

            //αν δε το έλαβε, τότε κλείνουμε το session διοτι μάλλον θα υπάρχει πρόβλημα
            if (!network.readUTF().equals(CERT_RECEIVED)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }
            //παράγουμε το συμμετρικό AES key
            symmetricKey = getAESkey();
            //initialize το hmac
            initializeHMAC();
            //encryptMessage το AES key (με το public του client) και στέλνουμε το encrypted AES KEY + IV params (base64 string τα bytes)
            Cipher cipher = Cipher.getInstance("RSA", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, receivedPublicKey);
            network.writeObject(new SealedObject(symmetricKey, cipher));
            network.writeObject(new SealedObject(Base64.getEncoder().encodeToString(iv.getIV()), cipher));
            //παραγωγή του random token
            //χρησιμοποιείται για να αποτρέψουμε τα replay attacks
            String token = new TokenGenerator().generateToken();
            network.writeObject(encryptMessage(token, token));
            //παραδειγμα συνομιλίας
            communicateSecurely(token);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private SecretKey getAESkey() throws NoSuchAlgorithmException {
        var keygen = KeyGenerator.getInstance("AES");
        keygen.init(256, new SecureRandom());
        return keygen.generateKey();
    }

}
