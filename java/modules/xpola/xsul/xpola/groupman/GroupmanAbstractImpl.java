/**
 * GroupmanAbstractImpl.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: GroupmanAbstractImpl.java,v 1.4 2005/04/09 05:03:41 lifang Exp $
 */

package xsul.xpola.groupman;

import edu.indiana.extreme.xsul.xpola.groupman.xsd.AddGroupInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.AddGroupOutDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.AddUsersToGroupInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.AddUsersToGroupOutDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.DeleteGroupsInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.DeleteGroupsOutDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListGroupsInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListGroupsOutDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListUsersOfGroupInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListUsersOfGroupOutDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.RemoveUsersFromGroupInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.RemoveUsersFromGroupOutDocument;
import xsul.MLogger;

public abstract class GroupmanAbstractImpl
    implements GroupmanPortType, GroupManager {
    
    private final static MLogger logger = MLogger.getLogger();
    
//    abstract protected void addGroup(String gname);
//    abstract protected void deleteGroups(String[] gnames);
//    abstract protected void addUsersToGroup(String gname, String[] unames);
//    abstract protected Vector listUsersOfGroup(String gname);
//    abstract protected Vector listGroups();
//    abstract protected void removeUsersFromGroup(String gname, String[] unames);
    
    public AddGroupOutDocument addGroup(AddGroupInDocument input) {
        String gname = input.getAddGroupIn().getGname();
        String[] metadata = input.getAddGroupIn().getMetadata().getItemArray();
        logger.finest("group name: " + gname);
        try {
            addGroup(gname, metadata);
        } catch (Exception e) {
            logger.severe("failed to add group", e);
        }
        return AddGroupOutDocument.Factory.newInstance();
    }
    
    public DeleteGroupsOutDocument deleteGroups(DeleteGroupsInDocument input) {
        String[] gnames = input.getDeleteGroupsIn().getGnames().getItemArray();
        try {
            deleteGroups(gnames);
        } catch (Exception e) {
            logger.severe("failed to delete groups", e);
        }
        return DeleteGroupsOutDocument.Factory.newInstance();
    }
    
    public AddUsersToGroupOutDocument addUsersToGroup(
        AddUsersToGroupInDocument input) {
        String[] unames =
            input.getAddUsersToGroupIn().getUnames().getItemArray();
        String gname = input.getAddUsersToGroupIn().getGname();
        String[] metadata = input.getAddUsersToGroupIn().getMetadata().getItemArray();
        try {
            addUsersToGroup(unames, gname, metadata);
        } catch (Exception e) {
            logger.severe("failed to add users to group", e);
        }
        return AddUsersToGroupOutDocument.Factory.newInstance();
    }
    
    public ListUsersOfGroupOutDocument listUsersOfGroup(
        ListUsersOfGroupInDocument input) {
        String gname = input.getListUsersOfGroupIn().getGname();
        ListUsersOfGroupOutDocument lugDoc =
            ListUsersOfGroupOutDocument.Factory.newInstance();
        try {
            String[] ulist = listUsersOfGroup(gname, false);
            if(ulist != null) {
                lugDoc.addNewListUsersOfGroupOut().addNewGnames()
                    .setItemArray(ulist);
            }
            else {
                logger.finest("user list null");
            }
        } catch (Exception e) {
            logger.severe("failed to list users of group", e);
        }
        return lugDoc;
    }
    
    public ListGroupsOutDocument listGroups(ListGroupsInDocument input) {
        ListGroupsOutDocument lgoDoc =
            ListGroupsOutDocument.Factory.newInstance();
        String[] metadata = input.getListGroupsIn().getMetadata().getItemArray();
        try {
            lgoDoc.addNewListGroupsOut().addNewGnames()
                .setItemArray(listGroups(metadata));
        } catch (Exception e) {
            logger.severe("failed to list groups", e);
        }
        return lgoDoc;
    }
    
    public RemoveUsersFromGroupOutDocument removeUsersFromGroup(
        RemoveUsersFromGroupInDocument input) {
        String gname = input.getRemoveUsersFromGroupIn().getGname();
        String[] unames =
            input.getRemoveUsersFromGroupIn().getUnames().getItemArray();
        try {
            removeUsersFromGroup(unames, gname);
        } catch (Exception e) {
            logger.severe("failed to remove users from group", e);
        }
        return RemoveUsersFromGroupOutDocument.Factory.newInstance();
    }
    
}

