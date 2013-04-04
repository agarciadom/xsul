/**
 * GroupmanImpl.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: GroupmanMemImpl.java,v 1.3 2005/04/09 05:03:41 lifang Exp $
 */

package xsul.xpola.groupman;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import xsul.MLogger;

public class GroupmanMemImpl extends GroupmanAbstractImpl {
    
    private final static MLogger logger = MLogger.getLogger();
    private Hashtable groups = new Hashtable(11);
    
    public void addGroup(String gname, String[] metadata) throws Exception {
        groups.put(gname, new Vector());
    }
    
    public void deleteGroups(String[] gnames) throws Exception {
        for (int i = 0; i < gnames.length; i++) {
            groups.remove(gnames[i]);
        }
    }
    
    public void addUsersToGroup(String[] unames, String gname, String[] metadata)
        throws Exception  {
        Vector ulist = (Vector)groups.get(gname);
        if(ulist == null) {
            logger.finest("user list null");
            ulist = new Vector(Arrays.asList(unames));
        }
        else {
            ulist.addAll(Arrays.asList(unames));
        }
    }
    
    public String[] listUsersOfGroup(String gname, boolean brec)
        throws Exception {
        Vector ulist = (Vector)groups.get(gname);
        return (String[])ulist.toArray(new String[0]);
    }
    
    public String[] listGroups(String[] metadata) throws Exception {
        Vector glist = new Vector(11);
        for(Enumeration enu = groups.keys(); enu.hasMoreElements();) {
            glist.add(enu.nextElement());
        }
        return (String[])glist.toArray(new String[0]);
    }

    public void removeUsersFromGroup(String[] unames, String gname)
        throws Exception {
        if(gname == null) {
            logger.finest("group name null");
        }
        else if(unames == null || unames.length == 0) {
            logger.finest("users name null");
        }
        else {
            Vector ulist = (Vector)groups.get(gname);
            if(ulist != null) {
                for(Enumeration enu = ulist.elements();
                    enu.hasMoreElements();) {
                    String udn = (String)enu.nextElement();
                    for(int i = 0; i < unames.length;i++) {
                        if(unames[i].trim().equals(udn.trim())) {
                            ulist.remove(udn);
                            break;
                        }
                    }
                }
            }
        }
    }
    
}


