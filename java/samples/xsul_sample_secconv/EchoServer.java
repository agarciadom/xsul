/**
 * EchoServer.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul_sample_secconv;

import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.processor.http.HttpDynamicInfosetProcessor;
import xsul.processor.secconv.SecurityRequestorProcessor;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoServer
{
    private static HttpDynamicInfosetProcessor customizedProcessor;
    
    public static final String USAGE =
        "usage: java "+EchoServer.class.getName()+" \n" +
        "\t[--port port number of the service]\n" +
        "\t[--passwd your password]\n"+
        "";
    
    private static final int PORT = 0;
    private static final int PSWD = 1;
    
    static String[] parseArgs(String[] args) throws IllegalArgumentException
    {
        String[] argNames = new String[4];
        argNames[PORT] = "--port";
        argNames[PSWD] = "--passwd";
        
        // set defaults
        String[] defaultValues = new String[4];
        defaultValues[PORT] = "8765";
        defaultValues[PSWD] = "";
        
        // set required
        boolean[] requiredArgs = new boolean[4];
        requiredArgs[PORT] = false;
        requiredArgs[PSWD] = false;
        
        return CapabilityUtil.parse(args, argNames, defaultValues,
                                    requiredArgs, USAGE);
    }
    
    public static void main(String[] args) throws Exception
    {
        String[] myArgs = parseArgs(args);
        int port = Integer.parseInt(myArgs[PORT]);
        char[] passwd = myArgs[PSWD].toCharArray();
        
        EchoService srv = new EchoServiceImpl();
        
        TypeHandlerRegistry registry = XsdTypeHandlerRegistry.getInstance();
        
        final SoapRpcReflectionBasedService service =
            new SoapRpcReflectionBasedService(srv, registry);
        
        customizedProcessor = new SecurityRequestorProcessor(service);
        
        customizedProcessor.setServerPort(port);
        
        customizedProcessor.start();
        System.out.println(customizedProcessor.getClass()+" running on "+customizedProcessor.getServer().getLocation());
        
    }
}

