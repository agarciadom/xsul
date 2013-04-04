/**
 * InteropFastSecure.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: InteropFastSecure.java,v 1.1 2005/06/22 13:12:41 lifang Exp $
 */

package simplesoap.client;

import simplesoap.contract.InteropTestRpc;
import xsul.xhandler.client.ClientFastDSigHandler;
import xsul.xwsif_runtime.WSIFRuntime;

public class InteropFastSecure {
    public static void main(String[] args) throws Exception {
        String wsdlLoc =
            args.length > 0 ? args[0] : "samples/simplesoap/contract/InteropTest.wsdl";
        System.out.println("Using WSDL "+wsdlLoc);
        InteropTestRpc stub = (InteropTestRpc) WSIFRuntime.newClient(wsdlLoc)
            .addHandler(new ClientFastDSigHandler("fast dsig handler"))
            .generateDynamicStub(InteropTestRpc.class);
                
        //InteropTestRpc stub = (InteropTestRpc) WSIFRuntime.stubFor(wsdlLoc, InteropTestRpc.class);
        String response = stub.echoString("Alice");
        System.out.println("echoString response="+response);
    }
}

