/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XServiceBase.java,v 1.18 2006/08/29 18:35:56 aslom Exp $
 */
package xsul.xservo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.message_router.MessageContext;
import xsul.message_router.MessageProcessingException;
import xsul.message_router.MessageRouterException;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.util.FastUUIDGen;
import xsul.ws_addressing.WsAddressing;
import xsul.ws_addressing.WsaEndpointReference;
import xsul.ws_addressing.WsaInvoker;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.ws_addressing.WsaRelatesTo;
import xsul.ws_addressing_xwsdl.WsaWsdlUtil;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlService;
import xsul.xhandler.XHandler;
import xsul.xhandler.XHandlerContext;
import xsul.xservo.XService;

/**
 * Base class to help to build XService implmentations.
 */
public abstract class XServiceBase implements XService {
    
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private Object impl;
    private String name;
    private WsdlDefinitions wsdlDefs;
    
    private String wsdlLoc;
    private boolean serviceStarted;
    private boolean serviceShutdown;
    private List chainOfHandlers = new ArrayList();
    private XHandlerContext handlerContext;
    
    private WsdlPort wsdlPort;
    
    
    public XServiceBase(String name) {
        this.name = name;
    }
    
    public XService addHandler(XHandler handler) {
        if(handlerContext == null) {
            //            throw new IllegalStateException(
            logger.warning(
                "missing service context: make sure to set WSDL etc.");
        }
        chainOfHandlers.add(handler);
        handler.init(handlerContext);
        return this;
    }
    
    public void setServiceShutdown(boolean serviceShutdown) {
        this.serviceShutdown = serviceShutdown;
    }
    
    public boolean isServiceShutdown() {
        return serviceShutdown;
    }
    
    public void setServiceStarted(boolean serviceStart) {
        this.serviceStarted = serviceStart;
    }
    
    public boolean isServiceStarted() {
        return serviceStarted;
    }
    
    public String getName() {
        return name;
    }
    
    public WsdlDefinitions getWsdl() {
        return wsdlDefs;
    }
    
    public void useWsdl(WsdlDefinitions defs) {
        this.wsdlDefs = defs;
        WsdlService wsdlServ = (WsdlService) defs.getServices().iterator().next();
        wsdlPort = (WsdlPort) wsdlServ.getPorts().iterator().next();
        handlerContext = new XHandlerContext(wsdlPort);
    }
    
    public String getWsdlLocationToUse() {
        return wsdlLoc;
    }
    
    public void useWsdlFromLocation(String wsdlLoc) {
        this.wsdlLoc = wsdlLoc;
    }
    
    public void useWsdlPort(QName portTypeQname, Class javaInterface) {
        throw new IllegalStateException("not implemented yet");
    }
    
    public Object getServiceImpl() {
        return this.impl;
    }
    
    public void useServiceImpl(Object impl) {
        this.impl = impl;
    }
    
    
    
    public void startService() throws MessageRouterException {
        if(handlerContext == null) {
            //            throw new IllegalStateException(
            logger.warning(
                "missing service context: make sure to set WSDL etc.");
        }
        if(isServiceShutdown()) {
            throw new MessageRouterException("service is not available");
        }
        if(isServiceStarted()) {
            throw new MessageRouterException("service is already started");
        }
        setServiceStarted(true);
    }
    
    public void stopService() throws MessageRouterException {
        setServiceStarted(false);
    }
    
    public void shutdownService() throws MessageRouterException {
        setServiceShutdown(true);
        for (int i =  chainOfHandlers.size() - 1; i >= 0; i--) {
            XHandler xh = (XHandler) chainOfHandlers.get(i);
            xh.done(handlerContext);
        }
    }
    
    public abstract void invoke(MessageContext ctx) throws MessageProcessingException;
    
