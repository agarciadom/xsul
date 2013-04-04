/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.globus.security.authentication.wssec;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xpath.XPathAPI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;


/**
 * This resolver is used for resolving same-document URI like URI="#id".
 * It is desgined to only work with SOAPEnvelopes. It looks for the Id in the first
 * child elemenet of the Body element.
 *
 */
public class WSSecurityIdResolver {
    private static Log logger =
        LogFactory.getLog(WSSecurityIdResolver.class.getName());
    private static WSSecurityIdResolver resolver;

    public synchronized static WSSecurityIdResolver getInstance() {
        if (resolver == null) {
            resolver = new WSSecurityIdResolver();
        }

        return resolver;
    }

    public Element getElementById(
        Document doc,
        String id
    ) {
        if (id == null) {
            return null;
        }

        id = id.trim();

        if ((id.length() == 0) || (id.charAt(0) != '#')) {
            return null;
        }

        id = id.substring(1);

        try {
            Element nscontext =
                XMLUtils.createDSctx(doc, "wsu", WSConstants.WSU_NS);

            Element element =
                (Element) XPathAPI.selectSingleNode(
                    doc, "//*[@wsu:Id='" + id + "']", nscontext
                );

            return element;
        } catch (TransformerException ex) {
            logger.error(ex);
        }

        return null;
    }
}
