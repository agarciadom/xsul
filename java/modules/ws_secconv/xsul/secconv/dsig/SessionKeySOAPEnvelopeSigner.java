/**
 * SecretSOAPEnvelopeSigner.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.secconv.dsig;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import javax.crypto.SecretKey;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.dsig.SOAPEnvelopeSigner;
import xsul.dsig.globus.security.authentication.SOAPBodyIdResolver;
import xsul.dsig.globus.security.authentication.wssec.Reference;
import xsul.dsig.globus.security.authentication.wssec.SecurityTokenReference;
import xsul.dsig.globus.security.authentication.wssec.WSConstants;
import xsul.dsig.globus.security.authentication.wssec.WSSecurityUtil;
import xsul.secconv.SCUtil;
import xsul.secconv.token.SecurityContextTokenType;

public class SessionKeySOAPEnvelopeSigner extends SOAPEnvelopeSigner
{
    private final static MLogger logger = MLogger.getLogger();
    private static SessionKeySOAPEnvelopeSigner instance;
    
    private String contextId;
    
    public SessionKeySOAPEnvelopeSigner() {
        super();
    }
    
    public SessionKeySOAPEnvelopeSigner(String contextId) {
        super();
        this.contextId = contextId;
    }
    
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }
    
    public String getContextId() {
        return contextId;
    }
    
    public synchronized static SOAPEnvelopeSigner getInstance() {
        if(instance == null) {
            instance = new SessionKeySOAPEnvelopeSigner();
        }
        
        return instance;
    }
    
    public synchronized static SOAPEnvelopeSigner getInstance(String contextId) {
        if(instance == null) {
            instance = new SessionKeySOAPEnvelopeSigner(contextId);
        }
        
        return instance;
    }
    
    public Document signSoapMessage(Document envelope)
        throws XsulException
    {
        try {
            Document doc = envelope;
            
            Element wssec = getWSSec(doc);
            
            String id = addBodyID(doc);
            String uri = "token" + System.currentTimeMillis();
            
            XMLSignature sig =
                new XMLSignature(doc,
                                 "http://extreme.indiana.edu/xmlsecurity",
                                 XMLSignature.ALGO_ID_MAC_HMAC_SHA1,
                                 Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            sig.getSignedInfo().addResourceResolver(getResourceResolver());
            
            sig.addDocument("#" + id);
            Reference ref = new Reference(doc);
            ref.setURI("#" + uri);
            
            SecurityTokenReference secRef = new SecurityTokenReference(doc);
            secRef.setReference(ref);
            KeyInfo info = sig.getKeyInfo();
            info.addUnknownElement(secRef.getElement());
            
            Key key = SCUtil.getSessionKey(contextId);
            sig.sign((SecretKey)key);
            
            SecurityContextTokenType sct = new SecurityContextTokenType();
            try {
                sct.setIdentifier(new URI(contextId));
            } catch (URISyntaxException e) {}
            
            // hook entry point
            if(! doAdditionalSigning(wssec, sig)) {
                wssec.appendChild(sig.getElement());
                wssec.appendChild(sct.getElement(doc));
            }
            
            return doc;
        } catch (Exception e) {
            throw new XsulException("could not sign message "+e, e);
        }
    }
    
    private Element getWSSec(Document doc) throws DOMException {
        Element root = (Element)doc.getFirstChild();
        if(logger.isFinestEnabled()) {
            ByteArrayOutputStream rootElem = new ByteArrayOutputStream();
            XMLUtils.outputDOM(root, rootElem);
            logger.finest("rootElemen=\n"+rootElem.toString());
        }
        
        Element body = (Element)root.getFirstChild();
        
        // Before adding a header, must sure there is not an existing header
        Element header =
            (Element) WSSecurityUtil.getDirectChild(root, XmlConstants.S_HEADER,
                                                    WSConstants.SOAP_NS);
        if(header == null) {
            logger.finest("\n>>>>>>> cannot find header. making new header.");
            header = doc.createElementNS(WSConstants.SOAP_NS, XmlConstants.S_HEADER);
            root.insertBefore(header, body);
        }
        
        // Before adding a wssec Security, must sure there is no wssec
        Element wssec =
            (Element) WSSecurityUtil.getDirectChild(header, "Security",
                                                    WSConstants.WSSE_NS);
        if(wssec == null) {
            logger.finest("\n>>>>>>> cannot find wssec. making new wssec.");
            wssec =
                doc.createElementNS(WSConstants.WSSE_NS, "wsse:Security");
            header.appendChild(wssec);
        }
        return wssec;
    }
    
    protected ResourceResolverSpi getResourceResolver() {
        return SOAPBodyIdResolver.getInstance();
    }
    
    /**
     *   If wssec has other children such as policy assertions, the
     *   signature should be inserted after them.
     **/
    protected boolean doAdditionalSigning(Element wssec,
                                          XMLSignature sig)
        throws DOMException
    {
        return false;
    }
    
}

