/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsaMessageInformationHeaders.java,v 1.18 2006/05/02 00:58:59 aslom Exp $
 */

package xsul.ws_addressing;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.Iterable;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.XmlSerializable;
import xsul.DataValidation;
import xsul.DataValidationException;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;

//Good article about chnages 2003->2004
//http://msdn.microsoft.com/webservices/default.aspx?pull=/library/en-us/dnwebsrv/html/wsaddressdelta.asp

//<wsa:To>xs:anyURI</wsa:To> ?  (changed ot optional in 1.0)
//<wsa:Action>xs:anyURI</wsa:Action>
//<wsa:From>endpoint-reference</wsa:From>?
//<wsa:ReplyTo>endpoint-reference</wsa:ReplyTo>?
//<wsa:FaultTo>endpoint-reference</wsa:FaultTo>?
//<wsa:MessageID>xs:anyURI</wsa:MessageID>?
//<wsa:RelatesTo RelationshipType="xs:anyURI"?>xs:anyURI</wsa:RelatesTo> *
//<xs:any* wsa:IsReferenceParameter='true'>*</a> from each element child of <wsa:ReferenceParameters>xs:any*</wsa:ReferenceParameters> ?
//REMOVED in 2004: <wsa:Recipient>endpoint-reference</wsa:Recipient>?

