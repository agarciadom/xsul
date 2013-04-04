/**
 * SCConstants.java
 *
 * @author Liang Fang
 * $Id: SCConstants.java,v 1.3 2004/11/16 03:44:58 lifang Exp $
 */

package xsul.secconv;

import java.net.URI;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;

public interface SCConstants
{
    /** GSI Secure Conversation message protection method type
     * that will be used or was used to protect the request.
     * Can be set to:
     * {@link Constants#SIGNATURE SIGNATURE} or
     * {@link Constants#ENCRYPTION ENCRYPTION} or
     * {@link Constants#NONE NONE}.
     */
    public static final String GSI_SEC_CONV =
        "org.globus.security.secConv.msg.type";
    
    /** GSI Secure Message protection method type
     * that will be used or was used to protect the request.
     * Can be set to:
     * {@link Constants#SIGNATURE SIGNATURE} or
     * {@link Constants#ENCRYPTION ENCRYPTION} or
     * {@link Constants#NONE NONE}.
     */
    public static final String GSI_SEC_MSG =
        "org.globus.security.secMsg.msg.type";
    
    /** GSI Secure Converation anonymous flag. If set to
     * <code>Boolean.TRUE</code>, then anonymous authentication is used,
     * or else client credentials are required.
     */
    public static final String GSI_SEC_CONV_ANON =
        "org.globus.security.secConv.anon";
    
    /** GSI Secure Conversation integrity message
     * protection method. */
    public static final Integer SIGNATURE
        = new Integer(1);
    
    /** GSI Secure Conversation privacy message
     * protection method. */
    public static final Integer ENCRYPTION
        = new Integer(2);
    
    /** GSI Secure Conversation none message
     * protection method. */
    public static final Integer NONE =
        new Integer(Integer.MAX_VALUE);
    
    /**
     * GRIM proxy policy handler.
     */
    public static final String GRIM_POLICY_HANDLER =
        "org.globus.wsrf.security.grim.policy.handler";
    
    /**
     * Authorization method.
     */
    public static final String AUTHORIZATION =
        "org.globus.security.authorization";
    
    public static final String CLIENT_DESCRIPTOR_FILE = "clientDescriptorFile";
    
    public static final String CLIENT_DESCRIPTOR = "clientDescriptor";
    
    public static final String CONTEXT = "org.globus.security.context";
    
    public static final String WS_SEC_CONV =
//      "org.globus.security.secConv.msg.type";
        "org.apache.wsse.secconv.msg.type";
    
    //    public static final String WS_SEC_MSG =
//      "org.apache.wsse.secconv.msg.type";
    
    public static final String WS_SEC_CONV_ANON =
        "org.apache.wsse.secconv.anon";
    
    /** Lifetime of the context */
    public static final String CONTEXT_LIFETIME
        = "org.globus.security.context.lifetime";
    
    public static final String WSSE_NS =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
//      "http://schemas.xmlsoap.org/ws/2002/04/secext";
    
    public static final String WST_NS
        = "http://schemas.xmlsoap.org/ws/2004/04/trust";
    
    public static final String WSP_NS
        = "http://schemas.xmlsoap.org/ws/2002/12/policy";
    
    public static final String WSC_NS
        = "http://schemas.xmlsoap.org/ws/2004/04/sc";
    
    public static final String WSU_NS
        = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    public static final String AUTHA_NS = "http://anl.gov/autha";
    
    public static final String PKI_NS = "http://extreme.org/pki";
    
    public static final String XCAP_NS = "http://extreme.org/2004/11/xcap";
    
    public static final String AUTHA_TOKEN = "http://anl.gov/authaToken";
    
    public static final String PKI_TOKEN = "http://extreme.org/pkiToken";
    /**
     * A list of QNames, in alphabetic order
     */
    
    public static final QName DERIVEDKEYTOKEN_QNAME
        = new QName(WSC_NS, "DerivedKeyToken");
    
    public static final QName IDENTIFIER_QNAME
        = new QName(WSC_NS, "identifier");
    
    public static final QName SECURITYCONTEXTTOKEN_QNAME
        = new QName(WSC_NS, "SecurityContextToken");
    
    public static final QName APPLIESTO_QNAME
        = new QName(WSP_NS, "AppliesTo");
    
    public static final QName BASE64_ENCODING =
        new QName(WSSE_NS, "Base64Binary");
    
    public static final String BASE64_ENCODING_ =
        new String(WSSE_NS+"/Base64Binary");
    
    public static final QName BASE_QNAME
        = new QName(WST_NS, "Base");
    
    public static final QName BINARYEXCHANGE_QNAME
        = new QName(WST_NS, "BinaryExchange");
    
    public static final QName CLAIMS_QNAME
        = new QName(WST_NS, "Claims");
    
    public static final QName CLIENTINITTOKEN_QNAME
        = new QName(AUTHA_NS, "ClientInitToken");
    
    public static final QName CLIENTRESPONSETOKEN_QNAME
        = new QName(AUTHA_NS, "ClientResponseToken");
    
    public static final QName KEYEXCHANGETOKEN_QNAME
        = new QName(WST_NS, "KeyExchangeToken");
    
    public static final QName LIFETIME_QNAME
        = new QName(WST_NS, "Lifetime");
    
    public static final QName REQUESTEDPROOFTOKEN_QNAME
        = new QName(WST_NS, "RequestedProofToken");
    
    public static final QName REQUESTEDSECURITYTOKEN_QNAME
        = new QName(WST_NS, "RequestedSecurityToken");
    
    public static final QName REQUESTEDTOKENREFERENCE_QNAME
        = new QName(WST_NS, "RequestedTokenReference");
    
    public static final QName REQUESTKET_QNAME
        = new QName(WST_NS, "RequestKET");
    
    public static final QName REQUESTSECURITYTOKEN_QNAME
        = new QName(WST_NS, "RequestSecurityToken");
    
    public static final QName REQUESTSECURITYTOKENRESPONSE_QNAME
        = new QName(WST_NS, "RequestSecurityTokenResponse");
    
    public static final QName REQUESTSECURITYTOKENRESPONSECOLLECTION_QNAME
        = new QName(WST_NS, "RequestSecurityTokenResponseCollection");
    
    public static final QName REQUESTTYPE_QNAME
        = new QName(WST_NS, "RequestType");
    
    public static final QName SECURITYTOKENREFERENCE_QNAME
        = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                    "SecurityTokenReference");
    
    public static final QName SERVERRESPONSETOKEN_QNAME
        = new QName(AUTHA_NS, "ServerResponseToken");
    
    public static final QName TOKENTYPE_QNAME
        = new QName(WST_NS, "TokenType");
    
    // For xpp3
    
    public final static XmlInfosetBuilder BUILDER = XmlConstants.BUILDER;

    public final static XmlNamespace WSTNS = BUILDER.newNamespace(
        "wst", "http://schemas.xmlsoap.org/ws/2004/04/trust");

    public final static XmlNamespace WSCNS = BUILDER.newNamespace(
        "wsc", "http://schemas.xmlsoap.org/ws/2004/04/sc");
    
    public final static XmlNamespace WSUNS = BUILDER.newNamespace(
        "wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

    public final static XmlNamespace AANS = BUILDER.newNamespace(
        "aa", "http://anl.gov/autha");
    
    public final static XmlNamespace PKINS = BUILDER.newNamespace(
        "pki", "http://extreme.org/pki");

    public final static String REQUEST_TYPE_ISSUE =
        new String("http://schemas.xmlsoap.org/ws/2004/04/security/trust/Issue");
}
