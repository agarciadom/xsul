/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: BenchServer.java,v 1.4 2005/02/16 05:52:52 aslom Exp $
 */

package soap_bench;

import java.net.URL;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.common_type_handler.CommonTypeHandlerRegistry;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;

/**
 * Run benchmark server.
 *
 * @version $Revision: 1.4 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class BenchServer {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static SoapHttpDynamicInfosetProcessor customizedProcessor;
    //private static HttpMiniServer miniMe;
    //private static HttpDynamicInfosetProcessor httpProcessor;
    
    public static void main(String[] args) throws Exception
    {
        //int port = Integer.parseInt(args[0]);
        
        //int port = 34321;
        String location = args.length > 0 ? args[0] : null;
        int port = 34321;
        if(location != null) {
            if(location.startsWith("http")) {
                URL u = new URL(location);
                port = u.getPort();
            } else {
                port = Integer.parseInt(args[0]);
            }
        }
        //int N = ... //ignored
        //String testType = ""; //args[1]; //ignored - we suport all types ...
        
//        int arrSizeToSend = 10;
//        if(args.length > 3) {
//            arrSizeToSend = Integer.parseInt(args[3]);
//        }
//        System.out.println("starting service on port "+port+" for arrays size "+arrSizeToSend);
        System.out.println("starting service on port "+port);
        
        EchoService srv = new ServiceImpl();
        
        final SoapRpcReflectionBasedService msgProcessor =
            new SoapRpcReflectionBasedService(srv, CommonTypeHandlerRegistry.getInstance());
        
        msgProcessor.setSupportedSoapFragrances(
            new SoapUtil[]{ Soap11Util.getInstance(), Soap12Util.getInstance() });
        
        customizedProcessor = new SoapHttpDynamicInfosetProcessor() {
            public XmlElement processMessage(XmlElement message) {
                XmlElement resp = msgProcessor.processMessage(message);
                return resp;
            }
        };
        customizedProcessor.setServerPort(port);
        
        customizedProcessor.setSupportedSoapFragrances(msgProcessor.getSupportedSoapFragrances());
        
        customizedProcessor.start();
        System.err.println("started on "+customizedProcessor.getServer().getLocation());
    }
    
    public static void shutdown() {
        try {customizedProcessor.shutdown();} catch(Exception e) {}
        
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {}
    }
    
}

