/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: CapabilityInvoker.java,v 1.4 2005/02/01 00:47:43 lifang Exp $
 */


package xsul.invoker.capability;

import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.xmlpull.v1.builder.XmlDocument;
import xsul.MLogger;
import xsul.dsig.saml.CapGlobusCredSOAPEnvelopeSigner;
import xsul.dsig.saml.CapGlobusCredSOAPEnvelopeVerifier;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityEnforcer;
import xsul.dsig.saml.authorization.CapabilityException;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import org.globus.gsi.GlobusCredentialException;

/**
 * CapabilityInvoker.java
 *
 * @author Liang Fang lifang@cs.indiana.edu
 */
public class CapabilityInvoker extends SoapHttpDynamicInfosetInvoker {
    
    private static final MLogger logger = MLogger.getLogger();
    
    protected GlobusCredential cred;
    protected X509Certificate[] trustedCerts;
    protected Capability cap;
    
    protected boolean sslEnabled = false;
    
    protected CapabilityInvoker(String loc) {
        super(loc);
        try {
            this.cred = GlobusCredential.getDefaultCredential();
            this.trustedCerts =
                TrustedCertificates.getDefaultTrustedCertificates().getCertificates();
        } catch (GlobusCredentialException e) {
            logger.warning("no default credential or trustedcerts");
            // ignored
        }
        
    }
    
    public CapabilityInvoker(GlobusCredential cred,
                             X509Certificate[] trustedCerts,
                             Capability cap,
                             String serviceLocation) {
        super(serviceLocation);
        
        this.cred = cred;
        this.trustedCerts = trustedCerts;
        this.cap = cap;
    }
    
    public CapabilityInvoker(GlobusCredential cred,
                             X509Certificate[] trustedCerts,
                             Capability cap,
                             String serviceLocation,
                             boolean sslEnabled) {
        super(serviceLocation);
        
        this.cred = cred;
        this.trustedCerts = trustedCerts;
        this.cap = cap;
        this.sslEnabled = sslEnabled;
    }
    
    public XmlDocument invokeXml(XmlDocument request)
        throws DynamicInfosetInvokerException {
        try {
            String subj = CapabilityUtil.canonicalizeSubject(cred.getSubject());
            logger.finest("subject: " + subj);
            request = CapabilityEnforcer.newInstance(cap, subj).addCapability(request);
        }
        catch(CapabilityException ce) {
            throw new DynamicInfosetInvokerException("could not add capability token", ce);
        }
        
        if(!sslEnabled) {
            XmlDocument signedRequest =
                CapGlobusCredSOAPEnvelopeSigner.getInstance(cred).signSoapMessage(request);
            XmlDocument response = super.invokeXml(signedRequest);
            CapGlobusCredSOAPEnvelopeVerifier.getInstance(cred, trustedCerts).verifySoapMessage(response);
            return response;
        }
        else {
            // if ssl is enabled, we don't do soap message signing and verifying.
            return super.invokeXml(request);
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


