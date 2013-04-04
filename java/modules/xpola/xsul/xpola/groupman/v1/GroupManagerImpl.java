/**
 * GroupManagerImpl.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: GroupManagerImpl.java,v 1.3 2005/04/09 05:03:42 lifang Exp $
 */

package xsul.xpola.groupman.v1;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import xsul.MLogger;
import xsul.xpola.groupman.GroupManager;

public class GroupManagerImpl implements GroupManager {
    private final static MLogger logger = MLogger.getLogger();
    
    private Hashtable groups = new Hashtable(11);
    
    public void addGroup(String gname, String[] meta)
        throws Exception {
        logger.finest("group name: " + gname);
        groups.put(gname, new Vector());
    }
    
    public String[] listGroups(String[] metadata) throws Exception {
        Vector list = new Vector(11);
        for(Enumeration enu = groups.keys(); enu.hasMoreElements();) {
            list.add(enu.nextElement());
        }
        return (String[])list.toArray(new String[0]);
    }
    
    public String[] listUsersOfGroup(String group, boolean recurse)
        throws Exception {
        Vector ulist = (Vector)groups.get(group);
        if(ulist == null) {
            logger.finest("user list null");
            return null;
        }
        
        return (String[])ulist.toArray(new String[0]);
    }
    
    public void addUsersToGroup(String[] users, String group, String[] meta)
        throws Exception {
        Vector ulist = (Vector)groups.get(group);
        if(ulist == null) {
            logger.finest("user list null");
            ulist = new Vector(Arrays.asList(users));
        }
        else {
            ulist.addAll(Arrays.asList(users));
        }
    }
    
    public void removeUsersFromGroup(String[] users, String group)
        throws Exception {
        if(users == null) {
            logger.finest("user list null");
            return;
        }
        
        Vector ulist = (Vector)groups.get(group);
        if(ulist != null) {
            for(Enumeration enu = ulist.elements(); enu.hasMoreElements();) {
                String udn = (String)enu.nextElement();
                for(int i = 0; i < users.length;i++) {
                    if(users[i].trim().equals(udn.trim())) {
                        ulist.remove(udn);
                        break;
                    }
                }
            }
        }
    }
    
    public void deleteGroups(String[] groupnames) throws Exception {
        if(groupnames == null) {
            logger.finest("group null");
            return;
        }
        
        for(Enumeration enu = groups.keys();enu.hasMoreElements();) {
            String gname = (String)enu.nextElement();
            for(int i = 0; i < groupnames.length;i++) {
                if(gname.trim().equals(groupnames[i].trim())) {
                    groups.remove(gname);
                }
            }
        }
    }
    
}

