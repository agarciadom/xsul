/**
 * AuthaClientNegotiator.java
 *
 * @author Liang Fang
 * $Id: AuthaClientNegotiator.java,v 1.4 2005/01/04 19:51:43 lifang Exp $
 */

package xsul.secconv.autha;

import gov.anl.protocol.autha.AuthAProtocol;
import java.math.BigInteger;
import java.net.URI;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;
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
import xsul.secconv.token.autha.ClientInitTokenType;
import xsul.secconv.token.autha.ClientResponseTokenType;
import xsul.secconv.token.autha.ServerResponseTokenType;

public class AuthaClientNegotiator implements ClientNegotiator
{
    private static final MLogger logger = MLogger.getLogger();
    
    private static BASE64Encoder encoder = new BASE64Encoder();
    
    protected String contextId;
    
    private char[] password = {'a', 'b', '4', 's', '2', '3', '$', '0'};
    
    private String clientName = "Liang";
    private String serverName = "Ying";
    
    // 512 bits by default
    private int bitLength = 512;
    
    private BigInteger p = null;
    private BigInteger g = null;
    
    private byte[] clientPubkey;
    private byte[] serverPubkey;
    // AuthA
    private byte[] authA1;
    // AuthB
    private byte[] authB2;
    // AuthBcheck
    private byte[] authB1;
    // Diffie Hellman key
    private byte[] dhKey;
    // Session key
    private byte[] rawsessionKey;
    private java.security.Key sessionKey = null;
    private String sKeyalgorithm  = "HmacMD5";
    
    private AuthAProtocol protocol;
    
    private SecurityContextTokenType contextToken;
    
    public AuthaClientNegotiator() {
    }
    
    public AuthaClientNegotiator(char[] passwd) {
        this.password = passwd;
    }
    
    public void setSKeyalgorithm(String sKeyalgorithm) {
        this.sKeyalgorithm = sKeyalgorithm;
    }
    
    public String getSKeyalgorithm() {
        return sKeyalgorithm;
    }

    public void setBitLength(int blen) {
        bitLength = blen;
    }
    
    public int getBitLength() {
        return bitLength;
    }
    
    public SecurityContextTokenType getContextToken() {
        return contextToken;
    }
    
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }
    
    public String getContextId() {
        return contextId;
    }
    
    
    public void setPassword(char[] passwd) {
        this.password = passwd;
    }
    
    public byte[] getRawSessionKey() {
        return rawsessionKey;
    }
    
    public java.security.Key getSessionKey() {
        if(sessionKey == null) {
            if(rawsessionKey == null) {
                return null;
            }
            
            sessionKey = new SecretKeySpec(rawsessionKey, sKeyalgorithm);
        }
        
        return sessionKey;
    }
    
    public void negotiate(SecurityRequestorService sr)
        throws RemoteException {
        
        generateAuthaClientKeysStep1();
        
        try {
            RequestSecurityTokenType rst = new RequestSecurityTokenType();
            rst.setTokenType(new URI(SCConstants.AUTHA_TOKEN));
            rst.setRequestType(new URI(SCConstants.REQUEST_TYPE_ISSUE));
            ClientInitTokenType cit = new ClientInitTokenType();
            cit.setBitLength(bitLength);
            cit.setP(p);
            cit.setG(g);
            cit.setClientName(clientName);
            cit.setServerName(serverName);
            cit.setX(clientPubkey);
            rst.setClientInitToken(cit);
            logger.finest("request security token: " + rst.toString());
            
            // make the remote call
            RequestSecurityTokenResponseType rstr =
                sr.requestSecurityToken(rst);
            logger.finest("request security token response: " + rstr.toString());
            
            processResponse(rstr);
            
            generateAuthaClientKeysStep2();
            
            RequestSecurityTokenResponseType rstr2 =
                new RequestSecurityTokenResponseType();
            rstr2.setTokenType(new URI(SCConstants.AUTHA_TOKEN));
            rstr2.setRequestType(new URI(SCConstants.REQUEST_TYPE_ISSUE));
            RequestedSecurityTokenType rdsToken =
                new RequestedSecurityTokenType();
            rdsToken.setSecurityContextToken(contextToken);
            rstr2.setRequestedSecurityToken(rdsToken);
            ClientResponseTokenType crt = new ClientResponseTokenType();
            crt.setAuthA(authA1);
            rstr2.setClientResponseToken(crt);

            // make the remote call again
            sr.requestSecurityTokenResponse(rstr2);
            
            logger.finest("negotiation done");
        } catch (Exception e) {
            //e.printStackTrace();
            logger.finest(e.getMessage());
            throw new RemoteException(e.getMessage(),e);
        }
    }
        
    private void generateAuthaClientKeysStep2() throws Exception {
        logger.finest("server pub key: " + encoder.encode(serverPubkey));
        protocol.calculateKeys(serverPubkey, true);
        authB1 = protocol.getAuthB();
        if(Arrays.equals(authB1, authB2)) {
            logger.finest("AuthB's are equal");
        }
        else {
            logger.finest("AuthB's are not equal");
            logger.finest("AuthB client: " + encoder.encode(authB1));
            logger.finest("AuthB server: " + encoder.encode(authB2));
            throw new Exception("AuthB's are not equal");
        }
        
        dhKey = protocol.getDHKey();
        authA1 = protocol.getAuthA();
        rawsessionKey = protocol.getSessionKey();
        
        logger.finest("got session key: " + encoder.encode(rawsessionKey));
    }
    
    private void processResponse(RequestSecurityTokenResponseType rstr)
        throws Exception {
        XmlElement srte = rstr.getServerResponseToken();
        
        if(srte != null) {
            ServerResponseTokenType srt =
                (ServerResponseTokenType)XmlElementAdapter.castOrWrap(srte,
                                                                      ServerResponseTokenType.class);
            
            serverPubkey = srt.getY();
            authB2 = srt.getAuthB();
        }
        else {
            logger.finest("srte null");
        }
        
        XmlElement rste = rstr.getRequestedSecurityToken();
        
        if(rste != null) {
            RequestedSecurityTokenType rst =
                (RequestedSecurityTokenType)XmlElementAdapter.castOrWrap(rste,
                                                                         RequestedSecurityTokenType.class);
            
            XmlElement scte = rst.getSecurityContextToken();
            if(scte != null) {
                SecurityContextTokenType sct =
                    (SecurityContextTokenType)XmlElementAdapter.castOrWrap(scte,
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
    
    private void generateAuthaClientKeysStep1() {
        try {
            SecureRandom rnd       = new SecureRandom();
            p = BigInteger.probablePrime(bitLength, rnd);
            g = BigInteger.probablePrime(bitLength, rnd);
            logger.finest("p: *********************\n" + p);
            logger.finest("g: *********************\n" + g);
            
            protocol = new AuthAProtocol(clientName, password, p, g, false);
            protocol.setTheothername(serverName);
            protocol.generateKeyPair();
            
            logger.finest("client key pair generated. ");
            
            clientPubkey = protocol.getPubkeyBytes(true);
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
        
}

