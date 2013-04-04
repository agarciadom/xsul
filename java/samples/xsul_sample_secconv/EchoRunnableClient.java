/**
 * EchoRunnableClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul_sample_secconv;

import java.lang.reflect.Proxy;
import xsul.invoker.secconv.SecurityRequestorInvoker;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;
import java.rmi.RemoteException;

public class EchoRunnableClient implements Runnable
{
    private String location;
    private String sclocation;
    
    public EchoRunnableClient(String location, String sclocation) {
        this.location = location;
        this.sclocation = sclocation;
    }
    
    public void run() {
        SecurityRequestorInvoker srinvoker =
            new SecurityRequestorInvoker(location, sclocation);
        //            srinvoker.setLocation("http://localhost:"+port);
        
        SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
            srinvoker, XsdTypeHandlerRegistry.getInstance());
        
        EchoService ref = (EchoService) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(), //XDirectoryService.class.getClassLoader(),
            new Class[] { EchoService.class },
            handler);
        
        try {
            long start = System.currentTimeMillis();
            String result = ref.sayHello("/hello");
            long end = System.currentTimeMillis();
            long elapse = end-start;
            System.out.println(Thread.currentThread().getName()+" result: " + result + " " +elapse);
        } catch (RemoteException e) {e.printStackTrace();}
    }
    
}

