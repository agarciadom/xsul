/**
 * EchoPasswdClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: EchoPasswdClient.java,v 1.1 2005/01/11 02:02:19 lifang Exp $
 */

package xsul_sample_capability;

import java.lang.reflect.Proxy;
import xsul.invoker.password.PasswordInvoker;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoPasswdClient {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        
        SoapHttpDynamicInfosetInvoker invoker =
            new PasswordInvoker("http://localhost:"+port);
        
        SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
            invoker, XsdTypeHandlerRegistry.getInstance());
        
        EchoService ref = (EchoService) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { EchoService.class },
            handler);
        
        String result = ref.sayHello("/hello");
        
        System.out.println("got back "+result);
    }
}