    public boolean process(final MessageContext ctx) throws MessageProcessingException {
        if(isServiceShutdown()) {
            throw new MessageRouterException("service is not available");
        }
        if(!isServiceStarted()) {
            throw new MessageRouterException("service is not started");
        }
        
        XmlElement incomingMsg = ctx.getIncomingMessage();
        XmlElement incomingSoapEnvEl = ctx.getIncomingEnvelope();
        //        XmlContainer incomingDocumentEl = incomingMsg.getRoot();
        //        XmlElement incomingSoapEnvEl = null;
        //        final XmlDocument incomingSoapEnvDoc;
        //        if(incomingDocumentEl instanceof XmlElement) {
        //            incomingSoapEnvEl = (XmlElement)incomingDocumentEl;
        //            incomingSoapEnvDoc = null;
        //        } else if(incomingDocumentEl instanceof XmlDocument) {
        //            incomingSoapEnvDoc = (XmlDocument)incomingDocumentEl;
        //            incomingSoapEnvEl = incomingSoapEnvDoc.getDocumentElement();
        //        } else {
        //            incomingSoapEnvDoc = null;
        //        }
        
        boolean addWsaHeadersToResponse = false;
        final String wsaOutgoingAction;
        {
            // now we make crucial decision if invocaiton will happen synchronously
            // and will block this thread to send response back
            // or we do invocation in a separate thread
            // then this thread will send immedaitely HTTP OK empty
            // and later other thread started will send actual response
            
            // currently decision is based by looking for WSA:ReplyTo header
            // in future it should be an annotation in service metadata!!!!
            
            final WsaMessageInformationHeaders requestWsaHeaders = new WsaMessageInformationHeaders(incomingSoapEnvEl);
            if(requestWsaHeaders.getReplyTo() != null) {
                final WsaEndpointReference replyTo = requestWsaHeaders.getReplyTo();
                final SoapUtil soapUtil = SoapUtil.selectSoapFragrance(incomingSoapEnvEl, new SoapUtil[]{
                            Soap12Util.getInstance(), Soap11Util.getInstance()});
                wsaOutgoingAction = WsaWsdlUtil.determineOutputWsaAction(wsdlPort, incomingMsg);
                if( replyTo.getAddress().equals(WsAddressing.URI_ROLE_ANONYMOUS) ) {
                    addWsaHeadersToResponse = true;
                } else {
                    
                    Runnable task = new Runnable() {
                        public void run() {
                            try {
                                // invoke service
                                XServiceBase.this.runFullInvocationLogicWithHandlers(
                                    XServiceBase.this, chainOfHandlers, ctx);
                                setOutgoingWsaHeaders(ctx, requestWsaHeaders, wsaOutgoingAction);
                                
                                XmlElement outgoingXml = ctx.getOutgoingMessage();
                                
                                XmlDocument responseEnvelope = xsul.util.XsulUtil.getDocumentOutOfElement(
                                    outgoingXml);
                                WsaInvoker invoker = new WsaInvoker();
                                //WsaInvoker invoker = WsaInvoker.getInstance();
                                invoker.sendXml(responseEnvelope);
                            } catch (Exception e) {
                                logger.severe("could not send response to "+requestWsaHeaders.getReplyTo(), e);
                            }
                        }
                        
                    };
                    //TODO use JDK5.Executor
                    // and now we actually have to send response -
                    // this is iblocking operation so should be runs in a separate thread...
                    Thread t = new Thread(task, Thread.currentThread().getName()+"-async");
                    //t.setDaemon(true);
                    
                    // send response asynchronously
                    t.start();
                    
                    //now we are finished
                    return true;
                    
                }
            } else {
                wsaOutgoingAction = null;
            }
        }
        
        runFullInvocationLogicWithHandlers(this, chainOfHandlers, ctx);
        if(addWsaHeadersToResponse && null != ctx.getOutgoingMessage()) {
            final WsaMessageInformationHeaders requestWsaHeaders = new WsaMessageInformationHeaders(incomingSoapEnvEl);
            setOutgoingWsaHeaders(ctx, requestWsaHeaders, wsaOutgoingAction);
        }
        return true;
        
    }
    
