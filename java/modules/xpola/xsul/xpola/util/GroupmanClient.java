/**
 * GroupmanClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: GroupmanClient.java,v 1.2 2005/04/13 07:09:37 lifang Exp $
 */

package xsul.xpola.util;

import edu.indiana.extreme.xsul.xpola.groupman.xsd.AddGroupInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListGroupsInDocument;
import edu.indiana.extreme.xsul.xpola.groupman.xsd.ListGroupsOutDocument;
import xsul.XsulVersion;
import xsul.xpola.groupman.GroupmanPortType;
import xsul.xpola.groupman.GroupmanService;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.XmlBeansWSIFRuntime;

public class GroupmanClient {
    public final static String REQUIRED_XSUL_VERSION = "2.0.3";
    
    private static void usage(String errMsg) {
        System.err.println("Usage: WSDL_URL topic inputFileURL outputDirURL");
    }
    
    public static void main(String[] args) throws Exception {
        XsulVersion.exitIfRequiredVersionMissing(REQUIRED_XSUL_VERSION);
        
        boolean selfTesting = System.getProperty("start_server") != null;
        try {
            if(selfTesting) { // just for testing
                System.out.println("Doing self testing");
                //String wsdlLoc = args[0];
                GroupmanService.main(new String[]{"0"});
                if(args.length != 3) {
                    throw new IllegalArgumentException("exactly one argument with topic expected in self test");
                }
                String newWsdlLoc = GroupmanService.getServiceWsdlLocation();
                String[] sarr =new String[]{newWsdlLoc, args[0], args[1], args[2]};
                args = sarr;
            }
        } finally {
            if(selfTesting) {
                try {GroupmanService.shutdownServer();} catch(Exception e) {}
            }
        }
        
        String wsdlLoc = args[0];
        WSIFClient wcl = XmlBeansWSIFRuntime.newClient(wsdlLoc);
        GroupmanPortType stub =
            (GroupmanPortType)wcl.generateDynamicStub(GroupmanPortType.class);
        AddGroupInDocument agDoc = AddGroupInDocument.Factory.newInstance();
        AddGroupInDocument.AddGroupIn ag = agDoc.addNewAddGroupIn();
        ag.setGname("first1");
        ag.addNewMetadata().addItem("abcd");
        stub.addGroup(agDoc);
        ag.setGname("second1");
        stub.addGroup(agDoc);
        
        ListGroupsInDocument lgDoc = ListGroupsInDocument.Factory.newInstance();
        lgDoc.addNewListGroupsIn().addNewMetadata().addItem("abcd"); //Welcome to weird world of XmlBeans!!! THIS IS REQUIRED!!!!
        ListGroupsOutDocument lgoDoc = stub.listGroups(lgDoc);
        String[] gnames =  lgoDoc.getListGroupsOut().getGnames().getItemArray();
        for (int i = 0; i < gnames.length; i++) {
            System.out.println(gnames[i]);
        }
    }
}