/**
 * Implementation of Message Information Headers from
 * <a href="http://www-106.ibm.com/developerworks/webservices/library/ws-add/#informationmodelforendpointreferences">Endpoint Reference</a>
 * from Web Services Addressing 2004 (WS-Addressing)
 *
 * @version $Revision: 1.18 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsaMessageInformationHeaders implements DataValidation, XmlSerializable
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    //private final static XmlNamespace wsa = WsAddressing.getDefaultNs();
    private final static XmlNamespace wsp = WsAddressing.POLICY_NS_2002_12;

    public final static String ACTION_EL = "Action";
    public final static String MESSAGE_ID_EL = "MessageID";
    public final static String TO_EL = "To";
    
    private XmlElement soapHeaderElement;
    private boolean standardWsa;
    private XmlNamespace wsaUsedInHeaders;
    
    public WsaMessageInformationHeaders(XmlElement soapHeadersOrEnvelope) {
        this(null,  soapHeadersOrEnvelope);
    }
    
    public WsaMessageInformationHeaders(XmlNamespace targetWsa, XmlElement soapHeadersOrEnvelope) {
        if(soapHeadersOrEnvelope == null) throw new IllegalArgumentException();
        String name = soapHeadersOrEnvelope.getName();
        XmlElement soapHeaders;
        if(XmlConstants.S_ENVELOPE.equals(name)) {
            soapHeaders = soapHeadersOrEnvelope.element(null, XmlConstants.S_HEADER);
            if(soapHeaders  == null) {
                soapHeaders = soapHeadersOrEnvelope.addElement(
                    0, soapHeadersOrEnvelope.newElement(soapHeadersOrEnvelope.getNamespace(), XmlConstants.S_HEADER));
                //throw new DataValidationException (
                //    "SOAP Header was expected in "+Util.safeXmlToString(soapHeadersOrEnvelope));
            }
            name = soapHeaders.getName();
        } else {
            soapHeaders = soapHeadersOrEnvelope;
        }
        if(!"Header".equals(name)) {
            throw new XsulException("expected Header element to reference WS-Addressing Headers not "+name);
        }
        this.soapHeaderElement = soapHeaders;
        
        if(targetWsa != null) {
            wsaUsedInHeaders = targetWsa;
        } else {
            // try to guess namespace based on presence of required "Action" element (required header in WSA)
            XmlElement addressEl = soapHeaders.element(null, ACTION_EL);
            if(addressEl != null) {
                wsaUsedInHeaders = addressEl.getNamespace();
            } else {
                wsaUsedInHeaders = WsAddressing.getDefaultNs();
                
            }
        }
        assert this.wsaUsedInHeaders != null;
        standardWsa = wsaUsedInHeaders.equals(WsAddressing.NS_WSA);
        //attach WS-Addressing namespace declaration to Envelope or Header if possible
        if(soapHeaderElement.getParent() instanceof XmlElement) {
            soapHeadersOrEnvelope = (XmlElement) soapHeaderElement.getParent();
        }
        if(soapHeadersOrEnvelope.lookupNamespaceByName(wsaUsedInHeaders.getNamespaceName()) == null) {
            if(soapHeadersOrEnvelope.lookupNamespaceByPrefix(wsaUsedInHeaders.getPrefix()) == null) {
                soapHeadersOrEnvelope.declareNamespace(wsaUsedInHeaders);
            }
        }
        if(soapHeadersOrEnvelope.lookupNamespaceByName(wsp.getNamespaceName()) == null) {
            if(soapHeadersOrEnvelope.lookupNamespaceByPrefix(wsp.getPrefix()) == null) {
                soapHeadersOrEnvelope.declareNamespace(wsp);
            }
        }
        //validateData();
    }
    
    public WsaMessageInformationHeaders(XmlNamespace targetWsa, XmlDocument doc) {
        this(targetWsa, doc.getDocumentElement());
    }
    
    public WsaMessageInformationHeaders(XmlDocument doc) {
        this(doc.getDocumentElement());
    }
    
    public XmlNamespace guessWsAddressingVersionUsedInHeaders() {
        return wsaUsedInHeaders;
    }
    
    public XmlElement getHeaderElement(XmlNamespace ns, String name) {
        return soapHeaderElement.element(ns, name);
    }
    
    public XmlElement getSoapHeaderElement() {
        return soapHeaderElement;
    }
    
    protected URI getUri(String name, boolean required) {
        XmlElement headerEl = soapHeaderElement.element(wsaUsedInHeaders, name);
        
        if(headerEl != null) {
            String t = headerEl.requiredTextContent();
            //String t = element(wsa, name, true).requiredTextContent();
            if(t != null) {
                try {
                    return new URI(t.trim());
                } catch(Exception e) {
                    throw new DataValidationException (
                        "wsa:"+name+" mut be of type xs:anyURI "+toString(), e);
                }
            }
        }
        if(required) {
            throw new DataValidationException(
                "required wsa:"+name+" was not found in "+toString());
        } else {
            return null;
        }
    }
    
    private void setUri(String name, URI uri) {
        XmlElement el = soapHeaderElement.element(wsaUsedInHeaders, name, true);
        el.removeAllChildren();
        el.addChild(uri.toString());
    }
    
    public URI getTo() throws DataValidationException {
        return getUri(TO_EL, false);
    }
    
    //wsa:To is required
    public URI getToRequired() throws DataValidationException {
        return getUri("To", true);
    }
    
    public void setTo(URI uri) {
        setUri("To", uri);
    }
    
    public URI getAction() {
        return getUri(ACTION_EL, false);
    }
    
    //wsa:Action is required
    public URI getActionRequired() {
        return getUri(ACTION_EL, true);
    }
    
    public void setAction(URI uri) {
        setUri(ACTION_EL, uri);
    }
    
    // /wsa:MessageID This optional element (of type xs:anyURI) conveys the [message id] property.
    public URI getMessageId() {
        return getUri(MESSAGE_ID_EL, false);
    }
    
    public void setMessageId(URI uri) {
        if(uri == null) throw new IllegalArgumentException(MESSAGE_ID_EL+" can not be null");
        setUri(MESSAGE_ID_EL, uri);
    }
    
    // /wsa:RelatesTo
    // This optional (repeating) element information item contributes one abstract [relationship] property value,
    //in the form of a (URI, QName) pair.
    //The [children] property of this element (which is of type xs:anyURI) conveys the [message id] of the related message
    //Iterable<XmlElement> but maybe //Itrable<WsRelatesTo>
    ///wsa:RelatesTo/@RelationshipType
    //JDK15 Iterable<WsaReltatesTo>
    public Iterable getRelatesTo() {
        final Iterator it = soapHeaderElement.elements(wsaUsedInHeaders, WsaRelatesTo.TYPE_NAME).iterator();
        // there is some beauty in deeply nested inner classes ...
        return new Iterable() {
            public Iterator iterator() {
                return new Iterator() {
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    public Object next() {
                        XmlElement el = (XmlElement) it.next();
                        if(el instanceof WsaRelatesTo) {
                            return (WsaRelatesTo) el;
                        } else {
                            return new WsaRelatesTo(el);
                        }
                    }
                    public void remove() {
                        it.remove();
                    }
                    
                };
            }
        };
    }
    
    public URI getRelatedRequestMessageId() {
        for(Iterator r = getRelatesTo().iterator(); r.hasNext();) {
            WsaRelatesTo rt = (WsaRelatesTo) r.next();
            if(rt.getRelationshipType().equals(WsAddressing.URI_DEFAULT_REPLY_RELATIONSHIP_TYPE)) {
                URI relatedMssageId = rt.getRelationship();
                return relatedMssageId;
            }
        }
        return null;
    }
    
    //<wsa:RelatesTo RelationshipType="..."?>xs:anyURI</wsa:RelatesTo>
    public void removeAllRelatesTo() {
        Iterator it = soapHeaderElement.elements(wsaUsedInHeaders, WsaRelatesTo.TYPE_NAME).iterator();
        while(it.hasNext()) {
            soapHeaderElement.removeChild(it.next());
        }
    }
    
    public void addRelatesTo(WsaRelatesTo relatesTo) {
        soapHeaderElement.addElement(relatesTo);
    }
    
    
    //<wsa:From>endpoint-reference</wsa:From>?
    //<wsa:ReplyTo>endpoint-reference</wsa:ReplyTo>?
    //<wsa:FaultTo>endpoint-reference</wsa:FaultTo>?
    protected WsaEndpointReference getEndpointReference(String name) {
        XmlElement el = soapHeaderElement.element(wsaUsedInHeaders, name); //??? should require exactly ONE?
        if(el == null) {
            return null;
        }
        if(el instanceof WsaEndpointReference) {
            return (WsaEndpointReference) el;
        } else {
            return new WsaEndpointReference(el);
        }
    }
    
    protected void setEndpointReference(String name, WsaEndpointReference epr) {
        //      try {
        //          epr = (WsaEndpointReference) epr.clone();
        //      } catch (CloneNotSupportedException e) {
        //          throw new XsulException("could not clone");
        //      }
        //epr.setNamespace(WsAddressing.getDefaultNs());
        epr.setNamespace(epr.getWsAddressingVersion());
        epr.setName(name);
        XmlElement el = soapHeaderElement.element(wsaUsedInHeaders, name);
        if(el != null) {
            soapHeaderElement.replaceChild(epr, el);
        } else {
            soapHeaderElement.addChild(epr);
        }
    }
    
    public WsaEndpointReference getFrom() {
        return getEndpointReference("From");
    }
    
    public void setFrom(WsaEndpointReference fromEpr) {
        setEndpointReference("From", fromEpr);
    }
    
    public WsaEndpointReference getReplyTo() {
        return getEndpointReference("ReplyTo");
    }
    
    public void setReplyTo(WsaEndpointReference replyToEpr) {
        setEndpointReference("ReplyTo", replyToEpr);
    }
    
    public WsaEndpointReference getFaultTo() {
        return getEndpointReference("FaultTo");
    }
    
    public void setFaultTo(WsaEndpointReference faultToEpr) {
        setEndpointReference("FaultTo", faultToEpr);
    }
    
    // REMOVED in WSA'04
    //    public WsaEndpointReference getRecipient() {
    //        return getEndpointReference("Recipient");
    //    }
    //
    //    public void setRecipient(WsaEndpointReference recipientEpr) {
    //        setEndpointReference("Recipient", recipientEpr);
    //    }
    //
    
    public void explodeEndpointReference(WsaEndpointReference epr) {
        //follow algorithm from 2.3. Binding Endpoint References
        //to bind endpoint reference into SOAP message headers
        
        //bind destination
        setTo(epr.getAddress());
        
        {
            XmlElement refParams = epr.getReferenceParameters();
            explodeChildren(refParams, standardWsa);
        }
        //copy reference properties as headers for pre-WSA 1.0
        if(!standardWsa) {
            XmlElement refProps = epr.getReferenceProperties();
            explodeChildren(refProps, standardWsa);
        }
    }
    
    private void explodeChildren(XmlElement refParams, boolean addFlag) throws XsulException {
        for(Iterator  i = refParams.requiredElementContent().iterator(); i.hasNext(); ) { //JDK15
            XmlElement param = (XmlElement) i.next();
            try {
                //TODO: should check if in-scope namespaces are cloned?
                XmlElement propClone = (XmlElement) param.clone();
                if(addFlag) {
                    //wsa:IsReferenceParameter='true'
                    propClone.addAttribute(WsAddressing.NS_WSA, "IsReferenceParameter", "true");
                }
                soapHeaderElement.addElement(propClone);
            } catch (CloneNotSupportedException e) {
                throw new XsulException("could not clone property:"+param);
            }
        }
    }
    
    public void validateData() throws DataValidationException {
        if(getTo() == null) {
            throw new DataValidationException("wsa:To is required in "+this);
        }
        if(getAction() == null) {
            throw new DataValidationException("wsa:Action is required in "+this);
        }
        //getRelatesTo();
        // check only one PortType
        //       elements(wsa,"PortType").size() < 2
        //service port max one
        //      elements(wsa,"ServiceName").size() < 2
        getMessageId();
        getFrom();
        getReplyTo();
        getFaultTo();
        //getRecipient();
    }
    
    public void serialize(XmlSerializer ser) throws IOException {
        ser.comment("START:"+getClass().getName());
        builder.serialize(soapHeaderElement, ser);
        ser.comment("END:"+getClass().getName());
    }
    
    public String toString() {
        return builder.serializeToString(this) ;
    }
}

/*WsaEndpointReference extends WsaEndpointReferenceReadOnly  {
 target.setReadOnly(false)
 //pass through setReadOnly
 }
 */

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (C) 2004 The Trustees of Indiana University.
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






