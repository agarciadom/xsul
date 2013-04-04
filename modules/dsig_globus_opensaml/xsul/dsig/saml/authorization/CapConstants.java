/**
 * Constants.java
 *
 * @author Liang Fang lifang@cs.indiana.edu
 * $Id: CapConstants.java,v 1.8 2006/02/03 01:58:58 lifang Exp $
 */

package xsul.dsig.saml.authorization;

import org.opensaml.SAMLDecision;
import org.opensaml.SAMLException;
import org.opensaml.XML;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;

public interface CapConstants {
    
    final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public static final String DENY = SAMLDecision.DENY;
    public static final String PERMIT = SAMLDecision.PERMIT;
    
    public static final String CAP_NAMEIDENTIFIER_FORMAT =
        "urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName";
    public static final String CAP_NAMEQUALIFIER = "subjectNS";
    
    public static final String INDETERMINATE = SAMLDecision.INDETERMINATE;
    public static final String SUCCESS = SAMLException.SUCCESS.toString();
    public static final String REQUESTDENIED = XML.SAMLP_NS + ":" + "RequestDenied";
    
    final static public XmlNamespace SAML_NS = builder.newNamespace(XML.SAML_NS);
    
    public static final String PENDING = "pending";
    public static final String DENIED = "denied";
    public static final String APPROVED = "approved";
    
    public static String DATEFORMAT = "yyyy.MM.dd 'at' HH:mm:ss z";
    // default life time for capabilities 30 min
    public static int DEFAULT_LIFETIME = 30 * 60 * 1000;
    
    // Specific to Registry (copied from GFacConstants)
    public static final String REGISTRY_URL =
        "http://rainier.extreme.indiana.edu:20202/service-registry";
    public static final String REGISTRY_XPATH_WHERE_STRING =
        "']/ogsi:memberServiceLocator/ogsi:reference/wsdl:definitions&ns_wsdl=http://schemas.xmlsoap.org/wsdl/&format=xml";
    
    public static final String REGISTRY_XPATH_SEARCH_STRING =
        "?xpath=/ogsi:entry[contains(.,'";
    
    public static final String REGISTRY_XPATH_SEARCH_SUFFIX =
        "')]/ogsi:memberServiceLocator/ogsi:reference/wsdl:definitions&ns_wsdl=http://schemas.xmlsoap.org/wsdl/&format=xml";
    
}



