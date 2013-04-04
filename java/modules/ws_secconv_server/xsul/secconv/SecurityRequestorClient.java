/**
 * SecurityRequestorClient.java
 *
 * @author Liang Fang
 * $Id: SecurityRequestorClient.java,v 1.1 2005/02/28 23:17:37 lifang Exp $
 */

package xsul.secconv;

import java.lang.reflect.Proxy;
import java.security.Key;
import sun.misc.BASE64Encoder;
import xsul.MLogger;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.secconv.SecurityRequestorService;
import xsul.secconv.autha.AuthaClientNegotiator;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class SecurityRequestorClient
{
    private static final MLogger logger = MLogger.getLogger();

    private static BASE64Encoder encoder = new BASE64Encoder();

    public static void main(String[] args) throws Exception
    {
        //int port = Integer.parseInt(args[0]);
        int port = 8989;
        String location = null;
        if(args.length > 0) {
            String s = args[0];
            if(s.startsWith("http")) {
                location = s;
            } else {
                port = Integer.parseInt(s);
            }
        }
        if(location == null) {
            location = "http://localhost:"+port;
        }
        
        SoapHttpDynamicInfosetInvoker invoker = new SoapHttpDynamicInfosetInvoker(location);
        
        SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
            invoker, XsdTypeHandlerRegistry.getInstance());
        invoker.setSoapFragrance(Soap12Util.getInstance());
        
        // JDK 1.3+ magic to create dynamic stub
        SecurityRequestorService ref = (SecurityRequestorService) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { SecurityRequestorService.class },
            handler);
        
        AuthaClientNegotiator autha = new AuthaClientNegotiator();
        autha.negotiate(ref);
        String contextId = autha.getContextId();
        logger.finest("got contextId from autha: "+contextId);
        Key key = autha.getSessionKey();
        logger.finest("encoded key: "+encoder.encode(key.getEncoded()));
        logger.finest("Context established");
    }
    
}

