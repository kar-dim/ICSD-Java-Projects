package crypto;

import util.NetworkOperations;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CryptoDH extends CryptoBase {
    private static final Logger LOGGER = Logger.getLogger(CryptoDH.class.getName());
    protected PrivateKey privateDHKey;
    protected PublicKey publicDHKey, publicDHExtKey; //publicDHExtKey = αυτό που λαμβάνει (public)
    protected byte[] dhSecretKey;
    public CryptoDH(NetworkOperations network, String keyStoreName, char[] keyStorePass, String trustStoreName, char[] trustStorePass, String keyAlias, char[] keyPass) {
        super(network, keyStoreName, keyStorePass, trustStoreName, trustStorePass, keyAlias, keyPass);
    }

    protected void generateDHKeys() {
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(2048);
            var keyPair = keyPairGenerator.generateKeyPair();
            privateDHKey = keyPair.getPrivate();
            publicDHKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    protected void generateDHSecretKey() {
        try {
            var keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(privateDHKey);
            keyAgreement.doPhase(publicDHExtKey, true);
            //σωστό μέγεθος (32bytes = 256bit) για το secret key
            dhSecretKey = new byte[32];
            System.arraycopy(keyAgreement.generateSecret(), 0, dhSecretKey, 0, dhSecretKey.length);
        } catch (IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
}
