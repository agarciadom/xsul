/**
 * SecretSOAPEnvelopeVerifier.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.secconv.dsig;

import java.security.Key;
import javax.xml.transform.TransformerException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xsul.MLogger;
import xsul.XsulException;
import xsul.dsig.SOAPEnvelopeVerifier;
import xsul.dsig.SignatureInfo;
import xsul.dsig.SignatureVerificationFailure;
import xsul.dsig.globus.security.authentication.SOAPBodyIdResolver;
import xsul.secconv.SCUtil;

public class SessionKeySOAPEnvelopeVerifier extends SOAPEnvelopeVerifier
{
    
    private final static MLogger logger = MLogger.getLogger();
    private static SessionKeySOAPEnvelopeVerifier instance;
    
    private String contextId;
    
    public SessionKeySOAPEnvelopeVerifier() {
    }
    
    public SessionKeySOAPEnvelopeVerifier(String contextId) {
        this.contextId = contextId;
    }
    
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }
    
    public String getContextId() {
        return contextId;
    }
    
    protected ResourceResolverSpi getResourceResolver()
    {
        return SOAPBodyIdResolver.getInstance();
    }
    
    public SignatureInfo verifySoapMessage(Document envelope)
        throws SignatureVerificationFailure, XsulException {
        Document doc = envelope;
        
        try {
            Element signatureElem = getSignatureElem(doc);
            
            XMLSignature sig =
                new XMLSignature(signatureElem,
                                 "http://extreme.indiana.edu/xmlsecurity");
            sig.getSignedInfo().addResourceResolver(getResourceResolver());
            //(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            logger.finest("cano="+sig.getSignedInfo().getCanonicalizationMethodURI());
            Key key = SCUtil.getSessionKey(contextId);
            if (!sig.checkSignatureValue(key)) {
                throw new XsulException(
                    "failed signature check - signature can not be validated by secret ");
            }
        }
        catch (XsulException e) {throw e;}
        catch (TransformerException e) {
            throw new XsulException("Transformer exception", e);
        }
        catch (DOMException e) {
            throw new XsulException("DOM exception", e);
        }
        catch (Exception e) {
            throw new XsulException("other exception", e);
        }
        
        return null;
    }
    
    private Element getSignatureElem(Document doc)
        throws TransformerException, DOMException {
        CachedXPathAPI xpathAPI = new CachedXPathAPI();
        Element nsctx = doc.createElement("nsctx");
        
        nsctx.setAttribute("xmlns:ds", Constants.SignatureSpecNS); //ALEK
        
        Element signatureElem =
            (Element) xpathAPI.selectSingleNode(doc,
                                                "//ds:Signature", nsctx);
        if(signatureElem == null) {
            throw new XsulException("could not find ds:Signature in envelope");
        }
        
        return signatureElem;
    }
    
}

