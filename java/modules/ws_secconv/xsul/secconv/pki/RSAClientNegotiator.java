/**
 * RSAClientNegotiator.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.secconv.pki;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.globus.gsi.TrustedCertificates;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import sun.misc.BASE64Encoder;
import xsul.MLogger;
import xsul.secconv.ClientNegotiator;
import xsul.secconv.SCConstants;
import xsul.secconv.SecurityRequestorService;
import xsul.secconv.token.RequestSecurityTokenResponseType;
import xsul.secconv.token.RequestSecurityTokenType;
import xsul.secconv.token.RequestedSecurityTokenType;
import xsul.secconv.token.SecurityContextTokenType;
import xsul.secconv.token.pki.ClientInitTokenType;
import xsul.secconv.token.pki.ServerResponseTokenType;

abstract public class RSAClientNegotiator implements ClientNegotiator {
    private static final MLogger logger = MLogger.getLogger();
    private static BASE64Encoder encoder = new BASE64Encoder();
    
    protected PrivateKey prikey = null;
    protected PublicKey pubkey = null;
    
    protected Key sessionKey = null;
    protected String contextId;
    protected SecurityContextTokenType contextToken;
    
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    
    public RSAClientNegotiator() {
    }
    
    protected abstract void init() throws Exception;
    
    protected abstract byte[] decrypt(byte[] serverPubkey, byte[] encsecret)
        throws Exception;
    
    public Key getSessionKey() {
        return sessionKey;
    }
    
    public String getContextId() {
        return contextId;
    }
    
    public void negotiate(SecurityRequestorService sr)
        throws RemoteException {
        try {
            RequestSecurityTokenType rst = new RequestSecurityTokenType();
            rst.setTokenType(new URI(SCConstants.PKI_TOKEN));
            rst.setRequestType(new URI(SCConstants.REQUEST_TYPE_ISSUE));
            rst.setClaimsType("HELLO");
            ClientInitTokenType cit = new ClientInitTokenType();
            if(pubkey == null) {
                init();
            }
            loadCertificate(cit);
            rst.setClientInitToken(cit);
            
            // make the remote call
            RequestSecurityTokenResponseType rstr =
                sr.requestSecurityToken(rst);
            logger.finest("request security token response: " + rstr);
            
            processResponse(rstr);
        } catch (URISyntaxException e) {
            throw new RemoteException("URI syntax error", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("", e);
        }
    }
    
    private void processResponse(RequestSecurityTokenResponseType rstr)
        throws Exception {
        XmlElement srte = rstr.getServerResponseToken();
        if(srte != null) {
            ServerResponseTokenType srt =
                (ServerResponseTokenType)XmlElementAdapter
                .castOrWrap(srte,
                            ServerResponseTokenType.class);
            
            byte[] encsecret = srt.getSecret();
            byte[] serverPubkey = srt.getPublicKey();
            byte[] secret = decrypt(serverPubkey, encsecret);
            sessionKey = new SecretKeySpec(secret, "HmacMD5");
            logger.finest("secret: " + encoder.encode(secret));
        }
        
        XmlElement rste = rstr.getRequestedSecurityToken();
        
        if(rste != null) {
            RequestedSecurityTokenType rst =
                (RequestedSecurityTokenType)XmlElementAdapter
                .castOrWrap(rste,
                            RequestedSecurityTokenType.class);
            
            XmlElement scte = rst.getSecurityContextToken();
            if(scte != null) {
                SecurityContextTokenType sct =
                    (SecurityContextTokenType)XmlElementAdapter
                    .castOrWrap(scte,
                                SecurityContextTokenType.class);
                URI contextid = sct.getIdentifier();
                contextId = contextid.toString();
                contextToken = sct;
                logger.finest("got contextid from server: " + contextId);
            }
            else {
                logger.finest("scte null");
            }
        }
        else {
            logger.finest("rste null");
        }
    }
    
    protected byte[] decryptSecret(PublicKey servPubkey, byte[] encsecret)
        throws NoSuchAlgorithmException, NoSuchPaddingException,
        IllegalStateException, InvalidKeyException,
        IllegalBlockSizeException, BadPaddingException {
        
        Cipher cipher1 = Cipher.getInstance("RSA");
        Cipher cipher2 = Cipher.getInstance("RSA");
        cipher1.init(Cipher.DECRYPT_MODE, prikey);
        cipher2.init(Cipher.DECRYPT_MODE, servPubkey);
        
        logger.finest("encrypted sec size: " + encsecret.length);
        
        // we have to divide it by N to avoid "input too long for some short
        // RSA keys such as those from proxy cert.
        int N = 4;
        int qlen = encsecret.length / N;
        
        byte[][] qenc = new byte[N][qlen];
        byte[][] qdnc = new byte[N][qlen];
        int count = 0;
        for(int i = 0; i < N; i++) {
            System.arraycopy(encsecret, qlen*i, qenc[i], 0, qlen);
            qdnc[i] = cipher1.doFinal(qenc[i]);
            count += qdnc[i].length;
        }
        byte[] sdnc = new byte[count];
        count = 0;
        for(int i = 0; i < N; i++) {
            System.arraycopy(qdnc[i], 0, sdnc, count, qdnc[i].length);
            count += qdnc[i].length;
        }
        byte[] dnc = cipher2.doFinal(sdnc);
        return dnc;
    }
    
    protected void loadCertificate(ClientInitTokenType cit)
        throws Exception {
        cit.setPublicKey(pubkey.getEncoded());
    }
    
    
}

