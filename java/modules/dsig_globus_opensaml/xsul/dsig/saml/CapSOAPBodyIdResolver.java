/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.saml;

import java.util.Set;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.apache.xml.utils.URI;
import org.apache.xpath.CachedXPathAPI;
import org.opensaml.XML;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.globus.security.authentication.wssec.WSConstants;
import xsul.dsig.globus.security.authentication.wssec.WSSecurityUtil;


/**
 * This resolver is used for resolving same-document URI like URI="#id".
 * It is desgined to only work with SOAPEnvelopes. It looks for the Id in the
 * first child elemenet of the Body element.
 *
 */
public class CapSOAPBodyIdResolver extends ResourceResolverSpi {
    private final static MLogger logger = MLogger.getLogger();
    private static ResourceResolverSpi resolver;

    /**
     * Method getInstance
     *
     * @return   a ResourceResolverSpi
     *
     */
    public synchronized static ResourceResolverSpi getInstance() {
        if (resolver == null) {
            resolver = new CapSOAPBodyIdResolver();
        }

        return resolver;
    }

    public XMLSignatureInput engineResolve(
        Attr uri,
        String BaseURI
    ) throws ResourceResolverException {
        
        logger.finest("\n>>>>>>> entering soapbodyidresolver engineResolve  <<<<<<<<");

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
        logger.finest("id: " + id);
        
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
        logger.finest("cId: " + cId);

        //////////// LIANG begin
        if (!id.equals(cId)) {
            logger.finest("yes, id is not equal to cId");
            Element header = (Element) WSSecurityUtil.getDirectChild(
                doc.getFirstChild(), XmlConstants.S_HEADER, null
            );
            logger.finest("got header");
            Element wssec = (Element) WSSecurityUtil.getDirectChild(
                header, "Security", WSConstants.WSSE_NS
            );
            logger.finest("got wssec");
            Element assertion = (Element) WSSecurityUtil.getDirectChild(
                wssec, "Assertion", XML.SAML_NS
            );
            logger.finest("got assertion");
            
            cId = assertion.getAttributeNS(WSConstants.WSU_NS, "Id");
            logger.finest("getting cId ... ");
            if ((cId == null) || (cId.length() == 0)) {
                logger.finest("first cId null ");
                cId = assertion.getAttributeNS(WSConstants.SOAP_SEC_NS, "id");
                logger.finest("tried again ");
            }
            
            if ((cId == null) || (cId.length() == 0)) {
                logger.finest("second cId null ");
                cId = assertion.getAttribute("AssertionID");
                logger.finest("tried again ");
            }

            logger.finest("saml cId: " + cId);
            if(id.equals(cId))
                selectedElem = assertion;
        }
        logger.finest("it reaches here");
        //////////// LIANG end
        
        if (!id.equals(cId)) {
            throw new ResourceResolverException(
                "generic.EmptyMessage", new Object[] { "Id not found" }, uri,
                BaseURI
            );
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
        logger.finest("XMLSignatureInput Result: "+result.toString());
        logger.finest("\n>>>>>>> leaving soapbodyidresolver engineResolve  <<<<<<<<");
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
