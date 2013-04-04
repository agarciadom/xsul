/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * based on xsul_dii.XsulDynamicInvoker.java,v 1.8 2005/01/18 10:02:38 aslom Exp $
 */

package xsul.async_msg;

import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulVersion;
import xsul.lead.LeadContextHeader;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xservo.XService;
import xsul.xservo_soap.XSoapDocLiteralService;
import xsul.xservo_soap_http.HttpBasedServices;

public class EchoAsyncService {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static XmlNamespace NS = builder.newNamespace("http://soapinterop.org/WSDLInteropTestDocLit");
    private static HttpBasedServices httpServices;
    private static final String SERVICE_NAME = "echoAsync";
    
    public static final String DEFAULT_WSDL_LOC = "tests/xsul/async_msg/echo_async.wsdl";
    
    private static void usage(String errMsg) {
        System.err.println("Usage: [port [wsdlLoc]] ");
        System.exit(1);
    }
    
    /**
     * Launch service from command line
     */
    public static void main(String[] args) {
        (new EchoAsyncService()).run(args);
    }
    
    private void run(String[] args) {
        XsulVersion.exitIfRequiredVersionMissing(XsulVersion.SPEC_VERSION); //sanity check
        XsulVersion.exitIfRequiredVersionMissing("2.1.3");
        if(args.length > 2) usage("");
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        httpServices = new HttpBasedServices(tcpPort);
        httpServices.setServerName("XSUL-EchoAsync/"+XsulVersion.getImplementationVersion());
        
        System.out.println("Server started on "+httpServices.getServerPort());
        
        String wsdlLoc = args.length > 1 ? args[1] : DEFAULT_WSDL_LOC;
        System.out.println("Using WSDL for service description from "+wsdlLoc);
        XService xsvc = httpServices.addService(
            //new XSoapRpcBasedService(SERVICE_NAME, wsdlLoc, new EchoPeerImpl()));
            new XSoapDocLiteralService(SERVICE_NAME, wsdlLoc, new EchoPeerImpl()));
        xsvc.addHandler(new StickySoapHeaderHandler("retrieve-lead-header", LeadContextHeader.TYPE));
        xsvc.startService();
        System.out.println("Service started");
        System.out.println("Service WSDL available at "+getServiceWsdlLocation());
    }
    
    public static String getServiceWsdlLocation() {
        //return httpServices.getServer().getLocation() + "/"+SERVICE_NAME+"?wsdl";
        return httpServices.getServiceWsdl(SERVICE_NAME);
    }
    public static void shutdownServer() {
        httpServices.getServer().shutdownServer();
    }
    
}

