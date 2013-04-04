/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: MessageContext.java,v 1.10 2006/08/29 18:25:57 aslom Exp $
 */

package xsul.message_router;

import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;

/**
 * XML-ish representation of contextual information about received XML message.
 *
 * @version $Revision: 1.10 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class MessageContext extends XmlElementAdapter {
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static String CONTEXT =  "context";
    public final static String INCOMING = "incoming"; //TOOD check NAMES and directions!!!!!
    public final static String MESSAGE = "message";
    public final static String ENVELOPE = "envelope";
    public final static String OUTGOING = "outgoing";
    public final static String DIRECTION = "direction";
    public final static String DIR_INCOMING = "incoming-direction";
    public final static String DIR_OUTGOING = "outgoing-direction";
    
    public final static XmlNamespace XSUL_CTX_NS =
        builder.newNamespace("http://www.extreme.indiana.edu/xgws/xsul/ctx");
    //XmlElement incomingMessage;
    // public final static String XSUL_CTX_NS = ;
    
    //    public String uri;
    //    public String soapAction;
    //    public XmlNamespace rootNamespace;
    //    public String rootName;
    //    public XmlNamespace soap11BodyChildNamespace;
    //    public String soap11BodyChildName;
    
    
    
    
    public MessageContext(String direction) {
        super(builder.newFragment(XSUL_CTX_NS, CONTEXT)); //special container element
        setDirection(direction);
    }
    
    public MessageContext(XmlElement targetToWrap) {
        super(targetToWrap);
    }
    
    public XmlElement getIncomingMessage() {
        XmlElement container = element(XSUL_CTX_NS, INCOMING, true).element(XSUL_CTX_NS, MESSAGE, true);
        if(container.hasChildren()) {
            return (XmlElement) container.requiredElementContent().iterator().next();
        } else {
            return null;
        }
        //TODO!!!
        //XmlDocument doc = element(XSUL_CTX_NS, INCOMING).child(1);
        //select child of body!
    }
    
    public void setIncomingMessage(XmlElement incomingMessage) {
        XmlElement el = element(XSUL_CTX_NS, INCOMING, true).element(XSUL_CTX_NS, MESSAGE, true);
        //todo el.replaceChildren(child);
        el.removeAllChildren();
        
        //NOTE: addChild is used ot create weak link -- parent of incomingMessage will NOT be modified
        el.addChild(incomingMessage);
    }
    
    public XmlElement getIncomingEnvelope() {
        XmlElement incomingMsg = getIncomingMessage();
        XmlContainer incomingDocumentEl = incomingMsg.getRoot();
        XmlElement envEl = null;
        if(incomingDocumentEl instanceof XmlElement) {
            envEl = (XmlElement)incomingDocumentEl;
        } else if(incomingDocumentEl instanceof XmlDocument) {
            envEl = ((XmlDocument)incomingDocumentEl).getDocumentElement();
        }
        return envEl;
    }
    
    public XmlDocument getIncomingEnvelopeDoc() {
        XmlElement incomingEnv = getIncomingEnvelope();
        if(incomingEnv.getParent() == null) {
            XmlDocument doc = builder.newDocument("1.0", Boolean.TRUE, "UTF-8");
            doc.setDocumentElement(incomingEnv);
        }
        XmlDocument envDoc =(XmlDocument) incomingEnv.getParent();
        return envDoc;
    }
    
    //    public XmlElement getIncomingEnvelope() {
    //        XmlElement container = element(XSUL_CTX_NS, INCOMING, true).element(XSUL_CTX_NS, ENVELOPE, true);
    //        if(container.hasChildren()) {
    //            return (XmlElement) container.requiredElementContent().iterator().next();
    //        } else {
    //            return null;
    //        }
    //    }
    
    //    public void setIncomingEnvelope(XmlElement incomingEnvelope) {
    //        XmlElement el = element(XSUL_CTX_NS, INCOMING, true).element(XSUL_CTX_NS, ENVELOPE, true);
    //        el.removeAllChildren();
    //
    //        //NOTE: addChild is used ot create weak link -- parent of incomingEnvelope will NOT be modified
    //        el.addChild(incomingEnvelope);
    //    }
    
    public XmlElement getOutgoingMessage() {
        XmlElement container = element(XSUL_CTX_NS, OUTGOING, true).element(XSUL_CTX_NS, MESSAGE, true);
        if(container.hasChildren()) {
            return (XmlElement) container.requiredElementContent().iterator().next();
        } else {
            return null;
        }
        //XmlElement outgoing = element(XSUL_CTX_NS, OUTGOING);
        //return el != null && el.hasChildren() ? child(1) : null;
    }
    
    public void setOutgoingMessage(XmlElement outgoingMessage) {
        XmlElement el = element(XSUL_CTX_NS, OUTGOING, true).element(XSUL_CTX_NS, MESSAGE, true);
        el.removeAllChildren();
        //NOTE: addChild is used ot create weak link -- parent of incomingMessage will NOT be modified
        if(outgoingMessage != null) {
            el.addChild(outgoingMessage);
        }
    }
    
    //TODO: default direction? Outgoing and Incoming
    //JDK5: enume MessageDirection
    public String getDirection() {
        XmlElement container = element(XSUL_CTX_NS, DIRECTION, true);
        if(container.hasChildren()) {
            return container.requiredTextContent();
        } else {
            return null;
        }
    }
    
    public void setDirection(String direction) {
        if(direction == null) throw new IllegalArgumentException();
        if(!direction.equals(DIR_INCOMING) && !direction.equals(DIR_OUTGOING)) {
            throw new IllegalArgumentException("wrong direction "+direction);
        }
        XmlElement container = element(XSUL_CTX_NS, DIRECTION, true);
        container.removeAllChildren();
        container.addChild(direction);
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





