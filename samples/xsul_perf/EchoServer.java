/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoServer.java,v 1.10 2004/03/30 19:13:09 aslom Exp $
 */

package xsul_perf;

import java.net.URL;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.ptls.PureTLSContext;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.ServerSocketFactory;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.puretls_client_socket_factory.PuretlsClientSocketFactory;
import xsul.puretls_server_socket_factory.PuretlsServerSocketFactory;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

/**
 * Run sample.
 *
 * @version $Revision: 1.10 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class EchoServer {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static SoapHttpDynamicInfosetProcessor customizedProcessor;
    //private static HttpMiniServer miniMe;
    //private static HttpDynamicInfosetProcessor httpProcessor;
    
    public static void main(String[] args) throws Exception
    {
        //int port = Integer.parseInt(args[0]);
        
        
        //int port = 34321;
        String location = args.length > 0 ? args[0] : null;
        boolean secure = location.startsWith("https://");
        int port = 34321;
        if(location.startsWith("http")) {
            URL u = new URL(location);
            port = u.getPort();
        } else {
            port = Integer.parseInt(args[0]);
        }
        
        
        EchoService srv = new EchoServiceImpl("EchoService");
        
        TypeHandlerRegistry registry = XsdTypeHandlerRegistry.getInstance();
        
        final SoapRpcReflectionBasedService msgProcessor =
            new SoapRpcReflectionBasedService(srv, registry);
        msgProcessor.setSupportedSoapFragrances(
            new SoapUtil[]{ Soap11Util.getInstance(), Soap12Util.getInstance() });
        
        customizedProcessor = new SoapHttpDynamicInfosetProcessor() {
            public XmlElement processMessage(XmlElement message) {
                XmlElement resp = msgProcessor.processMessage(message);
                return resp;
            }
        };
        if(secure) {
            //HttpMiniServlet serv = new XdrsServer();
            ServerSocketFactory secureSocketFactory;
            boolean useCogCredentials = false;
            if(useCogCredentials) {
                PureTLSContext ctx = new PureTLSContext();
                X509Certificate [] certs = TrustedCertificates.getDefaultTrustedCertificates().getCertificates();
                ctx.setTrustedCertificates(certs);
                GlobusCredential cred = GlobusCredential.getDefaultCredential();
                ctx.setCredential(cred);
                secureSocketFactory = new PuretlsServerSocketFactory(port, ctx);
            } else {
                secureSocketFactory = new PuretlsServerSocketFactory(
                    port, "root.pem", "server.pem", "password");
            }
            HttpMiniServer miniMe = new HttpMiniServer(secureSocketFactory);
            System.out.println("using PureTLS secure scoket factory");
            customizedProcessor.setServer(miniMe);
        } else {
            customizedProcessor.setServerPort(port);
        }
        
        customizedProcessor.setSupportedSoapFragrances(msgProcessor.getSupportedSoapFragrances());
        
        customizedProcessor.start();
        System.err.println("started on "+customizedProcessor.getServer().getLocation());
    }
    
    public static void shutdown() {
        customizedProcessor.shutdown();
        
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {}
    }
    
}



