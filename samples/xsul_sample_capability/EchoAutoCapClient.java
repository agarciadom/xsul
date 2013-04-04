/**
 * EchoAutoCapClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: EchoAutoCapClient.java,v 1.1 2005/01/10 22:05:57 lifang Exp $
 */

package xsul_sample_capability;

import java.lang.reflect.Proxy;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.capability.AutoCapabilityInvoker;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoAutoCapClient {
    
    public static final String USAGE =
        "usage: java "+EchoCapClient.class.getName()+" \n" +
        "\t[--url url of the service]\n" +
        "\t[--csurl url of the capman service]\n"+
        "";
    
    private static final int SERVICE_URL = 0;
    private static final int SERVICE_CAP = 1;
    
    static String[] parseArgs(String[] args) throws IllegalArgumentException {
        // set arg names expected
        String[] argNames = new String[5];
        argNames[SERVICE_URL] = "--url";
        argNames[SERVICE_CAP] = "--csurl";
        
        // set defaults
        String[] defaultValues = new String[5];
        defaultValues[SERVICE_URL] = null;
        defaultValues[SERVICE_CAP] = null;
        
        // set required
        boolean[] requiredArgs = new boolean[5];
        requiredArgs[SERVICE_URL] = true;
        requiredArgs[SERVICE_CAP] = true;
        
        return CapabilityUtil.parse(args, argNames, defaultValues, requiredArgs, USAGE);
    }
    
    public static void main(String[] args) {
        String[] myArgs = parseArgs(args);
        String svcurl = myArgs[SERVICE_URL];
        String svccap = myArgs[SERVICE_CAP];
        System.err.println("svcurl: " + svcurl);
        System.err.println("svccap: " + svccap);
        try {
            SoapHttpDynamicInfosetInvoker invoker =
                new AutoCapabilityInvoker(svcurl, svccap);

            SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
                invoker, XsdTypeHandlerRegistry.getInstance());
            
            EchoService ref = (EchoService) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] { EchoService.class },
                handler);
            
            String result = ref.sayHello("/hello");
            
            System.out.println("got back "+result);
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}

