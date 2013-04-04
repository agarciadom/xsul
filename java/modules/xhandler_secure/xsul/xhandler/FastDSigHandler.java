/**
 * FastDSigHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: FastDSigHandler.java,v 1.5 2006/04/30 06:48:14 aslom Exp $
 */

package xsul.xhandler;

import java.io.StringReader;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.DSConstants;
import xsul.dsig.FastDSigner;
import xsul.dsig.SignatureValueType;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.message_router.MessageContext;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.xsd_type_handler.util.Base64;

public abstract class FastDSigHandler  extends BaseHandler {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static FastDSigner fdsigner = new FastDSigner("SHA1withRSA");
    
    private GlobusCredential credential;
    private X509Certificate[] trustedCerts;
    
    public FastDSigHandler(String name,
                           GlobusCredential cred,
                           X509Certificate[] trustedCerts) {
        super(name);
        this.credential = cred;
        this.trustedCerts = trustedCerts;
    }
    
    public FastDSigHandler(String name) {
        super(name);
        try {
            this.credential = GlobusCredential.getDefaultCredential();
            this.trustedCerts =
                CapabilityUtil.getTrustedCertificates(null).getCertificates();
        } catch (Exception e) {}
    }
    
    public void init(XHandlerContext handlerConfig) {
        super.init(handlerConfig);
    }
    
    public boolean processOutgoingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        // locate the refered xml section -- body
        XmlElement body = soapEnvelope.element(null, "Body");
        String bodystr = builder.serializeToString(body);
        String encbody = new String(Base64.encode(bodystr.getBytes()));
        body.removeAllChildren();
        XmlElement encel =
            body.addElement(builder.newFragment(DSConstants.WSSE, "Encoded"));
        encel.addChild(encbody);
        
        byte[] signature = null;
        try {
            if(credential == null)
                throw new DynamicInfosetInvokerException("credential null");
            
            signature =
                fdsigner.sign(credential.getPrivateKey(), encbody.getBytes());
            if(logger.isFinestEnabled()) {
                logger.finest("right after signing: "+fdsigner.verify(credential.getCertificateChain()[0],
                                encbody, signature) + "for:\n"+encbody);
            }
        } catch (Exception e) {
            logger.severe("failed to sign", e);
        }
        String encoded = new String(Base64.encode(signature));
        // put the encoded sig into signature section
        SignatureValueType sval = new SignatureValueType();
        sval.addChild(encoded);
        XmlElement header = soapEnvelope.element(null, XmlConstants.S_HEADER , false);
        SoapUtil soapUtil =
            SoapUtil.selectSoapFragrance(soapEnvelope, new SoapUtil[]{
                    Soap12Util.getInstance(), Soap11Util.getInstance()});
        if(header == null) {
            if(soapUtil.getSoapVersion().equals("1.1")) {
                logger.finest("soap 1.1");
                header = builder.newFragment(Soap11Util.SOAP11_NS, XmlConstants.S_HEADER );
            }
            else if(soapUtil.getSoapVersion().equals("1.2")) {
                logger.finest("soap 1.2");
                header = builder.newFragment(Soap12Util.SOAP12_NS, XmlConstants.S_HEADER );
            }
            soapEnvelope.addChild(0, header);
        }
        header.addElement(sval);
        return false;
    }
    
    public boolean processIncomingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        logger.finest("context special2: "+builder.serializeToString(context));
        
        // get the encoded body section from body
        XmlElement body = soapEnvelope.element(null, "Body", false);
        XmlElement msg =
            context.element(null, "message", false);
        XmlElement encel = body.element(null, "Encoded", false);
        if(encel == null) {
            logger.finest("encoded null");
            throw new DynamicInfosetInvokerException("encoded element null");
        }
        Iterator iter = encel.children();
        // the actual base64 code
        String encbody = (String)iter.next();
        byte[] decbody = Base64.decode(encbody.toCharArray());
        // the decoded body in string
        String sbody = new String(decbody);
        
        XmlElement header = soapEnvelope.element(null, XmlConstants.S_HEADER, true);
        XmlElement sigelem =
            header.element(DSConstants.DSIG, SignatureValueType.NAME);
        iter = sigelem.children();
        String sig = (String)iter.next();
        byte[] decsig = Base64.decode(sig.toCharArray());
        try {
            if(credential == null) {
                logger.severe("credential null");
                throw new DynamicInfosetInvokerException("credential null");
            }
            else if(credential.getCertificateChain() == null) {
                logger.severe("credential.getCertificateChain()");
            }

            boolean b =
                fdsigner.verify(credential.getCertificateChain()[0], encbody, decsig);
            if(b == false) {
                logger.finest("encbody: " + new String(encbody));
                throw new Exception("verification failed");
            }
        } catch (Exception e) {
            logger.severe("failed to verify the signature", e);
            throw new DynamicInfosetInvokerException("failed to verify the signature", e);
        }
        
        XmlElement newbody =
            builder.parseFragmentFromReader(new StringReader(sbody));
        soapEnvelope.removeChild(body);
        soapEnvelope.addElement(newbody);
        XmlElement incomingMsg =
            (XmlElement)newbody.requiredElementContent().iterator().next();
        
        context.setIncomingMessage(incomingMsg);
        logger.finest("soap special: "+builder.serializeToString(soapEnvelope));
        logger.finest("context special: "+builder.serializeToString(context));
        return false;
    }
}

