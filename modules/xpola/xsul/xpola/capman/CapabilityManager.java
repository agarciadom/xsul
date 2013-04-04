/**
 * CapmanService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: CapabilityManager.java,v 1.4 2005/04/26 20:57:16 lifang Exp $
 */

package xsul.xpola.capman;

public interface CapabilityManager {
    
    public void registerCapability(String acap) throws Exception;
    
    public void updateCapability(String acap) throws Exception;
    
    public String getCapability(String handle, String userdn) throws Exception;

    public String getCapabilityByHandle(String handle) throws Exception;
    
    public String[] getCapabilitiesByOwner(String owner) throws Exception;
    
    public void revokeCapabilityByHandle(String handle) throws Exception;

    public void revokeCapabilitiesByOwner(String owner) throws Exception;

    public String[] getCapabilityHandlesByOwner(String ownerdn) throws Exception;
    
    public String[] getCapabilityHandlesByUser(String userdn) throws Exception;
    
    public String[] getAllCapabilityHandles() throws Exception;
    
}