    private void setOutgoingWsaHeaders(MessageContext ctx,
                                       WsaMessageInformationHeaders requestWsaHeaders,
                                       String wsaOutgoingAction) {
        XmlElement outgoingXml = ctx.getOutgoingMessage();
        
        XmlDocument responseEnvelope = xsul.util.XsulUtil.getDocumentOutOfElement(
            outgoingXml);
        
        //XmlDocument responseEnvelope = soapUtil.wrapBodyContent(responseMessage);
        // now we need to add all WSA headers etc ...
        WsaMessageInformationHeaders responseWsaHeaders =
            new WsaMessageInformationHeaders(responseEnvelope);
        responseWsaHeaders.explodeEndpointReference(requestWsaHeaders.getReplyTo());
        responseWsaHeaders.setMessageId(URI.create("uuid:"+FastUUIDGen.nextUUID()));
        URI relatedIncomingMessageId = requestWsaHeaders.getMessageId();
        if(relatedIncomingMessageId != null) {
            responseWsaHeaders.addRelatesTo(new WsaRelatesTo(requestWsaHeaders.getMessageId()));
        }
        
        responseWsaHeaders.setAction(URI.create(wsaOutgoingAction));
        
    }
    
    
    // This is static on purpose so it can be called later from other threads!!!!
    public static void runFullInvocationLogicWithHandlers(XServiceBase serviceToInvoke,
                                                          List chainOfHandlers,
                                                          MessageContext ctx) throws MessageProcessingException
    {
        //XmlElement incomingMsg = ctx.getIncomingMessage();
        XmlElement envEl =  ctx.getIncomingEnvelope();;
        //        XmlContainer incomingDocumentEl = incomingMsg.getRoot();
        //        XmlElement envEl = null;
        //        if(incomingDocumentEl instanceof XmlElement) {
        //            envEl = (XmlElement)incomingDocumentEl;
        //        } else if(incomingDocumentEl instanceof XmlDocument) {
        //            envEl = ((XmlDocument)incomingDocumentEl).getDocumentElement();
        //        }
        
        SoapUtil soapUtil = SoapUtil.selectSoapFragrance(envEl, new SoapUtil[]{
                    Soap12Util.getInstance(), Soap11Util.getInstance()});
        
        //try {
        // move the MessageContext "context" section in Header to the newly
        // generated MessageContext ctx. The "context" section was inserted
        // by previous Den processors for passing context information.
        XmlElement soapHeaderEl = envEl.element(null, XmlConstants.S_HEADER);
        if(soapHeaderEl != null) {
            XmlElement transferMsgCtxHeaderEl =
                soapHeaderEl.element(MessageContext.XSUL_CTX_NS, MessageContext.CONTEXT);
            if(transferMsgCtxHeaderEl != null) {
                for (java.util.Iterator iter = transferMsgCtxHeaderEl.children();iter.hasNext();) {
                    Object obj = iter.next();
                    logger.finest("elem classs: " + obj.getClass());
                    if(obj instanceof XmlElement) {
                        XmlElement el = (XmlElement)obj;
                        String name = el.getName();
                        logger.finest("context elem name: " + name);
                        // ignore incoming and direction elements as it's been added
                        if(!name.equals(MessageContext.INCOMING) &&
                               !name.equals(MessageContext.DIRECTION)) {
                            ctx.addChild(obj);
                        }
                    }
                }
                envEl.removeChild(transferMsgCtxHeaderEl);
            }
        }
        
        // pass incoming message through list of global handlers
        
        int lastHandler = -1;
        for(int i = 0; i < chainOfHandlers.size(); ++i) {
            XHandler gloabalHandler = (XHandler) chainOfHandlers.get(i);
            boolean shortCircuit = gloabalHandler.process(ctx);
            if(shortCircuit) {
                lastHandler = i;
                break;
            }
        }
        
        // continue with synchronous response
        
        if(lastHandler == -1) {
            serviceToInvoke.invoke(ctx); //FIXME: do not care about return value?
            lastHandler = chainOfHandlers.size()  - 1;
        }
        XmlElement outMsg = ctx.getOutgoingMessage();
        ctx.setDirection(MessageContext.DIR_OUTGOING);
        if(outMsg !=null) {
            soapUtil.wrapBodyContent(outMsg);
            //and pass outgoing message (if any) back through list of global handlers
            for(int i = lastHandler; i >= 0; --i) {
                XHandler gloabalHandler = (XHandler) chainOfHandlers.get(i);
                boolean shortCircuit = gloabalHandler.process(ctx);
                if(shortCircuit) {
                    //lastHandler = i;
                    //TODO: what exactly should happen here ...
                    break;
                }
            }
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



