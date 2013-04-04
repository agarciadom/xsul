/**
 * SecurityRequestor.java
 *
 * @author
 * $Id: SecurityRequestorService.java,v 1.1 2005/01/04 19:51:42 lifang Exp $
 */

package xsul.secconv;

import xsul.secconv.token.RequestSecurityTokenResponseType;
import xsul.secconv.token.RequestSecurityTokenType;

public interface SecurityRequestorService extends java.rmi.Remote {

    public void requestSecurityTokenResponse(RequestSecurityTokenResponseType response)
        throws java.rmi.RemoteException;
    
    public RequestSecurityTokenResponseType requestSecurityToken(RequestSecurityTokenType request)
        throws java.rmi.RemoteException;
    
}

