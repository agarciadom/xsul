/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: GlobusCredSOAPEnvelopeVerifier.java,v 1.20 2006/04/18 18:03:46 aslom Exp $
 */

package xsul.dsig.globus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import org.apache.xml.security.Init;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.apache.xml.security.signature.SignedInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.HexDump;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.CachedXPathAPI;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.proxy.ProxyPathValidator;
import org.globus.gsi.proxy.ProxyPathValidatorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import xsul.MLogger;
import xsul.XsulException;
import xsul.dsig.SOAPEnvelopeVerifier;
import xsul.dsig.SignatureInfo;
import xsul.dsig.globus.security.authentication.SOAPBodyIdResolver;
import xsul.dsig.globus.security.authentication.wssec.BinarySecurityToken;
import xsul.dsig.globus.security.authentication.wssec.BinarySecurityTokenFactory;
import xsul.dsig.globus.security.authentication.wssec.PKIPathSecurityToken;
import xsul.dsig.globus.security.authentication.wssec.Reference;
import xsul.dsig.globus.security.authentication.wssec.SecurityTokenReference;
import xsul.dsig.globus.security.authentication.wssec.WSSecurityException;
import xsul.dsig.globus.security.authentication.wssec.WSSecurityIdResolver;
import xsul.dsig.globus.security.authentication.wssec.WSSecurityUtil;
import xsul.dsig.globus.security.authentication.wssec.X509SecurityToken;
import xsul.util.XsulUtil;

//based on org.globus.ogsa.impl.security.authentication.wssec.WSSecurityEngine

public class GlobusCredSOAPEnvelopeVerifier extends SOAPEnvelopeVerifier {
    private static final boolean HEAVY_TRACING = false;
    private final static MLogger logger = MLogger.getLogger();
    private static GlobusCredSOAPEnvelopeVerifier instance;
    
    protected X509Certificate[] trustedCerts;
    
    static {
        Init.init();
    }
    
    public synchronized static GlobusCredSOAPEnvelopeVerifier getInstance() {
        if(instance == null) {
            instance = new GlobusCredSOAPEnvelopeVerifier();
        }
        return instance;
    }
    
    public static GlobusCredSOAPEnvelopeVerifier getInstance(GlobusCredential cred) {
        return getInstance(cred, null);
    }
    
    public static GlobusCredSOAPEnvelopeVerifier getInstance(GlobusCredential cred,
                                                             X509Certificate[] trustedCerts)
        throws XsulException {
        if(cred ==null) {
            throw new XsulException("globus credential can not be null");
        }
        else {
            return new GlobusCredSOAPEnvelopeVerifier(trustedCerts);
        }
    }
    
    protected GlobusCredSOAPEnvelopeVerifier() {
    }
    
    protected GlobusCredSOAPEnvelopeVerifier(X509Certificate[] trustedCerts) {
        this.trustedCerts = trustedCerts;
    }
    
    //    protected GlobusCredSOAPEnvelopeVerifier(GlobusCredential cred,
    //                                             X509Certificate[] trustedCerts) {
    //        if(cred == null) throw new IllegalArgumentException();
    //        this.cred = cred;
    //        this.trustedCerts = trustedCerts;
    //    }
//
    
