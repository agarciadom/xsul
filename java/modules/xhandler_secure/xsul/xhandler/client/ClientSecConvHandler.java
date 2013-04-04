/**
 * ClientSecConvHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ClientSecConvHandler.java,v 1.7 2006/04/30 06:48:14 aslom Exp $
 */

package xsul.xhandler.client;

import java.lang.reflect.Proxy;
import java.security.Key;
import java.util.Iterator;
import org.apache.xml.security.Init;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import sun.misc.BASE64Encoder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.message_router.MessageContext;
import xsul.secconv.ClientNegotiator;
import xsul.secconv.SCUtil;
import xsul.secconv.SecurityRequestorService;
import xsul.secconv.autha.AuthaClientNegotiator;
import xsul.secconv.dsig.SessionKeySOAPEnvelopeSigner;
import xsul.secconv.dsig.SessionKeySOAPEnvelopeVerifier;
import xsul.secconv.pki.GlobusCredClientNegotiator;
import xsul.secconv.pki.KeyStoreClientNegotiator;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlUtil;
import xsul.xhandler.BaseHandler;
import xsul.xhandler.MCtxConstants;
import xsul.xhandler.XHandlerContext;
import xsul.xhandler.exception.SecConvConfigurationException;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class ClientSecConvHandler extends BaseHandler {
    
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static BASE64Encoder encoder = new BASE64Encoder();
    
    private String scurl;
    private String contextId = null;
    
    static {
        Init.init();
    }
    
    public ClientSecConvHandler(String name, String scurl) {
        super(name);
        this.scurl = scurl;
    }
    
    public void init(XHandlerContext handlerConfig) {
        super.init(handlerConfig);
        boolean scRequired = false;
        WsdlPort port = handlerConfig.getWsdlPort();
        for(Iterator i =
            port.elements(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL)
            .iterator();i.hasNext();) {
            XmlElement featureEl = (XmlElement) i.next();
            String uri = featureEl.getAttributeValue(null, WsdlUtil.URI_ATTR);
            if(MCtxConstants.FEATURE_SECCONV.equals(uri)) {
                scRequired = true;
                break;
            }
        }
        if(scRequired) {
            String opt = System.getProperty("scprotocol");
            if(opt == null) {
                throw new SecConvConfigurationException(
                    "missing -Dscprotocol= ");
            }
            
            try {
                // establish context with securityrequestorservice
                contextId = establishSecurityContext(opt);
            }
            catch (IllegalArgumentException e) {
                throw new SecConvConfigurationException(
                    "failed to establish context(illegal argument)", e);
            }
            catch (DynamicInfosetInvokerException e) {
                throw new SecConvConfigurationException(
                    "failed to establish context(dynamic infoset problem)", e);
            }
            if(contextId == null) {
                throw new SecConvConfigurationException(
                    "failed to establish context");
            }
            setHandlerDisabled(false);
        }
        logger.finest("handlerDisabled="+isHandlerDisabled());
    }
    
    public boolean processOutgoingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        XmlElement ctxIdEl
            = context.element(MCtxConstants.NS, MCtxConstants.CLIENTCONTEXTID);
        if(ctxIdEl == null) {
            if(contextId == null) {
                throw new DynamicInfosetInvokerException(
                    "context not established yet");
            }
            context.addElement(MCtxConstants.NS, MCtxConstants.CLIENTCONTEXTID)
                .addChild(contextId);
        }
        else {
            contextId = ctxIdEl.requiredTextContent().trim();
        }
        
        SessionKeySOAPEnvelopeSigner sksigner =
            new SessionKeySOAPEnvelopeSigner(contextId);
        XmlDocument doc = (XmlDocument) soapEnvelope.getParent();
        XmlDocument signedDoc = sksigner.signSoapMessage(doc);
        XmlElement el = signedDoc.getDocumentElement();
        XmlElement outgoingMessage  = (XmlElement) el.element(null, "Body")
            .requiredElementContent()
            .iterator().next();
        context.setOutgoingMessage(outgoingMessage);
        
        return false;
    }
    
    public boolean processIncomingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        String contextId = null;
        XmlElement ctxIdEl
            = context.element(MCtxConstants.NS, MCtxConstants.CLIENTCONTEXTID);
        if(ctxIdEl == null) {
            throw new DynamicInfosetInvokerException("contextId null");
        }
        contextId = ctxIdEl.requiredTextContent().trim();
        
        SessionKeySOAPEnvelopeVerifier skverifier =
            new SessionKeySOAPEnvelopeVerifier(contextId);
        skverifier.verifySoapMessage(soapEnvelope);
        
        XmlElement sct = soapEnvelope.element(null, XmlConstants.S_HEADER)
            .element(MCtxConstants.WSSEC_NS, "Security")
            .element(null, "SecurityContextToken");
        soapEnvelope.removeChild(sct);
        
        return false;
    }
    
    private String establishSecurityContext(String opt)
        throws DynamicInfosetInvokerException, IllegalArgumentException {
        SoapHttpDynamicInfosetInvoker invoker =
            new SoapHttpDynamicInfosetInvoker(scurl);
        SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
            invoker, XsdTypeHandlerRegistry.getInstance());
        invoker.setSoapFragrance(Soap12Util.getInstance());
        
        SecurityRequestorService ref =
            (SecurityRequestorService) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { SecurityRequestorService.class },
            handler);
        
        try {
            ClientNegotiator cn = null;
            if(opt.equals("autha")) {
                //                char[] pw = SCUtil.getPassword();
                char[] pw = System.getProperty("password").toCharArray();
                if(pw == null) {
                    throw new IllegalArgumentException("no password found");
                }
                cn = new AuthaClientNegotiator(pw);
            }
            else if(opt.equals("ks")) {
                String alias = System.getProperty("alias");
                String kspasswd = System.getProperty("kspasswd");
                String password = System.getProperty("password");
                if(alias == null || kspasswd == null || password == null)
                    throw new IllegalArgumentException(
                        "parameters (alias/password/keystorepassord) missing");
                
                cn = new KeyStoreClientNegotiator(alias, password, kspasswd);
            }
            else if(opt.equals("globus")) {
                String proxycert = System.getProperty("proxy");
                if(proxycert == null)
                    cn = new GlobusCredClientNegotiator();
                else
                    cn = new GlobusCredClientNegotiator(proxycert);
            }
            else {
                throw new IllegalArgumentException("Unknown protocol: " + opt);
            }
            
            if(cn != null) {
                cn.negotiate(ref);
                String mycontextId = cn.getContextId();
                logger.finest("got contextId from negotiator: "+mycontextId);
                // a SecrectKeySpec instance
                Key skey = cn.getSessionKey();
                SCUtil.saveSessionKey(mycontextId, skey, false);
                logger.finest("encoded key: "+encoder.encode(skey.getEncoded()));
                logger.finest("Context established");
                return mycontextId;
            }
            else {
                throw new Exception("failed to instantiate client negotiator");
            }
        }
        catch(Exception e) {
            throw new DynamicInfosetInvokerException(
                "failed to estabhlish context", e);
        }
        
    }
    
}

