/**
 * KeyStoreClientNegotiator.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.secconv.pki;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Iterator;
import sun.misc.BASE64Encoder;
import xsul.MLogger;

public class KeyStoreClientNegotiator extends RSAClientNegotiator {
    private static final MLogger logger = MLogger.getLogger();
    private static BASE64Encoder encoder = new BASE64Encoder();
    
    private String password = "";
    private String alias = "";
    private String kspassword = "";
    private KeyStore keystore = null;
    
    public KeyStoreClientNegotiator(String alias,
                                    String password,
                                    String kspassword) {
        this.alias = alias;
        this.password = password;
        this.kspassword = kspassword;
    }
    
    public void setKeystore(KeyStore keystore) {
        this.keystore = keystore;
    }
    
    public KeyStore getKeystore() {
        return keystore;
    }
    
    public void setKspassword(String kspassword) {
        this.kspassword = kspassword;
    }
    
    public String getKspassword() {
        return kspassword;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getAlias() {
        return alias;
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
    
    protected byte[] decrypt(byte[] serverPubkey, byte[] encsecret)
        throws Exception {
        X509EncodedKeySpec servPubKeySpec = new X509EncodedKeySpec(serverPubkey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey servPubkey = keyFactory.generatePublic(servPubKeySpec);
        
        return decryptSecret(servPubkey, encsecret);
    }
    
    protected void pathValidation(byte[] pubkey) throws Exception {
        try {
            // Create the parameters for the validator
            PKIXParameters params = new PKIXParameters(keystore);
            X509Certificate[] cert = new X509Certificate[1];
            Iterator it = params.getTrustAnchors().iterator();
            for (; it.hasNext(); ) {
                TrustAnchor ta = (TrustAnchor)it.next();
                
                // Get certificate
                cert[0] = ta.getTrustedCert();
                System.out.println("subject name: "+cert[0].getSubjectDN().getName());
            }
            params.setRevocationEnabled(false);
            
            CertPath certPath = createCertPath(cert);
            
            // Create the validator and validate the path
            CertPathValidator certPathValidator
                = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
            CertPathValidatorResult result
                = certPathValidator.validate(certPath, params);
            
            // Get the CA used to validate this path
            PKIXCertPathValidatorResult pkixResult
                = (PKIXCertPathValidatorResult)result;
            TrustAnchor ta = pkixResult.getTrustAnchor();
            X509Certificate xcert = ta.getTrustedCert();
        } catch (KeyStoreException e) {
            logger.severe(e.getMessage());
            throw e;
        } catch (InvalidAlgorithmParameterException e) {
            logger.severe(e.getMessage());
            throw e;
        } catch (NoSuchAlgorithmException e) {
            logger.severe(e.getMessage());
            throw e;
        } catch (CertPathValidatorException e) {
            logger.severe(e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e;
        }
    }
    
    // The CA's certificate should be the last element in the array
    public CertPath createCertPath(Certificate[] certs) {
        try {
            CertificateFactory certFact
                = CertificateFactory.getInstance("X.509");
            CertPath path = certFact.generateCertPath(Arrays.asList(certs));
            return path;
        } catch (java.security.cert.CertificateEncodingException e) {
            logger.severe(e.getMessage());
        } catch (CertificateException e) {
            logger.severe(e.getMessage());
        }
        return null;
    }
}

