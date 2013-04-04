/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsaInvoker.java,v 1.13 2006/04/30 23:17:28 aslom Exp $
 */

package xsul.ws_addressing;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.invoker.DynamicInfosetInvoker;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.MessageInvoker;
import xsul.invoker.http.HttpDynamicInfosetInvoker;
import xsul.soap.SoapUtil;
import xsul.soap12_util.Soap12Util;
import xsul.util.FastUUIDGen;

/**
 * Invoker that will use WSA headers to decide where to send message (wsa:To) and
 * will use WSA processing node to indicate where to send replies.
 * <br />NOTE: this is class is not multi-thread safe and MUST be proected if used concurently!
 *
 * @version $Revision: 1.13 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsaInvoker implements MessageInvoker
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static String PROPERTY_SERIALIZER_INDENTATION =
        "http://xmlpull.org/v1/doc/properties.html#serializer-indentation";
    
    protected SoapUtil soapUtil = Soap12Util.getInstance();
    protected String indent = null; //"  ";
    protected boolean useHttpKeepAlive = true;
    protected DynamicInfosetInvoker invoker = new HttpDynamicInfosetInvoker() {
        protected void serializeInvocationDocument(XmlDocument input, XmlSerializer ser) {
            if(indent != null) {
                try {
                    ser.setProperty(PROPERTY_SERIALIZER_INDENTATION, indent);
                } catch (IllegalStateException e) {} catch (IllegalArgumentException e) {}
            }
            super.serializeInvocationDocument(input, ser);
        }
    };
    
    protected WsaEndpointReference targetEPR;
    protected XmlNamespace targetWsa;
    protected URI defaultAction;
    protected URI messageId;
    
    private WsaEndpointReference defaultReplyTo;
    
    private WsaEndpointReference defaultFaultTo;
    //private WsaProcessor node;
    
    //protected final static XmlPullBuilder builder = XmlPullBuilder.newInstance();
    //public WsaInvoker(WsaProcessor node)
    public WsaInvoker()
    {
        //this.node = node;
        //setDefaultFroom(node.buildFromAddress())
    }
    
    public void setTargetWsa(XmlNamespace targetWsa) {
        this.targetWsa = targetWsa;
    }
    
    public XmlNamespace getTargetWsa() {
        return targetWsa;
    }
    
    public void setUseHttpKeepAlive(boolean useHttpKeepAlive) {
        this.useHttpKeepAlive = useHttpKeepAlive;
        if(invoker instanceof HttpDynamicInfosetInvoker) {
            ((HttpDynamicInfosetInvoker)invoker).setKeepAlive(useHttpKeepAlive);
        }
    }
    
    public boolean isUseHttpKeepAlive() {
        return useHttpKeepAlive;
    }
    
    public void setIndent(String indent) {
        this.indent = indent;
    }
    
    public String getIndent() {
        return indent;
    }
    
    public void setTargetEPR(WsaEndpointReference targetEPR) {
        this.targetEPR = targetEPR;
    }
    
    public WsaEndpointReference getTargetEPR() {
        return targetEPR;
    }
    
    public void setMessageId(URI messageId) {
        this.messageId = messageId;
    }
    
    public URI getMessageId() {
        return messageId;
    }
    
    public void setDefaultAction(URI action) {
        this.defaultAction = action;
    }
    
    public URI getDefaultAction() {
        return defaultAction;
    }
    
    public SoapUtil getSoapFragrance() {
        return this.soapUtil;
    }
    
    /**
     * Set what SOAP utility to use for wrapping message into SOAP Envelope Body
     * If itis null no wrapping is done.
     */
    public void setSoapFragrance(SoapUtil soapFragrance) {
        //if(soapUtil
        this.soapUtil = soapFragrance;
    }
    
    //<wsa:MessageID> xs:anyURI </wsa:MessageID>
    
    //<wsa:RelatesTo RelationshipType="..."?>xs:anyURI</wsa:RelatesTo>
    
    //<wsa:To>xs:anyURI</wsa:To>
    //<wsa:Action>xs:anyURI</wsa:Action>
    
    
    //<wsa:From>endpoint-reference</wsa:From>
    //<wsa:ReplyTo>endpoint-reference</wsa:ReplyTo>
    //<wsa:FaultTo>endpoint-reference</wsa:FaultTo>
    //<wsa:Recipient>endpoint-reference</wsa:Recipient>
    
    
    //addDefaultRelatesTo(URI, QName)
    //removeAllRelatesTo()
    
    //setDefaultSoapHeaders(XmlElement headers)
    
    //setDefaultTo
    
    //setDefaultFrom
    public void setDefaultReplyTo(WsaEndpointReference replyToEpr) {
        this.defaultReplyTo = replyToEpr;
    }
    
    
    public void setDefaultFaultTo(WsaEndpointReference faultToEpr) {
        this.defaultFaultTo = faultToEpr;
    }
    //setDefaultFaultTo
    //setDefaultRecipient
    
    //setDefaultReferenceProperties
    
    
    protected XmlDocument wrapInSoapEnvelopeIfNecessary(XmlElement message)
        throws DynamicInfosetInvokerException
    {
        // TODO should allow to deal both with SOAP 1.1 and 1.2 (but no other???)
        
        XmlContainer parent = message.getParent();
        if(parent == null) {
            XmlDocument docEnv = soapUtil.wrapBodyContent(message);
            return docEnv;
        }
        XmlDocument docEnv;
        XmlContainer root = message.getRoot();
        if(root instanceof XmlDocument) {
            docEnv = (XmlDocument) root;
        } else {
            docEnv = builder.newDocument();
            docEnv.setDocumentElement((XmlElement)root);
        }
        
        // verify document is SOAP Envelope!
        XmlElement envelope = (XmlElement) docEnv.getDocumentElement();
        if(!soapUtil.isSoapEnvelopeSupported(envelope)) {
            throw new DynamicInfosetInvokerException("message must be in SOAP envelope not "+envelope);
        }
        return docEnv;
    }
    
    public XmlElement invokeMessage(XmlElement message) throws DynamicInfosetInvokerException
    {
        
        XmlDocument doc = wrapInSoapEnvelopeIfNecessary(message);
        //System.err.println(getClass().getName()+" "+xsul.util.Util.safeXmlToString(doc.getDocumentElement()));
        WsaMessageInformationHeaders wmih; {
            XmlNamespace wsa = targetWsa;
            if(targetEPR != null && wsa == null) {
                wsa = targetEPR.getWsAddressingVersion();
            }
            wmih = new WsaMessageInformationHeaders(wsa, doc);
        }
        if(targetEPR != null) {
            // explode targetER into SOAP headers
            wmih.explodeEndpointReference(targetEPR);
        }
        
        if(wmih.getMessageId() == null) {
            if(messageId != null) {
                wmih.setMessageId(messageId);
            } else {
                //wmih.setMessageId(URI.create("urn:"+System.currentTimeMillis()));
                wmih.setMessageId(URI.create("uuid:"+FastUUIDGen.nextUUID()));
            }
        }
        //use node if to null to add from and replyTo ...
        //if(defaultFrom != null) {
        //from = node.getEndpointReference()
        //}
        
        if(defaultFaultTo != null) {
            if(wmih.getFaultTo() == null) {
                wmih.setFaultTo(defaultFaultTo);
            }
        }
        
        if(defaultReplyTo != null) {
            if(wmih.getReplyTo() == null) {
                wmih.setReplyTo(defaultReplyTo);
            }
            //      if(wmih.getFaultTo() == null) {
            //          wmih.setFaultTo(defaultReplyTo);
            //      }
        }
        
        //PROBLEM:required WSDL to create default wsa:Action as defined in 3.3.2 Default Action Pattern
        // [target namespace]/[port type name]/[input|output name]
        //String wsaAction = "";
        // extract wsa:Action and use as SoapAction
        if(wmih.getAction() == null) {
            if(defaultAction== null) {
                throw new DynamicInfosetInvokerException("WS-Addressing action is required");
            }
            wmih.setAction(defaultAction);
        }
        
        XmlDocument responseDoc = sendXml(doc);
        
        if(responseDoc != null) {
            XmlElement response = soapUtil.requiredBodyContent(responseDoc); //select first child of S:Body
            
            if("Fault".equals(response.getName())) {
                //ALEK: Leaky Abstraciton: now can process both SOAP 1.1 and 1.2 ...
                if(XmlConstants.NS_URI_SOAP11.equals(response.getNamespaceName())
                       || XmlConstants.NS_URI_SOAP12.equals(response.getNamespaceName()) )
                {
                    // TODO extract faultcode + faultstring + detail etc.
                    //StringWriter sw = new StringWriter();
                    String s = builder.serializeToString(response);
                    //TODO extract fault!!!!!!!!
                    // check fault etc.
                    throw new DynamicInfosetInvokerException("remote exception .... "+s, response);
                }
            }
            
            return response;
        } else {
            return null;
        }
    }
    
    public XmlDocument sendXml(XmlDocument doc)
        throws XmlBuilderException, DynamicInfosetInvokerException
    {
        WsaMessageInformationHeaders wmih = new WsaMessageInformationHeaders(doc);
        DynamicInfosetInvoker xmlInvoker = lookupInvokerForAddress(wmih);
        XmlDocument responseDoc =  xmlInvoker.invokeXml(doc);
        return responseDoc;
    }
    
    private static class InvokerWithProtocol {
        private String protocol;
        private DynamicInfosetInvoker invoker;
        
        public InvokerWithProtocol(String protocol, DynamicInfosetInvoker invoker) {
            this.protocol = protocol;
            this.invoker = invoker;
        }
        
        //      public void setProtocol(String protocol) {
        //          this.protocol = protocol;
        //      }
        
        public String getProtocol() {
            return protocol;
        }
        
        //      public void setInvoker(DynamicInfosetInvoker invoker) {
        //          this.invoker = invoker;
        //      }
        
        public DynamicInfosetInvoker getInvoker() {
            return invoker;
        }
        
    }
    private List invokers;
    public void addInvoker(String protocol, DynamicInfosetInvoker invoker)
        throws DynamicInfosetInvokerException
    {
        if(invokers == null) {
            invokers = new LinkedList();
        }
        invokers.add(new InvokerWithProtocol(protocol, invoker));
    }
    
    protected DynamicInfosetInvoker lookupInvokerForDestination(WsaMessageInformationHeaders wmih)  {
        URI wsaTo = wmih.getTo();
        if(wsaTo == null) {
            return null;
        }
        String wsaToString = wsaTo.toString();
        DynamicInfosetInvoker xmlInvoker = null;
        if(invokers != null) {
            for(Iterator i = invokers.iterator(); i.hasNext();) {
                InvokerWithProtocol p = (InvokerWithProtocol) i.next();
                if(wsaToString.startsWith(p.getProtocol())){
                    xmlInvoker = p.getInvoker();
                }
            }
        }
        if(xmlInvoker == null) { //createDefaultOne
            if(wsaTo != null && wsaToString.startsWith("http://")) {
                xmlInvoker = invoker;
                //              throw new DynamicInfosetInvokerException(
                //                  "no HTTP invoker available for wsa:To destination '"+wsaTo
                //                      +"' in message "+builder.serializeToString(wmih.getSoapHeaderElement().getParent()));
                
                //little optimization
                //addInvoker("http://", xmlInvoker);
                
            }
        }
        if(xmlInvoker instanceof HttpDynamicInfosetInvoker) {
            HttpDynamicInfosetInvoker httpInvoker = (HttpDynamicInfosetInvoker) xmlInvoker;
            httpInvoker.setLocation(wsaTo.toString());
            URI wsaAction = wmih.getAction();
            if(wsaAction != null) {
                httpInvoker.setSoapAction(wsaAction.toString());
            }
        }
        return xmlInvoker;
    }
    
    /**
     * Extensibility point to support arbitrary wsa:To mapping to transports.
     */
    protected DynamicInfosetInvoker lookupInvokerForAddress(WsaMessageInformationHeaders wmih)
        throws XmlBuilderException, DynamicInfosetInvokerException
    {
        URI wsaTo = wmih.getTo(); //headers.element(WsAddressing.NS, "To").text();
        if(wsaTo == null) {
            //wmih.setTo()
            throw new DynamicInfosetInvokerException("WS-Addressing destination is required");
        }
        DynamicInfosetInvoker xmlInvoker = lookupInvokerForDestination(wmih);
        if(xmlInvoker == null) {
            throw new DynamicInfosetInvokerException(
                "unsupported wsa:To destination '"+wsaTo
                    +"' in message "+builder.serializeToString(wmih.getSoapHeaderElement().getParent()));
        } else {
            return xmlInvoker;
        }
    }
    
}



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




