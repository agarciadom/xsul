/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/* Copyright (c) 2006 Extreme! Lab, Indiana University. All rights reserved.
 * This software is open source. See the bottom of this file for the licence.
 * $Id: WSIFAsyncSoapHttpDiiWithHandlers.java,v 1.6 2006/08/29 18:25:58 aslom Exp $ */
package xsul.xwsif_runtime;

import java.net.URI;
import java.util.List;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.invoker.DynamicInfosetInvoker;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.soap.SoapDynamicInfosetInvoker;
import xsul.message_router.MessageContext;
import xsul.soap.SoapUtil;
import xsul.soap12_util.Soap12Util;
import xsul.util.FastUUIDGen;
import xsul.util.XsulUtil;
import xsul.ws_addressing.WsAddressing;
import xsul.ws_addressing.WsaEndpointReference;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.xhandler.XHandler;
import xsul.xwsif_runtime_async.WSIFAsyncResponseListener;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;

/**
 * This is dynamic stub implementating WSDL port/portType
 */
public class WSIFAsyncSoapHttpDiiWithHandlers implements SoapDynamicInfosetInvoker
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final List handlers;
    
    private WSIFAsyncResponsesCorrelator correlator;
    
    private long asyncTimeoutInMs;
    
    private SoapDynamicInfosetInvoker invoker;
    
    //private SoapUtil soapFragrance = Soap12Util.getInstance();
    
    public WSIFAsyncSoapHttpDiiWithHandlers(
        final SoapDynamicInfosetInvoker invoker,
        final List handlers,
        final WSIFAsyncResponsesCorrelator correlator,
        final long asyncTimeoutInMs)
    {
        if(invoker == null) throw new IllegalArgumentException();
        if(handlers == null) throw new IllegalArgumentException();
        this.invoker = invoker;
        this.handlers = handlers;
        this.correlator = correlator;
        this.asyncTimeoutInMs = asyncTimeoutInMs;
        
    }

    public String getLocation() {
        return invoker.getLocation();
    }
    
    public void setLocation(String url) {
        invoker.setLocation(url);
    }
        
    public void setSoapAction(String soapAction) {
        invoker.setSoapAction(soapAction);
    }
    
    public void setSoapFragrance(SoapUtil soapFragrance) {
        invoker.setSoapFragrance(soapFragrance);
    }
    
    public XmlDocument wrapAsSoapDocument(XmlElement message) throws DynamicInfosetInvokerException {
        return invoker.wrapAsSoapDocument(message);
    }
    
    public XmlElement extractBodyContent(XmlDocument respDoc) throws DynamicInfosetInvokerException {
        return invoker.extractBodyContent(respDoc);
    }

    public SoapUtil getSoapFragrance() {
        return invoker.getSoapFragrance();
    }
    
    public void setAsyncTimeoutInMs(long asyncTimeoutInMs) {
        this.asyncTimeoutInMs = asyncTimeoutInMs;
    }
    
    public long getAsyncTimeoutInMs() {
        return asyncTimeoutInMs;
    }
    
    public XmlDocument invokeXml(XmlDocument outgoingDoc) throws DynamicInfosetInvokerException {
        final MessageContext ctx = new MessageContext(MessageContext.DIR_OUTGOING);
        
        
        WSIFAsyncResponseListener callback = null;
        Object messageId = null;
        if(correlator != null) {
            messageId = correlator.addCorrelationToOutgoingMessage(outgoingDoc);
            assert (messageId != null);
            callback = new WSIFAsyncResponseListener() {
                private boolean done;
                public void processAsyncResponse(XmlDocument responseDoc) {
                    //XmlElement env = responseDoc.getDocumentElement();
                    XmlElement message = invoker.extractBodyContent(responseDoc);
                    //ctx.setIncomingEnvelope(env);
                    ctx.setIncomingMessage(message);
                    done = true;
                    synchronized(this) {
                        this.notify();
                    }
                }
                public boolean isDone() { return done; }
            };
            correlator.registerCallback(messageId, callback);
        } else {
            if(getSoapFragrance() != null) {
                WsaMessageInformationHeaders wmih = new WsaMessageInformationHeaders(outgoingDoc);
                wmih.setMessageId( URI.create("uuid:"+FastUUIDGen.nextUUID()) );
                if(wmih.getReplyTo() == null) {
                    wmih.setReplyTo(new WsaEndpointReference(WsAddressing.URI_ROLE_ANONYMOUS));
                }
            }
        }
        
        
        {
            XmlElement outgoingMessage;
            if(getSoapFragrance() != null) {
                outgoingMessage = getSoapFragrance().requiredBodyContent(outgoingDoc);
            } else {
                outgoingMessage = outgoingDoc.getDocumentElement();
            }
            ctx.setOutgoingMessage(outgoingMessage);
        }
        int lastHandler = -1;
        for (int i = 0; i < handlers.size(); i++) {
            XHandler handler  = (XHandler) handlers.get(i);
            if(handler.process(ctx)) {
                lastHandler = i;
                break;
            }
        }
        outgoingDoc = XsulUtil.getDocumentOutOfElement(ctx.getOutgoingMessage());
        
        // do actual invocation
        XmlDocument incomingDoc = null;
        if(lastHandler == -1) {
            incomingDoc = invoker.invokeXml(outgoingDoc);
            if(lastHandler == -1) {
                lastHandler = handlers.size() - 1;
            }
        }
        if(incomingDoc == null && callback != null) {
            // here we can do actual blocking for response;
            
            long endTime = 0;
            if(asyncTimeoutInMs > 0) {
                endTime = System.currentTimeMillis() + asyncTimeoutInMs;
            }
            while(!callback.isDone()) {
                try {
                    long timeToWait = 0;
                    if(asyncTimeoutInMs > 0) {
                        timeToWait = endTime - System.currentTimeMillis();
                        if(timeToWait <= 0) {
                            throw new DynamicInfosetInvokerException(
                                "no async response message received in "+asyncTimeoutInMs+" milliseconds");
                        }
                    }
                    synchronized(callback) {
                        callback.wait(timeToWait); //note 0 = infinity
                    }
                } catch (InterruptedException e) {
                }
            }
            messageId = null; // no longer needed -- callback was delivered
            //incomingDoc = (XmlDocument) ctx.getIncomingEnvelope().getParent();
            incomingDoc = ctx.getIncomingEnvelopeDoc();
        }
        if(incomingDoc != null) {
            if(correlator != null && messageId != null) {
                correlator.unregisterCallback(null);
            }
            {
                ctx.setDirection(MessageContext.DIR_INCOMING);
                XmlElement incomingMessage;
                if(getSoapFragrance() != null) {
                    incomingMessage = getSoapFragrance().requiredBodyContent(incomingDoc);
                } else {
                    incomingMessage = incomingDoc.getDocumentElement();
                }
                ctx.setIncomingMessage(incomingMessage);
            }
            for (int i = 0; i < handlers.size(); i++) {
                XHandler handler  = (XHandler) handlers.get(i);
                if(handler.process(ctx)) {
                    break;
                }
            }
            incomingDoc = XsulUtil.getDocumentOutOfElement(ctx.getIncomingMessage());
            
        }
        return incomingDoc;
    }
    
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2006 The Trustees of Indiana University. All rights
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



