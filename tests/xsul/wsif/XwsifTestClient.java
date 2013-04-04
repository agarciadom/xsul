/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * based on xsul_dii.XsulDynamicInvoker.java,v 1.8 2005/01/18 10:02:38 aslom Exp $
 */

package xsul.wsif;

import java.io.File;
import java.net.URI;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulVersion;
import xsul.util.XsulUtil;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import xsul.xwsif_runtime_async_http.XsulSoapHttpWsaResponsesCorrelator;
import xsul.xwsif_runtime_async_msgbox.XsulMsgBoxWsaResponsesCorrelator;

public class XwsifTestClient {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static XmlNamespace NS = builder.newNamespace(
        XwsifTestService.XWSDLC_PORTTYPE_QNAME.getNamespaceURI());
    
    private static void usage(String errMsg) {
        if(errMsg != null) {
            System.err.println("Error: "+errMsg);
        }
        System.err.println("Usage: [-msgbox URL] [-client_port p] [-count n] [WSDL_URL] [-no_stub]");
        System.exit(1);
    }
    
    public static void main(String[] args) throws Exception {
        XsulVersion.exitIfRequiredVersionMissing(XsulVersion.SPEC_VERSION); //sanity check
        
        runClient(args);
    }
    
    private static void runClient(String[] args) throws Exception {
        URI base = ((new File(".")).toURI());
        
        String wsdlLoc = null;
        
        System.err.println("CLIENT Starting "+XwsifTestClient.class.getName());
        
        String msgBoxServiceLoc = null;
        int clientPort = -1;
        int count = 1;
        boolean useStub = true;
        for (int i = 0; i < args.length; i++)
        {
            String n = args[i];
            if(n.startsWith("-h")) {
                usage("");
            } else if(n.equals("-msgbox")) {
                msgBoxServiceLoc = args[++i];
            } else if(n.equals("-client_port")) {
                clientPort = Integer.parseInt(args[++i]);
            } else if(n.equals("-count")) {
                count = Integer.parseInt(args[++i]);
            } else if(n.equals("-no_stub")) {
                useStub = false;
            } else if(!n.startsWith("-")){
                wsdlLoc = n;
            }
        }
        
        boolean selfTesting = System.getProperty("start_server") != null;
        if(selfTesting) { // just for testing
            //            System.out.println("CLIENT Doing starting embedded server (self testing)");
            //            String wsdlToUse = wsdlLoc != null ? wsdlLoc : EchoAsyncService.DEFAULT_WSDL_LOC;
            //            EchoAsyncService.main(new String[]{"0", wsdlToUse});
            //            wsdlLoc = EchoAsyncService.getServiceWsdlLocation();
        }
        
        if(wsdlLoc == null) {
            usage("WSDL_URL is missing");
        }
        
        System.err.println("CLIENT invoking operation echoString using WSDL from "+wsdlLoc);
        
        WSIFAsyncResponsesCorrelator correlator;
        if(msgBoxServiceLoc != null){
            System.err.println("CLIENT using async correlator with message box service "+msgBoxServiceLoc);
            correlator = new XsulMsgBoxWsaResponsesCorrelator(msgBoxServiceLoc);
        } else if(clientPort != -1) {
            correlator = new XsulSoapHttpWsaResponsesCorrelator(clientPort);
            String  serverLoc = ((XsulSoapHttpWsaResponsesCorrelator)correlator).getServerLocation();
            System.err.println("CLIENT using async correlator that runs web servcer on "+serverLoc);
        } else {
            correlator = null;
        }
        
        WSIFClient wclient = WSIFRuntime.newClient(wsdlLoc)
            .useAsyncMessaging(correlator)
            .setAsyncResponseTimeoutInMs(33000L); // to simplify testing set to just few seconds
        
        XwsifTestService stub = null;
        WSIFOperation operation = null;
        WSIFMessage inputMessage = null;
        WSIFMessage outputMessage = null;
        WSIFMessage faultMessage = null;
        if(useStub) {
            stub = (XwsifTestService) wclient.generateDynamicStub(XwsifTestService.class);
        } else {
            WSIFPort wsifPort = wclient.getPort();
            operation = wsifPort.createOperation("echoString");
            outputMessage = operation.createOutputMessage();
            //((XmlElement)outputMessage).removeAllChildren();
            faultMessage = operation.createFaultMessage();
            //((XmlElement)faultMessage).removeAllChildren();
        }
        
        System.err.println("CLIENT sending "+count+" messages");
        
        String METHOD_NAME = "executeInputOnly";
        boolean ONEWAY = true;
        for (int i = 0; i < count; i++) {
            XmlElement in = builder.newFragment(NS, "METHOD_NAME");
            in.addElement("cid").addChild("hello"+(i + 1));
            
            if(!useStub) {
                //inputMessage = operation.createInputMessage();
                //in2 = (XmlElement) inputMessage;
                //in2.removeAllChildren(); //ignore whatever WSIF wants to put into message ...
                inputMessage = new WSIFMessageElement(in);
            }
            String s = XsulUtil.safeXmlToString((XmlContainer)in);
            XmlElement result = null;
            if(stub != null) {
                System.err.println("CLIENT using stub to send message:\n"+s);
                //result =
                stub.executeInputOnly(in);
            } else {
                System.err.println("CLIENT using WSIF to send message:\n"+s);
                boolean success;
                if(ONEWAY) {
                    success = operation.executeRequestResponseOperation(
                        inputMessage, outputMessage, faultMessage);
                    if(success) {
                        result = (XmlElement) outputMessage;
                    } else {
                        result = (XmlElement) faultMessage;
                    }
                } else {
                    operation.executeInputOnlyOperation(inputMessage);
                    success =  true;
                }
            }
            
            System.err.println("CLIENT received message:\n"+
                                   (result != null ? XsulUtil.safeXmlToString(result) : "<NONE>"));
        }
        
        try {
            int secs = 1; //40
            System.err.println("CLIENT now will wait for "+secs+" [s]");
            Thread.currentThread().sleep(secs * 1000L);
            System.err.println("CLIENT waiting finished");
        } catch (InterruptedException e) {}
        
        if(selfTesting) {
            //try {EchoAsyncService.shutdownServer();} catch(Exception e) {}
        }
        System.err.println("CLIENT finished");
    }
    
    
}



