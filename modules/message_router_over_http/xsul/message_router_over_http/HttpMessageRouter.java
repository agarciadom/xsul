/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpMessageRouter.java,v 1.7 2006/04/30 06:48:13 aslom Exp $
 */

package xsul.message_router_over_http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;
import xsul.message_router.MessageContext;
import xsul.message_router.MessageProcessingException;
import xsul.message_router.MessageRouter;
import xsul.message_router.MessageRouterException;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;

//TODO: consider case of N routers - all injecting into the same target message chain :-)

/**
 * Select processor for incoming mesage essentially converts
 * XML document (DynamicInfosetProcessor)
 * into XML message that can be consumed by chained MessageRouter.
 *
 *
 * @version $Revision: 1.7 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public abstract class HttpMessageRouter implements MessageRouter {
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    //    private static final MLogger xmlOut = MLogger.getLogger("trace.xsul.xml.processor.out");
    //    private static final MLogger xmlIn = MLogger.getLogger("trace.xsul.xml.processor.in");
    private HttpMiniServer server;
    private RouterServlet servlet;
    
    public HttpMessageRouter() {
        this(0);
    }
    
    public HttpMessageRouter(int tcpPort) throws MessageRouterException {
        try {
            server = new HttpMiniServer(tcpPort);
        } catch (HttpServerException e) {
            throw new MessageRouterException("could not create router on TCP port "+tcpPort, e);
        }
        servlet = new RouterServlet();
        server.useServlet(servlet);
    }
    
    public abstract boolean process(MessageContext context) throws MessageProcessingException;
    
    public HttpMiniServer getHttpServer() { return server; }
    
    public void startService() throws MessageRouterException {
        try {
            server.startServer();
        } catch (HttpServerException e) {
            throw new MessageRouterException("could not start router on "+server.getLocation(), e);
        }
    }
    
    public void stopService() throws MessageRouterException {
        try {
            server.stopServer();
        } catch (HttpServerException e) {
            throw new MessageRouterException("could not start router on "+server.getLocation(), e);
        }
    }
    
    public void shutdownService() throws MessageRouterException {
        try {
            server.shutdownServer();
        } catch (HttpServerException e) {
            throw new MessageRouterException("could not start router on "+server.getLocation(), e);
        }
    }
    
    //NOTE: allows extending so for example HTTP Basic Auth check can be added easily!
    public void service(HttpServerRequest req, HttpServerResponse res)
        throws HttpServerException
    {
        String encoding = req.getCharset();
        InputStream is = req.getInputStream();
        
        XmlDocument incomingXmlDoc = builder.parseInputStream(is, encoding);
        
        HttpMessageContext ctx = new HttpMessageContext(req);
        
        XmlElement incomingXml = incomingXmlDoc.getDocumentElement();
        String soaped = null;
        if(XmlConstants.S_ENVELOPE.equals(incomingXml.getName())) {
            String n = incomingXml.getNamespaceName();
            //ALEK fix it !!!!
            if(Soap11Util.NS_URI_SOAP11.equals(n)) {
                soaped = Soap11Util.NS_URI_SOAP11;
                // extract body content
                XmlElement body = incomingXml.element(Soap11Util.SOAP11_NS, "Body");
                incomingXml = (XmlElement) body.requiredElementContent().iterator().next();
            } else if(Soap12Util.NS_URI_SOAP12.equals(n)) {
                soaped = Soap12Util.NS_URI_SOAP12;
                // extract body content
                XmlElement body = incomingXml.element(Soap12Util.SOAP12_NS, "Body");
                incomingXml = (XmlElement) body.requiredElementContent().iterator().next();
            }
        }
        
        ctx.setIncomingMessage(incomingXml);
        //XmlDocument xmlRes = processXml(xmlReq);
        
        XmlElement outgoingXml = null;
        
        OutputStream os = res.getOutputStream();
        String charset = "UTF-8";
        res.setContentType("text/xml");
        
        if(process(ctx)) { // actual processing
            outgoingXml = ctx.getOutgoingMessage();
            //TODO!!!!!!!!!!!!: handle faults (message.name()=="Fault" to send HTTP 500
            
            
            if(outgoingXml != null) {
                if(outgoingXml.getParent() == null) {
                    if(soaped != null) {
                        // wrap message in SOAP Envelope as it is not yet soap-ified :-)
                        XmlDocument doc = null;
                        SoapUtil soapUtil;
                        if(soaped == Soap11Util.NS_URI_SOAP11) {
                            soapUtil = Soap11Util.getInstance();
                        } else if(soaped == Soap12Util.NS_URI_SOAP12) {
                            soapUtil = Soap12Util.getInstance();
                        } else {
                            throw new IllegalStateException("unsupported SOAP "+soaped);
                        }
                        doc = soapUtil.wrapBodyContent(outgoingXml);
                        builder.serializeToOutputStream(doc, os, charset);
                    } else {
                        builder.serializeToOutputStream(outgoingXml, os, charset); //check efficiency!
                    }
                    
                } else {
                    XmlContainer top = outgoingXml.getRoot();
                    builder.serializeToOutputStream(top, os, charset);
                }
                
            }
            
        } else {
            final String msg = "could not find service to process message";
            //res.setStatus(500, msg);
            XmlDocument doc = null;
            SoapUtil soapUtil;
            if(soaped == Soap11Util.NS_URI_SOAP11) {
                soapUtil = Soap11Util.getInstance();
            } else if(soaped == Soap12Util.NS_URI_SOAP12) {
                soapUtil = Soap12Util.getInstance();
            } else {
                throw new MessageRouterException(msg);
            }
            doc = soapUtil.wrapBodyContent(
                soapUtil.generateSoapServerFault(msg, null));
            builder.serializeToOutputStream(doc, os, charset);
            
        }
        
        
        try {
            os.close();
        } catch (IOException e) {}
        
    }
    
    
    
    //    /** Nover call this method directly instead register your own messaging */
    //    public XmlDocument processXml(XmlDocument input)
    //        throws DynamicInfosetProcessorException
    //    {
    //        throw new DynamicInfosetProcessorException("this method should never be called!!!!");
    //    }
    
    private class RouterServlet extends HttpMiniServlet {
        public void service(HttpServerRequest req, HttpServerResponse res)
            throws HttpServerException
        {
            HttpMessageRouter.this.service(req, res);
        }
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





