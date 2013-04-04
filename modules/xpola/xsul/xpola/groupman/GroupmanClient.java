/**
 * GroupmanClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: GroupmanClient.java,v 1.6 2005/04/27 23:53:13 aslom Exp $
 */

package xsul.xpola.groupman;

import edu.indiana.extreme.xsul.xpola.groupman.xsd.AddGroupInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.AddUsersToGroupInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.DeleteGroupsInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListGroupsInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListGroupsOutDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListUsersOfGroupInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListUsersOfGroupOutDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.RemoveUsersFromGroupInDocument;
import xsul.xwsif_runtime.XmlBeansWSIFRuntime;

public class GroupmanClient implements GroupManager {
    
    private GroupmanPortType groupman;
    
    public GroupmanClient(String location) {
        String wsdlLoc =
            GroupManager.class.getResource("groupman.wsdl").toString();
        groupman = (GroupmanPortType)XmlBeansWSIFRuntime.newClient(wsdlLoc, location)
            .generateDynamicStub(GroupmanPortType.class);
    }
    
    public String[] listGroups(String[] metadata) throws Exception {
        ListGroupsInDocument inDoc =
            ListGroupsInDocument.Factory.newInstance();
        inDoc.addNewListGroupsIn().addNewMetadata().setItemArray(metadata);
        ListGroupsOutDocument outDoc = groupman.listGroups(inDoc);
        return outDoc.getListGroupsOut().getGnames().getItemArray();
    }
    
    public void removeUsersFromGroup(String[] unames, String gname)
        throws Exception {
        RemoveUsersFromGroupInDocument inDoc =
            RemoveUsersFromGroupInDocument.Factory.newInstance();
        RemoveUsersFromGroupInDocument.RemoveUsersFromGroupIn riidoc =
            inDoc.addNewRemoveUsersFromGroupIn();
        riidoc.addNewUnames().setItemArray(unames);
        riidoc.setGname(gname);
        groupman.removeUsersFromGroup(inDoc);
    }
    
    public String[] listUsersOfGroup(String gname, boolean recursive)
        throws Exception {
        ListUsersOfGroupInDocument inDoc =
            ListUsersOfGroupInDocument.Factory.newInstance();
        ListUsersOfGroupInDocument.ListUsersOfGroupIn lugdoc =
            inDoc.addNewListUsersOfGroupIn();
        lugdoc.setGname(gname);
        lugdoc.setRecursive(recursive);
        ListUsersOfGroupOutDocument outDoc = groupman.listUsersOfGroup(inDoc);
        return outDoc.getListUsersOfGroupOut().getGnames().getItemArray();
    }
    
    public void addUsersToGroup(String[] unames, String gname, String[] metadata)
        throws Exception {
        AddUsersToGroupInDocument inDoc =
            AddUsersToGroupInDocument.Factory.newInstance();
        AddUsersToGroupInDocument.AddUsersToGroupIn atgdoc =
            inDoc.addNewAddUsersToGroupIn();
        atgdoc.addNewUnames().setItemArray(unames);
        atgdoc.addNewMetadata().setItemArray(metadata);
        atgdoc.setGname(gname);
        groupman.addUsersToGroup(inDoc);
    }
    
    public void addGroup(String gname, String[] metadata) throws Exception {
        AddGroupInDocument inDoc =
            AddGroupInDocument.Factory.newInstance();
        AddGroupInDocument.AddGroupIn agdoc = inDoc.addNewAddGroupIn();
        agdoc.setGname(gname);
        agdoc.addNewMetadata().setItemArray(metadata);
        groupman.addGroup(inDoc);
    }
    
    public void deleteGroups(String[] gnames) throws Exception {
        DeleteGroupsInDocument inDoc =
            DeleteGroupsInDocument.Factory.newInstance();
        inDoc.addNewDeleteGroupsIn().addNewGnames().setItemArray(gnames);
        groupman.deleteGroups(inDoc);
    }
}

