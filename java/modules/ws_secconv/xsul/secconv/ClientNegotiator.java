/**
 * ClientNegotiator.java
 *
 * @author Liang Fang
 * $Id: ClientNegotiator.java,v 1.3 2005/01/04 19:51:42 lifang Exp $
 */

package xsul.secconv;
import java.rmi.RemoteException;
import java.security.Key;
import xsul.secconv.SecurityRequestorService;

public interface ClientNegotiator
{
    public void negotiate(SecurityRequestorService sr)  throws RemoteException;
    
    public String getContextId();
    
    public Key getSessionKey();
}


