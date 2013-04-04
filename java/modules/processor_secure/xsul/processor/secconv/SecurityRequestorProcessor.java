/**
 * SecurityRequestorProcessor.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: SecurityRequestorProcessor.java,v 1.5 2006/04/30 06:48:13 aslom Exp $
 */

package xsul.processor.secconv;

import org.apache.xml.security.Init;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.SignatureInfo;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.processor.MessageProcessor;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.secconv.dsig.SessionKeySOAPEnvelopeSigner;
import xsul.secconv.dsig.SessionKeySOAPEnvelopeVerifier;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;

public class SecurityRequestorProcessor
    extends SoapHttpDynamicInfosetProcessor
{
    private static final MLogger logger = MLogger.getLogger();
    
    private MessageProcessor service;

    static {
        Init.init();
    }
    
    public SecurityRequestorProcessor() {
        super();
    }
    
    public SecurityRequestorProcessor(MessageProcessor service) {
        super();
        this.service = service;
    }
    
    public XmlElement processMessage(XmlElement message)
        throws DynamicInfosetProcessorException {
        
        XmlElement resp = service.processMessage(message);
        return resp;
    }
    
    public XmlDocument processSoapEnvelope(XmlElement envelope, SoapUtil su)
    {
        final SoapUtil soapUtil = Soap11Util.getInstance();
        String contextId = null;
        
        XmlElement header = envelope.findElementByName(XmlConstants.S_HEADER);
        XmlElement sec = header.findElementByName("Security");
        XmlElement sct = sec.findElementByName("SecurityContextToken");
        XmlElement iden = sct.findElementByName("Identifier");
        if(iden != null)
            logger.finest("iideen: " + iden.toString());
        else {
            logger.finest("iden null");
            XmlDocument fault =
                soapUtil.wrapBodyContent(
                soapUtil.generateSoapClientFault("unathorized access", null));
            
            return fault;
        }
        
        contextId = iden.requiredTextContent();
        logger.finest("contextidididid: " + contextId);
        
        SignatureInfo si =
            new SessionKeySOAPEnvelopeVerifier(contextId).verifySoapMessage(envelope);
        
        XmlDocument respDoc = super.processSoapEnvelope(envelope, su);
        
        XmlDocument signedDoc =
            SessionKeySOAPEnvelopeSigner.getInstance(contextId).signSoapMessage(respDoc);
        
        return signedDoc;
    }
    
}

