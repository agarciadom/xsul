/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * based on xsul_dii.XsulDynamicInvoker.java,v 1.8 2005/01/18 10:02:38 aslom Exp $
 */

package xsul.xhandler_soap_sticky_header;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.message_router.MessageContext;
import xsul.xhandler.BaseHandler;
import xsul.xhandler.XHandlerContext;

/**
 * This is very simple handler that can extract given SOAP header from icoming message
 * and put it into thread local context (accessible as StickySoapHeaderHandler.getHeaderAs())
 * and will set set some header for all outgoing messages (and clears this thread local context
 * for outgoing message to avoid memory leaking).
 */
public class StickySoapHeaderHandler extends BaseHandler {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static ThreadLocal tl = new ThreadLocal();
    
    private XmlElement defaultHeaderToUse;
    
    private QName headerQName;
    private XmlNamespace headerNs;
    
    public StickySoapHeaderHandler(String name, QName headerQName) {
        super(name);
        if(headerQName == null) throw new IllegalArgumentException();
        this.headerQName = headerQName;
        this.headerNs = builder.newNamespace(headerQName.getNamespaceURI());
    }
    
    /**
     * This header will be used (this is immutable!)
     */
    public StickySoapHeaderHandler(String name, XmlElement headerToUse) {
        super(name);
        if(headerToUse == null) throw new IllegalArgumentException();
        this.defaultHeaderToUse = headerToUse;
        this.headerQName = new QName(headerToUse.getName(), headerToUse.getNamespaceName());
        this.headerNs = headerToUse.getNamespace();
    }

    public static XmlElement getHeader(QName headerQName) {
            Object value = tl.get();
        if(value == null) {
            return null;
        }
        Map map = (Map) value;
        XmlElement header = null;
        synchronized(map) {
            header = (XmlElement) map.get(headerQName);
        }
        return header;
    }
    
    public static XmlElementAdapter getHeaderAs(Class headerType, QName headerQName) {
        XmlElement header = getHeader(headerQName);
        XmlElementAdapter adapted = XmlElementAdapter.castOrWrap(header, headerType);
        return adapted;
    }
    
    public void init(XHandlerContext handlerConfig) {
        super.init(handlerConfig);
    }
    
    public boolean processOutgoingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException
    {
        // first set header (if any!)
        XmlElement headerToUse = getHeader(this.headerQName);
        // garbage collection
        tl.set(null);
        if(headerToUse == null) {
            headerToUse = defaultHeaderToUse;
        }
        if(headerToUse != null && soapEnvelope.getName().equals(XmlConstants.S_ENVELOPE)) {
            XmlElement headerCopy;
            try {
                headerCopy = (XmlElement) headerToUse.clone();
            } catch (CloneNotSupportedException e) {
                throw new DynamicInfosetInvokerException("could not clone");
            }
            XmlElement soapHeader = soapEnvelope.element(null, XmlConstants.S_HEADER);
            if(soapHeader == null) { // needs to add SOAP header to envelope
                soapHeader = soapEnvelope.newElement(soapEnvelope.getNamespace(), XmlConstants.S_HEADER);
                soapEnvelope.addElement(0, soapHeader);
            }
            soapHeader.addElement(headerCopy);
        }
        return false;
    }
    
    public boolean processIncomingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException
    {
        // set header ot use form incoming SOAP packet *only* if it is not already set
        XmlElement headerToUse = getHeader(this.headerQName);
        if(headerToUse == null) {
            XmlElement soapHeader = soapEnvelope.element(null, XmlConstants.S_HEADER);
            if(soapHeader != null) {
                XmlElement headerValue = soapHeader.element(
                    headerNs, headerQName.getLocalPart());
                if(headerValue != null) {
                    try {
                        headerToUse = (XmlElement) headerValue.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new DynamicInfosetInvokerException(
                            "could not clone soap header "+headerValue, e);
                    }
                }
            }
        }
        // now set header in context -- if there is any header ...
        if(headerToUse != null) {
            Object value = tl.get();
            Map map;
            if(value != null) {
                map = (Map) value;
            } else {
                map = new HashMap();
                tl.set(map);
            }
            
            synchronized(map) {
                map.put(headerQName, headerToUse);
            }
            
        }
        return false;
    }
}


