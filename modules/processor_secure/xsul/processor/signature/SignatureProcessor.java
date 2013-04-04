/**
 * SignatureProcessor.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.processor.signature;

import java.security.Principal;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import xsul.MLogger;
import xsul.dsig.SignatureInfo;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeSigner;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeVerifier;
import xsul.processor.MessageProcessor;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;

public class SignatureProcessor extends SoapHttpDynamicInfosetProcessor {
    private final static MLogger logger = MLogger.getLogger();
    
    private GlobusCredential cred;
    private X509Certificate[] trustedCerts;
    private MessageProcessor service;
    
    private boolean checkSignature = true;
    private boolean signMessage = true;
    
    public SignatureProcessor(GlobusCredential cred,
                              X509Certificate[] trustedCerts,
                              MessageProcessor service,
                              int port) {
        super(port);
        this.cred = cred;
        this.trustedCerts = trustedCerts;
        this.service = service;
    }
    
    public XmlDocument processSoapEnvelope(XmlElement envelope, SoapUtil su) {
        final SoapUtil soapUtil = Soap11Util.getInstance();
        if(checkSignature) {
            SignatureInfo si = GlobusCredSOAPEnvelopeVerifier.getInstance(cred, trustedCerts).verifySoapMessage(envelope);
            if(!isAuthorized(si.getSubjectDn(), envelope)) {
                XmlDocument fault =
                    soapUtil.wrapBodyContent(
                    soapUtil.generateSoapClientFault("unathorized access", null));
                XmlDocument signedDoc =
                    GlobusCredSOAPEnvelopeSigner.getInstance(cred).signSoapMessage(fault);
                return signedDoc;
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
    
    private boolean isAuthorized(Principal dn, XmlElement envelope)
        throws RuntimeException {
        // TODO
        return true;
    }
    
    
}

