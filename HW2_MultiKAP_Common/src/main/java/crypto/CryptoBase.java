package crypto;

import domain.Message;
import exception.ConnectionNotSafeException;
import util.NetworkOperations;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.MessageType.EXIT;

public abstract class CryptoBase {
    private static final Logger LOGGER = Logger.getLogger(CryptoBase.class.getName());
    protected SecretKey symmetricKey;
    protected final NetworkOperations network;
    protected Mac mac;
    protected IvParameterSpec iv;
    protected KeyStore keyStore;
    protected KeyStore trustStore;
    protected PrivateKey privateKey;

    public abstract void performKeyExchange();

    public CryptoBase(NetworkOperations networkOperations, String keyStoreName, char[] keyStorePassword, String trustStoreName, char[] trustStorePassword, String keyAlias, char[] keyPassword) {
        keyStore = loadKeyStore(keyStoreName, keyStorePassword); //φορτώνουμε το keystore
        trustStore = loadKeyStore(trustStoreName, trustStorePassword); //φορτώνουμε το truststore
        privateKey = getPrivateKey(keyAlias, keyPassword);
        network = networkOperations;
    }

    //παράγει τo RSA private key, αυτο τα παίρνουμε από αρχεία (keystoreCL1 για "server" και keystoreCL2 για client)
    private PrivateKey getPrivateKey(String keyAlias, char[] keyPass) {
        try {
            return (PrivateKey) keyStore.getKey(keyAlias, keyPass);
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private KeyStore loadKeyStore(String keyStoreFile, char[] password) {
        try {
            KeyStore keystore = KeyStore.getInstance("JKS"); //με keytool το φτιάξαμε σε JKS format
            InputStream is = new File(keyStoreFile).toURI().toURL().openStream();
            keystore.load(is, password);
            is.close();
            return keystore;
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
        return null;
    }

    //hash με sha-256, επιστρέφει τη hex μορφή των bytes
    protected String SHA256Hash(String message) {
        try {
            byte[] hashed =  MessageDigest.getInstance("SHA-256").digest(message.getBytes(StandardCharsets.UTF_8));
            //μετατροπή σε hex
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hashed.length; i++) {
                sb.append(Integer.toString((hashed[i] & 0xff) + 0x100, 16).substring(1));
            }
            //επιστροφή του hash σε string
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    //Μέθοδος που checkάρει αν το certificate που στέλνεται έχει γίνει sign από την ca
    protected boolean checkReceivedCertificate(X509Certificate cer) {
        try {
            //διάβασμα του certificate του CA από το truststore
            X509Certificate ca_cer = (X509Certificate) trustStore.getCertificate("CAcer");
            //ελέγχουμε αν το certificate που δίνεται έχει υπογραφτεί από τον CA
            cer.verify(ca_cer.getPublicKey());
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    protected String HMACSign(String data) {
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
    }

    //μέθοδος για encrypt ενός μηνύματος
    protected SealedObject encrypt(String msg, String token) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, iv);
            Message message = new Message(msg, token, HMACSign( msg + token));
            return new SealedObject(message, cipher);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    //μέθοδος για decrypt
    protected Message decrypt(SealedObject sobj) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);
            return (Message) sobj.getObject(cipher);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    //παραγωγή της HMAC
    protected void initializeHMAC() {
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(symmetricKey.getEncoded(), "HMACSHA256"));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    protected void createIV() {
        byte[] iv_bytes = new byte[16];
        new SecureRandom().nextBytes(iv_bytes);
        iv = new IvParameterSpec(iv_bytes);
    }

    protected void communicateSecurely(String token) throws ConnectionNotSafeException, IOException, ClassNotFoundException {
        //παράδειγμα συνομιλίας
        Scanner scan = new Scanner(System.in);
        while (true) {
            //λαμβάνουμε μήνυμα
            Message msgReceived = decrypt((SealedObject) network.readObject());
            String msgReceivedHMAC = msgReceived.getHMAC();
            //έλεγχος του hmac του μηνύματος
            if (msgReceivedHMAC == null || !msgReceivedHMAC.equals(HMACSign(msgReceived.toString()))) {
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }

            //έλεγχος αν το token είναι σωστό
            String hash = SHA256Hash(msgReceived.toString());
            if (hash == null || !hash.equals(SHA256Hash(msgReceived.getMessage() + token))) {
                //άλλαξε το token = replay attack!
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }

            LOGGER.log(Level.INFO, "Client: " + msgReceived.getMessage());
            if (msgReceived.getMessage().trim().equalsIgnoreCase(EXIT)) {
                LOGGER.log(Level.INFO, "The peer has left");
                return;
            }
            //απάντηση
            LOGGER.log(Level.INFO, "Type something: ");
            String outgoingMessage = scan.nextLine();
            LOGGER.log(Level.INFO, "Server: " + outgoingMessage);
            network.writeObject(encrypt(outgoingMessage, token));
            if (outgoingMessage.trim().equalsIgnoreCase(EXIT)) {
                return;
            }
        }
    }

    protected void communicateSecurelyAsClient(String token) throws IOException, ConnectionNotSafeException, ClassNotFoundException {
        //παράδειγμα συνομιλίας
        Scanner scan = new Scanner(System.in);
        while (true) {
            //δημιουργία μηνύματος
            LOGGER.log(Level.INFO,"Type something: ");
            String outgoingMessage = scan.nextLine();
            LOGGER.log(Level.INFO, "Client: " + outgoingMessage);

            //στέλνουμε το μήνυμα
            network.writeObject(encrypt(outgoingMessage, token));
            if (outgoingMessage.trim().equalsIgnoreCase(EXIT)) {
                return;
            }
            //λαμβάνουμε μήνυμα
            Message msgReceived = this.decrypt((SealedObject) network.readObject());
            String receivedHMAC = msgReceived.getHMAC();
            //έλεγχος του hmac του μηνύματος
            if (!receivedHMAC.equals(HMACSign(msgReceived.toString()))) {
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }
            //έλεγχος αν το token είναι σωστό
            String hash = SHA256Hash(msgReceived.toString());
            if (!hash.equals(SHA256Hash(msgReceived.getMessage() + token))) {
                //άλλαξε το token = replay attack!
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }
            LOGGER.log(Level.INFO, "Server: " + msgReceived.getMessage());
            if (msgReceived.getMessage().trim().equalsIgnoreCase(EXIT)) {
                return;
            }
        }
    }
}
