/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SOAPEnvelopeSigner.java,v 1.8 2006/03/08 09:13:35 aslom Exp $
 */

package xsul.dsig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.Init;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.dsig.apache.axis.uti.XMLUtils;
import xsul.dsig.globus.security.authentication.wssec.WSConstants;
import xsul.dsig.globus.security.authentication.wssec.WSSecurityUtil;

public abstract class SOAPEnvelopeSigner
{
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    protected String actor;
    protected String baseURI;
    
    private static DocumentBuilderFactory dbfNonValidating;
    private static DocumentBuilderFactory dbfValidating;
    
    static {
        Init.init();
        dbfNonValidating = DocumentBuilderFactory.newInstance();
        dbfNonValidating.setNamespaceAware(true);
        dbfValidating = DocumentBuilderFactory.newInstance();
        dbfValidating.setNamespaceAware(true);
        dbfValidating.setValidating(true);
    }
    
    public abstract Document signSoapMessage(Document envelope)
        throws XsulException;
    
    
    public String signSoapMessage(String envelope)
        throws XsulException
    {
        return signSoapMessage(envelope, false);
    }
    
    public String signSoapMessage(String envelope, boolean validation)
        throws XsulException
    {
        return signSoapMessage(envelope, validation ? dbfValidating : dbfNonValidating);
    }
    
    public String signSoapMessage(String envelope, DocumentBuilderFactory dbf)
        throws XsulException
    {
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(envelope.getBytes()));
            
            Document env = signSoapMessage(doc);
            
            ByteArrayOutputStream bf = new ByteArrayOutputStream();
            XMLUtils.DocumentToStream(env, bf);
            String soapenv = bf.toString();
            bf.close();
            return soapenv;
        } catch (ParserConfigurationException e) {
            throw new XsulException("could not sign message "+e, e);
        } catch (SAXException e) {
            throw new XsulException("could not sign message "+e, e);
        } catch (IOException e) {
            throw new XsulException("could not sign message "+e, e);
        } catch (FactoryConfigurationError e) {
            throw new XsulException("could not sign message "+e, e);
        }
        
    }
    
    public XmlDocument signSoapMessage(XmlDocument envelope)
        throws XsulException
    {
        return signSoapMessage(envelope, false);
    }
    
    public XmlDocument signSoapMessage(XmlDocument envelope, boolean validation)
        throws XsulException
    {
        try {
            String soapmsg = builder.serializeToString(envelope);
            String signed =  signSoapMessage(soapmsg, validation);
            //logger.finest("signed="+signed);
            XmlDocument signedDoc = builder.parseReader(new StringReader(signed));
            removeDuplicateNamespaceDeclarations(signedDoc);
            return signedDoc;
        } catch (Exception e) {
            throw new XsulException("could not sign envelope "+builder.serializeToString(envelope), e);
        }
    }
    
    public String getActor()
    {
        return (this.actor == null) ? "" : this.actor;
    }
    
    public String getBaseURI()
    {
        return (this.baseURI == null) ? "" : this.baseURI;
    }
    
    public void setActor(String actor)
    {
        this.actor = actor;
    }
    
    protected String addBodyID(Document doc) throws Exception
    {
        Element bodyElement = WSSecurityUtil.findFirstBodyElement(doc);
        
        if (bodyElement == null)
        {
            throw new Exception("Element node not found");
        }
        
        String id = bodyElement.getAttributeNS(WSConstants.WSU_NS, "Id");
        
        if ((id == null) || (id.length() == 0))
        {
            id = "digestSource";
            
            String prefix =
                WSSecurityUtil.setNamespace(
                bodyElement, WSConstants.WSU_NS, WSConstants.WSU_PREFIX
            );
            bodyElement.setAttributeNS(WSConstants.WSU_NS, prefix + ":Id", id);
        }
        
        logger.info("id: "+id);
        
        return id;
    }
    
    
    private void removeDuplicateNamespaceDeclarations(XmlDocument signedDoc) {
        //remove duplicates from top element and all children
        removeDuplicateNamespaceDeclarations(signedDoc.getDocumentElement());
        
        //        Iterator iter = signedDoc.getDocumentElement().children();
        //        while(iter.hasNext()) {
        //            Object child = iter.next();
        //            if(child instanceof XmlElement) {
        //                removeDuplicateNamespaceDeclarations((XmlElement) child);
        //            }
        //        }
    }
    
    private void removeDuplicateNamespaceDeclarations(XmlElement el) {
        // NOTE: this method will de-c18n input XML element - makes it *much* easier to read XML ...
        XmlContainer parent = el.getParent();
        // only remove duplicates if element has parent element with NS declarations
        if(parent != null && (parent instanceof XmlElement)) {
            // walk list of namespaces -- store list of non-duplicates -- then replace (if any)
            List preservedNs = new LinkedList();
            {
                XmlElement parentEl = (XmlElement) parent;
                Iterator iter = el.namespaces();
                while(iter.hasNext()) {
                    XmlNamespace ns = (XmlNamespace) iter.next();
                    XmlNamespace parentNs =  parentEl.lookupNamespaceByPrefix(ns.getPrefix());
                    if(parentNs == null || !parentNs.getNamespaceName().equals(ns.getNamespaceName())) {
                        preservedNs.add(ns);
                    }
                }
            }
            el.removeAllNamespaceDeclarations();
            if(!preservedNs.isEmpty()) {
                Iterator iter = preservedNs.iterator();
                while(iter.hasNext()) {
                    XmlNamespace ns = (XmlNamespace) iter.next();
                    el.declareNamespace(ns);
                }
            }
        }
        
        // for all children that are XmlElement call recursively
        Iterator iter = el.children();
        while(iter.hasNext()) {
            Object child = iter.next();
            if(child instanceof XmlElement) {
                XmlElement childEl = (XmlElement) child;
                String name = childEl.getName();
                if(!"Assertion".equals(name)) { // do not mess with SAML!
                    removeDuplicateNamespaceDeclarations(childEl);
                }
            }
        }
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



