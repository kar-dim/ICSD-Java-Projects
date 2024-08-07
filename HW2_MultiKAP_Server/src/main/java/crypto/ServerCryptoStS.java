package crypto;//Nikolaos Katsiopis icsd13076
//Dimitrios Karatzas icsd13072

import exception.ConnectionNotSafeException;
import exception.UnknownProtocolCommandException;
import util.NetworkOperations;
import util.TokenGenerator;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SealedObject;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.*;

public class CryptoStS extends CryptoBase {
    private static final Logger LOGGER = Logger.getLogger(CryptoStS.class.getName());
    private BigInteger p, g; //παράμετροι p,g, τους παράγει ο client και τους στέλνει στον "server" πριν ξεκινήσει η διαδικασία
    private KeyPair keypair_dh; //τα DH keypair_dh
    private KeyAgreement keyagree;
    private SecureRandom random;
    public CryptoStS(NetworkOperations network, String keyStoreName, char[] keyStorePass, String trustStoreName, char[] trustStorePass) {
        super(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass);
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
               [Client generates SHARED KEY: (g^y)^x (επειδή g^x^y = g^y^x το shared key ειναι ιδιο)
               <SignedCiphertextReceived-
               [Client VERIFY sig με shared]
               <----K(sign(g^x, g^y))----
               -SignedCiphertextReceived>
               [Server VERIFY sig με shared]
               -StartSymmetricEncryption->     -> τέλος αλγορίθμου, AES KEY: K
               -Encrypted token(με AES)->
     */
    public void performKeyExchange() {
        try {
            //λαμβάνουμε πρώτα StartSession Και στέλνουμε ΟΚ
            if (!network.readUTF().equals(START_SESSION)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }
            //στέλνουμε "ΟΚ"
            network.writeUTF(OK);

            //λαμβάνουμε p,g και IV από client
            p = (BigInteger) network.readObject();
            g = (BigInteger) network.readObject();
            iv = new IvParameterSpec(Base64.getDecoder().decode((String) network.readObject()));
            network.writeUTF(PARAMETERS_RECEIVED);

            //με βάση τα p,g παράγουμε τα DH public/private key μας
            generateParameters();
            X509Certificate cer_received = (X509Certificate) network.readObject();
            network.writeUTF(CERTIFICATE_RECEIVED);

            //Validate
            if (!checkReceivedCertificate(cer_received)) {
                throw new ConnectionNotSafeException("The certificate can't be verified!");
            }

            //Στέλνουμε το certificate μας
            FileInputStream fis = new FileInputStream("HW2_MultiKAP_Server/certificates/client1signed.cer");
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(fis);
            network.writeObject(cert);

            //αν δε το έλαβε, τότε κλείνουμε το session διοτι μάλλον θα υπάρχει πρόβλημα
            if (!network.readUTF().equals(CERTIFICATE_RECEIVED)) {
                LOGGER.log(Level.SEVERE, "Protocol error\nExiting session...");
                return;
            }

            //λαμβάνουμε το public dh key (Δηλαδή το g^x)
            PublicKey received_dh_pubkey = (PublicKey) network.readObject();
            network.writeUTF(PUBLIC_DH_KEY_RECEIVED);

            //στέλνουμε το g^y. το g^y είναι το public dh key (server)
            network.writeObject(keypair_dh.getPublic());
            //αν δεν μας απαντήσει ο client με PublicDHKeyReceived τότε σφάλμα
            if (!network.readUTF().equals(PUBLIC_DH_KEY_RECEIVED)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }

            /* 3: encrypted μήνυμα */
            //τώρα παράγουμε το shared κλειδί. Εμείς δεν κάνουμε τη λειτουργία (g^x)^y (client) ή (g^y)^x (server)
            //αυτό γίνεται αυτόματα από το keyagree
            keyagree.init(keypair_dh.getPrivate());
            keyagree.doPhase(received_dh_pubkey, true);
            symmetricKey = keyagree.generateSecret("AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, this.symmetricKey, this.iv);
            Signature sig = Signature.getInstance("SHA256withRSA", "BC");
            sig.initSign(privateKey);
            sig.update(keypair_dh.getPublic().getEncoded());
            sig.update(received_dh_pubkey.getEncoded());
            network.writeObject(new SealedObject(Base64.getEncoder().encodeToString(sig.sign()), cipher));
            //αν δε το έλαβε, σφάλμα
            if (!network.readUTF().equals(SIGNED_CIPHERTEXT_RECEIVED)) {
                throw new UnknownProtocolCommandException("Unknown command\nExiting session...");
            }

            cipher.init(Cipher.DECRYPT_MODE, this.symmetricKey, this.iv);
            //τώρα λαμβάνουμε το signed ciphertext
            SealedObject sobj = (SealedObject) network.readObject();
            network.writeUTF(SIGNED_CIPHERTEXT_RECEIVED);
            byte[] signed_ciphertext = Base64.getDecoder().decode((String) sobj.getObject(cipher));
            //στη συνέχεια θα αποκρυπτογραφήσουμε με το συμμετρικό κλειδί την υπογραφή του server
            sig = Signature.getInstance("SHA256withRSA", "BC");
            sig.initVerify(cer_received);
            sig.update(received_dh_pubkey.getEncoded());
            sig.update(keypair_dh.getPublic().getEncoded());
            if (!sig.verify(signed_ciphertext)) {
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }
            //εφόσον κάναμε verify, μπορούμε να στείλουμε το πρώτο μήνυμα με AES
            //παραγωγή του random token
            String token = new TokenGenerator().generateToken();
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, iv);
            //στέλνουμε το μήνυμα + token
            network.writeObject(new SealedObject(START_SYMMETRIC_ENCRYPTION, cipher));
            network.writeObject(new SealedObject(token, cipher));
            //έχει τελειώσει το key agreement, παραγωγή της HMAC
            initializeHMAC();

            //παράδειγμα συνομιλίας
            communicateSecurely(token);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    //παραγωγή των κλειδιών DH μέσω των p, g που δεχτήκαμε από client 
    private void generateParameters() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DiffieHellman");
            keyagree = KeyAgreement.getInstance("DiffieHellman");
            DHParameterSpec dhPS = new DHParameterSpec(p, g);
            keyPairGen.initialize(dhPS, this.random);
            keypair_dh = keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
