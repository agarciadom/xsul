/**
 * ServerNegotiator.java
 *
 * @author Liang Fang
 * $Id: ServerNegotiator.java,v 1.1 2004/09/23 06:29:44 lifang Exp $
 */

package xsul.secconv;

import java.rmi.RemoteException;
import xsul.secconv.token.RequestSecurityTokenResponseType;
import xsul.secconv.token.RequestSecurityTokenType;

public interface ServerNegotiator
{
	public Object processRequest(RequestSecurityTokenType request)
		throws RemoteException;
	
	public Object processRequest(RequestSecurityTokenResponseType response)
		throws RemoteException;
	
	public java.security.Key getSessionKey() throws RemoteException;
}


