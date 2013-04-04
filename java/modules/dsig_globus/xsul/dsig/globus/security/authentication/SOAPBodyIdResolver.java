/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.globus.security.authentication;

import xsul.dsig.globus.security.authentication.wssec.WSConstants;
import xsul.dsig.globus.security.authentication.wssec.WSSecurityUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.apache.xml.utils.URI;
import org.apache.xpath.CachedXPathAPI;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Set;


/**
 * This resolver is used for resolving same-document URI like URI="#id".
 * It is desgined to only work with SOAPEnvelopes. It looks for the Id in the
 * first child elemenet of the Body element.
 *
 */
public class SOAPBodyIdResolver extends ResourceResolverSpi {
    private static Log logger =
        LogFactory.getLog(SOAPBodyIdResolver.class.getName());
    private static SOAPBodyIdResolver resolver;

    public synchronized static ResourceResolverSpi getInstance() {
        if (resolver == null) {
            resolver = new SOAPBodyIdResolver();
        }

        return resolver;
    }

    public XMLSignatureInput engineResolve(
        Attr uri,
        String BaseURI
    ) throws ResourceResolverException {
        String uriNodeValue = uri.getNodeValue();
        NodeList resultNodes = null;
        Document doc = uri.getOwnerDocument();

        // this must be done so that Xalan can catch ALL namespaces
        XMLUtils.circumventBug2650(doc);

        CachedXPathAPI cXPathAPI = new CachedXPathAPI();

        /*
         * URI="#chapter1"
         * Identifies a node-set containing the element with ID attribute
         * value 'chapter1' of the XML resource containing the signature.
         * XML Signature (and its applications) modify this node-set to
         * include the element plus all descendents including namespaces and
         * attributes -- but not comments.
         */
        String id = uriNodeValue.substring(1);

        Element selectedElem = WSSecurityUtil.findFirstBodyElement(doc);

        if (selectedElem == null) {
            throw new ResourceResolverException(
                "generic.EmptyMessage",
                new Object[] { "Body element not found" }, uri, BaseURI
            );
        }

        String cId = selectedElem.getAttributeNS(WSConstants.WSU_NS, "Id");

        if ((cId == null) || (cId.length() == 0)) {
            cId = selectedElem.getAttributeNS(WSConstants.SOAP_SEC_NS, "id");
        }

        if (!id.equals(cId)) {
        		selectedElem = (Element)selectedElem.getParentNode();
        		cId = selectedElem.getAttributeNS(WSConstants.WSU_NS, "Id");

            if ((cId == null) || (cId.length() == 0)) {
                cId = selectedElem.getAttributeNS(WSConstants.SOAP_SEC_NS, "id");
            }

            if (!id.equals(cId)) {
	            throw new ResourceResolverException(
	                "generic.EmptyMessage", new Object[] { "Id not found" }, uri,
	                BaseURI
	            );
            }     
        }

        try {
            resultNodes =
                cXPathAPI.selectNodeList(
                    selectedElem,
                    Canonicalizer.XPATH_C14N_WITH_COMMENTS_SINGLE_NODE//.XPATH_C14N_OMIT_COMMENTS_SINGLE_NODE
                );
        } catch (javax.xml.transform.TransformerException ex) {
            throw new ResourceResolverException(
                "generic.EmptyMessage", ex, uri, BaseURI
            );
        }

        Set resultSet = XMLUtils.convertNodelistToSet(resultNodes);
        XMLSignatureInput result = new XMLSignatureInput(resultSet);//, cXPathAPI);

        result.setMIMEType("text/xml");

        try {
            URI uriNew = new URI(new URI(BaseURI), uri.getNodeValue());
            result.setSourceURI(uriNew.toString());
        } catch (URI.MalformedURIException ex) {
            result.setSourceURI(BaseURI);
        }

        return result;
    }

    public boolean engineCanResolve(
        Attr uri,
        String BaseURI
    ) {
        if (uri == null) {
            return false;
        }

        String uriNodeValue = uri.getNodeValue();

        return uriNodeValue.startsWith("#");
    }
}
