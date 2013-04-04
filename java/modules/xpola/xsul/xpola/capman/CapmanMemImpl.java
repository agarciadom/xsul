/**
 * CapmanMemImpl.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: CapmanMemImpl.java,v 1.4 2005/04/26 20:57:16 lifang Exp $
 */

package xsul.xpola.capman;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import org.opensaml.SAMLAssertion;
import xsul.MLogger;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityUtil;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityOutDocument;

public class CapmanMemImpl extends CapmanAbstractImpl {
        
    private static final MLogger logger = MLogger.getLogger();
    private Vector requests;
    private Vector caps;
    
    public String getCapability(String handle, String userdn) throws Exception {
        // TODO
        return null;
    }
    
    public String[] getAllCapabilityHandles() {
        Vector handles = new Vector(11);
        for(Enumeration myenum = caps.elements();myenum.hasMoreElements();) {
            Capability cap = (Capability)myenum.nextElement();
            String epr = cap.getResource();
            handles.add(epr);
        }
        
        return (String[])handles.toArray(new String[0]);
    }
    
    public String[] getCapabilityHandlesByUser(String userdn) {
        String user = CapabilityUtil.canonicalizeSubject(userdn);
        logger.finest("caller's name="+user);
        if(caps == null)
            return null;
        
        Vector handlev = new Vector(1);
        for(Enumeration myenum = caps.elements();myenum.hasMoreElements();) {
            Capability cap = (Capability)myenum.nextElement();
            Collection users = cap.getUsers();
            if(CapabilityUtil.exist(user, new Vector(users))) {
                handlev.add(cap);
            }
        }
        
        return (String[])handlev.toArray(new String[0]);
    }
    
    public String[] getCapabilityHandlesByOwner(String ownerdn) {
        String owner = CapabilityUtil.canonicalizeSubject(ownerdn);
        logger.finest("caller's name="+owner);
        
        Vector handlev = new Vector(1);
        for(Enumeration myenum = caps.elements();myenum.hasMoreElements();) {
            Capability cap = (Capability)myenum.nextElement();
            if(cap.getOwner().equals(owner))
                handlev.add(cap);
        }
        
        return (String[])handlev.toArray(new String[0]);
    }
    
    public void registerCapability(String acap) {
        Capability cap = new Capability(acap);
        registerCapability(cap);
    }
    
    private void registerCapability(Capability cap) {
        String name = cap.getOwner();
        logger.finest("caller's name="+name);
        
        Capability oldcap = CapabilityUtil.locate(cap.getResource(), caps);
        // if the capbility with the same EPR is already there
        if(oldcap != null) {
            // if they are now owned by the same owner, throw exception
            // an EPR cannot be owned by two different people
            if(!name.equals(oldcap.getOwner())) {
                logger.severe("you cannnot overwrite the capability for "
                                  + cap.getResource());
            }
            else {
                // otherwise, add the difference of user DNs from UserDNs(new)-UserDNs(old)
                Collection olduserdns = oldcap.getUsers();
                Collection newuserdns = cap.getUsers();
                if(newuserdns == null) {
                    return;
                }
                else if(olduserdns == null) {
                    caps.remove(oldcap);
                    caps.add(cap);
                }
                else {
                    // Set does not have duplicated items
                    HashSet userhs =
                        new HashSet(newuserdns.size()+olduserdns.size());
                    for(Iterator iter = newuserdns.iterator();iter.hasNext();) {
                        String user = (String)iter.next();
                        userhs.add(user);
                    }
                    for(Iterator iter = olduserdns.iterator();iter.hasNext();) {
                        String user = (String)iter.next();
                        userhs.add(user);
                    }
                    cap.setUsers(userhs);
                    
                    SAMLAssertion[] oldsas = oldcap.getAllAssertions();
                    
                    for(int i = 0;i < oldsas.length;i++) {
                        cap.addAssertion(oldsas[i]);
                    }
                    
                    caps.remove(oldcap);
                    caps.add(cap);
                    
                    // for debugging
                    if(logger.isFinestEnabled()) {
                        Collection userdns = cap.getUsers();
                        logger.finest("caps size: " + caps.size());
                        logger.finest("userdns size: " + userdns.size());
                        int count = 0;
                        for(Iterator iter = userdns.iterator(); iter.hasNext();) {
                            Object o = iter.next();
                            logger.finest("user "+count+": " + o);
                            count++;
                        }
                    }
                }
            }
        }
        else {
            caps.add(cap);
        }
    }
    
    public String getCapabilityByHandle(String handle) {
        Capability cap = CapabilityUtil.locate(handle, caps);
        return cap.toString();
    }
    
    public String[] getCapabilitiesByOwner(String owner) throws Exception {
        // TODO
        return null;
    }
    
    public void revokeCapabilitiesByOwner(String owner) throws Exception {
        // TODO
    }
    
    public void revokeCapabilityByHandle(String handle) {
        Capability cap = CapabilityUtil.locate(handle, caps);
        caps.remove(cap);
    }
    
    public void updateCapability(String acap) {
        Capability cap = new Capability(acap);
        revokeCapabilityByHandle(cap.getResource());
        registerCapability(cap);
    }
    
    public String getRequest(String id) {
        // TODO
        return null;
    }
    
    public String processRequest(String id) {
        // TODO
        return null;
    }
    
    public void removeRequest(String id) {
        // TODO
    }
    
    public void registerRequest(String request) {
        // TODO
    }

    public void removeRequestById(String id) throws Exception {
        // TODO
    }
    
    public void removeRequestsByIssuer(String issuer) throws Exception {
        // TODO
    }
    
    public String[] getRequestsByIssuer(String issuer) throws Exception {
        // TODO
        return null;
    }
    
    public void responseToRequest(String response) throws Exception {
        // TODO
    }
    
    public String[] getRequestsByReceiver(String receiver) throws Exception {
        // TODO
        return null;
    }
    
    public String getRequestById(String id) throws Exception {
        // TODO
        return null;
    }
        
}

