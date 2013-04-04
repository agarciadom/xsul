/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: BaseHandler.java,v 1.5 2005/02/01 00:46:43 lifang Exp $
 */
package xsul.xhandler;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.message_router.MessageContext;
import xsul.message_router.MessageProcessingException;

/**
 * Base class to simplify writing handlers: simply override processIncoming|OutgoingXml method
 */

public class BaseHandler implements XHandler {
    
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private String name;
    private boolean handlerDisabled = true;
    private boolean initialized;
    private boolean finished;

    public BaseHandler(String name) {
        this.name = name;
    }
    
    public void init(XHandlerContext handlerConfig) {
        if(initialized) {
            throw new IllegalStateException("handler was alredy initialized");
        }
        if(finished) {
            throw new IllegalStateException("handler can not be re-initialized");
        }
        initialized = true;
    }
    
    public void done(XHandlerContext handlerConfig) {
        if(!initialized) {
            throw new IllegalStateException("handler that is finished but not initialized?!");
        }
        if(finished) {
            throw new IllegalStateException("handler is already finished");
        }
        finished = true;
    }
    
    public boolean processOutgoingXml(XmlElement message, MessageContext context)
        throws DynamicInfosetInvokerException
    {
        return false;
    }
    
    public boolean processIncomingXml(XmlElement message, MessageContext context)
        throws DynamicInfosetInvokerException
    {
        return false;
    }
    
    public boolean process(MessageContext context) throws MessageProcessingException {
        String direction = context.getDirection();
        boolean incomingDir = direction.equals(MessageContext.DIR_INCOMING);
        XmlElement el;
        if(incomingDir) {
            el = context.getIncomingMessage();
        } else {
            el = context.getOutgoingMessage();
        }
        XmlContainer container = el.getRoot();//little glitch in impl
        if(container == null || container == el) {
            XmlContainer parent  = el.getParent();
            if(parent  != null && parent  instanceof XmlElement) {
                container = ((XmlElement) parent ).getRoot();
            }
        }
        XmlElement message;
        if(container instanceof XmlDocument) {
            XmlDocument doc = (XmlDocument) container;
            message = doc.getDocumentElement();
        } else if(container instanceof XmlElement) {
            //doc = builder.newDocument();
            //doc.setDocumentElement((XmlElement) container);
            XmlDocument doc = builder.newDocument("1.0", Boolean.TRUE, "UTF-8");
            message = (XmlElement) container;
            doc.setDocumentElement(message);
            
        } else {
            throw new MessageProcessingException("unknown type of containeir "+container.getClass());
        }
        if(incomingDir) {
            return processIncomingXml(message, context);
        } else {
            return processOutgoingXml(message, context);
        }
        
    }
    
    public String getName() {
        return name;
    }
    
    public void setHandlerDisabled(boolean handlerDisabled) {
        this.handlerDisabled = handlerDisabled;
    }
    
    public boolean isHandlerDisabled() {
        return handlerDisabled;
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


