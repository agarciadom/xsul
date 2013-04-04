/**
 * RequestManager.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: RequestManager.java,v 1.1 2005/03/07 00:12:27 lifang Exp $
 */

package xsul.xpola.capman;

public interface RequestManager {
    
    public void registerRequest(String request) throws Exception;

    public void responseToRequest(String response) throws Exception;
    
    public void removeRequestById(String id) throws Exception;
    
    public void removeRequestsByIssuer(String requester) throws Exception;
    
    public String getRequestById(String id) throws Exception;
    
    public String[] getRequestsByIssuer(String issuer) throws Exception;
    
    public String[] getRequestsByReceiver(String receiver) throws Exception;
    
}

