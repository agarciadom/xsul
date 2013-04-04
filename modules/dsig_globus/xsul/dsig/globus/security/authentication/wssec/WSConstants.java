/*
 This file is licensed under the terms of the Globus Toolkit Public
 License, found at http://www.globus.org/toolkit/download/license.html.
 */
package xsul.dsig.globus.security.authentication.wssec;

import javax.xml.namespace.QName;
import xsul.XmlConstants;


public interface WSConstants {
    public static final String WSSE_NS =
    		"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String WSSE_PREFIX = "wsse";
    public static final String WSU_NS =
    		"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    public static final String WSU_PREFIX = "wsu";
    public static final String SIG_NS = "http://www.w3.org/2000/09/xmldsig#";
    public static final String ENC_NS = "http://www.w3.org/2001/04/xmlenc#";
    public static final String SOAP_SEC_NS =
        "http://schemas.xmlsoap.org/soap/security/2000-12";
    public static final String SOAP_NS =
        "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP12_NS =
        XmlConstants.NS_URI_SOAP12;
    public static final String WS_SEC_LN = "Security";
    public static final QName WSSE_QNAME = new QName(WSSE_NS, WS_SEC_LN);
}
