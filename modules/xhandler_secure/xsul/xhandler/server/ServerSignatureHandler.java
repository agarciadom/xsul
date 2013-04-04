/**
 * SignatureProcessorHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ServerSignatureHandler.java,v 1.8 2006/04/30 06:48:14 aslom Exp $
 */

package xsul.xhandler.server;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.SignatureInfo;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeSigner;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeVerifier;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.message_router.MessageContext;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlUtil;
import xsul.xhandler.BaseHandler;
import xsul.xhandler.MCtxConstants;
import xsul.xhandler.XHandlerContext;

public class ServerSignatureHandler extends BaseHandler {
    private final static MLogger logger = MLogger.getLogger();
    
    private GlobusCredential credential;
    private X509Certificate[] trustedCerts;
    
    public ServerSignatureHandler(String name) {
        super(name);
        try {
            this.credential = GlobusCredential.getDefaultCredential();
            this.trustedCerts =
                CapabilityUtil.getTrustedCertificates(null).getCertificates();
        } catch (Exception e) {}
    }
    
    public ServerSignatureHandler(String name,
                                  GlobusCredential cred,
                                  X509Certificate[] trustedCerts) {
        super(name);
        this.credential = cred;
        this.trustedCerts = trustedCerts;
    }
    
    public void setCredential(GlobusCredential cred) {
        this.credential = cred;
    }
    
    public void setTrustedCerts(X509Certificate[] trustedCerts) {
        this.trustedCerts = trustedCerts;
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
                if(MCtxConstants.FEATURE_SIGNATURE.equals(uri)) {
                    logger.config("signaure attr existed");
                    return;
                }
            }
        }
        featureEl.addAttribute(WsdlUtil.URI_ATTR,
                               MCtxConstants.FEATURE_SIGNATURE);
        featureEl.addAttribute(WsdlUtil.REQUIRED_ATTR, "true");
    }
    
    public boolean processOutgoingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        if(needSigning(context)) {
            XmlDocument doc = (XmlDocument) soapEnvelope.getParent();
            if(doc == null)
                logger.finest("doc is null!!!!!");
            XmlDocument signedDoc =
                GlobusCredSOAPEnvelopeSigner.getInstance(credential)
                .signSoapMessage(doc);
            XmlElement el = signedDoc.getDocumentElement();
            XmlElement outgoingMessage  = (XmlElement) el.element(null, "Body")
                .requiredElementContent()
                .iterator().next();
            context.setOutgoingMessage(outgoingMessage);
            context.addElement(MCtxConstants.NS, MCtxConstants.SIGNED);
        }
        return false;
    }
    
    public boolean processIncomingXml(XmlElement envelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        if(needSigCheck(context)) {
            SignatureInfo si =
                GlobusCredSOAPEnvelopeVerifier
                .getInstance(credential, trustedCerts)
                .verifySoapMessage(envelope);
            context.addElement(MCtxConstants.NS, MCtxConstants.SIGCHECKED);
            
            // remove the verified signature, not necessary any more
            // it saves the next handler's parsing time
            XmlElement dsig = envelope.element(null, XmlConstants.S_HEADER)
                .element(MCtxConstants.WSSEC_NS, "Security")
                .element(MCtxConstants.SIG_NS, "Signature");
            envelope.removeChild(dsig);
            
            if(!isAuthorized(si.getSubjectDn(), envelope)) {
                final SoapUtil soapUtil = SoapUtil.selectSoapFragrance(
                    envelope,
                    new SoapUtil[]{Soap11Util.getInstance(),
                            Soap12Util.getInstance()});
                XmlElement faultEl = soapUtil
                    .generateSoapClientFault("unathorized access", null);
                XmlDocument fault = soapUtil.wrapBodyContent(faultEl);
                if(needSigning(context)) {
                    XmlDocument signedDoc =
                        GlobusCredSOAPEnvelopeSigner.getInstance(credential)
                        .signSoapMessage(fault);
                    context.addElement(MCtxConstants.NS, MCtxConstants.SIGNED);
                }
                context.setOutgoingMessage(faultEl);
                return true;
            }
            String principal = si.getSubjectDn().getName();
            context.addElement(MCtxConstants.NS, MCtxConstants.PRINCIPAL)
                .addChild(principal);
        }
        return false;
    }
    
    protected boolean isAuthorized(Principal dn, XmlElement envelope)
        throws RuntimeException {
        // TODO
        return true;
    }
    
    private boolean needSigCheck(MessageContext context) {
        boolean needSigCheck =
            (context.element(MCtxConstants.NS,
                             MCtxConstants.NOSIGCHECK) == null &&
                 context.element(MCtxConstants.NS,
                                 MCtxConstants.SIGCHECKED) == null);
        return needSigCheck;
    }
    
    private boolean needSigning(MessageContext context) {
        boolean needSigning =
            (context.element(MCtxConstants.NS,
                             MCtxConstants.NOSIGNING) == null &&
                 context.element(MCtxConstants.NS,
                                 MCtxConstants.SIGNED) == null);
        return needSigning;
    }
    
}


