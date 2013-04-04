/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpBasedServices.java,v 1.21 2006/08/29 18:25:58 aslom Exp $
 */
package xsul.xservo_soap_http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.parser_pool.XmlPullParserPool;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.XsulVersion;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;
import xsul.http_server.ServerSocketFactory;
import xsul.message_router.MessageRouterException;
import xsul.message_router_over_http.HttpMessageContext;
import xsul.monitoring.XsulMonitoringStats;
import xsul.monitoring.XsulMonitoringUtil;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.util.Utf8Reader;
import xsul.util.Utf8Writer;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlService;
import xsul.wsdl.WsdlUtil;
import xsul.xservo.XService;
import xsul.xservo.XServiceServo;

/**
 * This class provides services over HTTP.
 */
public class HttpBasedServices implements XServiceServo {
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static final XmlPullParserPool pool = new XmlPullParserPool(builder.getFactory());
    private static final String WSDL_SUFFIX = "?wsdl";
    
    private HttpMiniServer server;
    private HttpMiniServlet servlet;
    private int serverPort;
    //private ServerSocketFactory serverSocketFactory;
    private long startTime = System.currentTimeMillis() - 1; //defensive programming ot avoid divide-per-zero
    private long requestXmlMsgCount;
    private long numberOfConnections;
    private String serverName = XsulVersion.getUserAgent();
    
    private Map services = new TreeMap();
    
    public HttpBasedServices(int tcpPort) throws DynamicInfosetProcessorException  {
        setServerPort(tcpPort);
        init();
    }
    
    public HttpBasedServices(ServerSocketFactory serverSocketFactory)
        throws DynamicInfosetProcessorException
    {
        this.server = new HttpMiniServer(serverSocketFactory);
        init();
    }
    
    public HttpBasedServices(HttpMiniServer server)
        throws DynamicInfosetProcessorException
    {
        this.server = server;
        init();
    }
    
