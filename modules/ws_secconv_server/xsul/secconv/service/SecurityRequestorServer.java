/**
 * SecurityRequestorServer.java
 *
 * @author Liang Fang
 * $Id: SecurityRequestorServer.java,v 1.1 2005/02/28 23:17:37 lifang Exp $
 */

package xsul.secconv.service;

import org.xmlpull.v1.builder.XmlElement;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.secconv.SecurityRequestorService;
import xsul.secconv.service.impl.SecurityRequestorServiceImpl;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class SecurityRequestorServer
{
    private static SoapHttpDynamicInfosetProcessor customizedProcessor;
    
    public static void main(String[] args) throws Exception
    {
        int port = Integer.parseInt(args[0]);
                
        SecurityRequestorService srv =
            new SecurityRequestorServiceImpl("http://localhost:"+port+"/SecurityRequestorService");
        
        TypeHandlerRegistry registry = XsdTypeHandlerRegistry.getInstance();
        
        final SoapRpcReflectionBasedService msgProcessor =
            new SoapRpcReflectionBasedService(srv, registry);
        
        msgProcessor.setSupportedSoapFragrances(
            new SoapUtil[]{ Soap11Util.getInstance(), Soap12Util.getInstance() });
        
        customizedProcessor = new SoapHttpDynamicInfosetProcessor() {
            public XmlElement processMessage(XmlElement message) {
                return msgProcessor.processMessage(message);
            }
        };
        customizedProcessor.setSupportedSoapFragrances(msgProcessor.getSupportedSoapFragrances());
        
        customizedProcessor.setServerPort(port);
        
        customizedProcessor.start();
        
    }
    
    public static void shutdown() {
        customizedProcessor.shutdown();
        
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {}
    }
    
}


