/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: CapGlobusCredSOAPEnvelopeSigner.java,v 1.3 2004/04/25 23:52:27 lifang Exp $
 */

package xsul.dsig.saml;

import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.globus.gsi.GlobusCredential;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import xsul.MLogger;
import xsul.XsulException;
import xsul.dsig.SOAPEnvelopeSigner;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeSigner;
import xsul.dsig.globus.security.authentication.wssec.PKIPathSecurityToken;
import java.io.ByteArrayOutputStream;
import org.apache.xml.security.utils.XMLUtils;
import java.io.IOException;

//based on org.globus.ogsa.impl.security.authentication.wssec.WSSecurityEngine

public class CapGlobusCredSOAPEnvelopeSigner extends GlobusCredSOAPEnvelopeSigner
{
    private final static MLogger logger = MLogger.getLogger();
    
    private static CapGlobusCredSOAPEnvelopeSigner instance;
    
    public synchronized static SOAPEnvelopeSigner getInstance()
    {
        if(instance == null)
        {
            instance = new CapGlobusCredSOAPEnvelopeSigner();
        }
        return instance;
    }
    
    public static SOAPEnvelopeSigner getInstance(GlobusCredential cred)
        throws XsulException
    {
        if(cred ==null)
        {
            throw new XsulException("globus credential can not be null");
        }
        else
        {
            return new CapGlobusCredSOAPEnvelopeSigner(cred);
        }
    }
    
    
    protected CapGlobusCredSOAPEnvelopeSigner()
    {
        super();
    }
    
    protected CapGlobusCredSOAPEnvelopeSigner(GlobusCredential cred)
    {
        super(cred);
    }
    
    protected ResourceResolverSpi getResourceResolver()
    {
        return CapSOAPBodyIdResolver.getInstance();
    }
    
    
    
    //    public Document signSoapMessage(Document envelope)
    //        throws XsulException
    //    {
    //        try {
    //            Document doc = envelope;
//
    //            Element root = (Element)doc.getFirstChild();
    //            Element body = (Element)root.getLastChild();
    //            Element header = (Element) WSSecurityUtil.getDirectChild(
    //                root, "Header", null
    //            );
    ////???            Element header = (Element) WSSecurityUtil.getDirectChild(
    ////                root, "Header", WSConstants.SOAP_NS
    ////            );
    //            if(header == null) {
    //                logger.finest("\n>>>>>>> cannot find header. making new header. ");
    ////              header = doc.createElementNS(WSConstants.SOAP_NS, "Header");
    //                header = doc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Header");
//
    //                root.insertBefore(header, body);
    //            }
    //            Element wssec = (Element) WSSecurityUtil.getDirectChild(
    //                header, "Security", WSConstants.WSSE_NS
    //            );
    //            if(wssec == null) {
    //                logger.finest("\n>>>>>>> cannot find wssec. making new wssec. ");
    //                wssec = doc.createElementNS(WSConstants.WSSE_NS, "Security");
    //                header.appendChild(wssec);
    //            }
//
    //            String id = addBodyID(doc);
    //            String uri = "token" + System.currentTimeMillis();
//
    //            XMLSignature sig = new XMLSignature(doc,
    //                                                "http://extreme.indiana.edu/xmlsecurity",
    //                                                XMLSignature.ALGO_ID_SIGNATURE_RSA,
    //                                                Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
    //            sig.getSignedInfo().addResourceResolver(SOAPBodyIdResolver.getInstance());
//
    //            //              Transforms transforms = new Transforms(doc);
    //            //              transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
    //            //              sig.addDocument("#"+id, transforms);
    //            sig.addDocument("#"+id);
//
    //            Reference ref = new Reference(doc);
    //            ref.setURI("#" + uri);
//
    //            SecurityTokenReference secRef = new SecurityTokenReference(doc);
    //            secRef.setReference(ref);
    //            KeyInfo info = sig.getKeyInfo();
    //            info.addUnknownElement(secRef.getElement());
//
    //            GlobusCredential gc = getGlobusCredential();
    //            if(gc == null) {
    //                throw new XsulException("Globus Credential not found!");
    //            }
    //            sig.getSignedInfo().generateDigestValues();
    //            sig.sign(cred.getPrivateKey());
//
    //            X509Certificate[] certs = cred.getCertificateChain();
//
    //            logger.finest("signed with cred="+cred);
//
//
    //            PKIPathSecurityToken token = new PKIPathSecurityToken(doc);
    //            token.setX509Certificates(certs, true);
    //            token.setID(uri);
//
//
//
//
    //            doAdditionalSigning(wssec, token, sig);
//
//
//
//
//
//
    //            Key pk = cred.getCertificateChain()[0].getPublicKey();
    //            boolean verify; // = sig.checkSignatureValue(pk);
    //            verify = GlobusCredSOAPEnvelopeVerifier.checkSignatureValue(sig, pk);
    //            logger.finest("\n\n\n\nverify1="+verify);
    //            if(!verify) {
    //                throw new RuntimeException("verify1 failed");
    //            }
//
    //            pk = certs[0].getPublicKey();
    //            //verify = sig.checkSignatureValue(certs[0]);
    //            verify = GlobusCredSOAPEnvelopeVerifier.checkSignatureValue(sig, pk);
    //            if(!verify) {
    //                throw new XsulException("verify2 failed");
    //            }
    //            logger.finest("\n\n\n\nverify2="+verify);
    //            return doc;
    //        } catch (Exception e) {
    //            throw new XsulException("could not sign message "+e, e);
    //        }
    //    }
    
    /**
     * Method doAdditionalSigning
     *   If wssec has other children such as policy assertions, the
     *   signature should be inserted after them.
     * @param    wssec               an Element
     * @param    token               a  PKIPathSecurityToken
     * @param    sig                 a  XMLSignature
     *
     * @return   a boolean
     *
     * @exception   DOMException
     *
     */
    protected boolean doAdditionalSigning(Element wssec, PKIPathSecurityToken token, XMLSignature sig) throws DOMException
    {
        // TODO multiple assertions
        // assertion here is only for signature being inserted before it
        Element assertion = (Element)wssec.getFirstChild();
        
        if(assertion != null)
        {
            logger.finest("inserting signature after assertion");
            wssec.insertBefore(token.getElement(), assertion);
            wssec.insertBefore(sig.getElement(), assertion);
            return true;
        }
        else
        {
            logger.finest("\n>>>>>>> cannot find assertions.");
            if(logger.isFinestEnabled()) {
                ByteArrayOutputStream wssecElem = new ByteArrayOutputStream();
                XMLUtils.outputDOM(wssec, wssecElem);
                logger.finest("wssecElemen=\n"+wssecElem.toString());
                try
                {
                    wssecElem.close();
                }
                catch (IOException e) {}
            }
            
            return false;
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


