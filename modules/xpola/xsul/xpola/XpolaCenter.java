/**
 * XpolaServer.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: XpolaCenter.java,v 1.7 2005/06/16 16:41:55 aslom Exp $
 */

package xsul.xpola;

import xsul.XsulVersion;
import xsul.xpola.capman.PersistentCapman;
import xsul.xpola.db.conn.DBConnManager;
import xsul.xpola.groupman.PersistentGroupman;
import xsul.xservices_xbeans.XmlBeansBasedService;
import xsul.xservo.XService;
import xsul.xservo_soap_http.HttpBasedServices;

public class XpolaCenter {
    private static HttpBasedServices httpServices;
    
    public static void main(String[] args) {
        XsulVersion.exitIfRequiredVersionMissing(XsulVersion.SPEC_VERSION);
        XsulVersion.exitIfRequiredVersionMissing("2.0.4");
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        httpServices = new HttpBasedServices(tcpPort);
        System.out.println("Server started on "+httpServices.getServerPort());
        String cwsdlLoc = XpolaCenter.class
            .getResource("capman/capman.wsdl").toString();
        String gwsdlLoc = XpolaCenter.class
            .getResource("groupman/groupman.wsdl").toString();
        XService cmsvc =
            httpServices.addService(
            new XmlBeansBasedService("capman",
                                     cwsdlLoc,
                                     new PersistentCapman(DBConnManager.HSQL,
                                                          args[1], "sa", "")));
        XService gmsvc =
            httpServices.addService(
            new XmlBeansBasedService("groupman",
                                     gwsdlLoc,
                                     new PersistentGroupman(DBConnManager.HSQL,
                                                            args[1], "sa", "")));
        cmsvc.startService();
        gmsvc.startService();
    }
    
    public static void shutdownServer() {
        httpServices.getServer().shutdownServer();
    }
}

