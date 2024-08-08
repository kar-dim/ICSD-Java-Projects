package crypto;//Dimitrios Karatzas icsd13072

import exception.ConnectionNotSafeException;
import exception.UnknownProtocolCommandException;
import util.NetworkOperations;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SealedObject;
import javax.crypto.spec.DHParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class ClientCryptoStS extends CryptoBase {
    private static final Logger LOGGER = Logger.getLogger(ClientCryptoStS.class.getName());
    private BigInteger p, g; //παράμετροι p,q, τους παράγει ο client και τους στέλνει στον "server" πριν ξεκινήσει η διαδικασία
    private KeyPair keypairDh; //τα DH public/private key μας
    private KeyAgreement keyAgreement;

    public ClientCryptoStS(NetworkOperations network, String keyStoreName, char[] keyStorePass, String trustStoreName, char[] trustStorePass, String keyAlias, char[] keyPass) {
        super(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass);
        createIV();
        generateParameters();
    }

    //Πρωτόκολλο
    /*
            SERVER                    CLIENT
               <-------StartSession------
               ------------OK----------->
               <---------p,g,IV----------
               ----ParametersReceived--->
               <------Certificate--------
               ---CertificateReceived--->
               -------Certificate------->
               <--CertificateReceived----
               <---PublicDHKey_Client----
               ---PublicDHKeyReceived--->
               [Server generates SHARED KEY: (g^x)^y έστω Κ], x: client's private dh key, y: server's private dh key
               ----PublicDHKey_Server--->
               <---PublicDHKeyReceived---
               -----K(sign(g^y,g^x))---->
               [Client generates SHARED KEY: (g^y)^x έστω Κ
               <SignedCiphertextReceived-
               [Client VERIFY sig με shared]
               <----K(sign(g^x, g^y))----
               -SignedCiphertextReceived>
               [Server VERIFY sig με shared]
               -StartSymmetricEncryption->     -> τέλος αλγορίθμου, AES KEY: K
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
            //στη συνέχεια στέλνουμε τα p,g και IV στον "server"
            network.writeObject(p);
            network.writeObject(g);
            network.writeObject(Base64.getEncoder().encodeToString(iv.getIV()));
            //αν δεν μας απαντήσει ο "server" με ParametersReceived τότε σφάλμα
            if (!network.readUTF().equals(PARAMETERS_RECEIVED)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }

            //Στέλνουμε το certificate μας
            FileInputStream fis = new FileInputStream("HW2_MultiKAP_Client/certificates/client2signed.cer");
            var cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(fis);
            network.writeObject(cert);

            //αν δε το έλαβε, τότε κλείνουμε το session διοτι μάλλον θα υπάρχει πρόβλημα
            if (!network.readUTF().equals(CERTIFICATE_RECEIVED)) {
                LOGGER.log(Level.SEVERE, "Protocol error\nExiting session...");
                return;
            }

            //μας στέλνει το Certificate οπότε εμείς το ελέγχουμε, δηλαδή αν έχει υπογραφτεί με την CA
            //από το truststore μας
            var cerReceived = (X509Certificate) network.readObject();

            // στέλνουμε ACK ότι το πήραμε
            network.writeUTF(CERTIFICATE_RECEIVED);

            //Validate το certificate
            if (!checkReceivedCertificate(cerReceived)) {
                throw new ConnectionNotSafeException("The certificate can't be verified!");
            }

            //στέλνουμε το g^x μας. το g^x είναι το public dh key (client)
            network.writeObject(keypairDh.getPublic());
            //αν δεν μας απαντήσει ο "server" με PublicDHKeyReceived τότε σφάλμα
            if (!network.readUTF().equals(PUBLIC_DH_KEY_RECEIVED)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }

            /* 2: public DH key (g^y) */
            //λαμβάνουμε το public dh key (Δηλαδή το g^y)
            PublicKey receivedDhPubkey = (PublicKey) network.readObject();
            //στέλνουμε ack ότι το πήραμε
            network.writeUTF(PUBLIC_DH_KEY_RECEIVED);

            /* 3: encrypted μήνυμα */
            //τώρα παράγουμε το shared κλειδί.
            keyAgreement.init(keypairDh.getPrivate());
            keyAgreement.doPhase(receivedDhPubkey, true);
            symmetricKey = keyAgreement.generateSecret("AES");

            //αρχικοποίηση του cipher Με AES
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);

            //λαμβάνουμε το signed ciphertext
            SealedObject sobj = (SealedObject) network.readObject();
            //στέλνουμε ack ότι το λάβαμε
            network.writeUTF(SIGNED_CIPHERTEXT_RECEIVED);

            byte[] signedCiphertext = Base64.getDecoder().decode((String) sobj.getObject(cipher));
            //στη συνέχεια θα αποκρυπτογραφήσουμε με το συμμετρικό κλειδί την υπογραφή του server
            if (!verifySignature(cerReceived, signedCiphertext, receivedDhPubkey.getEncoded(), keypairDh.getPublic().getEncoded())) {
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }
            // στέλνουμε το signed ciphertext κρυπτογραφημένο μέσω του συμμετρικού κλειδιού
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, iv);
            network.writeObject(new SealedObject(Base64.getEncoder().encodeToString(signBytes(keypairDh.getPublic().getEncoded(), receivedDhPubkey.getEncoded())), cipher));
            //αν δε το έλαβε, σφάλμα
            if (!network.readUTF().equals(SIGNED_CIPHERTEXT_RECEIVED)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }

            cipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);
            //αν όλα πάνε καλά ο Server θα στείλει "StartSymmetricEncryption" encrypted με το AES κλειδί
            var sealedObject = (SealedObject) network.readObject();
            String startMessageReceived = (String) sealedObject.getObject(cipher);
            if (!startMessageReceived.equals(START_SYMMETRIC_ENCRYPTION)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }
            //initialize το hmac
            initializeHMAC();
            //διάβασμα του session token (αποκρυπτογράφηση με το AES πρώτα)
            sealedObject = (SealedObject) network.readObject();
            String token = (String) sealedObject.getObject(cipher);

            //παράδειγμα συνομιλίας
            communicateSecurelyAsClient(token);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    //παραγωγή p και g, γενικά για να είναι ασφαλείς οι p,g, πρέπει να είναι αρκετά μεγάλοι (όχι υπερβολικά
    //ώστε να μη καθυστερούν), μια ικανοποιητική τιμή είναι 2048bits για το p και 256bits για το g
    private void generateParameters() {
        try {
            SecureRandom random = new SecureRandom();
            p = BigInteger.probablePrime(2048, random);
            g = BigInteger.probablePrime(256, random);
            //παραγωγή των keyagree
            var keyPairGen = KeyPairGenerator.getInstance("DiffieHellman");
            keyAgreement = KeyAgreement.getInstance("DiffieHellman");
            var dhParameters = new DHParameterSpec(p, g);
            keyPairGen.initialize(dhParameters, random);
            keypairDh = keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
