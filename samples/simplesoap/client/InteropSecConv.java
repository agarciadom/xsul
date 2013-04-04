/**
 * InteropSecConv.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: InteropSecConv.java,v 1.5 2005/03/01 04:36:49 lifang Exp $
 */

package simplesoap.client;

import simplesoap.contract.InteropTestRpc;
import simplesoap.service.InteropSecConvService;
import simplesoap.service.InteropService;
import xsul.xhandler.XHandler;
import xsul.xhandler.client.ClientSecConvHandler;
import xsul.xwsif_runtime.WSIFRuntime;

public class InteropSecConv {
    public static void main(String[] args) throws Exception {
        String wsdlLoc = args.length > 0 ? args[0] : "samples/simplesoap/contract/InteropTest.wsdl";
        boolean selfTesting = System.getProperty("start_server") != null;
        if(selfTesting) { // just for testing
            System.out.println("Doing self testing");
            InteropService.main(new String[]{"0", wsdlLoc});
            wsdlLoc = InteropSecConvService.getServiceWsdlLocation();
        }
        System.out.println("Using WSDL "+wsdlLoc);
        InteropTestRpc stub = (InteropTestRpc) WSIFRuntime.newClient(wsdlLoc)
            .addHandler(new ClientSecConvHandler("client signature",
                                                 args.length > 1 ? args[1] : "http://localhost:5656"))
            .generateDynamicStub(InteropTestRpc.class);
        String response = stub.echoString("Alice");
        System.out.println("echoString response="+response);
        if(selfTesting) {
            InteropSecConvService.shutdownServer();
        }
    }
}

