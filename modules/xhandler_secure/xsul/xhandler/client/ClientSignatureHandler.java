/**
 * SignatureInvokerHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ClientSignatureHandler.java,v 1.10 2006/04/30 06:48:14 aslom Exp $
 */

package xsul.xhandler.client;

import java.security.cert.X509Certificate;
import java.util.Iterator;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeSigner;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeVerifier;
import xsul.dsig.saml.authorization.CapabilityException;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.message_router.MessageContext;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlUtil;
import xsul.xhandler.BaseHandler;
import xsul.xhandler.MCtxConstants;
import xsul.xhandler.XHandlerContext;
import xsul.xhandler.exception.SignatureConfigurationException;

public class ClientSignatureHandler extends BaseHandler {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private GlobusCredential credential;
    private X509Certificate[] trustedCerts;
    
    public ClientSignatureHandler(String name) {
        super(name);
        try {
            this.credential = GlobusCredential.getDefaultCredential();
            this.trustedCerts =
                CapabilityUtil.getTrustedCertificates(null).getCertificates();
        } catch (Exception e) {}
    }
    
    public ClientSignatureHandler(String name,
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
        boolean sigRequired = false;
        WsdlPort port = handlerConfig.getWsdlPort();
        for(Iterator i =
            port.elements(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL)
            .iterator();i.hasNext();) {
            XmlElement featureEl = (XmlElement) i.next();
            String uri = featureEl.getAttributeValue(null, WsdlUtil.URI_ATTR);
            if(MCtxConstants.FEATURE_SIGNATURE.equals(uri)) {
                sigRequired = true;
                break;
            }
        }
        if(sigRequired) {
            if(credential == null || trustedCerts == null) {
                try {
                    this.credential = GlobusCredential.getDefaultCredential();
                    this.trustedCerts =
                        CapabilityUtil.getTrustedCertificates(null)
                        .getCertificates();
                }
                catch (CapabilityException e) {
                    throw new SignatureConfigurationException(
                        "failed to get trusted certs", e);
                }
                catch (GlobusCredentialException e) {
                    throw new SignatureConfigurationException(
                        "failed to get globus cert", e);
                }
            }
            logger.finest("grid user: " + credential.getSubject());
        }
        setHandlerDisabled(!sigRequired);
        logger.finest("dsig handlerDisabled="+isHandlerDisabled());
    }
    
    public boolean processOutgoingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        if(needSigning(context)) {
            XmlDocument doc = (XmlDocument) soapEnvelope.getParent();
            XmlDocument signedRequest =
                GlobusCredSOAPEnvelopeSigner.getInstance(credential)
                .signSoapMessage(doc);
            XmlElement el = signedRequest.getDocumentElement();
            XmlElement outgoingMessage  = (XmlElement) el.element(null, "Body")
                .requiredElementContent()
                .iterator().next();
            context.setOutgoingMessage(outgoingMessage);
            context.addElement(MCtxConstants.NS, MCtxConstants.SIGNED);
            doc = xsul.util.XsulUtil.getDocumentOutOfElement(outgoingMessage);
            logger.warning("signed doc content: " + builder.serializeToString(doc));
        }
        return false;
    }
    
    public boolean processIncomingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        if(needSigCheck(context)) {
            GlobusCredSOAPEnvelopeVerifier.getInstance(credential, trustedCerts)
                .verifySoapMessage(soapEnvelope);
            context.addElement(MCtxConstants.NS, MCtxConstants.SIGCHECKED);
            XmlElement dsig = soapEnvelope.element(null, XmlConstants.S_HEADER)
                .element(MCtxConstants.WSSEC_NS, "Security")
                .element(MCtxConstants.SIG_NS, "Signature");
            soapEnvelope.removeChild(dsig);
        }
        return false;
    }
    
    private boolean needSigning(MessageContext context) {
        boolean needSigning =
            (context.element(MCtxConstants.NS,
                             MCtxConstants.NOSIGNING) == null &&
                 context.element(MCtxConstants.NS,
                                 MCtxConstants.SIGNED) == null);
        return needSigning;
    }
    
    private boolean needSigCheck(MessageContext context) {
        boolean needSigCheck =
            (context.element(MCtxConstants.NS,
                             MCtxConstants.NOSIGCHECK) == null &&
                 context.element(MCtxConstants.NS,
                                 MCtxConstants.SIGCHECKED) == null);
        return needSigCheck;
    }
}





