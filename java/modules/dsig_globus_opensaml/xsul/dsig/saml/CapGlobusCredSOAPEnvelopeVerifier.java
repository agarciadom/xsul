/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: CapGlobusCredSOAPEnvelopeVerifier.java,v 1.4 2005/05/06 04:03:55 lifang Exp $
 */

package xsul.dsig.saml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.security.Principal;
import java.security.cert.X509Certificate;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.globus.gsi.GlobusCredential;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLException;
import org.w3c.dom.Element;
import xsul.MLogger;
import xsul.XsulException;
import xsul.dsig.SignatureInfo;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeVerifier;
import xsul.dsig.saml.authorization.Capability;

//based on org.globus.ogsa.impl.security.authentication.wssec.WSSecurityEngine

public class CapGlobusCredSOAPEnvelopeVerifier extends GlobusCredSOAPEnvelopeVerifier
{
    private final static MLogger logger = MLogger.getLogger();
    private static CapGlobusCredSOAPEnvelopeVerifier instance;
    
    public synchronized static GlobusCredSOAPEnvelopeVerifier getInstance()
    {
        if(instance == null)
        {
            instance = new CapGlobusCredSOAPEnvelopeVerifier();
        }
        return instance;
    }
    
    public static GlobusCredSOAPEnvelopeVerifier getInstance(GlobusCredential cred)
    {
        return getInstance(cred, null);
    }
    
    public static GlobusCredSOAPEnvelopeVerifier getInstance(X509Certificate[] trustedCerts)
        throws XsulException
    {
        instance = new CapGlobusCredSOAPEnvelopeVerifier(trustedCerts);
        return instance;
    }
    
    protected CapGlobusCredSOAPEnvelopeVerifier()
    {
        super();
    }
    
