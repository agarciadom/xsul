/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoServer.java,v 1.5 2005/05/04 20:52:17 lifang Exp $
 */

package xsul_sample_capability;


import java.security.Principal;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlElement;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.processor.signature.SignatureProcessor;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoServer {
    
    //private static HttpMiniServer miniMe;
    private static SoapHttpDynamicInfosetProcessor customizedProcessor;
    
    public static final String USAGE =
        "usage: java "+EchoServer.class.getName()+" \n" +
        "\t[--port port number of the service]\n" +
        "";
    
    private static final int PORT = 0;
    
    static String[] parseArgs(String[] args) throws IllegalArgumentException {
        String[] argNames = new String[4];
        argNames[PORT] = "--port";
        
        // set defaults
        String[] defaultValues = new String[4];
        defaultValues[PORT] = "8989";
        
        // set required
        boolean[] requiredArgs = new boolean[4];
        requiredArgs[PORT] = false;
        
        return CapabilityUtil.parse(args, argNames, defaultValues, requiredArgs, USAGE);
    }
    
    public static boolean isAuthorized(Principal dn, XmlElement envelope) throws RuntimeException {
        //System.out.println("\n\n\n\nchecking authorization dn="+dn);
        return true;
    }
    
    
    public static void main(String[] args) throws Exception {
        String[] myArgs = parseArgs(args);
        int port = Integer.parseInt(myArgs[PORT]);
        
        //TODO: Dispatcher dsptr = new Soap11ReflectionBasedDispatcher(srv);
        EchoService srv = new EchoServiceImpl();
        //HttpMiniServlet serv = new XdrsServer();
        
        //miniMe = new HttpMiniServer(port);
        
        TypeHandlerRegistry registry = XsdTypeHandlerRegistry.getInstance();
        
        SoapRpcReflectionBasedService service =
            new SoapRpcReflectionBasedService(srv, registry);
        
        GlobusCredential cred = CapabilityUtil.getGlobusCredential();
        //final GlobusCredential cred = CapabilityUtil.getGlobusCredential(null, null);
        X509Certificate[] trustedCerts = CapabilityUtil.getTrustedCertificates().getCertificates();
        customizedProcessor =
            new SignatureProcessor(cred, trustedCerts, service, port);
        customizedProcessor.start();
        System.out.println(customizedProcessor.getClass()+" running on "+customizedProcessor.getServer().getLocation());
    }
    
    public static void shutdown() {
        if(customizedProcessor != null){
            customizedProcessor.shutdown();
            
            try {
                Thread.currentThread().sleep(1000);
            }
            catch (InterruptedException e) {}
        }
    }
    
}




