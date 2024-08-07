package crypto;//Nikolaos Katsiopis icsd13076
//Dimitrios Karatzas icsd13072

import exception.ConnectionNotSafeException;
import exception.UnknownProtocolCommandException;
import util.NetworkOperations;
import util.TokenGenerator;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class CryptoRSA extends CryptoBase {
    private static final Logger LOGGER = Logger.getLogger(CryptoRSA.class.getName());

    public CryptoRSA(NetworkOperations network, String keyStoreName, char[] keyStorePass, String trustStoreName, char[] trustStorePass) {
        super(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass);
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
            X509Certificate cer_received = (X509Certificate) network.readObject();

            //Στέλνουμε ACK οτι το λάβαμε
            network.writeUTF(CERT_RECEIVED);
            //Validate το certificate
            if (!checkReceivedCertificate(cer_received)) {
                throw new ConnectionNotSafeException("The certificate can't be verified!\n");
            }
            //extract public key από το certificate
            //αυτό που λαμβάνει
            PublicKey reveived_pubkey = cer_received.getPublicKey();

            //διάβασμα από το αρχείο
            FileInputStream fis = new FileInputStream("HW2_MultiKAP_Server/certificates/client1signed.cer");
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(fis);
            network.writeObject(cert); //εδώ στέλνουμε το certificate

            //αν δε το έλαβε, τότε κλείνουμε το session διοτι μάλλον θα υπάρχει πρόβλημα
            if (!network.readUTF().equals(CERT_RECEIVED)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }
            //παράγουμε το συμμετρικό AES key
            symmetricKey = getAESkey();
            //initialize το hmac
            initializeHMAC();
            //encrypt το AES key (με το public του client) και στέλνουμε το encrypted AES KEY + IV params (base64 string τα bytes)
            Cipher cipher = Cipher.getInstance("RSA", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, reveived_pubkey);
            network.writeObject(new SealedObject(symmetricKey, cipher));
            network.writeObject(new SealedObject(Base64.getEncoder().encodeToString(this.iv.getIV()), cipher));

            //παραγωγή του random token
            //χρησιμοποιείται για να αποτρέψουμε τα replay attacks
            String token = new TokenGenerator().generateToken();
            network.writeObject(encrypt(token, token));
            //παραδειγμα συνομιλιας
            communicateSecurely(token);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private SecretKey getAESkey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(256, new SecureRandom());
        return keygen.generateKey();
    }

}