/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1) All redistributions of source code must retain the above
 *    copyright notice, the list of authors in the original source
 *    code, this list of conditions and the disclaimer listed in this
 *    license;
 *
 * 2) All redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the disclaimer
 *    listed in this license in the documentation and/or other
 *    materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include
 *    the following acknowledgement:
 *
 *      "This product includes software developed by the Indiana
 *      University Extreme! Lab.  For further information please visit
 *      http://www.extreme.indiana.edu/"
 *
 *    Alternatively, this acknowledgment may appear in the software
 *    itself, and wherever such third-party acknowledgments normally
 *    appear.
 *
 * 4) The name "Indiana University" or "Indiana University
 *    Extreme! Lab" shall not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission from Indiana University.  For written permission,
 *    please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana
 *    University" name nor may "Indiana University" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code
 * provided does not infringe the patent or any other intellectual
 * property rights of any other entity.  Indiana University disclaims any
 * liability to any recipient for claims brought by any other entity
 * based on infringement of intellectual property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH
 * NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA
 * UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT
 * SOFTWARE IS FREE OF INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR
 * OTHER PROPRIETARY RIGHTS.  INDIANA UNIVERSITY MAKES NO WARRANTIES THAT
 * SOFTWARE IS FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP
 * DOORS", "WORMS", OR OTHER HARMFUL CODE.  LICENSEE ASSUMES THE ENTIRE
 * RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS,
 * AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION GENERATED USING
 * SOFTWARE.
 */

