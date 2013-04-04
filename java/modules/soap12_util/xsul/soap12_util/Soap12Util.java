/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Soap12Util.java,v 1.13 2006/04/30 06:48:13 aslom Exp $
 */

package xsul.soap12_util;
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

/**
 * SOAP 1.2 envelope manipulations utility methods.
 *
 * @version $Revision: 1.13 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class Soap12Util extends SoapUtil {
    
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public final static String NS_URI_SOAP12 = XmlConstants.NS_URI_SOAP12;
    
    public final static String SOAP12_ENC_PREFIX = "SOAP-ENC";
    
    public final static XmlNamespace SOAP12_ENC_NS = builder.newNamespace(
        SOAP12_ENC_PREFIX, "http://www.w3.org/2001/06/soap-encoding");
    
    public final static XmlNamespace SOAP12_NS = XmlConstants.SOAP12_NS;
    
    public static final String URI_SOAP12_NEXT_ROLE =
        "http://www.w3.org/2003/05/soap-envelope/role/next";
    public static final String URI_SOAP12_NONE_ROLE =
        "http://www.w3.org/2003/05/soap-envelope/role/none";
    public static final String URI_SOAP12_ULTIMATE_ROLE =
        "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver";
    
    public static final String ELEM_ENVELOPE = XmlConstants.S_ENVELOPE ;
    public static final String ELEM_HEADER = XmlConstants.S_HEADER ;
    public static final String ELEM_BODY = XmlConstants.S_BODY;
    
    public static final String ATTR_MUST_UNDERSTAND = "mustUnderstand" ;
    public static final String ATTR_ACTOR = "actor" ;
    public static final String ATTR_ROLE = "role" ;
    
    private final static Soap12Util instance = new Soap12Util();
    
    Soap12Util() {
    }
    
    
    public static Soap12Util getInstance() {
        return instance;
    }
    
    public String getSoapVersion() {
        return "1.2";
    }
    
    public boolean isSoapEnvelopeSupported(XmlElement root)
    {
        if (NS_URI_SOAP12.equals(root.getNamespace().getNamespaceName())
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
        
        XmlElement envelope = doc.addDocumentElement(SOAP12_NS, ELEM_ENVELOPE);
        envelope.declareNamespace(SOAP12_NS);
        //declare prefixes for some common namespaces
        envelope.declareNamespace(XmlConstants.XS_NS);
        envelope.declareNamespace(XmlConstants.XSI_NS);
        
        XmlElement body = envelope.addElement(SOAP12_NS, "Body");
        body.addElement(bodyContent);
        
        return doc;
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
        // check if message has Envelope/Body
        if(!NS_URI_SOAP12.equals(root.getNamespaceName())) {
            throw new XsulException ("expected SOAP 1.2 Envelope not "+root);
        }
        if(!ELEM_ENVELOPE.equals(root.getName())) {
            throw new XsulException (
                "expected top level SOAP 1.2 element name to be Envelope not "+root);
        }
        XmlElement body = root.findElementByName(NS_URI_SOAP12, "Body");
        if(body == null) {
            throw new XsulException (
                "missing required Body element in SOAP 1.1 Envelope"+root);
        }
        //TODO get first non ignorable child element
        // add  XPath to make it easier
        XmlElement content = (XmlElement) body.requiredElementContent().iterator().next();
        return content;
    }
    
    
    
    //    public XmlElement findBodyContent(XmlDocument respDoc)
    //        throws XsulException
    //    {
    //        // check if response has Envelope/Body
    //        XmlElement root = respDoc.getDocumentElement();
    //        if(!NS_URI_SOAP12.equals(root.getNamespaceName())) {
    //            throw new XsulException ("expected SOAP 1.2 Envelope not "+root);
    //        }
    //        if(!"Envelope".equals(root.getName())) {
    //            throw new XsulException (
    //                "expected top level SOAP 1.2 element name to be Envelope not "+root);
    //        }
    //        XmlElement body = root.findElementByName(NS_URI_SOAP12, "Body");
    //        //TODO get first non ignorable child element
    //        // add  XPath to make it easier
    //        XmlElement content = (XmlElement) body.requiredElementContent().iterator().next();
    //        return content;
    //    }
    
    
    public XmlElement generateSoapFault(XmlNamespace faultCodeValueNs,
                                        String faultCodeValueName,
                                        String reasonTextEnglish,
                                        Throwable ex)
        throws XsulException
    {
        return generateSoap12Fault(faultCodeValueNs,
                                   faultCodeValueName,
                                   reasonTextEnglish,
                                   ex);
    }
    
    public XmlElement generateSoapClientFault(String reasonTextEnglish, Throwable ex) throws XsulException {
        //http://www.w3.org/TR/soap12-part1/#tabsoapfaultcodes
        return generateSoapFault(SOAP12_NS, "Sender", reasonTextEnglish, ex);
    }
    
    public XmlElement generateSoapServerFault(String reasonTextEnglish, Throwable ex) throws XsulException {
        //http://www.w3.org/TR/soap12-part1/#tabsoapfaultcodes
        return generateSoapFault(SOAP12_NS, "Receiver", reasonTextEnglish, ex);
    }
    
    public XmlElement generateSoap12Fault(XmlNamespace  faultCodeValueNs,
                                          String faultCodeValueName,
                                          String reasonTextEnglish,
                                          Throwable ex)
    {
        return generateSoap12Fault(faultCodeValueNs,
                                   faultCodeValueName,
                                   null,
                                   null,
                                   reasonTextEnglish,
                                   ex);
    }
    
    public XmlElement generateSoap12Fault(XmlNamespace  faultCodeValueNs,
                                          String faultCodeValueName,
                                          String faultSubcodeValueNs,
                                          String faultSubcodeValueName,
                                          String reasonTextEnglish,
                                          Throwable ex)
    {
        XmlElement faultEl = builder.newFragment( SOAP12_NS, "Fault" );
        if(faultCodeValueNs == null) {
            faultCodeValueNs = SOAP12_NS;
        }
        if(! NS_URI_SOAP12.equals(faultCodeValueNs.getNamespaceName())) {
            faultEl.declareNamespace("n", faultCodeValueNs.getNamespaceName());
        }
        
        
        //http://www.w3.org/TR/2003/REC-soap12-part1-20030624/#faultcodeelement
        XmlElement codeEl = faultEl.addElement(SOAP12_NS, "Code");
        XmlElement valueEl = codeEl.addElement(SOAP12_NS, "Value");
        valueEl.addChild(new QNameElText(valueEl, faultCodeValueNs.getNamespaceName(), faultCodeValueName));
        
        //http://www.w3.org/TR/2003/REC-soap12-part1-20030624/#faultsubcodeelement
        if(faultSubcodeValueName != null) {
            if(!NS_URI_SOAP12.equals(faultSubcodeValueNs)) {
                faultEl.declareNamespace("m", faultSubcodeValueNs);
            }
            XmlElement subCodeEl = faultEl.addElement(SOAP12_NS, "Subcode");
            XmlElement subValueEl = subCodeEl.addElement(SOAP12_NS, "Value");
            subValueEl.addChild(new QNameElText(subValueEl, faultSubcodeValueNs, faultSubcodeValueName));
        }
        
        //http://www.w3.org/TR/2003/REC-soap12-part1-20030624/#faultstringelement
        XmlElement reasonEl = faultEl.addElement(SOAP12_NS, "Reason");
        XmlElement textEl = reasonEl.addElement(SOAP12_NS, "Text");
        textEl.addAttribute(XmlConstants.XML_NS, "lang", "en-US");
        if(reasonTextEnglish == null) reasonTextEnglish = "{null}";
        textEl.addChild(reasonTextEnglish);
        
        XmlElement detail = faultEl.addElement(SOAP12_NS, "Detail");
        if(ex != null) {
            final XmlNamespace axisNs = faultEl.newNamespace("n", "http://xml.apache.org/axis/");
            
            String msg =ex.getMessage();
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
    }
    
    
}

