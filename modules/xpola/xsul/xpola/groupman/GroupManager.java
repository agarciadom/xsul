/**
 * GroupManager.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: GroupManager.java,v 1.6 2005/04/27 21:31:54 lifang Exp $
 */

package xsul.xpola.groupman;

public interface GroupManager {
    
    public void addGroup(String gname, String[] metadata)
        throws Exception;

    /**
     * Method addUsersToGroup adds userdns to a group. Users must be existing
     * users. If the group does not exist, it will be created.
     *
     * @param    unames              a  String[] for a list of user DNs
     * @param    gname               a  String for the group name
     * @param    metadata            a  String[] for the metadata of the group.
     * For the time being, metadata[0] is the description of the group. Futher
     * information can be added as metadata[1], [2] ... if needed.
     *
     * @exception   Exception
     *
     */
    public void addUsersToGroup(String[] unames, String gname, String[] metadata)
        throws Exception;
    
    // Delete groups. Will not delete the userdns of the groups
    public void deleteGroups(String[] gnames) throws Exception;
        
    /**
     * Method listGroups lists all the groups that match the corresponding
     * metadata. Metadata could be null.
     *
     * @param    metadata            a  String[]
     *
     * @return   a String[] for the list of group names
     *
     * @exception   Exception
     *
     */
    public String[] listGroups(String[] metadata) throws Exception;
    
     // List all the userdns of a group. Does not list subgroups of the group
    public String[] listUsersOfGroup(String gname, boolean recursive)
        throws Exception;
    
    // Remove the userdns from the specified group.
    // This will not delete the userdns or the group
    public void removeUsersFromGroup(String[] unames, String gname)
        throws Exception;
}

