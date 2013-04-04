/**
 * InteropCapability.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: InteropCapability.java,v 1.5 2005/03/22 04:36:34 lifang Exp $
 */

package simplesoap.client;

import simplesoap.contract.InteropTestRpc;
import simplesoap.service.InteropCapService;
import simplesoap.service.InteropService;
import xsul.xhandler.XHandler;
import xsul.xhandler.client.ClientCapabilityHandler;
import xsul.xhandler.client.ClientSignatureHandler;
import xsul.xwsif_runtime.WSIFRuntime;

public class InteropCapability {
    public static void main(String[] args) throws Exception {
        String wsdlLoc = "samples/simplesoap/contract/InteropTest.wsdl";
        XHandler capabilityHander = new ClientCapabilityHandler("cap-client",
                                                                args[0],
                                                                args[1]);
        XHandler signatureHandler = new ClientSignatureHandler("sig-handler");
        InteropTestRpc stub = (InteropTestRpc) WSIFRuntime.newClient(wsdlLoc, args[0])
            .addHandler(capabilityHander)
            .addHandler(signatureHandler)
            .generateDynamicStub(InteropTestRpc.class);
        String response = stub.echoString("Alice");
        System.out.println("echoString response="+response);
    }
}

