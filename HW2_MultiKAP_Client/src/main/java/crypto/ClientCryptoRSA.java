package crypto;//Dimitrios Karatzas icsd13072

import domain.Message;
import exception.ConnectionNotSafeException;
import exception.UnknownProtocolCommandException;
import util.NetworkOperations;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class ClientCryptoRSA extends CryptoBase {
    private static final Logger LOGGER = Logger.getLogger(ClientCryptoRSA.class.getName());

    public ClientCryptoRSA(NetworkOperations network, String keyStoreName, char[] keyStorePass, String trustStoreName, char[] trustStorePass, String keyAlias, char[] keyPass) {
        super(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass);
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
            //ξεκινάμε πρώτοι ως client και στέλνουμε StartSession για να ξεκινήσει η διαδικασία
            network.writeUTF(START_SESSION);

            //αν δε μας απαντήσει ο "server" με ΟΚ τότε σφάλμα
            if (!network.readUTF().equals(OK)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }
            //Στέλνουμε το certificate μας
            try (FileInputStream fis = new FileInputStream("HW2_MultiKAP_Client/certificates/client2signed.cer")) {
                var cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(fis);
                network.writeObject(cert); //στέλνουμε το certificate μας
            }

            //αν δεν το έλαβε, τότε κλείνουμε το session διοτι μάλλον θα υπάρχει πρόβλημα
            if (!network.readUTF().equals(CERT_RECEIVED)) {
                LOGGER.log(Level.SEVERE, "Protocol error\nExiting session...");
                return;
            }

            //λαμβάνουμε certificate 
            var cerReceived = (X509Certificate) network.readObject();
            network.writeUTF(CERT_RECEIVED);

            //Validate το certificate
            if (!checkReceivedCertificate(cerReceived)) {
                throw new ConnectionNotSafeException("The certificate can't be verified!");
            }

            //λαμβάνουμε το συμμετρικό κλειδί + τα IV parameters
            SealedObject sealedAesKey = (SealedObject) network.readObject();
            SealedObject sealedIv = (SealedObject) network.readObject();
            //αρχικοποίηση του cipher για αποκρυπτογράφηση του συμμετρικού κλειδιού
            Cipher cipher = Cipher.getInstance("RSA", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            symmetricKey = (SecretKey) sealedAesKey.getObject(cipher);
            iv = new IvParameterSpec(Base64.getDecoder().decode((String) sealedIv.getObject(cipher)));
            initializeHMAC();
            //παιρνουμε το session token (αποκρυπτογράφηση με το AES key)
            Message msg = decryptMessage((SealedObject) network.readObject());
            //παραδειγμα συνομιλιας
            communicateSecurelyAsClient(msg.token());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
}
