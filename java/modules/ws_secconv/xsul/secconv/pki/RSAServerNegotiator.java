/**
 * RSAServerNegotiator.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.secconv.pki;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import sun.misc.BASE64Encoder;
import xsul.MLogger;
import xsul.secconv.SCConstants;
import xsul.secconv.SCUtil;
import xsul.secconv.ServerNegotiator;
import xsul.secconv.token.RequestSecurityTokenResponseType;
import xsul.secconv.token.RequestSecurityTokenType;
import xsul.secconv.token.RequestedSecurityTokenType;
import xsul.secconv.token.SecurityContextTokenType;
import xsul.secconv.token.pki.ClientInitTokenType;
import xsul.secconv.token.pki.ServerResponseTokenType;

abstract public class RSAServerNegotiator implements ServerNegotiator {
    private static final MLogger logger = MLogger.getLogger();
    private static BASE64Encoder encoder = new BASE64Encoder();
    
    private Key sessionKey = null;
    protected PrivateKey prikey = null;
    protected PublicKey pubkey = null;
    protected PublicKey clPubkey;
    
    private byte secret[] = new byte[20];
    private SecureRandom secrand = null;
    
    protected String contextId;
    protected SecurityContextTokenType contextToken;
    
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    
    public RSAServerNegotiator() {
    }
    
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }
    
    public String getContextId() {
        return contextId;
    }
    
    public Key getSessionKey() throws RemoteException {
        SecretKeySpec skey = new SecretKeySpec(secret, "HmacMD5");
        return skey;
    }
    
    public Object processRequest(RequestSecurityTokenType request)
        throws RemoteException {
        ServerResponseTokenType srt = new ServerResponseTokenType();
        
        String claims = request.getClaims();
        
        if(!claims.equals("HELLO")) {
            throw new RemoteException("claim is not understandable: " + claims);
        }
        
        XmlElement cite = request.getClientInitToken();
        ClientInitTokenType cit =
            (ClientInitTokenType)XmlElementAdapter
            .castOrWrap(cite, ClientInitTokenType.class);
        byte[] clpubkey = cit.getPublicKey();
        
        try {
            getClientPublicKey(clpubkey);
        } catch (Exception e) {
            throw new RemoteException("cannot get client public key", e);
        }
        
        // return the public key
        if(pubkey == null) {
            try {
                init();
            } catch (Exception e) {
                throw new RemoteException("RSAServer initialization failed", e);
            }
        }
        
        if(pubkey == null)
            throw new RemoteException("public key null");
        // Set up random number generator.
        
        
        try {
            generateSecret();
        }
        catch (Exception e) {
            throw new RemoteException("", e);
        }
        
        if(secret == null)
            throw new RemoteException("secret key null");
        
        // encrypt secret with Ecp and Ess: Ecp(Ess(Skey))
        try {
            byte[] encsec = encryptSecret();
            loadCertificate(srt);
            srt.setSecret(encsec);
        }
        catch (Exception e) {
            throw new RemoteException("", e);
        }
        
        return createRSTR(srt, request);
    }
    
    protected void getClientPublicKey(byte[] clpubkey) throws Exception {
        logger.finest("client pub key: " + encoder.encode(clpubkey));
        try {
            X509EncodedKeySpec clPubKeySpec = new X509EncodedKeySpec(clpubkey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            clPubkey = keyFactory.generatePublic(clPubKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RemoteException("invliad key spec", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RemoteException("no such algo", e);
        }
    }
    
    protected void loadCertificate(ServerResponseTokenType srt)
        throws Exception {
        srt.setPublicKey(pubkey.getEncoded());
    }
    
    public Object processRequest(RequestSecurityTokenResponseType response)
        throws RemoteException {
        // TODO
        return null;
    }
    
    protected abstract void init() throws Exception;
    
    private RequestSecurityTokenResponseType createRSTR(ServerResponseTokenType srt, RequestSecurityTokenType request) throws RemoteException {
        RequestSecurityTokenResponseType rstr =
            new RequestSecurityTokenResponseType();
        try {
            rstr.setTokenType(new URI(SCConstants.AUTHA_TOKEN));
            rstr.setRequestType(new URI(SCConstants.REQUEST_TYPE_ISSUE));
        } catch (URISyntaxException e) {
        }
        rstr.setServerResponseToken(srt);
        String contextId = SCUtil.createContextId(request.hashCode());
        try {
            SCUtil.saveSessionKey(contextId, getSessionKey(), true);
        } catch (Exception e) {throw new RemoteException("failed to save session key", e);}
        
        RequestedSecurityTokenType rstt =
            SCUtil.createNewContextResource(contextId);
        rstr.setRequestedSecurityToken(rstt);
        return rstr;
    }
    
    private void generateSecret() throws Exception {
        secrand = SecureRandom.getInstance("SHA1PRNG","SUN");
        secrand.setSeed("i am a seed".getBytes());
        secrand.nextBytes(secret);
        logger.finest("secret: " + encoder.encode(secret));
    }
    
    private byte[] encryptSecret()
        throws NoSuchPaddingException, IllegalStateException, InvalidKeyException,
        NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
        NoSuchProviderException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher1 = Cipher.getInstance("RSA", "BC");
        Cipher cipher2 = Cipher.getInstance("RSA", "BC");
        cipher1.init(Cipher.ENCRYPT_MODE, prikey);
        cipher2.init(Cipher.ENCRYPT_MODE, clPubkey);
        
        byte[] enc = cipher1.doFinal(secret);
        
        // we have to divide it by N to avoid "input too long for some short
        // RSA keys such as those from proxy cert.
        int N = 4;
        int qlen = enc.length/N;
        logger.finest("elen: " + qlen
                          + " client pubkey len: "
                          + clPubkey.getEncoded().length*8
                          + " private key len: "
                          + prikey.getEncoded().length*8);
        
        logger.finest("done enc2");
        byte[][] encN2 = new byte[N][qlen];
        int count = 0;
        for(int i = 0; i < N;i++) {
            encN2[i] = cipher2.doFinal(enc, qlen*i, qlen);
            count += encN2[i].length;
        }
        byte[] enc2 = new byte[count];
        count = 0;
        for(int i = 0;i < N;i++) {
            System.arraycopy(encN2[i], 0, enc2, count, encN2[i].length);
            count += encN2[i].length;
        }
        
        return enc2;
    }
    
}

