/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Soap11Util.java,v 1.14 2006/04/30 06:48:13 aslom Exp $
 */

package xsul.soap11_util;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.soap.SoapUtil;
import xsul.util.QNameElText;
import xsul.util.XsulUtil;

/**
 * SOAP 1.1 envelope manipulations utility methods.
 *
 * @version $Revision: 1.14 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class Soap11Util extends SoapUtil {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public final static String SOAP11_ENC_PREFIX = "SOAP-ENC";
    
    public final static String NS_URI_SOAP11 = XmlConstants.NS_URI_SOAP11;
    public final static String SOAP11_NEXT_ACTOR =
        "http://schemas.xmlsoap.org/soap/actor/next" ;
    
    public final static XmlNamespace SOAP11_NS = XmlConstants.SOAP11_NS;
    public final static XmlNamespace SOAP11_ENC_NS = builder.newNamespace(
        SOAP11_ENC_PREFIX, "http://schemas.xmlsoap.org/soap/encoding/");
    
    public static final String ELEM_ENVELOPE = XmlConstants.S_ENVELOPE ;
    public static final String ELEM_HEADER = XmlConstants.S_HEADER;
    public static final String ELEM_BODY = XmlConstants.S_BODY ;
    
    public static final String ATTR_MUST_UNDERSTAND = "mustUnderstand" ;
    public static final String ATTR_ACTOR = "actor" ;
    public static final String ATTR_ROLE = "role" ;
    
    private final static Soap11Util instance = new Soap11Util();
    
    Soap11Util() {
    }
    
    
    public static Soap11Util getInstance() {
        return instance;
    }
    
    public String getSoapVersion() {
        return "1.1";
    }
    
    public boolean isSoapEnvelopeSupported(XmlElement root)
    {
        if (NS_URI_SOAP11.equals(root.getNamespace().getNamespaceName())
                && ELEM_ENVELOPE.equals(root.getName()))
        {
            return true;
        } else {
            return false;
        }
        
    }
    
    public XmlDocument wrapBodyContent(XmlElement bodyContent)
        throws XsulException
    {
        if(bodyContent == null) {
            return null;
        }
        if(bodyContent.getParent() != null) {
            XmlContainer top = bodyContent.getRoot();
            if(top instanceof XmlDocument) {
                return (XmlDocument) top;
            }
        }
        XmlDocument doc = builder.newDocument();
        
        final XmlNamespace soapNs = builder.newNamespace("S", NS_URI_SOAP11);
        XmlElement envelope = doc.addDocumentElement(soapNs, ELEM_ENVELOPE);
        envelope.declareNamespace(soapNs);
        //declare prefixes for some common namespaces
        envelope.declareNamespace(XmlConstants.XS_NS);
        envelope.declareNamespace(XmlConstants.XSI_NS);
        
        XmlElement body = envelope.addElement(soapNs, "Body");
        body.addElement(bodyContent);
        
        return doc;
    }
    
    public XmlElement generateSoapFault(XmlNamespace faultCodeValueNs,
                                        String faultCodeValueName,
                                        String reasonTextEnglish,
                                        Throwable ex) throws XsulException
    {
        XmlElement faultEl = builder.newFragment( SOAP11_NS, "Fault" );
        if(!NS_URI_SOAP11.equals(faultCodeValueNs.getNamespaceName())) {
            faultEl.declareNamespace("n", faultCodeValueNs.getNamespaceName());
        }
        
        XmlElement faultCodeEl = faultEl.addElement("faultcode");
        faultCodeEl.addChild(new QNameElText(faultCodeEl,
                                             faultCodeValueNs.getNamespaceName(),
                                             faultCodeValueName));
        if(reasonTextEnglish == null) reasonTextEnglish = "{null}";
        faultEl.addElement("faultstring").addChild(reasonTextEnglish);
        
        XmlElement detail = faultEl.addElement("detail");
        if(ex != null) {
            final XmlNamespace axisNs = faultEl.newNamespace("n", "http://xml.apache.org/axis/");
            
            String msg = ex.getMessage();
            if(msg != null) {
                XmlElement exName = detail.addElement(axisNs, "exceptionName");
                exName.declareNamespace(axisNs);
                exName.addChild(msg);
            }
            XmlElement stackTrace = detail.addElement(axisNs, "stackTrace");
            stackTrace.declareNamespace(axisNs);
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            stackTrace.addChild(sw.toString());
        }
        
        return faultEl;
        //XmlDocument envelope = wrapBodyContent(faultEl);
        
        //        // resolve prefix to use in fault code (it MUST be done after SOAP:Envelope is constructed!
        //        XmlNamespace ns = faultCodeEl.lookupNamespaceByName(faultCodeNs);
        //        if(ns == null || ns.getPrefix() == null) {
        //            // declare prefix if does nto exist
        //            final XmlNamespace faultNs = builder.newNamespace("ns", faultCodeNs);
        //            ns = faultCodeEl.declareNamespace(faultNs);
        //        }
        //        String faultcode = ns.getPrefix() + ':' + faultCodeName;
        //        faultCodeEl.addChild(faultcode);
        
        //return envelope;
    }
    
    public XmlElement generateSoapClientFault(String reasonTextEnglish, Throwable ex)
        throws XsulException
    {
        return generateSoapFault(SOAP11_NS, "Client", reasonTextEnglish, ex);
    }
    
    public XmlElement generateSoapServerFault(String reasonTextEnglish, Throwable ex)
        throws XsulException
    {
        return generateSoapFault(SOAP11_NS, "Server", reasonTextEnglish, ex);
    }
    
    
    public XmlElement requiredBodyContent(XmlDocument respDoc)
        throws XsulException
    {
        // check if response has Envelope/Body
        XmlElement root = respDoc.getDocumentElement();
        return requiredBodyContent(root);
    }
    
    public XmlElement requiredBodyContent(XmlElement root)
        throws XsulException
    {
        if(!NS_URI_SOAP11.equals(root.getNamespaceName())) {
            throw new XsulException ("expected SOAP 1.1 Envelope not "+root);
        }
        if(!ELEM_ENVELOPE.equals(root.getName())) {
            throw new XsulException (
                "expected top level SOAP 1.1 element name to be Envelope not "+root);
        }
        XmlElement body = root.findElementByName(NS_URI_SOAP11, "Body");
        if(body == null) {
            throw new XsulException (
                "missing required Body element in SOAP 1.1 Envelope"+root);
        }
        //TODO get first non ignorable child element
        // add  XPath to make it easier
        if(!body.hasChildren()) {
            throw new  XsulException("SOAP message must have body " //FIXME //TODO //ALEK throw new SoapClientFault
                                         +"- got "+XsulUtil.safeXmlToString(root));
        }
        XmlElement content = (XmlElement) body.requiredElementContent().iterator().next();
        return content;
    }
    
    
    //    public XmlElement findBodyContent(XmlElement message, boolean rpc) {
    //        Util.removeIgnorableSpace(message);
    //
    //        if (NS_URI_SOAP11.equals(message.getNamespace().getNamespaceName())) {
    //            if ("Envelope".equals(message.getName())) {
    //                XmlElement envelope = message;
    //
    //                XmlElement body = envelope.findElementByName(NS_URI_SOAP11, "Body");
    //
    //                if (body != null) {
    //                    XmlElement payload = (XmlElement) body.children().next();
    //                    if (rpc) {
    //                        return (XmlElement) payload.children().next();
    //                    }
    //                    return payload;
    //                } else
    //                    //todo: process faults nicely
    //                    return envelope;
    //            }
    //        }
    //
    //        return message;
    //    }
    
    //    /**
    //     * Wrap XML message (this should be XmlElement correspondingto WSDL 1.1 message with parts)
    //     * inside SOAP 1.1 Body and return whole SOAP 1.1 Envelope as XML document.
    //     * If operation name is not null RPC style is assumend and message is element
    //     * name is changed to operationName (and provided namespace if notn null).
    //     * If operation name is null the first message child is extracted and put directly
    //     * into SOAP Bpody elment. In this case if message must have exactly onechild
    //     * or exception is thrown.
    //     */
    //    public XmlDocument wrapMessageContent(XmlElement message,
    //                                        String operationNamespace,
    //                                        String operationName)
    //        throws XsulException
    //    {
    //        try {
    //          if (operationName != null) {
    //              //logger.info("adding rpc style operation name and namespace");
    //              message.setName(operationName); //operation.getLocalPart());
    //              //String operationNamespace = operation.getNamespaceURI();
    //              //XmlNamespace ns = null;
    //              //if (!("".equals(operationNamespace))) {
    //              //    ns = message.lookupNamespaceByName(operationNamespace);
    //              //    ns = message.declareNamespace("ns0", operationNamespace);
    //              //}
    //
    //              //deep-set of ns
    //              //setNs(message, null);
    //              if(operationNamespace != null) {
    //                  message.setNamespace(message.newNamespace(operationNamespace));
    //              }
    //              //result = message;
    //          } else {
    //              // extract the ony one parameter as described in doc/lit binding
    //              Iterator c = message.children();
    //              Object child = c.next();
    //              if(c.hasNext()) {
    //                  throw new IllegalArgumentException("expected only one part in message for doc/lit");
    //              }
    //              if(child instanceof XmlElement) {
    //                  message = (XmlElement) child;
    //                  message.setParent(null);
    //              } else {
    //                  throw new IllegalArgumentException(
    //                      "illegal state: message part expected to be element not "+child);
    //              }
    //          }
    //
    //          XmlDocument envelope = wrapBodyContent(message);
    //          return envelope;
    //          //return wrapBodyContent(message);
    //        } catch(XmlBuilderException e) {
    //          throw new XsulException("could not create SOAP 1.1 envelope", e);
    //        }
    //    }
    //
    
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





