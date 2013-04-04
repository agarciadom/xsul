/**
 * GroupmanClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: GroupmanClient.java,v 1.4 2005/04/09 05:03:42 lifang Exp $
 */

package xsul.xpola.groupman.v1;

import java.util.Vector;
import xsul.XsulVersion;
import xsul.xpola.groupman.GroupManager;
import xsul.xpola.util.XpolaUtil;
import xsul.xwsif_runtime.WSIFRuntime;

public class GroupmanClient {
    public static void main(String[] args) throws Exception {
        XsulVersion.exitIfRequiredVersionMissing("1.1.26");
        String gswsdlLoc =
            GroupManager.class.getResource("groupman.wsdl").toString();
        System.out.println("groupman loc: " + gswsdlLoc);
        GroupManager gmstub = (GroupManager) WSIFRuntime
            .newClient(gswsdlLoc, args[0])
            .generateDynamicStub(GroupManager.class);
        gmstub.addGroup(args[2], new String[]{"testgroup"});
        Vector users = XpolaUtil.getUserlist(args[1]);
        String[] susers = (String[])users.toArray(new String[0]);
        for (int i = 0; i < susers.length; i++) {
            System.out.println("user " + i + ": " + susers[i]);
        }
        if(users != null) {
            gmstub.addUsersToGroup((String[])users.toArray(new String[0]),
                                   args[2], new String[]{"testgroup"});
        }
        String[] groups = gmstub.listGroups(new String[0]);
        for(int i = 0; i < groups.length;i++) {
            System.out.println("group " + i + ": " + groups[i]);
        }
        String[] unames = gmstub.listUsersOfGroup(args[2], false);
        for (int i = 0; i < unames.length; i++) {
            System.out.println("user " + i + ": " + unames[i]);
        }
    }
}

