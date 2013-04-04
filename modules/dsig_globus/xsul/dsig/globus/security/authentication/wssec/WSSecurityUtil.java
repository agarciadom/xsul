/*
 This file is licensed under the terms of the Globus Toolkit Public
 License, found at http://www.globus.org/toolkit/download/license.html.
 */
package xsul.dsig.globus.security.authentication.wssec;


import java.util.Iterator;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.apache.axis.uti.XMLUtils;


public class WSSecurityUtil {
    
    private final static MLogger logger = MLogger.getLogger();
    private static Log log = LogFactory.getLog(WSSecurityUtil.class.getName());
    
    /**
     * Returns first WS-Security header for a given actor.
     * Only one WS-Security header is allowed for an actor.
     */
    public static SOAPHeaderElement getSecurityHeader(
        SOAPEnvelope env,
        String actor
    ) throws SOAPException {
        SOAPHeader header = env.getHeader();
        
        if (header == null) {
            return null;
        }
        
        Iterator headerElements = header.examineHeaderElements(actor);
        
        while (headerElements.hasNext()) {
            SOAPHeaderElement he = (SOAPHeaderElement) headerElements.next();
            Name nm = he.getElementName();
            
            // find ws-security header
            if (
                nm.getLocalName().equalsIgnoreCase(WSConstants.WS_SEC_LN) &&
                nm.getURI().equalsIgnoreCase(WSConstants.WSSE_NS)
            ) {
                return he;
            }
        }
        
        return null;
    }
    
    // all below are DOM based
    
    /**
     * Returns the first WS-Security header element for a given actor
     * Only one WS-Security header is allowed for an actor.
     */
    public static Element getSecurityHeader(
        Document doc,
        String actor
    ) {
        Element soapHeaderElement =
            (Element) getDirectChild(
            doc.getFirstChild(), XmlConstants.S_HEADER, WSConstants.SOAP_NS
        );
        
        // TODO: this can also be slightly optimized
        NodeList list =
            soapHeaderElement.getElementsByTagNameNS(
            WSConstants.WSSE_NS, WSConstants.WS_SEC_LN
        );
        int len = list.getLength();
        Element elem;
        Attr attr;
        String hActor;
        
        for (int i = 0; i < len; i++) {
            elem = (Element) list.item(i);
            attr = elem.getAttributeNodeNS(WSConstants.SOAP_NS, "actor");
            hActor = (attr != null) ? attr.getValue() : null;
            
            if (
               (
                   ((hActor == null) || (hActor.length() == 0)) &&
                   ((actor == null) || (actor.length() == 0))
               ) ||
               (
                   (hActor != null) && (actor != null) &&
                       hActor.equalsIgnoreCase(actor)
               )
            ) {
                return elem;
            }
        }
        
        return null;
    }
    
    public static Node getDirectChild(
        Node fNode,
        String localName,
        String namespace
    ) {
        for (
            Node currentChild = fNode.getFirstChild(); currentChild != null;
            currentChild = currentChild.getNextSibling()
        ) {
            // sometimes, namespace might be null somehow
            if((namespace == null || namespace.equalsIgnoreCase(currentChild.getNamespaceURI())) &&
               localName.equalsIgnoreCase(currentChild.getLocalName())) {
                return currentChild;
            }
        }
        
        return null;
    }
    
    public static Element findFirstBodyElement(Document doc) {
        Element soapBodyElement =
            (Element) WSSecurityUtil.getDirectChild(
            doc.getFirstChild(), "Body", WSConstants.SOAP_NS
        );
        
        if (soapBodyElement == null) {
            soapBodyElement = (Element) WSSecurityUtil.getDirectChild(
                doc.getFirstChild(), "Body", WSConstants.SOAP12_NS
            );
        }
        
        if(soapBodyElement == null)
            logger.finest("yes soapBodyElement is null");

        for (
            Node currentChild = soapBodyElement.getFirstChild();
            currentChild != null;
            currentChild = currentChild.getNextSibling()
        ) {
            if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) currentChild;
            }
        }
        
        return null;
    }
    
    public static String setNamespace(
        Element element,
        String namespace,
        String prefix
    ) {
        String pre = XMLUtils.getPrefix(namespace, element);
        
        if (pre != null) {
            return pre;
        }
        
        element.setAttributeNS(
            "http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, namespace
        );
        
        return prefix;
    }
}
