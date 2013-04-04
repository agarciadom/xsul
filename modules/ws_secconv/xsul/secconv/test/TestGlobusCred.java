/**
 * TestGlobusCred.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.secconv.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Vector;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.asn1.DERObject;
import org.globus.common.CoGProperties;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.gsi.bc.BouncyCastleUtil;
import org.globus.util.Base64;
import sun.misc.BASE64Encoder;
import xsul.dsig.globus.security.authentication.wssec.ProxyPathValidator;
import xsul.dsig.globus.security.authentication.wssec.ProxyPathValidatorException;
import xsul.dsig.saml.authorization.CapabilityUtil;

public class TestGlobusCred {
    private static BASE64Encoder encoder = new BASE64Encoder();
    
    public static void keystore() {
        String uhome = System.getProperty("user.home");
        System.out.println("uhome: " + uhome);
        try {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream(uhome+"\\.keystore"),
                          "liang".toCharArray());
            System.out.println("key provider: " + keystore.getProvider());
            PrivateKey prikey = (PrivateKey) keystore.getKey("liang", "fangliang".toCharArray());
            PublicKey pubkey  = keystore.getCertificate("liang").getPublicKey();
            if(prikey == null) {
                System.out.println("null");
            }
            System.out.println("done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws Exception {
        keystore();
    }
    
    public static void main1(String[] args) throws Exception {
        
        GlobusCredential globusCred = GlobusCredential.getDefaultCredential();
        TrustedCertificates trustedCerts = CapabilityUtil.getTrustedCertificates(null);
        X509Certificate certs[] = globusCred.getCertificateChain();
        X509Certificate[] certs2 = new X509Certificate[1];
        certs2[0] = certs[0];
        PublicKey pkey = certs2[0].getPublicKey();
        System.out.println(pkey.getFormat());
        System.out.println(pkey.getClass());
        byte[] pubkey = certs2[0].getEncoded();
        //        byte[] pubkey = pkey.getEncoded();
        ProxyPathValidator validator = new ProxyPathValidator();
        
        DERObject obj = BouncyCastleUtil.toDERObject(pubkey);
        pubkey = BouncyCastleUtil.toByteArray(obj);
        ByteArrayInputStream in = new ByteArrayInputStream(pubkey);
        certs[0] = CertUtil.loadCertificate(in);
        try {
            validator.validate(certs, trustedCerts);
        }
        catch (ProxyPathValidatorException e) {
            e.printStackTrace();
        }
        
        //        ASN1Sequence seq = ASN1Sequence.getInstance(obj);
        //        int size = seq.size();
        //        certs = new X509Certificate[size];
        //        for (int i = 0; i < size; i++) {
        //            DEREncodable ded = seq.getObjectAt(i);
        //            obj = seq.getObjectAt(i).getDERObject();
        //            if(obj instanceof DERConstructedSequence) {
        //                System.out.println(((DERConstructedSequence)obj).toString());
        //            }
        //            else {
        //                System.out.println(((DERBitString)obj).getString());
        //            }
        //            pubkey = BouncyCastleUtil.toByteArray(obj);
        //            in = new ByteArrayInputStream(pubkey);
        //            certs[(true) ? (size - 1 - i) : i] =
        //                CertUtil.loadCertificate(in);
        //        }
//
        //        try {
        //            validator.validate(certs, trustedCerts);
        //        }
        //        catch (ProxyPathValidatorException e) {
        //            e.printStackTrace();
        //        }
        //        X509Certificate[] certs = trustedCerts.getCertificates();
        //        HashSet set = new HashSet(10);
        //        for(int i = 0; i < certs.length; i++) {
        //            System.out.println(certs[i].getSubjectDN().getName());
        //            TrustAnchor ta = new TrustAnchor(certs[i], null);
        //            set.add(ta);
        //        }
        //        List certList = Arrays.asList(certs);
        //        CertificateFactory cf =
        //            CertificateFactory.getInstance("X.509");
        //        CertPath certPath = cf.generateCertPath(certList);
        //        PKIXParameters params = new PKIXParameters(set);
        //        params.setRevocationEnabled(false);
        
        
        X509Certificate xcert = importCertificate("C:\\Documents and Settings\\lifang\\.globus\\fang.cer");
        System.out.println(xcert.getSubjectDN());
        System.out.println(xcert.getPublicKey());
        System.out.println("+++++++++++++");
        
        X509Certificate cert = CertUtil.loadCertificate("C:\\Documents and Settings\\lifang\\.globus\\usercert.cer");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        //        testglobus2();
        
        
        //        Cipher ecipher = Cipher.getInstance("RSA");
        //        Cipher dcipher = Cipher.getInstance("RSA");
        //        ecipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
        //        OpenSSLKey k = new BouncyCastleOpenSSLKey("C:\\Documents and Settings\\lifang\\.globus\\userkey2.pem");
        //        dcipher.init(Cipher.DECRYPT_MODE, k.getPrivateKey());
        //        String str = "all I want to do is to have some fun";
        //        byte[] utf8 = str.getBytes("UTF8");
        //        byte[] enc = ecipher.doFinal(utf8);
        //        System.out.println("encrypted: " + encoder.encode(enc));
        //        byte[] dnc = dcipher.doFinal(enc);
        //        System.out.println("decrypted: " + encoder.encode(dnc));
    }
    
    private static void testglobus2() throws GlobusCredentialException,
        NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
        UnsupportedEncodingException, IllegalBlockSizeException,
        BadPaddingException, IllegalStateException {
        GlobusCredential globusCred = GlobusCredential.getDefaultCredential();
        PrivateKey prikey = globusCred.getPrivateKey();
        X509Certificate certs[] = globusCred.getCertificateChain();
        PublicKey pubkey = certs[0].getPublicKey();
        
        Cipher ecipher = Cipher.getInstance("RSA");
        Cipher dcipher = Cipher.getInstance("RSA");
        ecipher.init(Cipher.ENCRYPT_MODE, pubkey);
        dcipher.init(Cipher.DECRYPT_MODE, prikey);
        
        byte[] utf8 = "all I want to do is to have some fun".getBytes("UTF8");
        byte[] enc = ecipher.doFinal(utf8);
        System.out.println("enc size: " + enc.length);
        System.out.println("pubkey len: " + pubkey.getEncoded().length*8);
        System.out.println("prikey len: " + prikey.getEncoded().length*8);
        byte[] enc2 = ecipher.doFinal(enc);
        byte[] dnc2 = dcipher.doFinal(enc2);
        byte[] dnc = dcipher.doFinal(dnc2);
        System.out.println("dnc: " + new String(dnc));
    }
    
    private static void parseGlobusCred() throws IOException, GeneralSecurityException {
        String proxyLocation = CoGProperties.getDefault().getProxyFile();
        System.out.println("proxyLoc: " + proxyLocation);
        InputStream input = new FileInputStream(proxyLocation);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        X509Certificate cert = null;
        Vector chain = new Vector(3);
        PrivateKey key = null;
        while( (line = reader.readLine()) != null  ) {
            if (line.indexOf("BEGIN CERTIFICATE") != -1) {
                byte [] data = getDecodedPEMObject(reader);
                cert = CertUtil.loadCertificate(new ByteArrayInputStream(data));
                System.out.println("cert: " + encoder.encode(cert.getEncoded()));
                chain.addElement(cert);
            } else if (line.indexOf("BEGIN RSA PRIVATE KEY") != -1) {
                byte [] data = getDecodedPEMObject(reader);
                System.out.println("private key data: " + encoder.encode(data));
                OpenSSLKey k = new BouncyCastleOpenSSLKey("RSA", data);
                key = k.getPrivateKey();
                System.out.println("private key: " + encoder.encode(key.getEncoded()));
            }
        }
    }
    
    private static void testglobusCred() throws IllegalStateException, IllegalBlockSizeException, InvalidKeyException, GlobusCredentialException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, BadPaddingException {
        GlobusCredential globusCred = GlobusCredential.getDefaultCredential();
        PrivateKey gprikey = globusCred.getPrivateKey();
        System.out.println("private key: " + encoder.encode(gprikey.getEncoded()));
        Certificate certs[] = globusCred.getCertificateChain();
        Certificate gcert = globusCred.getIdentityCertificate(); {
            PublicKey gpubkey = gcert.getPublicKey();
            System.out.println("publickey: " + encoder.encode(gpubkey.getEncoded()));
            Cipher ecipher = Cipher.getInstance("RSA");
            Cipher dcipher = Cipher.getInstance("RSA");
            ecipher.init(Cipher.ENCRYPT_MODE, gprikey);
            dcipher.init(Cipher.DECRYPT_MODE, gpubkey);
            
            String str = "all I want to do is to have some fun";
            byte[] utf8 = str.getBytes("UTF8");
            byte[] enc = ecipher.doFinal(utf8);
            System.out.println("encrypted: " + encoder.encode(enc));
            
            byte[] dnc = dcipher.doFinal(enc);
            System.out.println("decrypted: " + new String(dnc));
        }
        for(int i = 0; i < certs.length;i++) {
            PublicKey gpubkey = certs[i].getPublicKey();
            
            Cipher ecipher = Cipher.getInstance("RSA");
            Cipher dcipher = Cipher.getInstance("RSA");
            ecipher.init(Cipher.ENCRYPT_MODE, gprikey);
            dcipher.init(Cipher.DECRYPT_MODE, gpubkey);
            
            String str = "all I want to do is to have some fun";
            byte[] utf8 = str.getBytes("UTF8");
            byte[] enc = ecipher.doFinal(utf8);
            System.out.println("encrypted: " + encoder.encode(enc));
            
            byte[] dnc = dcipher.doFinal(enc);
            System.out.println("decrypted: " + new String(dnc));
        }
    }
    
    private static void testwithKeystore()
        throws CertificateException, KeyStoreException, IllegalStateException,
        InvalidKeyException, IOException, UnrecoverableKeyException,
        IllegalBlockSizeException, BadPaddingException {
        
        Cipher elcipher = null;
        Cipher dlcipher = null;
        Cipher efcipher = null;
        Cipher dfcipher = null;
        
        getCertFromFile();
        
        try {
            
            KeyStore ks = getKeyStore();
            
            PrivateKey liangpri = (PrivateKey) ks.getKey("jeremy", "jeremy".toCharArray());
            Certificate liangcert  = ks.getCertificate("jeremy");
            System.out.println("key class: " + liangpri.getClass());
            System.out.println("Certificate class: " + liangcert.getClass());
            PrivateKey fangpri = (PrivateKey) ks.getKey("ying", "yingliu".toCharArray());
            Certificate fangcert  = ks.getCertificate("ying");
            System.out.println("key class: " + fangpri.getClass());
            System.out.println("Certificate class: " + fangcert.getClass());
            
            elcipher = Cipher.getInstance("RSA");
            dlcipher = Cipher.getInstance("RSA");
            elcipher.init(Cipher.ENCRYPT_MODE, liangpri);
            dlcipher.init(Cipher.DECRYPT_MODE, liangcert.getPublicKey());
            efcipher = Cipher.getInstance("RSA");
            dfcipher = Cipher.getInstance("RSA");
            efcipher.init(Cipher.ENCRYPT_MODE, fangcert.getPublicKey());
            dfcipher.init(Cipher.DECRYPT_MODE, fangpri);
            System.out.println("The encryption algo is: "+ elcipher.getAlgorithm());
            System.out.println("The decryption algo is: "+ dlcipher.getAlgorithm());
            
        }catch (NoSuchPaddingException e) {
        }catch (java.security.NoSuchAlgorithmException e) {
        }
        
        String str = "all I want to do is to have some fun";
        byte[] utf8 = str.getBytes("UTF8");
        byte[] enc1 = elcipher.doFinal(utf8);
        byte[] enc21 = efcipher.doFinal(enc1, 0, 64);
        byte[] enc22 = efcipher.doFinal(enc1, 64, 64);
        
        System.out.println("encrypted: " + encoder.encode(enc21));
        System.out.println(encoder.encode(enc22));
        
        byte[] dnc11 = dfcipher.doFinal(enc21);
        byte[] dnc12 = dfcipher.doFinal(enc22);
        byte[] dnc1 = new byte[128];
        System.arraycopy(dnc11, 0, dnc1, 0, 64);
        System.arraycopy(dnc12, 0, dnc1, 64, 64);
        byte[] dnc = dlcipher.doFinal(dnc1);
        System.out.println("decrypted: " + new String(dnc));
    }
    
    private static KeyStore getKeyStore()
        throws CertificateException, IOException,
        KeyStoreException, NoSuchAlgorithmException {
        String uhome = System.getProperty("user.home");
        String jhome = System.getProperty("java.home");
        System.out.println("uhome: " + uhome);
        System.out.println("jhome: " + jhome);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(uhome+"\\.keystore"), "mypassword".toCharArray());
        System.out.println("key provider: " + ks.getProvider());
        return ks;
    }
    
    private static void bc() throws Exception {
        Cipher          cl_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC"); // Client Cipher.
        Cipher          se_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC"); // Server Cipher.
        Cipher          rsa_eng = Cipher.getInstance("RSA/None/OAEPPadding","BC"); // RSA Cipher.
        Mac             cl_mac = Mac.getInstance("HMACSHA1","BC"); // Client side MAC
        Mac             se_mac = Mac.getInstance("HMACSHA1","BC"); // Server side MAC
        PublicKey       cl_pubkey = null; // The clients public key for encryption and decryption.
        X509Certificate cl_cert = null; // The clients authentication and signing certificate.
        
    }
    
    private static void getCertFromFile() throws CertificateException, IOException {
        InputStream inStream =
            new FileInputStream("fang64.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
        inStream.close();
        RSAPublicKey pubk = (RSAPublicKey)cert.getPublicKey();
    }
    
    private static final byte[] getDecodedPEMObject(BufferedReader reader)
        throws IOException {
        String line;
        StringBuffer buf = new StringBuffer();
        while( (line = reader.readLine()) != null  ) {
            if (line.indexOf("--END") != -1) { // found end
                return Base64.decode(buf.toString().getBytes());
            } else {
                buf.append(line);
            }
        }
        throw new EOFException("PEM footer missing");
    }
    
    public static X509Certificate importCertificate(String filename) {
        X509Certificate cert = null;
        try {
            CertificateFactory cf =
                CertificateFactory.getInstance("X509");
            
            /*
             * Get the File I/O of the Certificate
             */
            FileInputStream fr = new FileInputStream(filename);
            
            /*
             *  Construct the certificate based on the import
             */
            cert = (X509Certificate) cf.generateCertificate(fr);
            
            /*
             *  catches.
             */
        }
        catch (CertificateException e) {
            e.printStackTrace();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return cert;
    }
    
}