    protected CapGlobusCredSOAPEnvelopeVerifier(X509Certificate[] trustedCerts)
    {
        super(trustedCerts);
    }
    
    
    protected ResourceResolverSpi getResourceResolver()
    {
        logger.finest("getting a SamlSOAPBodyIdResolver");
        return CapSOAPBodyIdResolver.getInstance();
    }
    
    
    //    public SignatureInfo verifySoapMessage(Document envelope)
    //        throws XsulException
    //    {
    //        try {
    //            Document doc = envelope;
    //            //            {
    //            //                OutputStream baos = new ByteArrayOutputStream();
    //            //
    //            //                XMLSerializer serializer = new XMLSerializer (
    //            //                    new PrintWriter(baos) , null);
    //            //                serializer.asDOMSerializer();
    //            //                serializer.serialize(envelope);
    //            //                logger.finest("SIGNATUR2_XML_START\n"+Util.printable(baos.toString())+"\nSIGNATUR2_XML_END\n");
    //            //            }
//
    //            CachedXPathAPI xpathAPI = new CachedXPathAPI();
    //            Element nsctx = doc.createElement("nsctx");
//
    //            nsctx.setAttribute("xmlns:ds", Constants.SignatureSpecNS); //ALEK
//
    //            Element signatureElem = (Element) xpathAPI.selectSingleNode(doc,
    //                                                                        "//ds:Signature", nsctx);
    //            if(signatureElem == null) {
    //                throw new XsulException("could not find ds:Signature in envelope");
    //            }
//
    //            ByteArrayOutputStream serializedSignatureElement = new ByteArrayOutputStream();
    //            XMLUtils.outputDOM(signatureElem, serializedSignatureElement);
    //            logger.finest("signatureElemen=\n"+serializedSignatureElement.toString());
    //            serializedSignatureElement.close();
//
    //            XMLSignature sig = new XMLSignature(signatureElem, "http://extreme.indiana.edu/xmlsecurity");
//
    //            X509Certificate[] certs = null;
    //            KeyInfo info = sig.getKeyInfo();
//
    //            sig.getSignedInfo().addResourceResolver(SOAPBodyIdResolver.getInstance());
//
    //            logger.finest("cano="+sig.getSignedInfo().getCanonicalizationMethodURI()); //(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
    //            //      {
    //            //          OutputStream baos = new ByteArrayOutputStream();
    //            //
    //            //          XMLSerializer serializer = new XMLSerializer (
    //            //              new PrintWriter(baos) , null);
    //            //          serializer.asDOMSerializer();
    //            //          serializer.serialize(signatureElem);
    //            //          logger.finest("SIGNATURE_XML_START\n"+Util.printable(baos.toString())+"\nSIGNATURE_XML_END\n");
    //            //      }
    //            SignedInfo sinfo = sig.getSignedInfo();
//
    //            if (info.containsX509Data())
    //            {
    //                logger.info("keyinfo contains x509 data");
    //                certs = getCertificatesX509Data(info);
    //            }
    //            else
    //            {
    //                logger.info("try to get x509 data from security token");
    //                Node node =
    //                    WSSecurityUtil.getDirectChild(
    //                    info.getElement(),
    //                    SecurityTokenReference.TOKEN.getLocalPart(),
    //                    SecurityTokenReference.TOKEN.getNamespaceURI()
    //                );
//
    //                if (node == null)
    //                {
    //                    throw new WSSecurityException(
    //                        WSSecurityException.INVALID_SECURITY, "unsupportedKeyInfo",
    //                        null
    //                    );
    //                }
    //                else
    //                {
    //                    certs = getCertificatesTokenReference((Element) node);
    //                    logger.info("cert0: " + certs[0]);
    //                }
    //            }
    //            try {
    //                certs[0].checkValidity();
    //            } catch(java.security.cert.CertificateExpiredException ex) {
    //                throw new XsulException(
    //                    "expired certificate identifed by "+certs[0].getSubjectDN()
    //                        +" used for signing of message certificate is "+certs[0], ex);
    //            }
//
    //            if (!sig.checkSignatureValue(certs[0])) {
    //                throw new XsulException(
    //                    "failed signature check - signature is not validated by certifcate "+certs[0]);
    //            }
    //            sig.setFollowNestedManifests(false);
//
    //            Principal subjectDn = certs[0].getSubjectDN();
//
    //            // select form list of trusted certificates only that could be used to certify signing key
    //            //TrustedCertificates tc = TrustedCertificates.getDefaultTrustedCertificates();
    //            //X509Certificate[] trustedCerts = tc.getCertificates();
    //            //            if(trustedCerts != null) {
    //            //                String subjectDnAsString = subjectDn.toString();
    //            //                for(int i = 0; i < trustedCerts.length; i++) {
    //            //                    X509Certificate trustedCert = trustedCerts[i];
    //            //                    String trustedCertIssuerDn = trustedCert.getIssuerDN().toString();
    //            //                    // cut off cn=...
    //            //                    int pos;
    //            //                    if((pos = trustedCertIssuerDn.toLowerCase().lastIndexOf(",cn=")) != -1) {
    //            //                        trustedCertIssuerDn = trustedCertIssuerDn.substring(0, pos);
    //            //                    }
    //            //                    if(subjectDnAsString.indexOf(trustedCertIssuerDn) >= 0) {
    //            //                        Key pk = trustedCert.getPublicKey();
    //            //                        verify = checkSignatureValue( sig, pk );
    //            //                    }
    //            //                    if(verify) {
    //            //                        break;
    //            //                    }
    //            //                }
    //            //            }
    //            if(trustedCerts != null) {
    //                ProxyPathValidator validator = new ProxyPathValidator();
    //                try {
    //                    validator.validate(certs, trustedCerts);
    //                } catch (ProxyPathValidatorException e) {
    //                    // last ditch effort - we always accept self issued proxies ..
    //                    //boolean verify = sig.checkSignatureValue(cred.getCertificateChain()[0]);
    //                    Key pk = cred.getCertificateChain()[0].getPublicKey();
    //                    boolean verify = checkSignatureValue( sig, pk );
    //                    if(!verify) {
    //                        throw new XsulException(
    //                            "could not verify signature for "+serializedSignatureElement.toString(), e);
    //                    }
    //                }
    //            } else {
    //                Key pk = cred.getCertificateChain()[0].getPublicKey();
    //                boolean verify = checkSignatureValue( sig, pk );
    //                if(!verify) {
    //                    throw new XsulException(
    //                        "could not verify signature for message signed by "+subjectDn
    //                            +" using "+cred.getCertificateChain()[0]);
    //                }
    //            }
//
    //            //boolean verify = sig.checkSignatureValue(certs[0]);
//
    //            logger.finest("The signature is valid");
//
//
//
//
//
//
    //            return extractSignatureInfo(subjectDn, signatureElem);
//
//
    //        } catch (Exception e) {
    //            throw new XsulException("could not verify signature "+e, e);
    //        }
    //    }
    
    protected SignatureInfo extractSignatureInfo(Principal subjectDn, Element signatureElem)
        throws Exception
    {
        Capability cap = null;
        
        Element assertion = (Element)signatureElem.getNextSibling();
        if(assertion == null)
        {
            logger.finest("no assertion available!!!");
        }
        else
        {
            if(logger.isFinestEnabled())
            {
                ByteArrayOutputStream serializedAssersionElement = new ByteArrayOutputStream();
                XMLUtils.outputDOM(assertion, serializedAssersionElement);
                logger.finest("assertionElemen=\n"+serializedAssersionElement.toString());
                serializedAssersionElement.close();
            }
            
            SAMLAssertion sa = new SAMLAssertion(assertion);
            SAMLAssertion[] assertions = {sa};
            cap = new Capability(Arrays.asList(assertions));
            
            logger.finest("capabiltiy generated: "+cap.toString());
        }
        
        return new CapSignatureInfo(subjectDn, cap);
        
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





