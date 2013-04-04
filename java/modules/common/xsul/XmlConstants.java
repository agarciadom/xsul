/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XmlConstants.java,v 1.14 2006/04/30 06:48:12 aslom Exp $
 */

package xsul;


import org.xmlpull.mxp1.MXParserFactory;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;

/**
 * One place to put XML (and SOAP) related constants.
 *
 * @version $Revision: 1.14 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XmlConstants {
    private final static MLogger logger = MLogger.getLogger();
    /** NOTE: this is non final fiel ONLY to allow overriding default factory by applets! **/
    public static XmlInfosetBuilder BUILDER;

    public static final String DEFAULT_CHARSET = "utf-8";
    public static final String CONTENT_TYPE_HTML ="text/html; charset="+DEFAULT_CHARSET;
    public static final String XHTML_MIMETYPE = "application/xhtml+xml";
    public static final String CONTENT_TYPE_XHTML = XHTML_MIMETYPE+";charset="+DEFAULT_CHARSET;
    public static final String CONTENT_TYPE_XML ="text/xml; charset="+DEFAULT_CHARSET;
    
    public final static String NS_URI_XSD = "http://www.w3.org/2001/XMLSchema";
    public final static String NS_URI_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public final static String NS_URI_XMLNS = "http://www.w3.org/2000/xmlns/";
    public final static String NS_URI_XML = "http://www.w3.org/XML/1998/namespace";
    public final static String NS_URI_XHTML = "http://www.w3.org/1999/xhtml";
    
    public final static String NS_URI_SOAP11 = "http://schemas.xmlsoap.org/soap/envelope/";
    public final static String NS_URI_SOAP12 = "http://www.w3.org/2003/05/soap-envelope";
    
    public final static XmlNamespace SOAP11_NS;
    public final static XmlNamespace SOAP12_NS;
    
    public final static String XSD_PREFIX = "xsd";
    public final static String XSI_PREFIX = "xsi";
    public final static String XML_PREFIX = "xml";
    
    public final static XmlNamespace XS_NS;
    public final static XmlNamespace XSI_NS;
    public final static XmlNamespace XML_NS;
    public final static XmlNamespace XHTML_NS;
    
    // to avoid typos
    public final static String S_ENVELOPE = "Envelope";
    public final static String S_HEADER = "Header";
    public final static String S_BODY = "Body";
    
    //private OperationStyle style = OperationStyle.DOCUMENT_STYLE;
    //private QName operation = null;
    
    static {
        try {
            BUILDER = XmlInfosetBuilder.newInstance();
        } catch(java.security.AccessControlException aex) {
            logger.config("cant load default XmlPull parser factory (running in applet?)", aex);
            try {
                XmlPullParserFactory factory = new MXParserFactory();
                BUILDER = XmlInfosetBuilder.newInstance(factory);
            } catch(Exception e) {
                //this is fatal error! this SHOULD NEVER happen but ...
                String msg = "could not fallback to XPP3/MXP1 parser factory";
                logger.severe(msg, e);
                System.err.println(msg);
                e.printStackTrace();
                throw new XsulException(msg, e);
            }
        }
        SOAP11_NS = BUILDER.newNamespace("S",NS_URI_SOAP11);
        SOAP12_NS = BUILDER.newNamespace("S",NS_URI_SOAP12);
        XS_NS = BUILDER.newNamespace(XSD_PREFIX, NS_URI_XSD);
        XSI_NS = BUILDER.newNamespace(XSI_PREFIX, NS_URI_XSI);
        XML_NS  = BUILDER.newNamespace(XML_PREFIX, NS_URI_XML);
        XHTML_NS = BUILDER.newNamespace("", NS_URI_XHTML);
    }
    //
    //        //XS_NS =
    //        //} catch (XmlBuilderException e) {
    //        //this should never happen but just in case ...
    //        //    e.printStackTrace();
    //        //}
    //    }
    
    
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2005 The Trustees of Indiana University.
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


