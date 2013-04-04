/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: GlobusCredSOAPEnvelopeSigner.java,v 1.18 2006/04/30 06:48:12 aslom Exp $
 */

package xsul.dsig.globus;

import java.io.ByteArrayOutputStream;
import java.security.cert.X509Certificate;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.dsig.SOAPEnvelopeSigner;
import xsul.dsig.globus.security.authentication.SOAPBodyIdResolver;
import xsul.dsig.globus.security.authentication.wssec.PKIPathSecurityToken;
import xsul.dsig.globus.security.authentication.wssec.Reference;
import xsul.dsig.globus.security.authentication.wssec.SecurityTokenReference;
import xsul.dsig.globus.security.authentication.wssec.WSConstants;
import xsul.dsig.globus.security.authentication.wssec.WSSecurityUtil;

//based on org.globus.ogsa.impl.security.authentication.wssec.WSSecurityEngine

public class GlobusCredSOAPEnvelopeSigner extends SOAPEnvelopeSigner {
    private final static MLogger logger = MLogger.getLogger();
    private static GlobusCredSOAPEnvelopeSigner instance;
    
    protected GlobusCredential cred;
    
    static {
        Init.init();
    }
    
    public synchronized static SOAPEnvelopeSigner getInstance() {
        if(instance == null) {
            instance = new GlobusCredSOAPEnvelopeSigner();
        }
        return instance;
    }
    
    public static SOAPEnvelopeSigner getInstance(GlobusCredential cred)
        throws XsulException {
        if(cred ==null) {
            throw new XsulException("globus credential can not be null");
        } else {
            return new GlobusCredSOAPEnvelopeSigner(cred);
        }
    }
    
    
    protected GlobusCredSOAPEnvelopeSigner() {
        useGlobusCredentialbyDefault();
    }
    
    protected GlobusCredSOAPEnvelopeSigner(GlobusCredential cred) {
        if(cred == null) throw new IllegalArgumentException();
        this.cred = cred;
    }
    
    public GlobusCredential getGlobusCredential() {
        //if(cred == null)
        //    return useGlobusCredentialbyDefault();
        //assert cred != null;
        return cred;
    }
    
    private GlobusCredential useGlobusCredentialbyDefault() throws XsulException {
        try {
            cred = GlobusCredential.getDefaultCredential();
        }
        catch(GlobusCredentialException e) {
            throw new XsulException("could not obtain default globus credential", e);
        }
        
        return cred;
    }
    
    protected ResourceResolverSpi getResourceResolver() {
        return SOAPBodyIdResolver.getInstance();
    }
    
    public Document signSoapMessage(Document envelope)
        throws XsulException {
        try {
            Document doc = envelope;
            
            
            Element root = (Element)doc.getFirstChild();
            if(logger.isFinestEnabled()) {
                ByteArrayOutputStream rootElem = new ByteArrayOutputStream();
                XMLUtils.outputDOM(root, rootElem);
                logger.finest("rootElemen=\n"+rootElem.toString());
                //              try
                //              {
                //                  rootElem.close();
                //              } catch (Exception e) {}
            }
            
            Element body = (Element)root.getFirstChild();
            
            // Before adding a header, must sure there is not an existing header
            Element header = (Element) WSSecurityUtil.getDirectChild(root, XmlConstants.S_HEADER, WSConstants.SOAP_NS);
            //            Element header = (Element) WSSecurityUtil.getDirectChild(root, "Header", null);
            if(header == null) {
                logger.finest(">>>>>>> cannot find header. making new header. ");
                header = doc.createElementNS(WSConstants.SOAP_NS, XmlConstants.S_HEADER);
                header.setPrefix(root.getPrefix());
                root.insertBefore(header, body);
            }
            
            // Before adding a wssec Security, must sure there is not an existing wssec
            Element wssec = (Element) WSSecurityUtil.getDirectChild(header, "Security", WSConstants.WSSE_NS);
            if(wssec == null) {
                logger.finest("\n>>>>>>> cannot find wssec. making new wssec. ");
                wssec = doc.createElementNS(WSConstants.WSSE_NS, "wsse:Security");
                header.appendChild(wssec);
            }
            
            
            String id = addBodyID(doc);
            String uri = "token" + System.currentTimeMillis();
            
            XMLSignature sig = new XMLSignature(doc,
                                                "http://extreme.indiana.edu/xmlsecurity",
                                                XMLSignature.ALGO_ID_SIGNATURE_RSA,
                                                Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            sig.getSignedInfo().addResourceResolver(getResourceResolver());
            
            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
            sig.addDocument("#"+id, transforms);
//          sig.addDocument("#"+id);
            Reference ref = new Reference(doc);
            ref.setURI("#" + uri);
            
            SecurityTokenReference secRef = new SecurityTokenReference(doc);
            secRef.setReference(ref);
            KeyInfo info = sig.getKeyInfo();
            info.addUnknownElement(secRef.getElement());
            
            
            GlobusCredential gc = getGlobusCredential();
            if(gc == null) {
                throw new XsulException("Globus Credential not found!");
            }
            //sig.getSignedInfo().generateDigestValues();
            sig.sign(cred.getPrivateKey());
            X509Certificate[] certs = cred.getCertificateChain();
            
            logger.finest("signed with cred="+cred);
            
            
            PKIPathSecurityToken token = new PKIPathSecurityToken(doc);
            token.setX509Certificates(certs, true);
            token.setID(uri);
            
            // hook entry point
            //          if(! doAdditionalSigning(wssec, token, sig)) {
            //              wssec.appendChild(token.getElement());
            //              wssec.appendChild(sig.getElement());
            //          }
//
            // other element such as assertion
            Element other = (Element)wssec.getFirstChild();
            if(other != null) {
                logger.finest("inserting signature after assertion");
                wssec.insertBefore(token.getElement(), other);
                wssec.insertBefore(sig.getElement(), other);
            }
            else {
                wssec.appendChild(token.getElement());
                wssec.appendChild(sig.getElement());
            }
            
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
            
            logger.finest("new sig verified ?: "+sig.getSignedInfo().verify(false));
            return doc;
        } catch (Exception e) {
            throw new XsulException("could not sign message "+e, e);
        }
    }
    
    /**
     *   If wssec has other children such as policy assertions, the
     *   signature should be inserted after them.
     **/
    protected boolean doAdditionalSigning(Element wssec, PKIPathSecurityToken token, XMLSignature sig)
        throws DOMException {
        return false;
    }
    
    
    //    public XmlNode signSoapMessage(XmlNode envelope)
    //        throws Exception
    //    {
    //        String soapmsg = XmlNodeUtil.convertXmlTreeToString(envelope);
    //        soapmsg = signSoapMessage(soapmsg);
    //        XmlNode soapenv = XmlNodeUtil.convertStringToXmlTree(soapmsg);
    //
    //        return soapenv;
    //    }
    
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

