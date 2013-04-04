/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SOAPEnvelopeVerifier.java,v 1.8 2004/06/10 21:36:27 sshirasu Exp $
 */

package xsul.dsig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;

public abstract class SOAPEnvelopeVerifier
{
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static DocumentBuilderFactory dbfNonValidating;
    private static DocumentBuilderFactory dbfValidating;
    private static final MLogger logger = MLogger.getLogger();
        
    static
        {
                dbfNonValidating = DocumentBuilderFactory.newInstance();
                dbfNonValidating.setNamespaceAware(true);
                dbfValidating = DocumentBuilderFactory.newInstance();
                dbfValidating.setNamespaceAware(true);
                dbfValidating.setValidating(true);
    }
        
    public abstract SignatureInfo verifySoapMessage(Document envelope)
                throws SignatureVerificationFailure, XsulException;
        
    public SignatureInfo verifySoapMessage(String envelope)
                throws SignatureVerificationFailure, XsulException
    {
                return verifySoapMessage(envelope, false);
    }
        
    public SignatureInfo verifySoapMessage(String envelope, boolean validation)
                throws SignatureVerificationFailure, XsulException
    {
                return verifySoapMessage(envelope, validation ? dbfValidating : dbfNonValidating);
    }
    
    public SignatureInfo verifySoapMessage(String envelope, DocumentBuilderFactory dbf)
                throws SignatureVerificationFailure, XsulException
    {
                Document doc;
                try
                {
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        doc = db.parse(new ByteArrayInputStream(envelope.getBytes()));
                        if(logger.isFinestEnabled()) {
                                logger.finest("\nthe soap envelope: " + envelope);
                                ByteArrayOutputStream docElem = new ByteArrayOutputStream();
                                XMLUtils.outputDOM(doc, docElem);
                                logger.finest("the docElem=\n"+docElem.toString());
                                docElem.close();
                        }
                }
                catch (ParserConfigurationException e)
                {
                        throw new XsulException("could not build DOM document", e);
                }
                catch (SAXException e)
                {
                        throw new XsulException("could not build DOM document", e);
                }
                catch (IOException e)
                {
                        throw new XsulException("could not build DOM document", e);
                }
                
                return verifySoapMessage(doc);
    }
        
        
    public SignatureInfo verifySoapMessage(XmlElement envelope)
                throws SignatureVerificationFailure, XsulException
    {
                String soapmsg = builder.serializeToString(envelope);
                return verifySoapMessage(soapmsg);
    }
        
    public SignatureInfo verifySoapMessage(XmlDocument envelope)
                throws SignatureVerificationFailure, XsulException
    {
                return verifySoapMessage(envelope, false);
    }
    
    public SignatureInfo verifySoapMessage(XmlDocument envelope, boolean validation)
                throws SignatureVerificationFailure, XsulException
    {
                String soapmsg = builder.serializeToString(envelope);
                return verifySoapMessage(soapmsg, validation);
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