    private void init() {
        try {
            start();
        } catch (IOException e) {
            throw new DynamicInfosetProcessorException("could not start server", e);
        }
    }
    
    
    public void setServerName(String serverName) {
        if(serverName == null) throw new IllegalArgumentException("null");
        if(serverName.length() == 0) throw new IllegalArgumentException("empty");
        this.serverName = serverName;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    //    public void addGlobalHandler(XHandler handler) {
    //        //globalHandlers.add(handler);
    //    }
    
    public XService addService(XService service) {
        String name = service.getName();
        if(name == null) throw new IllegalArgumentException();
        if(services.get(name) != null) {
            throw new XsulException("there is already service with name "+name);
        }
        String serviceLoc = getServer().getLocation() + "/"+ name;
        WsdlDefinitions def = service.getWsdl();
        if(def != null) {
            // override location
            WsdlService wsdlService = (WsdlService) def.getServices().iterator().next();
            // now override port --- this needs lot of refinement to select righr port!!!!
            WsdlPort wsdlPort = (WsdlPort) wsdlService.getPorts().iterator().next();
            WsdlUtil.replaceWsdlSoapAddr(wsdlPort, serviceLoc);
        }
        services.put(name, service);
        service.useWsdlFromLocation(getServiceWsdl(service.getName()));
        return service;
    }
    
    public String getServiceWsdl(String serviceName) {
        //return httpServices.getServer().getLocation() + "/"+SERVICE_NAME+"?wsdl";
        if(serviceName == null) throw new IllegalArgumentException();
        if(services.get(serviceName) == null) {
            throw new XsulException("there is no service with name "+serviceName);
        }
        String loc = getServer().getLocation() + "/"+serviceName+WSDL_SUFFIX;
        return loc;
    }
        
    //    public XService newService(String name, String wsdlLoc, Object serviceImpl) {
    //
    //        WsdlDefinitions def = WsdlResolver.getInstance().loadWsdlFromPath(serviceImpl.getClass(), wsdlLoc);
    //        XService service = new XSoapServices(name);
    //        service.useWsdl(def);
    //        service.useServiceImpl(serviceImpl);
    //        //TODO: attach service to HTTP server servlet dispatcher under "name"
    //        service.startService();
    //        return service;
    //    }
    
    public void setServerPort(int tcpPort) throws DynamicInfosetProcessorException {
        if(tcpPort < 0 || tcpPort > 65535) {
            throw new IllegalArgumentException("TCP port must be between 0 and 65535");
        }
        this.serverPort = tcpPort;
    }
    
    public int getServerPort() {
        if(server != null) {
            return server.getServerPort();
        } else {
            return this.serverPort;
        }
    }
    
    public HttpMiniServer getServer() { return server; }
    
    public void setServer(HttpMiniServer server) throws DynamicInfosetProcessorException {
        if(this.server != null) {
            throw new DynamicInfosetProcessorException("server is already set");
        }
        this.server = server;
    }
    
    public void start() throws IOException, DynamicInfosetProcessorException {
        if(server == null) {
            server= new HttpMiniServer(serverPort);
        }
        if(servlet == null) {
            servlet= new HdisServlet();
        }
        
        server.useServlet(servlet);
        
        if(!server.isRunning()) {
            server.startServer();
        }
    }
    
    public void stop() throws DynamicInfosetProcessorException {
        if(server == null) {
            throw new DynamicInfosetProcessorException("server HTTP processor was not started");
        }
        server.stopServer();
    }
    
    public void shutdown() throws DynamicInfosetProcessorException {
        if(server == null) {
            throw new DynamicInfosetProcessorException("server HTTP processor was not started");
        }
        server.shutdownServer();
    }
    
    // allow extending so for example HTTP Basic Auth check can be added easily!
    public void service(HttpServerRequest req, HttpServerResponse res)
        throws HttpServerException
    {
        ++numberOfConnections;
        final String UTF8 = "utf-8";
        String contentType = req.getContentType();
        String method = req.getMethod();
        if(method.equals("POST")) {
            if(contentType != null && contentType.indexOf("xml") != -1) {
                ++requestXmlMsgCount;
                serviceXml(req, res);
                return;
            } else {
                throw new HttpServerException(
                    "could not process POST - unsupported Content-type '"+contentType+"'");
            }
        }
        String path = req.getPath();
        if(method.equals("GET")) {
            int posWsdl = -1;
            if((posWsdl = path.indexOf(WSDL_SUFFIX)) != -1) {
                int lastSlash = path.lastIndexOf("/");
                String name = path.substring(lastSlash+1, posWsdl);
                XService service = (XService) services.get(name);
                if(service != null) {
                    // OK now we have WSDL
                    WsdlDefinitions  def = service.getWsdl();
                    XmlDocument outgoingDoc = builder.newDocument("1.0", Boolean.TRUE, UTF8);
                    outgoingDoc.setDocumentElement(def);
                    writeXmlDoc(res, UTF8, outgoingDoc);
                } else {
                    throw new HttpServerException("could not find service named '"+name+"'");
                }
            } else {
                
                // print some monitoring statistics
                // alive, uptime, #msgs, average ($msgs/second)
                XsulMonitoringStats s = XsulMonitoringUtil.createStats(
                    serverName, startTime, requestXmlMsgCount, numberOfConnections);
              

                XmlDocument xhtmlDoc =  XsulMonitoringUtil.createXhtmlStatsDoc(s, UTF8);
                XmlElement htmlBody = xhtmlDoc.getDocumentElement().requiredElement(null, "body");
                XmlNamespace htmlNs = htmlBody.getNamespace();
                XmlElement p = htmlBody.addElement(htmlNs, "p");
                p.addChild("Services WSDL: ");
                // add list of services
                for(Iterator i = services.keySet().iterator(); i.hasNext(); ) {
                    String serviceName = (String) i.next();
                    String serviceWsdl = getServiceWsdl(serviceName);
                    XmlElement a = p.addElement("a");
                    a.addAttribute("href", serviceWsdl);
                    a.addChild(serviceName);
                    //a.addChild(" WSDL");
                    p.addChild(" ");
                }
                        
                //                XmlDocument outgoingDoc = builder.newDocument("1.0", Boolean.TRUE, UTF8);
                //                XmlElement xhtml = XsulMonitoringUtil.createXhtmlStats(s);
                //                outgoingDoc.setDocumentElement(xhtml);
                writeXmlDoc(res, UTF8, xhtmlDoc);
            }
            
        } else {
            throw new HttpServerException("could not process GET '"+path+"'");
        }
    }
    
    
    public void serviceXml(HttpServerRequest req, HttpServerResponse res)
        throws HttpServerException
    {
        final String UTF8 = "utf-8";
        if(req.getCharset() == null) {
            throw new HttpServerException("content type encoding is required");
        }
        String encoding = req.getCharset();
        InputStream is = req.getInputStream();
        
        XmlDocument incomingXmlDoc;
        XmlPullParser pp;
        try {
            pp = pool.getPullParserFromPool();
        } catch(XmlPullParserException e) {
            throw new HttpServerException("could not get XML pull parser from the pool", e);
        }
        Reader reader = null;
        try {
            
            try {
                String enc = encoding.toLowerCase();
                if(UTF8.equals(enc) || "utf8".equals(enc))  {
                    reader = new Utf8Reader(is, 8*1024);
                    pp.setInput(reader);
                } else {
                    pp.setInput(is, encoding);
                }
            } catch(XmlPullParserException e) {
                throw new HttpServerException("could not set parser input", e);
            }
            incomingXmlDoc = builder.parse(pp);
            
        } finally {
            pool.returnPullParserToPool(pp);
        }
        
        
        HttpMessageContext ctx = new HttpMessageContext(req);
        
        XmlElement incomingDocumentEl = incomingXmlDoc.getDocumentElement();
        //String soaped = null;
        SoapUtil soapUtil = SoapUtil.selectSoapFragrance(incomingDocumentEl, new SoapUtil[]{
                    Soap12Util.getInstance(), Soap11Util.getInstance()});
        XmlElement incomingXml = soapUtil.requiredBodyContent(incomingDocumentEl);
        
        ctx.setIncomingMessage(incomingXml);
        
        
        XmlDocument outgoingDoc = null;
        
        try {
            XsulMonitoringStats stats = XsulMonitoringUtil.createStats(
                serverName, startTime, requestXmlMsgCount, numberOfConnections);
            outgoingDoc = XsulMonitoringUtil.processMonitoringRequest(
                stats,
                incomingXml, soapUtil); // support "ping" method!
            
            if(outgoingDoc == null) {
                invoke(ctx, soapUtil);
            }
            
            if(outgoingDoc == null) {
                XmlElement outgoingXml = ctx.getOutgoingMessage();
                
                if(outgoingXml != null) {
                    outgoingDoc = xsul.util.XsulUtil.getDocumentOutOfElement(outgoingXml);
                }
            }
            
        } catch(Exception ex) {
            outgoingDoc = soapUtil.wrapBodyContent(
                soapUtil.generateSoapServerFault("service failed: "+ex.getMessage(), ex));
        } finally {
            
            try {
                if(reader != null) {
                    reader.close();
                } else {
                    is.close();
                }
            } catch (IOException e) {
                throw new HttpServerException("could not close request input stream", e);
            }
        }
        
        if(outgoingDoc != null) {
            writeXmlDoc(res, UTF8, outgoingDoc);
        } else {
            //res.setContentType(
            res.setStatusCode("202");
            res.setReasonPhrase("OK");
        }
        //throw new HttpServerException("not implemented");
        
        
    }
    
    private void writeXmlDoc(HttpServerResponse res, final String UTF8, XmlDocument outgoingDoc)
        throws XmlBuilderException, HttpServerException
    {
        OutputStream os = res.getOutputStream();
        //String charset = "UTF-8";
        String charset = UTF8; //"utf-8";
        res.setContentType(charset);
        res.setContentType("text/xml");
        //builder.serializeToOutputStream(xmlRes, os, charset); //check efficiency!
        Writer u8w = new Utf8Writer(os, 8*1024);
        serializeXmlResponse(outgoingDoc, u8w);
        try {
            u8w.flush();
        } catch (IOException e) {}
        
        try {
            u8w.close();
        } catch (IOException e) {
            throw new HttpServerException("problem when serializing XML result to "+charset);
        }
    }
    
    private void invoke(HttpMessageContext ctx, SoapUtil soapUtil) throws MessageRouterException {
        String path = ctx.getHttpRequestPath();
        if(path.indexOf("/") == 0) {
            path = path.substring(1);
        }
        int slashPos = path.indexOf("/");
        String name;
        if(slashPos != -1) {
            name = path.substring(0, slashPos);
        } else {
            name = path;
        }
        XService service = (XService) services.get(name);
        
        if(service == null) {
            
            // fixme: a hack by liang. may have some better way to do it?
            logger.warning("no service named '"+name+"' was found.");
            logger.warning("look for dispatcher");
            service = (XService) services.get("dispatcher");
            if(service == null) {
                throw new MessageRouterException("no service named '"+name+"' was found");
            }
            else {
                logger.finest("found dispatcher service");
            }
        }
        
        service.process(ctx);
        
        //        // pass incoming message through list of global handlers
        //
        //        int lastHandler = -1;
        //        for(int i = 0; i < globalHandlers.size(); ++i) {
        //            XHandler gloabalHandler = (XHandler) globalHandlers.get(i);
        //            boolean shortCircuit = gloabalHandler.process(ctx);
        //            if(shortCircuit) {
        //                lastHandler = i;
        //                break;
        //            }
        //        }
        //        if(lastHandler == -1) {
        //            service.process(ctx); //FIXME: do not care about return value?
        //            lastHandler = globalHandlers.size()  - 1;
        //        }
        //        soapUtil.wrapBodyContent(ctx.getOutgoingMessage());
        //        ctx.setDirection(MessageContext.DIR_OUTGOING);
        //        //and pass outgoing message (if any) back through list of global handlers
        //        for(int i = lastHandler; i >= 0; --i) {
        //            XHandler gloabalHandler = (XHandler) globalHandlers.get(i);
        //            boolean shortCircuit = gloabalHandler.process(ctx);
        //            if(shortCircuit) {
        //                //lastHandler = i;
        //                //TODO: what exactly should happen here ...
        //                break;
        //            }
        //        }
    }
    
    
    //    private XmlDocument processSoapEnvelope(XmlElement envelope, final SoapUtil soapFragrance)
    //        throws DynamicInfosetProcessorException
    //    {
    //        // concert envelope to String
    //        //                System.err.println(getClass().getName()+" received envelope="
    //        //                                       +builder.serializeToString(envelope));
    //        logger.finest("received envelope="+builder.serializeToString(envelope));
    //        // this XML string could be convertedto DOM ot whatever API one preferes (like JDOM, DOM4J, ...)
    //
    //        XmlElement soapHeader = envelope.element(null, "Header");
    //        //String location = getServer().getLocation();
    //        final WsaMessageInformationHeaders requestWsaHeaders;
    //        if(soapHeader != null) {
    //            //throw new XsulException("SOAP message must have headers");
    //
    //            requestWsaHeaders = new WsaMessageInformationHeaders(envelope);
    //            //System.err.println(getClass().getName()+" message destinaiton="+wsah.getTo());
    //            //assertEquals(location, wsah.getTo().toString());
    //            //      if(!location.equals(wsah.getTo().toString())) {
    //            //          throw new IllegalStateException();
    //            //      }
    //        } else {
    //            requestWsaHeaders = null;
    //        }
    //        final XmlElement message = soapFragrance.requiredBodyContent(envelope);
    //
    //        XmlElement responseMessage;
    //        XmlElement fault;
    //        try {
    //            responseMessage = null;//processMessage(message);
    //            fault = null;
    //        } catch (Exception e) {
    //            fault = soapFragrance.generateSoapClientFault("could not process: "+e.getMessage(), e);
    //            responseMessage = fault;
    //            //return soapFragrance.wrapBodyContent(fault);
    //        }
    //
    //        if(responseMessage == null) {
    //            // no response needed -- method wants to be one-way
    //            return null;
    //        }
    //
    //        //TODO: use getFaultTo if there was fault
    //        if(requestWsaHeaders != null) {
    //            if(requestWsaHeaders.getReplyTo() != null) {
    //                if(requestWsaHeaders.getReplyTo().getAddress().equals(WsAddressing.URI_ROLE_ANONYMOUS)) {
    //                    //to send back response wrapped in SOAP envelope
    //                    XmlDocument responseEnvelope = soapFragrance.wrapBodyContent(responseMessage);
    //                    WsaMessageInformationHeaders responseWsaHeaders =
    //                        new WsaMessageInformationHeaders(responseEnvelope);
    //                    responseWsaHeaders.setMessageId(URI.create(FastUUIDGen.nextUUID()));
    //                    responseWsaHeaders.explodeEndpointReference(requestWsaHeaders.getReplyTo());
    //                    URI messageId = requestWsaHeaders.getMessageId();
    //                    if(messageId != null) {
    //                        responseWsaHeaders.addRelatesTo(new WsaRelatesTo(requestWsaHeaders.getMessageId()));
    //                    }
    //
    //                    return responseEnvelope;
    //
    //                } else {
    //                    asyncSendResponse(soapFragrance, responseMessage, requestWsaHeaders);
    //                    // no response sent - actual response will be sent over new connection
    //                    return null;
    //                }
    //            } else {
    //                // no response needed
    //                return null;
    //            }
    //        } else {
    //            // should we add WSA stuff even if it was not in request?!
    //            XmlDocument responseEnvelope = soapFragrance.wrapBodyContent(responseMessage);
    //            return responseEnvelope;
    //        }
    //    }
    //
    //    private void asyncSendResponse(final SoapUtil soapFragrance,
    //                                   final XmlElement responseMessage,
    //                                   final WsaMessageInformationHeaders requestWsaHeaders) {
    //        Runnable r = new Runnable() {
    //            public void run() {
    //                try {
    //                    XmlDocument responseEnvelope = soapFragrance.wrapBodyContent(responseMessage);
    //                    // now we need to add all WSA headers etc ...
    //                    WsaMessageInformationHeaders responseWsaHeaders =
    //                        new WsaMessageInformationHeaders(responseEnvelope);
    //                    responseWsaHeaders.explodeEndpointReference(requestWsaHeaders.getReplyTo());
    //                    URI messageId = requestWsaHeaders.getMessageId();
    //                    if(messageId != null) {
    //                        responseWsaHeaders.addRelatesTo(new WsaRelatesTo(requestWsaHeaders.getMessageId()));
    //                    }
    //                    WsaInvoker invoker = new WsaInvoker();
    //                    //invoker.setDefaultAction(URI.create(MESSAGE_URI+"Response")); //TODO use WSDL!!!!
    //                    invoker.sendXml(responseEnvelope);
    //                } catch (Exception e) {
    //                    logger.finest("could not send response to "+requestWsaHeaders.getReplyTo(), e);
    //                }
    //            }
    //        };
    //        //LATER: use Executor / ThreadPool
    //        new Thread(r).start();
    //    }
    //
    //
    private void serializeXmlResponse(XmlDocument xmlRes, Writer writer) throws XmlBuilderException {
        //builder.serializeToWriter(xmlRes, u8w);
        //builder.serializeToWriter(input, utf8Writer);
        XmlSerializer ser = null;
        try {
            ser = builder.getFactory().newSerializer();
            ser.setOutput(writer);
            final String SERIALIZER_ATTVALUE_USE_APOSTROPHE =
                "http://xmlpull.org/v1/doc/features.html#serializer-attvalue-use-apostrophe";
            ser.setFeature(SERIALIZER_ATTVALUE_USE_APOSTROPHE, true);
        } catch (Exception e) {
            throw new XmlBuilderException("could not serialize node to writer", e);
        }
        serializeXmlResponse(xmlRes, ser);
        try {
            ser.flush();
        } catch (IOException e) {
            throw new XmlBuilderException("could not flush output", e);
        }
    }
    
    private void serializeXmlResponse(XmlDocument xmlRes, XmlSerializer ser) {
        builder.serialize(xmlRes, ser);
    }
    
    /**
     * Plumbing as in Java class can not extend two classes ...
     */
    private class HdisServlet extends HttpMiniServlet {
        public void service(HttpServerRequest req, HttpServerResponse res)
            throws HttpServerException
        {   // NOTE: required casts for "this" to enclosing class !!!!
            HttpBasedServices.this.service(req, res);
        }
    }
}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 *
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 *
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 *
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 *
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */



