/**
 * AuthaServerNegotiator.java
 *
 * @author Liang Fang
 * $Id: AuthaServerNegotiator.java,v 1.4 2005/03/01 19:05:25 lifang Exp $
 */

package xsul.secconv.autha;

import gov.anl.protocol.autha.AuthAProtocol;
import java.math.BigInteger;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Arrays;
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
import xsul.secconv.token.autha.ClientInitTokenType;
import xsul.secconv.token.autha.ClientResponseTokenType;
import xsul.secconv.token.autha.ServerResponseTokenType;
import java.net.URISyntaxException;

public class AuthaServerNegotiator implements ServerNegotiator {
    private static final MLogger logger = MLogger.getLogger();
    private static BASE64Encoder encoder = new BASE64Encoder();
    
    //    private static AuthaServerNegotiator INSTANCE = new AuthaServerNegotiator();
    // fixme: an interface to OTP? isn't setter/getter enough?
    private char[] password = {'a', 'b', '4', 's', '2', '3', '$', '0'};
    
    /**
     * The following private members are all supposed to be set internally:
     * either through the parameters passed over from the client, or generated
     * accordingly.
     */
    private String clientName = "Liang";
    private String serverName = "Ying";
    // 512 bits by default
    private int bitLength = 512;
    // paramters p and g from the client side
    private BigInteger p = null;
    private BigInteger g = null;
    // Client's public key
    private byte[] clientPubkey = null;
    // Server's public key
    private byte[] serverPubkey = null;
    // AuthB
    private byte[] authB2 = null;
    // AuthAcheck
    private byte[] authA2 = null;
    // Diffie Hellman Key
    private byte[] dhKey = null;
    // Session Key
    private byte[] rawsessionKey = null;
    private java.security.Key sessionKey = null;
    // AuthA protocol instance
    private AuthAProtocol protocol;
    
    public AuthaServerNegotiator() {
        logger.finest("autha server negotiator initiated.");
    }
    
    public AuthaServerNegotiator(char[] passwd) {
        if(passwd != null) {
            password = passwd;
        }
        logger.finest("autha server negotiator initiated.");
    }
    
    //    static public AuthaServerNegotiator getInstance() {
    //        return INSTANCE;
    //    }
//
    public void setPassword(char[] passwd) {
        password = passwd;
    }
    
    public char[] getPassword() {
        return password;
    }
    
    public int getBitLength() {
        return bitLength;
    }
    
    public BigInteger getP() {
        return p;
    }
    
    public BigInteger getG() {
        return g;
    }
    
    public byte[] getClientPublicKey() {
        return clientPubkey;
    }
    
    public byte[] getServerPublicKey() {
        return serverPubkey;
    }
    
    public byte[] getAuthB() {
        return authB2;
    }
    
    public byte[] getAuthA() {
        return authA2;
    }
    
    public byte[] getRawSessionKey() {
        return rawsessionKey;
    }
    
    public java.security.Key getSessionKey() throws RemoteException {
        if(sessionKey == null) {
            if(rawsessionKey == null) {
                throw new RemoteException("Session Key is not avaliable");
            }
            
            SecretKeySpec nsessionkey = new SecretKeySpec(rawsessionKey, "HmacMD5");
            
            sessionKey = nsessionkey;
        }
        
        return sessionKey;
    }
    
    public byte[] getDHKey() {
        return dhKey;
    }
    
    public synchronized Object processRequest(RequestSecurityTokenType request)
        throws RemoteException {
        
        ServerResponseTokenType srt = new ServerResponseTokenType();
        
        try {
            XmlElement cite = request.getClientInitToken();
            
            ClientInitTokenType cit =
                (ClientInitTokenType)XmlElementAdapter.castOrWrap(cite, ClientInitTokenType.class);
            
            p = cit.getP();
            g = cit.getG();
            clientName = cit.getClientName();
            serverName = cit.getServerName();
            bitLength = cit.getBitLength();
            clientPubkey = cit.getX();
            logger.finest("p: ++++++++++++\n" + p);
            
            generateAuthaKeys();
            
            srt.setY(serverPubkey);
            srt.setAuthB(authB2);
            
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
        
        return createRSTR(srt, request);
    }
    
    public Object processRequest(RequestSecurityTokenResponseType response)
        throws RemoteException {
        
        // AuthAcheck from the client
        byte[] aa1 = null;
        
        XmlElement cite = response.getClientResponseToken();
        
        ClientResponseTokenType crt =
            (ClientResponseTokenType)XmlElementAdapter.castOrWrap(cite, ClientResponseTokenType.class);
        
        aa1 = crt.getAuthA();
        
        if(Arrays.equals(aa1, authA2)) {
            logger.finest("AuthA's are equal");
        }
        else {
            logger.finest("AuthA's are not equal");
            logger.finest("AuthA client: " + encoder.encode(aa1));
            logger.finest("AuthA server: " + encoder.encode(authA2));
            throw new RemoteException("AuthA's are not equal");
        }
        
        return "match";
    }
    
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
    
    private void generateAuthaKeys() throws RemoteException {
        
        if(p == null) {
            throw new RemoteException("Parameter P is null.");
        }
        
        if(g == null) {
            throw new RemoteException("Parameter G is null.");
        }
        
        if(password == null) {
            throw new RemoteException("Password has not been set yet.");
        }
        
        if(serverName == null) {
            serverName = "";
        }
        
        if(clientName == null) {
            clientName = "";
        }
        
        protocol = new AuthAProtocol(serverName, password, p, g, true);
        protocol.setTheothername(clientName);
        
        try {
            protocol.generateKeyPair();
        } catch (Exception e) {
            throw new RemoteException("failed to generate key pair: "
                                          + e.getMessage(), e);
        }
        
        logger.finest("B initialization done ... ");
        
        try {
            logger.finest("client pub key: " + encoder.encode(clientPubkey));
            protocol.calculateKeys(clientPubkey, true);
        } catch (Exception e) {
            throw new RemoteException("failed to calculate keys: "
                                          + e.getMessage(), e);
        }
        
        /**
         * Get the server's public key in encryption. It is going to be sent
         * in RSTR.
         */
        try {
            serverPubkey = protocol.getPubkeyBytes(true);
        } catch (Exception e) {
            throw new RemoteException("failed to get pub key bytes: "
                                          + e.getMessage(), e);
        }
        
        authB2 = protocol.getAuthB();
        authA2 = protocol.getAuthAcheck();
        dhKey = protocol.getDHKey();
        rawsessionKey = protocol.getSessionKey();
        
        logger.finest("AuthA server: " + encoder.encode(authA2));
        logger.finest("got session key: " + encoder.encode(rawsessionKey));
    }
    
}

