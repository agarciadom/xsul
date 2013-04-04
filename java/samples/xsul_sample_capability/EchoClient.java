/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoClient.java,v 1.5 2005/05/04 20:52:17 lifang Exp $
 */

package xsul_sample_capability;


import java.lang.reflect.Proxy;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeSigner;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeVerifier;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.signature.SignatureInvoker;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoClient
{
    /////////////////////////////////////////////////////////////////////////
    // Local static methods to start service                               //
    /////////////////////////////////////////////////////////////////////////
    
    public static final String USAGE =
        "usage: java "+EchoClient.class.getName()+" \n" +
        "\t[--port port number of the service]\n" +
        "";
    
    private static final int PORT = 0;
    
    static String[] parseArgs(String[] args) throws IllegalArgumentException
    {
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
    
    public static void main(String[] args)
    {
        try
        {
            String[] myArgs = parseArgs(args);
            int port = Integer.parseInt(myArgs[PORT]);
            
            EchoServer.main(new String[]{"--port", ""+port});
            
            //TODO XmlElement response = invoker.invokeSoap11(request);
            
            final GlobusCredential cred = CapabilityUtil.getGlobusCredential();
            //            final GlobusCredential cred = CapabilityUtil.getGlobusCredential(null, null);
            final X509Certificate[] trustedCerts = CapabilityUtil.getTrustedCertificates().getCertificates();
            //construct XML

            SoapHttpDynamicInfosetInvoker invoker =
                new SignatureInvoker(cred, trustedCerts, "http://localhost:"+port);
            
            SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
                invoker, XsdTypeHandlerRegistry.getInstance());
            
            EchoService ref = (EchoService) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(), //XDirectoryService.class.getClassLoader(),
                new Class[] { EchoService.class },
                handler);
            
            String result = ref.sayHello("/hello");
            
            System.out.println("got back "+result);
            //            // do actual invocation by wrapping message in SOAP 1.1 Envelope and sending it over HTTP
            //            XmlElement request = builder.parseFragmentFromReader(
            //                new StringReader("<getNode><path>/hello</path></getNode>"));
            //            XmlElement response = invoker.invokeMessage(request);
            
            //            XmlElement response = handler.getLastResponse();
            //
            //            String r = builder.serializeToString(response);
            //            System.out.println("got back "+r);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            EchoServer.shutdown();
        }
    }
}




