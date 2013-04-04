/**
 * SecurityRequestorService.java
 *
 * @author Liang Fang
 * $Id: SecurityRequestorServiceImpl.java,v 1.2 2005/03/07 00:55:43 lifang Exp $
 */

package xsul.secconv.service.impl;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.Key;
import sun.misc.BASE64Encoder;
import xsul.MLogger;
import xsul.secconv.SCConstants;
import xsul.secconv.SecurityRequestorService;
import xsul.secconv.SecurityRequestorServiceConstants;
import xsul.secconv.ServerNegotiator;
import xsul.secconv.autha.AuthaServerNegotiator;
import xsul.secconv.pki.GlobusCredServerNegotiator;
import xsul.secconv.pki.KeyStoreServerNegotiator;
import xsul.secconv.token.RequestSecurityTokenResponseType;
import xsul.secconv.token.RequestSecurityTokenType;

public class SecurityRequestorServiceImpl
    implements SecurityRequestorService, SecurityRequestorServiceConstants {
    
    private static final MLogger logger = MLogger.getLogger();
    private static BASE64Encoder encoder = new BASE64Encoder();
    
    private String serviceName = "SecurityRequestorService";
//    private String contextId = null;
    
    // fixme: sntor is supposed to be put into the resource context ...
    // here the SRS is only able to do one WS-SecConv interaction at a time
    //    private ServerNegotiator sntor = null;
    
    public SecurityRequestorServiceImpl() {
    }
    
    public SecurityRequestorServiceImpl(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public RequestSecurityTokenResponseType
        requestSecurityToken(RequestSecurityTokenType request)
        throws RemoteException {
        
        RequestSecurityTokenResponseType rstr = null;
        //          new RequestSecurityTokenResponseType();
        
        try {
            
            if(request.getTokenType().equals(SCConstants.AUTHA_TOKEN)) {
                return negotiatebyAuthA(request);
            }
            else if(request.getTokenType().equals(SCConstants.PKI_TOKEN)) {
                return negotiatebyRSA(request);
            }
            
        }
        catch (URISyntaxException e) {
            throw new RemoteException("Incorrect URI format", e);
        }
        catch(Exception e) {
            throw new RemoteException("Context problem ", e);
        }
        
        return rstr;
    }
    
    private RequestSecurityTokenResponseType negotiatebyRSA(RequestSecurityTokenType request)
        throws Exception {
        
        String alias = System.getProperty("alias");
        String kspasswd = System.getProperty("kspasswd");
        String password = System.getProperty("password");
        String proxy = System.getProperty("proxy");
        ServerNegotiator sntor;
        if(alias == null || kspasswd == null || password == null) {
            if(proxy == null)
                sntor = new GlobusCredServerNegotiator();
            else
                sntor = new GlobusCredServerNegotiator(proxy);
        }
        else {
            sntor = new KeyStoreServerNegotiator(alias, password, kspasswd);
        }
        
        return(RequestSecurityTokenResponseType)sntor.processRequest(request);
    }
    
    private RequestSecurityTokenResponseType negotiatebyAuthA(RequestSecurityTokenType request)
        throws Exception {
        char[] pw = System.getProperty("password").toCharArray();
        ServerNegotiator sntor = new AuthaServerNegotiator(pw);
        return(RequestSecurityTokenResponseType)sntor.processRequest(request);
    }
    
    public void requestSecurityTokenResponse(RequestSecurityTokenResponseType response)
        throws RemoteException {
        // TODO
    }
    
    private void saveSessionKeyintoContext(Key key) throws Exception {
        
        // fixme: use some other persistent mechanism later
//        SCUtil.saveSessionKey(contextId, key, true);
    }
    
}

