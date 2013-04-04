/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SoapHttpDynamicInfosetInvoker.java,v 1.11 2006/04/21 20:05:32 aslom Exp $
 */

package xsul.invoker.soap_over_http;

import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.http_client.HttpClientConnectionManager;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.http.HttpDynamicInfosetInvoker;
import xsul.invoker.soap.SoapDynamicInfosetInvoker;
import xsul.invoker.soap.SoapMessageInvoker;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;

/**
 * This class allows to send SOAP (by default 1.1) to HTTP endpoint.
 * Simply set endpoint location and execute invoke*().
 * Easy to change to use SOAP 1.2 by calling one methos.
 *
 * @version $Revision: 1.11 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class SoapHttpDynamicInfosetInvoker extends HttpDynamicInfosetInvoker
    implements SoapMessageInvoker, SoapDynamicInfosetInvoker
{
    private static final MLogger logger = MLogger.getLogger();
    private static final XmlInfosetBuilder builder = XmlConstants.BUILDER;
    //private HttpDynamicInfosetInvoker httpInvoker;
    private SoapUtil soapUtil = Soap11Util.getInstance();
    
    public SoapHttpDynamicInfosetInvoker() {
        //httpInvoker = new HttpDynamicInfosetInvoker();
    }
    
    public SoapHttpDynamicInfosetInvoker(String locationUrl)
        throws DynamicInfosetInvokerException
    {
        super(locationUrl);
    }
    
    public SoapHttpDynamicInfosetInvoker(HttpClientConnectionManager connMgr)
        throws DynamicInfosetInvokerException
    {
        super(connMgr);
    }
    
    public XmlDocument wrapAsSoapDocument(XmlElement message)
        throws DynamicInfosetInvokerException
    {
        //SoapDocument doc = SoapFactory.newSoapDocument(SoapUtil.SOAP11);
        //doc.getEnvelope().getBody().addElement(message);
        if(soapUtil != null) {
            return soapUtil.wrapBodyContent(message);
        } else {
            XmlContainer root = message.getRoot();
            if(root instanceof XmlDocument) {
                return (XmlDocument) root;
            } else {
                throw new DynamicInfosetInvokerException(
                    "message to send directly must be contained in XmlDocument");
            }
        }
    }
    
    public XmlElement extractBodyContent(XmlDocument respDoc ) {
        //JDK15 SoapDocument soapDoc = XmlDocumentAdapter.narrow<SoapDocument>(respDoc);
        //SoapDocument soapDoc = (SoapDocument) XmlDocumentAdapter.narrow(
        //    respDoc, SoapDocument.class); //SoapDocument.narrow(respDoc) ????
        
        //SoapDocument soapDoc = (SoapDocument) SoapUtil.narrowDocument(respDoc);
        
        //soapUtil.requireBodyContent(respDoc); //use the same SOAP version as input message!
        //XmlElement content = Soap11Util.findBodyContent(respDoc);
        //XmlElement content = soapDoc.getEnvelope().getBody().requiredElementContent().iterator().next();
        
        XmlElement content;
        if(soapUtil != null) {
            if(logger.isFinestEnabled()) {
                String responseXml = builder.serializeToString(respDoc);
                logger.finest("resp doc: " + responseXml);
            }
            content = soapUtil.requiredBodyContent(respDoc);
            if(logger.isFinestEnabled()) {
                String responseXml = builder.serializeToString(content);
                logger.finest("content xml: " + responseXml);
            }
        } else {
            content = respDoc.getDocumentElement();
        }
        return content;
    }
    
    public SoapUtil getSoapFragrance() {
        return soapUtil;
    }
    
    /**
     * Set what SOAP utility to use for wrapping message into SOAP Envelope Body
     * If itis null no wrapping is done.
     */
    public void setSoapFragrance(SoapUtil soapFragrance) {
        //if(soapUtil
        this.soapUtil = soapFragrance;
    }
    
    /**
     * Message must contain exact XML  to put inside SOAP 1.1 Body that
     * is sent inside SOAP 1.1 Envelope to XML Web Service, the method returns result of
     * service invocation as first child of returned Envelope/Body (or null if one-way operation).
     * Input message must be a standalone fragment (message.getParent() == null).
     */
    public XmlElement invokeMessage(XmlElement message) throws DynamicInfosetInvokerException
    {
        XmlDocument doc = wrapAsSoapDocument(message);
        
        XmlDocument respDoc = invokeXml(doc);
        
        if(respDoc == null) { //handle one way messaging!
            return null;
        }
        
        XmlElement content = extractBodyContent(respDoc);
        return content;
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