    protected X509Certificate[] getCertificatesX509Data(KeyInfo info)
        throws Exception {
        int len = info.lengthX509Data();
        
        if (len != 1) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, "invalidX509Data",
                new Object[] { new Integer(len) }
            );
        }
        
        X509Data data = info.itemX509Data(0);
        int certLen = data.lengthCertificate();
        
        if (certLen <= 0) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, "invalidCertData",
                new Object[] { new Integer(certLen) }
            );
        }
        
        X509Certificate[] certs = new X509Certificate[certLen];
        XMLX509Certificate xmlCert;
        ByteArrayInputStream input;
        
        for (int i = 0; i < certLen; i++) {
            xmlCert = data.itemCertificate(i);
            input = new ByteArrayInputStream(xmlCert.getCertificateBytes());
            certs[i] = CertUtil.loadCertificate(input);
        }
        
        return certs;
    }
    
    protected X509Certificate[] getCertificatesTokenReference(Element elem)
        throws Exception {
        SecurityTokenReference secRef = new SecurityTokenReference(elem);
        Reference ref = secRef.getReference();
        
        if (ref == null) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY, "noReference"
            );
        }
        
        String uri = ref.getURI();
        
        logger.info("Token reference uri: " + uri);
        
        if (uri == null) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY, "badReferenceURI"
            );
        }
        
        WSSecurityIdResolver resolver = WSSecurityIdResolver.getInstance();
        
        Element tokElement =
            resolver.getElementById(elem.getOwnerDocument(), uri);
        
        if (tokElement == null) {
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "noToken",
                new Object[] { uri }
            );
        }
        
        BinarySecurityTokenFactory tokenFactory =
            BinarySecurityTokenFactory.getInstance();
        
        BinarySecurityToken token =
            tokenFactory.createSecurityToken(tokElement);
        
        if (token instanceof PKIPathSecurityToken) {
            return ((PKIPathSecurityToken) token).getX509Certificates(true);
        }else if(token instanceof X509SecurityToken){
        	X509Certificate cert = ((X509SecurityToken) token).getX509Certificate();
        	return new X509Certificate[]{cert};
        }else {
        	System.out.println("###############"+token);
            throw new WSSecurityException(
                WSSecurityException.UNSUPPORTED_SECURITY_TOKEN, "unhandledToken",
                new Object[] { token.getClass().getName() }
            );
        }
    }
    
    
    protected ResourceResolverSpi getResourceResolver() {
        return SOAPBodyIdResolver.getInstance();
    }
    
    
    public SignatureInfo verifySoapMessage(Document envelope)
        throws XsulException {
        try {
            Document doc = envelope;
            
            // only if the log level == FINEST -- serialization takes time!
            if(logger.getLevel().equals(MLogger.Level.ALL)) {
                OutputStream baos = new ByteArrayOutputStream();
                XMLSerializer serializer = new XMLSerializer (
                    new PrintWriter(baos) , null);
                serializer.asDOMSerializer();
                serializer.serialize(envelope);
                logger.finest("SIGNATUR2_XML_START\n"+XsulUtil.printable(baos.toString())+"\nSIGNATUR2_XML_END\n");
            }
            
            CachedXPathAPI xpathAPI = new CachedXPathAPI();
            Element nsctx = doc.createElement("nsctx");
            
            nsctx.setAttribute("xmlns:ds", Constants.SignatureSpecNS); //ALEK
            
            Element signatureElem =
                (Element) xpathAPI.selectSingleNode(doc,
                                                    "//ds:Signature", nsctx);
            if(signatureElem == null) {
                throw new XsulException("could not find ds:Signature in envelope");
            }
            
            XMLSignature sig =
                new XMLSignature(signatureElem,
                                 null);
            
            KeyInfo info = sig.getKeyInfo();
            
            sig.getSignedInfo().addResourceResolver(getResourceResolver());
//            if(logger.getLevel().equals(MLogger.Level.ALL)) {
//                ByteArrayOutputStream serializedSignatureElement = new ByteArrayOutputStream();
//                XMLUtils.outputDOM(sig.getElement(), serializedSignatureElement);
//                logger.finest("signatureElem2=\n"+serializedSignatureElement.toString());
//                serializedSignatureElement.close();
//            }
//            logger.finest("old sig verified?: "+sig.getSignedInfo().verify(false));
            
            X509Certificate[] certs = null;
            if (info.containsX509Data()) {
                logger.info("keyinfo contains x509 data");
                certs = getCertificatesX509Data(info);
            } else {
                logger.info("try to get x509 data from security token");
                Node node =
                    WSSecurityUtil.getDirectChild(
                    info.getElement(),
                    SecurityTokenReference.TOKEN.getLocalPart(),
                    SecurityTokenReference.TOKEN.getNamespaceURI()
                );
                
                if (node == null) {
                    throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY, "unsupportedKeyInfo",
                        null
                    );
                } else {
                    certs = getCertificatesTokenReference((Element) node);
                    logger.info("cert0: " + certs[0]);
                }
            }
            try {
                certs[0].checkValidity();
            } catch(java.security.cert.CertificateExpiredException ex) {
                throw new XsulException(
                    "expired certificate identifed by "+certs[0].getSubjectDN()
                        +" used for signing of message certificate is "+certs[0], ex);
            }
            
            sig.setFollowNestedManifests(false);
            // select form list of trusted certificates only that could be used to certify signing key
            //TrustedCertificates tc = TrustedCertificates.getDefaultTrustedCertificates();
            //X509Certificate[] trustedCerts = tc.getCertificates();
            //            if(trustedCerts != null) {
            //                String subjectDnAsString = subjectDn.toString();
            //                for(int i = 0; i < trustedCerts.length; i++) {
            //                    X509Certificate trustedCert = trustedCerts[i];
            //                    String trustedCertIssuerDn = trustedCert.getIssuerDN().toString();
            //                    // cut off cn=...
            //                    int pos;
            //                    if((pos = trustedCertIssuerDn.toLowerCase().lastIndexOf(",cn=")) != -1) {
            //                        trustedCertIssuerDn = trustedCertIssuerDn.substring(0, pos);
            //                    }
            //                    if(subjectDnAsString.indexOf(trustedCertIssuerDn) >= 0) {
            //                        Key pk = trustedCert.getPublicKey();
            //                        verify = checkSignatureValue( sig, pk );
            //                    }
            //                    if(verify) {
            //                        break;
            //                    }
            //                }
            //            }
            Principal subjectDn = certs[0].getSubjectDN();
            
            if(trustedCerts != null) {
                ProxyPathValidator validator = new ProxyPathValidator();
                try {
                    validator.validate(certs, trustedCerts);
                }
                catch (ProxyPathValidatorException e) {
                    // last ditch effort - we always accept self issued proxies ..
                    //boolean verify = sig.checkSignatureValue(cred.getCertificateChain()[0]);
                    Key pk = certs[0].getPublicKey();
                    boolean verify = checkSignatureValue( sig, pk );
                    if(!verify) {
                        ByteArrayOutputStream serializedSignatureElement = new ByteArrayOutputStream();
                        XMLUtils.outputDOM(signatureElem, serializedSignatureElement);
                        serializedSignatureElement.close();
                        throw new XsulException(
                            "could not verify signature for "+serializedSignatureElement.toString(), e);
                    }
                }
            } else {
                Key pk = certs[0].getPublicKey();
                boolean verify = checkSignatureValue( sig, pk );
                if(!verify) {
                    logger.finest("cred pubkey: " + GlobusCredential.getDefaultCredential().getCertificateChain()[0]);
                    throw new XsulException(
                        "could not verify signature for message signed by "+subjectDn
                            +" using "+certs[0]);
                }
            }
            
            //boolean verify = sig.checkSignatureValue(certs[0]);
            
            //            if (!sig.checkSignatureValue(certs[0])) {
            //                throw new XsulException(
            //                    "failed signature check - signature can not be validated by certifcate "+certs[0]);
            //            }
//
            
            logger.finest("The signature is valid");
            
            
            //return new GlobusSignatureInfo(subjectDn);
            return extractSignatureInfo(subjectDn, signatureElem);
        }
        catch (Exception e) {
        	e.printStackTrace();
            throw new XsulException("could not verify signature "+e, e);
        }
    }
    
    protected SignatureInfo extractSignatureInfo(Principal subjectDn, Element signatureElem)
        throws Exception {
        return new GlobusSignatureInfo(subjectDn);
    }
    
    public static boolean checkSignatureValue(XMLSignature sig, Key pk) throws XMLSignatureException {
        
        //COMMENT: pk suggests it can only be a public key?
        //check to see if the key is not null
        if (pk == null) {
            Object exArgs[] = { "Didn't get a key" };
            
            throw new XMLSignatureException("empty", exArgs);
        }
        
        // all references inside the signedinfo need to be dereferenced and
        // digested again to see if the outcome matches the stored value in the
        // SignedInfo.
        // If _followManifestsDuringValidation is true it will do the same for
        // References inside a Manifest.
        try {
            if (!sig.getSignedInfo()
                .verify(false)) {
                return false;
            }
            
            
            //create a SignatureAlgorithms from the SignatureMethod inside
            //SignedInfo. This is used to validate the signature.
            SignatureAlgorithm sa =
                new SignatureAlgorithm(sig.getSignedInfo()
                                           .getSignatureMethodElement(), sig.getBaseURI());
            
            logger.finest("SignatureMethodURI = " + sa.getAlgorithmURI());
            logger.finest("jceSigAlgorithm    = " + sa.getJCEAlgorithmString());
            logger.finest("jceSigProvider     = " + sa.getJCEProviderName());
            logger.finest("PublicKey = " + pk);
            sa.initVerify(pk);
            
            
            
            
            // Get the canonicalized (normalized) SignedInfo
            SignedInfo sigInfo = sig.getSignedInfo();
            byte inputBytes[] = sigInfo.getCanonicalizedOctetStream();
            if(HEAVY_TRACING) logger.finest("inputBytes="+XsulUtil.printable(new String(inputBytes)));
            if(HEAVY_TRACING) logger.finest("inputBytesHEX="+HexDump.byteArrayToHexString(inputBytes));
            
            //set the input bytes on the SignateAlgorithm
            sa.update(inputBytes);
            
            //retrieve the byte[] from the stored signature
            byte sigBytes[] = sig.getSignatureValue();
            
            if(HEAVY_TRACING) logger.finest("SignatureValue = "
                                                + HexDump.byteArrayToHexString(sigBytes));
            
            if(HEAVY_TRACING) logger.finest("SHA1 inputBytes="+SHA1(inputBytes)+" sigBytes="+SHA1(sigBytes));
            
            //Have SignatureAlgorithm sign the input bytes and compare them to the
            //bytes that were stored in the signature.
            boolean verify = sa.verify(sigBytes);
            logger.finest("XXX verify="+verify);
            return verify;
        }
        catch (XMLSecurityException ex) {
            throw new XMLSignatureException("empty", ex);
        }
    }
    
    public static String SHA1(byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        
        
        md.update(data);
        byte[] digest = md.digest();
        return HexDump.byteArrayToHexString(digest);
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



