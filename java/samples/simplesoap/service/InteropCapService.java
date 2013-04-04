/**
 * InteropCapService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: InteropCapService.java,v 1.5 2005/05/04 06:29:58 lifang Exp $
 */

package simplesoap.service;

import xsul.xhandler.server.ServerCapabilityHandler;
import xsul.xhandler.server.ServerSignatureHandler;
import xsul.xservo_soap_http.HttpBasedServices;
import xsul.xservo_soap.XSoapRpcBasedService;

public class InteropCapService {
    private static HttpBasedServices httpServer;
    
    public static void main(String[] args) throws Exception {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        httpServer = new HttpBasedServices(tcpPort);
        System.out.println("Server started on "+httpServer.getServerPort());
        String wsdlLoc = args.length > 1 ? args[1] : "samples/simplesoap/contract/InteropTest.wsdl";
        System.out.println("Loading WSDL from "+wsdlLoc);
        httpServer.addService(new XSoapRpcBasedService("interop", wsdlLoc, new InteropTestRpcImpl()))
            .addHandler(new ServerSignatureHandler("sig-server"))
            .addHandler(new ServerCapabilityHandler("cap-server", "http://localhost:"+tcpPort))
            .startService();
    }
    
    public static String getServiceWsdlLocation() {
        return httpServer.getServer().getLocation() + "/interop?wsdl";
    }
    public static void shutdownServer() {
        httpServer.getServer().shutdownServer();
    }
}


