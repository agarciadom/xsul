/**
 * EchoPasswdServer.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: EchoPasswdServer.java,v 1.1 2005/01/11 02:02:19 lifang Exp $
 */

package xsul_sample_capability;

import xsul.processor.password.PasswordProcessor;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoPasswdServer {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        
        EchoService srv = new EchoServiceImpl();
        TypeHandlerRegistry registry = XsdTypeHandlerRegistry.getInstance();
        SoapRpcReflectionBasedService service =
            new SoapRpcReflectionBasedService(srv, registry);
        SoapHttpDynamicInfosetProcessor passwdServer =
            new PasswordProcessor(service, port);
        passwdServer.start();
    }
}

