/**
 * CapabilityProcessor.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.processor.capability;

import java.security.Principal;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import xsul.MLogger;
import xsul.dsig.SignatureInfo;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeSigner;
import xsul.dsig.saml.CapGlobusCredSOAPEnvelopeVerifier;
import xsul.dsig.saml.CapSignatureInfo;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityAuthorizer;
import xsul.dsig.saml.authorization.CapabilityException;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.processor.MessageProcessor;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;

public class CapabilityProcessor extends SoapHttpDynamicInfosetProcessor {
    
    private final static MLogger logger = MLogger.getLogger();
    
    private String svc_uri;
    private GlobusCredential cred;
    private X509Certificate[] trustedCerts;
    private MessageProcessor service;
    
    private boolean checkSignature = true;
    private boolean signMessage = true;
    private String owner_name;
    
    public CapabilityProcessor(GlobusCredential cred,
                               X509Certificate[] trustedCerts,
                               MessageProcessor service,
                               int port,
                               String svc_uri) {
        super(port);
        this.svc_uri = svc_uri;
        this.cred = cred;
        this.trustedCerts = trustedCerts;
        this.service = service;
        this.owner_name = CapabilityUtil.canonicalizeSubject(cred.getSubject());
    }
    
    public void setCheckSignature(boolean checkSignature) {
        this.checkSignature = checkSignature;
    }
    
    public boolean isCheckSignature() {
        return checkSignature;
    }
    
    public void setSignMessage(boolean signMessage) {
        this.signMessage = signMessage;
    }
    
    public boolean isSignMessage() {
        return signMessage;
    }
    
    public XmlDocument processSoapEnvelope(XmlElement envelope, SoapUtil su) {
        final SoapUtil soapUtil = Soap11Util.getInstance();
        if(checkSignature)  {
            SignatureInfo si
                = CapGlobusCredSOAPEnvelopeVerifier.getInstance(cred, trustedCerts).verifySoapMessage(envelope);
            try {
                String service_uri = svc_uri;
                if(service_uri == null || service_uri.equals("")) {
                    // get the service url at run time as the identifier
                    service_uri = getServer().getLocation();
                    logger.finest("service uri: " + service_uri);
                }
                
                CapabilityAuthorizer authorizer
                    = CapabilityAuthorizer.newInstance(service_uri, owner_name);
                Principal pr = si.getSubjectDn();
                
                if(authorizer == null)
                    throw new CapabilityException("No authorizer found");
                
                if(si instanceof CapSignatureInfo) {
                    // Authorizer for Capability
                    Capability apc = ((CapSignatureInfo)si).getCapability();
                    if(apc == null)
                        throw new CapabilityException("no capability token in the SOAP message");
                    
                    authorizer.isAuthorized(pr, apc, envelope);
                }
                else {
                    throw new CapabilityException("No SamlSignatureInfo found");
                }
                
            }
            catch(CapabilityException e) {
                XmlDocument fault =
                    soapUtil.wrapBodyContent(
                    soapUtil.generateSoapClientFault("unathorized access", e));
                if(signMessage) {
                    XmlDocument signedDoc =
                        GlobusCredSOAPEnvelopeSigner.getInstance(cred).signSoapMessage(fault);
                    return signedDoc;
                }
            }
        }
        XmlDocument respDoc =  super.processSoapEnvelope(envelope, su);
        if(signMessage) {
            XmlDocument signedDoc =
                GlobusCredSOAPEnvelopeSigner.getInstance(cred).signSoapMessage(respDoc);
            return signedDoc;
        }
        else {
            return respDoc;
        }
    }
    
    public XmlElement processMessage(XmlElement message) {
        XmlElement resp = service.processMessage(message);
        return resp;
    }
    
}

