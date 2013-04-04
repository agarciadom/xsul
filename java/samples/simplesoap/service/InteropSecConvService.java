/**
 * InteropSecConvService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: InteropSecConvService.java,v 1.4 2005/01/31 23:01:51 aslom Exp $
 */

package simplesoap.service;

import xsul.xhandler.server.ServerSecConvHandler;
import xsul.xservo_soap_http.HttpBasedServices;
import xsul.xservo_soap.XSoapRpcBasedService;

public class InteropSecConvService {
    private static final String SERVICE_NAME = "interop";
    private static HttpBasedServices httpServer;
    
    /**
     * Simple command line
     */
    public static void main(String[] args) throws Exception {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        httpServer = new HttpBasedServices(tcpPort);
        //httpServer.addGlobalHandler(new ServerSecConvHandler("secconv-checker"));
        System.out.println("Server started on "+httpServer.getServerPort());
        String wsdlLoc = args.length > 1 ? args[1] : "samples/simplesoap/contract/InteropTest.wsdl";
        System.out.println("Loading WSDL from "+wsdlLoc);
        httpServer.addService(new XSoapRpcBasedService( SERVICE_NAME, wsdlLoc, new InteropTestRpcImpl()))
            .addHandler(new ServerSecConvHandler("secconv-checker"))
            .startService();
    }
    
    public static String getServiceWsdlLocation() {
        return httpServer.getServer().getLocation() + "/"+SERVICE_NAME+"?wsdl";
    }
    public static void shutdownServer() {
        httpServer.getServer().shutdownServer();
    }
}


