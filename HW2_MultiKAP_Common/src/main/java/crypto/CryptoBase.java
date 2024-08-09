package crypto;

import domain.Message;
import exception.ConnectionNotSafeException;
import util.NetworkOperations;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
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
        try (InputStream is = new File(keyStoreFile).toURI().toURL().openStream()){
            var keystore = KeyStore.getInstance("JKS");
            keystore.load(is, password);
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
            StringBuilder sb = new StringBuilder();
            for (byte oneByte : hashed)
                sb.append(Integer.toString((oneByte & 0xff) + 0x100, 16).substring(1));
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    //Μέθοδος που checkάρει αν το certificate που στέλνεται έχει γίνει sign από την ca
    protected boolean checkReceivedCertificate(X509Certificate cer) {
        try {
            var trustStoreCertificate = (X509Certificate) trustStore.getCertificate("CAcer");
            cer.verify(trustStoreCertificate.getPublicKey());
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    protected String HMACSign(String data) {
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
    }

    protected SealedObject encryptMessage(String msg, String token) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, iv);
            return new SealedObject(new Message(msg, token, HMACSign( msg + token)), cipher);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    protected Message decryptMessage(SealedObject sealedObject) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);
            return (Message) sealedObject.getObject(cipher);
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
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        iv = new IvParameterSpec(ivBytes);
    }

    protected void communicateSecurely(String token) throws ConnectionNotSafeException, IOException, ClassNotFoundException {
        //παράδειγμα συνομιλίας
        Scanner scan = new Scanner(System.in);
        while (true) {
            //λαμβάνουμε μήνυμα
            Message msgReceived = decryptMessage((SealedObject) network.readObject());
            String msgReceivedHMAC = msgReceived.hmac();
            //έλεγχος του hmac του μηνύματος
            if (msgReceivedHMAC == null || !msgReceivedHMAC.equals(HMACSign(msgReceived.toString()))) {
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }

            //έλεγχος αν το token είναι σωστό
            String hash = SHA256Hash(msgReceived.toString());
            if (hash == null || !hash.equals(SHA256Hash(msgReceived.message() + token))) {
                //άλλαξε το token = replay attack!
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }

            LOGGER.log(Level.INFO, "Client: " + msgReceived.message());
            if (msgReceived.message().trim().equalsIgnoreCase(EXIT)) {
                LOGGER.log(Level.INFO, "The peer has left");
                return;
            }
            //απάντηση
            LOGGER.log(Level.INFO, "Type something: ");
            String outgoingMessage = scan.nextLine();
            LOGGER.log(Level.INFO, "Server: " + outgoingMessage);
            network.writeObject(encryptMessage(outgoingMessage, token));
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
            network.writeObject(encryptMessage(outgoingMessage, token));
            if (outgoingMessage.trim().equalsIgnoreCase(EXIT)) {
                return;
            }
            //λαμβάνουμε μήνυμα
            Message msgReceived = decryptMessage((SealedObject) network.readObject());
            //έλεγχος του hmac του μηνύματος
            if (!msgReceived.hmac().equals(HMACSign(msgReceived.toString()))) {
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }
            //έλεγχος αν το token είναι σωστό
            String hash = SHA256Hash(msgReceived.toString());
            if (!hash.equals(SHA256Hash(msgReceived.message() + token))) {
                //άλλαξε το token = replay attack!
                throw new ConnectionNotSafeException("Your connection is not secure!");
            }
            LOGGER.log(Level.INFO, "Server: " + msgReceived.message());
            if (msgReceived.message().trim().equalsIgnoreCase(EXIT)) {
                return;
            }
        }
    }

    protected byte[] signBytes(byte[]...bytesArray) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA", "BC");
        sig.initSign(privateKey);
        for (var byteArr : bytesArray)
            sig.update(byteArr);
        return sig.sign();
    }

    protected boolean verifySignature(Certificate certificate, byte[] signature, byte[]...bytesArray) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA", "BC");
        sig.initVerify(certificate);
        for (var byteArr : bytesArray)
            sig.update(byteArr);
        return sig.verify(signature);
    }
}
