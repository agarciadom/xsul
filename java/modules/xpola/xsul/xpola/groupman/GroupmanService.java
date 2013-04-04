/**
 * GroupmanService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: GroupmanService.java,v 1.3 2005/04/09 05:03:41 lifang Exp $
 */

package xsul.xpola.groupman;

import xsul.XsulVersion;
import xsul.xpola.db.conn.DBConnManager;
import xsul.xservices_xbeans.XmlBeansBasedService;
import xsul.xservo.XService;
import xsul.xservo_soap_http.HttpBasedServices;

public class GroupmanService {
    private static HttpBasedServices httpServices;
    private static final String SERVICE_NAME = "groupman";
    
    private GroupmanService() {
    }
    
    public static void main(String[] args) throws Exception {
        XsulVersion.exitIfRequiredVersionMissing(XsulVersion.SPEC_VERSION); //sanity check
        XsulVersion.exitIfRequiredVersionMissing("2.0.3");
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        httpServices = new HttpBasedServices(tcpPort);
        System.out.println("Server started on "+httpServices.getServerPort());
        String wsdlLoc =
            GroupManager.class.getResource("groupman.wsdl").toString();
        System.out.println("Using WSDL for service description from "+wsdlLoc);
        XService xsvc = httpServices.addService(
            new XmlBeansBasedService(SERVICE_NAME,
                                     wsdlLoc,
                                     new PersistentGroupman(DBConnManager.HSQL,
                                                            args[1], "sa", "")));
        xsvc.startService();
        System.out.println("Service started");
        System.out.println("Service WSDL available at "+getServiceWsdlLocation());
    }

    public static String getServiceWsdlLocation() {
        return httpServices.getServer().getLocation() + "/"+SERVICE_NAME+"?wsdl";
    }
    public static void shutdownServer() {
        httpServices.getServer().shutdownServer();
    }
}

