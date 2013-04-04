/**
 * ClientCapabilityHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ClientCapabilityHandler.java,v 1.11 2005/05/04 06:29:58 lifang Exp $
 */

package xsul.xhandler.client;

import java.security.cert.X509Certificate;
import java.util.Iterator;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityEnforcer;
import xsul.dsig.saml.authorization.CapabilityException;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.message_router.MessageContext;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlUtil;
import xsul.xhandler.BaseHandler;
import xsul.xhandler.MCtxConstants;
import xsul.xhandler.XHandlerContext;
import xsul.xhandler.exception.CapabilityConfigurationException;
import xsul.xpola.XpolaFactory;
import xsul.xpola.capman.CapabilityManager;

public class ClientCapabilityHandler extends BaseHandler {
    
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private GlobusCredential credential;
    private X509Certificate[] trustedCerts;
    private String capmanLoc;
    private String svcLoc;
    private Capability cap;
    
    /**
     * The capability is given
     *
     * @param    name                a  String
     * @param    cap                 a  Capability
     *
     */
    public ClientCapabilityHandler(String name, Capability cap) {
        super(name);
        this.cap = cap;
        this.svcLoc = cap.getResource();
        try {
            this.credential = GlobusCredential.getDefaultCredential();
            this.trustedCerts =
                CapabilityUtil.getTrustedCertificates(null).getCertificates();
        } catch (Exception e) {}
    }
    
    public ClientCapabilityHandler(String name,
                                   GlobusCredential cred,
                                   X509Certificate[] trustedCerts,
                                   Capability cap) {
        super(name);
        this.credential = cred;
        this.trustedCerts = trustedCerts;
        this.cap = cap;
        this.svcLoc = cap.getResource();
    }
    
    /**
     * The capability has be fetched from a capman server specified in
     * capman service url
     *
     * @param    name                a  String
     * @param    capsvcurl           The URL of capman service
     *
     */
    public ClientCapabilityHandler(String name, String svcloc, String capsvcloc) {
        super(name);
        this.capmanLoc = capsvcloc;
        // http://host:port/interop?wsdl ==> http://host:port/interop
        int idx = svcloc.indexOf('?');
        if(idx > 0) {
            this.svcLoc = svcloc.substring(0, idx);
        }
        else {
            this.svcLoc = svcloc;
        }
        try {
            this.credential = GlobusCredential.getDefaultCredential();
            this.trustedCerts =
                CapabilityUtil.getTrustedCertificates(null).getCertificates();
        } catch (Exception e) {}
    }
    
    public ClientCapabilityHandler(String name,
                                   GlobusCredential cred,
                                   X509Certificate[] trustedCerts,
                                   String svcloc, String capsvcloc) {
        super(name);
        this.credential = cred;
        this.trustedCerts = trustedCerts;
        this.capmanLoc = capsvcloc;
        int idx = svcloc.indexOf('?');
        if(idx > 0) {
            this.svcLoc = svcloc.substring(0, idx);
        }
        else {
            this.svcLoc = svcloc;
        }
    }
    
    public void setCapmanLoc(String capman_location) {
        this.capmanLoc = capman_location;
    }
    
    public void setCap(Capability cap) {
        this.cap = cap;
    }
    
    public void setCredential(GlobusCredential credential) {
        this.credential = credential;
    }
    
    public void setTrustedCerts(X509Certificate[] trustedCerts) {
        this.trustedCerts = trustedCerts;
    }
    
    public void init(XHandlerContext handlerConfig) {
        super.init(handlerConfig);
        boolean sigExisted = false;
        boolean capRequired = false;
        WsdlPort port = handlerConfig.getWsdlPort();
        for(Iterator i =
            port.elements(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL)
            .iterator();
            i.hasNext();) {
            XmlElement featureEl = (XmlElement) i.next();
            String uri = featureEl.getAttributeValue(null, WsdlUtil.URI_ATTR);
            if(MCtxConstants.FEATURE_SIGNATURE.equals(uri)) {
                logger.config("signaure attr existed");
                sigExisted = true;
            }
            else if(MCtxConstants.FEATURE_CAPABILITY.equals(uri)) {
                logger.config("capability attr existed");
                capRequired = true;
            }
            if(sigExisted && capRequired) {
                setHandlerDisabled(false);
                break;
            }
        }
        
        if(sigExisted == false && capRequired == true)  {
            throw new CapabilityConfigurationException(
                "server wsdl configuration missing signature handler");
        }
        
        logger.finest("handlerDisabled="+isHandlerDisabled());
    }
    
    public boolean processOutgoingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        
        if(context.element(MCtxConstants.NS,
                           MCtxConstants.SIGNED) != null) {
            throw new DynamicInfosetInvokerException(
                "Signature handler should be after capability handler.");
        }
        
        if(needCap(context)) {
            
            if(cap == null) {
                getCapability();
            }
            
            try {
                String subject =
                    CapabilityUtil.canonicalizeSubject(credential.getSubject());
                logger.finest("subject: " + subject);
                XmlDocument doc = (XmlDocument) soapEnvelope.getParent();
                XmlDocument capEnabled =
                    CapabilityEnforcer.newInstance(cap, subject).addCapability(doc);
                XmlElement el = capEnabled.getDocumentElement();
                XmlElement outgoingMessage  = (XmlElement) el.element(null, "Body")
                    .requiredElementContent()
                    .iterator().next();
                context.setOutgoingMessage(outgoingMessage);
                context.addElement(MCtxConstants.NS, MCtxConstants.CAPENFORCED);
            }
            catch(CapabilityException ce) {
                throw new DynamicInfosetInvokerException("could not add capability token", ce);
            }
        }
        return false;
    }
    
    public boolean processIncomingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        // nothing to do
        return false;
    }
    
    private void getCapability() throws CapabilityException {
        try {
            String wsdlLoc =
                CapabilityManager.class.getResource("capman.wsdl").toString();
            logger.finest("Using WSDL "+wsdlLoc);
            
            CapabilityManager cpstub = XpolaFactory.getCapman(capmanLoc);
            String subj =
                CapabilityUtil.canonicalizeSubject(credential.getSubject());
            String capstr = cpstub.getCapability(svcLoc, subj);
            logger.finest(capstr.toString());
            cap = new Capability(capstr);
        } catch (Exception e) {
            throw new CapabilityException("failed to get capability", e);
        }
        
        // get the cap from capman service specified automatically
        //        SoapHttpDynamicInfosetInvoker invoker =
        //            new SignatureInvoker(credential, trustedCerts, capmanLoc);
        //
        //        SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
        //            invoker, XsdTypeHandlerRegistry.getInstance());
        //
        //        CapabilityManager ref = (CapabilityManager) Proxy.newProxyInstance(
        //            Thread.currentThread().getContextClassLoader(),
        //            new Class[] { CapabilityManager.class },
        //            handler);
        //
        //        try {
        //            String _cap = ref.getCapabilityByHandle(svcLoc);
        //            logger.finest(_cap.toString());
        //            cap = new Capability(_cap);
        //        } catch (Exception e) {
        //            String error = "unable to get capability from "
        //                + capmanLoc + " for " + svcLoc;
        //            logger.severe(error);
        //            throw new DynamicInfosetInvokerException(error, e);
        //        }
    }
    
    private boolean needCap(MessageContext context) {
        boolean needCapCheck =
            (context.element(MCtxConstants.NS,
                             MCtxConstants.NOCAPABILITY) == null &&
                 context.element(MCtxConstants.NS,
                                 MCtxConstants.CAPENFORCED) == null);
        return needCapCheck;
    }
}


