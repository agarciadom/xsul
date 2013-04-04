/**
 * KeyStoreServerNegotiator.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.secconv.pki;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import sun.misc.BASE64Encoder;
import xsul.MLogger;

public class KeyStoreServerNegotiator extends RSAServerNegotiator
{
    private static final MLogger logger = MLogger.getLogger();

    private KeyStore keystore = null;
    private String alias = "";
    private String password = "";
    private String kspassword = "";
    
    public KeyStoreServerNegotiator(String alias,
                                    String password,
                                    String kspasswd) {
        this.alias = alias;
        this.password = password;
        this.kspassword = kspasswd;
    }

    protected void init() throws Exception {
        String uhome = System.getProperty("user.home");
        logger.finest("uhome: " + uhome);
        try {
            keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream(uhome+"\\.keystore"),
                          kspassword.toCharArray());
            logger.finest("key provider: " + keystore.getProvider());
            prikey = (PrivateKey) keystore.getKey(alias, password.toCharArray());
            pubkey  = keystore.getCertificate(alias).getPublicKey();
        } catch (CertificateException e) {
            logger.severe("cert problem", e);
            throw e;
        } catch (IOException e) {
            logger.severe("IO problem", e);
            throw e;
        } catch (KeyStoreException e) {
            logger.severe("keystore problem", e);
            throw e;
        } catch (NoSuchAlgorithmException e) {
            logger.severe("no such algorithm", e);
            throw e;
        } catch (UnrecoverableKeyException e) {
            logger.severe("Unrecoverable key problem", e);
            throw e;
        }
    }
        
}

