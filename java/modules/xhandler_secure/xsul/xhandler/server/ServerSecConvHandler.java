/**
 * ServerSecConvHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ServerSecConvHandler.java,v 1.5 2006/04/30 06:48:14 aslom Exp $
 */

package xsul.xhandler.server;

import java.util.Iterator;
import org.apache.xml.security.Init;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.SignatureInfo;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.message_router.MessageContext;
import xsul.secconv.dsig.SessionKeySOAPEnvelopeSigner;
import xsul.secconv.dsig.SessionKeySOAPEnvelopeVerifier;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlUtil;
import xsul.xhandler.BaseHandler;
import xsul.xhandler.MCtxConstants;
import xsul.xhandler.XHandlerContext;

public class ServerSecConvHandler extends BaseHandler {
    
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static String CONTEXTID = "server-context-id";
    
    static {
        Init.init();
    }
    
    public ServerSecConvHandler(String name) {
        super(name);
    }
    
    public void init(XHandlerContext handlerConfig) {
        super.init(handlerConfig);
        
        // wsdl(port) may not exist
        if(handlerConfig == null)
            return;
        
        WsdlPort port = handlerConfig.getWsdlPort();
        XmlElement featureEl =
            port.element(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL);
        if(featureEl == null) {
            featureEl =
                port.addElement(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL);
        }
        else {
            for(Iterator i =
                port.elements(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL)
                .iterator();i.hasNext();) {
                XmlElement featureEl2 = (XmlElement) i.next();
                String uri =
                    featureEl2.getAttributeValue(null, WsdlUtil.URI_ATTR);
                if(MCtxConstants.FEATURE_SECCONV.equals(uri)) {
                    logger.config("secconv attr existed");
                    return;
                }
            }
        }
        featureEl.addAttribute(WsdlUtil.URI_ATTR, MCtxConstants.FEATURE_SECCONV);
        featureEl.addAttribute(WsdlUtil.REQUIRED_ATTR, "true");
    }
    
    public boolean processOutgoingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        
        XmlDocument doc = (XmlDocument) soapEnvelope.getParent();
        XmlElement ctxIdEl = context.element(MCtxConstants.NS, CONTEXTID);
        if(ctxIdEl == null) {
            throw new DynamicInfosetInvokerException("context id null");
        }
        logger.finest("ctxid elem: " + builder.serializeToString(ctxIdEl));
        String contextId = ctxIdEl.requiredTextContent().trim();
        logger.finest("contextid from msgctx: " + contextId);
        
        XmlDocument signedDoc =
            SessionKeySOAPEnvelopeSigner.getInstance(contextId).signSoapMessage(doc);
        
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
        final SoapUtil soapUtil = SoapUtil.selectSoapFragrance(
            soapEnvelope,
            new SoapUtil[]{Soap11Util.getInstance(),Soap12Util.getInstance()});
        String contextId = null;
        
        XmlElement sct = null;
        XmlElement iden = null;
        try {
            sct = soapEnvelope.element(null, XmlConstants.S_HEADER)
                .element(MCtxConstants.WSSEC_NS, "Security")
                .element(null, "SecurityContextToken");
            iden = sct.element(null, "Identifier");
        } catch(Exception e) {
            // catch NullpointerException
            logger.severe("failed to get SCT identifier", e);
        }
        
        if(iden != null)
            logger.finest("identifier: " + iden.toString());
        else {
            logger.finest("identifier null");
            XmlElement faultEl = soapUtil
                .generateSoapClientFault("unathorized access", null);
            XmlDocument fault =
                soapUtil.wrapBodyContent(faultEl);
            
            context.setOutgoingMessage(faultEl);
            return true;
        }
        
        contextId = iden.requiredTextContent();
        logger.finest("***contextId: " + contextId);
        
        context.addElement(MCtxConstants.NS, CONTEXTID).addChild(contextId);
        SignatureInfo si =
            new SessionKeySOAPEnvelopeVerifier(contextId).verifySoapMessage(soapEnvelope);
        context.addElement(MCtxConstants.NS, MCtxConstants.SIGCHECKED);
        soapEnvelope.removeChild(sct);
        
        return false;
    }
}

