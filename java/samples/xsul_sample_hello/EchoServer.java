/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoServer.java,v 1.16 2006/04/30 06:48:15 aslom Exp $
 */

package xsul_sample_hello;

import java.net.URI;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.ws_addressing.WsaInvoker;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.ws_addressing.WsaRelatesTo;
import xsul.ws_addressing.WsaUtil;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

/**
 * Run sample.
 *
 * @version $Revision: 1.16 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class EchoServer extends SoapHttpDynamicInfosetProcessor{
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static SoapHttpDynamicInfosetProcessor customizedProcessor;
    public final static String MESSAGE_URI = "http://example.com/EchoPlusOne";
    public final static XmlNamespace MESSAGE_URI_NS = builder.newNamespace(MESSAGE_URI);
    //private static HttpMiniServer miniMe;
    //private static HttpDynamicInfosetProcessor httpProcessor;
    SoapRpcReflectionBasedService msgProcessor;
    
    public EchoServer(SoapRpcReflectionBasedService msgProcessor) {
        this.msgProcessor = msgProcessor;
        setSupportedSoapFragrances(msgProcessor.getSupportedSoapFragrances());
    }
    
    public XmlDocument processSoapEnvelope(XmlElement envelope, final SoapUtil soapFragrance)
        throws DynamicInfosetProcessorException
        
    {
        // concert envelope to String
        //                System.err.println(getClass().getName()+" received envelope="
        //                                       +builder.serializeToString(envelope));
        logger.finest("received envelope="+builder.serializeToString(envelope));
        // this XML string could be convertedto DOM ot whatever API one preferes (like JDOM, DOM4J, ...)
        
        XmlElement soapHeader = envelope.element(null, XmlConstants.S_HEADER);
        String location = getServer().getLocation();
        final WsaMessageInformationHeaders requestWsaHeaders;
        if(soapHeader != null) {
            //throw new XsulException("SOAP message must have headers");
            
            requestWsaHeaders = new WsaMessageInformationHeaders(envelope);
            //System.err.println(getClass().getName()+" message destinaiton="+wsah.getTo());
            //assertEquals(location, wsah.getTo().toString());
            //      if(!location.equals(wsah.getTo().toString())) {
            //          throw new IllegalStateException();
            //      }
        } else {
            requestWsaHeaders = null;
        }
        final XmlElement message = soapFragrance.requiredBodyContent(envelope);
        
        XmlElement responseMessage;
        XmlElement fault;
        try {
            responseMessage = processMessage(message);
            fault = null;
        } catch (Exception e) {
            fault = soapFragrance.generateSoapClientFault("could not process: "+e.getMessage(), e);
            responseMessage = fault;
            //return soapFragrance.wrapBodyContent(fault);
        }
        
        if(responseMessage == null) {
            // no response needed -- method wants to be one-way
            return null;
        }
        
        if(requestWsaHeaders != null) {
            if(requestWsaHeaders.getReplyTo() != null) {
                //if(requestWsaHeaders.getReplyTo().getAddress().equals(WsAddressing.URI_ROLE_ANONYMOUS)) {
                if(WsaUtil.isAsyncReplyRequired(envelope)) {
                    asyncSendResponse(soapFragrance, responseMessage, requestWsaHeaders);
                    // no XML in response sent but empty 202 OK - actual response will be sent over new connection
                    return null;
                } else {
                    // send back response wrapped in SOAP envelope
                    return soapFragrance.wrapBodyContent(responseMessage);
                }
            } else {
                // no response needed
                return null;
            }
        } else {
            // should we add WSA stuff even if it was not in request?!
            XmlDocument responseEnvelope = soapFragrance.wrapBodyContent(responseMessage);
            return responseEnvelope;
        }
    }

    private void asyncSendResponse(final SoapUtil soapFragrance,
                                   final XmlElement responseMessage,
                                   final WsaMessageInformationHeaders requestWsaHeaders) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    XmlDocument responseEnvelope = soapFragrance.wrapBodyContent(responseMessage);
                    // now we need to add all WSA headers etc ...
                    WsaMessageInformationHeaders responseWsaHeaders =
                        new WsaMessageInformationHeaders(responseEnvelope);
                    responseWsaHeaders.explodeEndpointReference(requestWsaHeaders.getReplyTo());
                    URI messageId = requestWsaHeaders.getMessageId();
                    if(messageId != null) {
                        responseWsaHeaders.addRelatesTo(new WsaRelatesTo(requestWsaHeaders.getMessageId()));
                    }
                    WsaInvoker invoker = new WsaInvoker();
                    invoker.setDefaultAction(URI.create(MESSAGE_URI+"Response"));
                    invoker.sendXml(responseEnvelope);
                } catch (Exception e) {
                    logger.finest("could not send response to "+requestWsaHeaders.getReplyTo(), e);
                }
            }
        };
        //LATER: use Executor / ThreadPool
        new Thread(r).start();
    }
    
    
    public XmlElement processMessage(XmlElement message) {
        // delegate call to Java method or whatever ...
        return msgProcessor.processMessage(message);
    }
    
    public static void main(String[] args) throws Exception
    {
        int port = Integer.parseInt(args[0]);
        
        
        EchoService srv = new EchoServiceImpl("PathBasedService");
        //HttpMiniServlet serv = new XdrsServer();
        
        //miniMe = new HttpMiniServer(port);
        
        TypeHandlerRegistry registry = XsdTypeHandlerRegistry.getInstance();
        
        final SoapRpcReflectionBasedService msgProcessor = new SoapRpcReflectionBasedService(srv, registry);
        msgProcessor.setSupportedSoapFragrances(
            new SoapUtil[]{ Soap11Util.getInstance(), Soap12Util.getInstance() });
        
        customizedProcessor = new EchoServer(msgProcessor);
        
        customizedProcessor.setServerPort(port);
        customizedProcessor.start();
        
    }
    
    public static void serverShutdown() {
        customizedProcessor.shutdown();
        
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {}
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


