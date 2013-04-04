/**
 * AutoCapabilityInvoker.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.invoker.capability;

import java.lang.reflect.Proxy;
import org.xmlpull.v1.builder.XmlDocument;
import xsul.MLogger;
import xsul.dsig.saml.authorization.Capability;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.signature.SignatureInvoker;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xpola.capman.CapabilityManager;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class AutoCapabilityInvoker extends CapabilityInvoker {
    
    private static final MLogger logger = MLogger.getLogger();
    
    // capaman service location
    private String caps_location = "";
    
    public AutoCapabilityInvoker(String loc, String csloc) {
        super(loc);
        this.caps_location = csloc;
    }
    
    public XmlDocument invokeXml(XmlDocument request)
        throws DynamicInfosetInvokerException {
        
        if(cap == null) {
            
            // get the cap from capman service specified automatically
            SoapHttpDynamicInfosetInvoker invoker =
                new SignatureInvoker(cred, trustedCerts, caps_location);
            
            SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
                invoker, XsdTypeHandlerRegistry.getInstance());
            
            CapabilityManager ref = (CapabilityManager) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] { CapabilityManager.class },
                handler);
            
            try {
                String cap1 = ref.getCapabilityByHandle(getLocation());
                logger.finest(cap1.toString());
                cap = new Capability(cap1);
            } catch (Exception e) {
                String error = "unable to get capability from "
                                  + caps_location + " for " + getLocation();
                logger.severe(error);
                throw new DynamicInfosetInvokerException(error, e);
            }
        }
        
        return super.invokeXml(request);
    }
}

