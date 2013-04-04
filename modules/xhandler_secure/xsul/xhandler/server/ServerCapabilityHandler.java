/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * based on xsul_dii.XsulDynamicInvoker.java,v 1.8 2005/01/18 10:02:38 aslom Exp $
 */
package xsul.xhandler.server;

import java.security.cert.X509Certificate;
import java.util.Iterator;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.saml.authorization.CapConstants;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityAuthorizer;
import xsul.dsig.saml.authorization.CapabilityException;
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

/* Work with SignatureHandler in a chain
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ServerCapabilityHandler.java,v 1.10 2006/04/30 06:48:14 aslom Exp $
 */
public class ServerCapabilityHandler extends BaseHandler {
    
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private GlobusCredential credential;
    private X509Certificate[] trustedCerts;
    private String svcurl;
    private String owner_name;
    
    public ServerCapabilityHandler(String name, String svcloc) {
        super(name);
        this.svcurl = svcloc;
        try {
            this.credential = GlobusCredential.getDefaultCredential();
            this.trustedCerts =
                CapabilityUtil.getTrustedCertificates(null).getCertificates();
            this.owner_name =
                CapabilityUtil.canonicalizeSubject(credential.getSubject());
        } catch (Exception e) {
            logger.config("could not configure server cap handler", e);
        }
    }
    
    public ServerCapabilityHandler(String name,
                                   GlobusCredential cred,
                                   X509Certificate[] trustedCerts,
                                   String svcloc) {
        super(name);
        this.credential = cred;
        this.trustedCerts = trustedCerts;
        this.svcurl = svcloc;
        this.owner_name = CapabilityUtil.canonicalizeSubject(cred.getSubject());
    }
    
    public void init(XHandlerContext handlerConfig) {
        super.init(handlerConfig);
        
        // wsdl(port) may not exist
        if(handlerConfig == null)
            return;
        
        boolean sigExisted = false;
        boolean capExisted = false;
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
                capExisted = true;
            }
            if(sigExisted && capExisted) {
                return;
            }
        }
        
        if(sigExisted == false)  {
            // change it to warning because it might serve in a Den service node
            logger.warning("missing signature handler");
            //            throw new CapabilityConfigurationException(
            //                "missing signature handler");
        }
        
        // add capability attr
        XmlElement featureEl =
            port.addElement(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL);
        featureEl.addAttribute(WsdlUtil.URI_ATTR,
                               MCtxConstants.FEATURE_CAPABILITY);
        featureEl.addAttribute(WsdlUtil.REQUIRED_ATTR, "true");
    }
    
    
    public boolean processOutgoingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        // nothing about capability here.
        return false;
    }
    
    public boolean processIncomingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        if(needCapCheck(context))  {
            final SoapUtil soapUtil = SoapUtil.selectSoapFragrance(
                soapEnvelope,
                new SoapUtil[]{Soap11Util.getInstance(),
                        Soap12Util.getInstance()});
            try {
                String service_uri = svcurl;
                if(service_uri == null || service_uri.equals("")) {
                    // fixme: get the service url at run time as the identifier
                    // service_uri = getServer().getLocation();
                }
                logger.finest("service uri: " + service_uri);
                
                CapabilityAuthorizer authorizer
                    = CapabilityAuthorizer.newInstance(service_uri, owner_name);
                
                if(authorizer == null)
                    throw new CapabilityException("No authorizer found");
                
                // assume being set from signature handler
                XmlElement prinEl = context.element(MCtxConstants.NS,
                                                    MCtxConstants.PRINCIPAL);
                if(prinEl == null) {
                    throw new DynamicInfosetInvokerException("principal null");
                }
                String principal = prinEl.requiredTextContent();
                XmlElement capEl =
                    soapEnvelope.element(null, XmlConstants.S_HEADER)
                    .element(null, "Security")
                    .element(CapConstants.SAML_NS, "Assertion");
                if(capEl == null) {
                    logger.finest("capability element null");
                    XmlElement faultEl = soapUtil
                        .generateSoapClientFault("unathorized access: capability null", null);
                    XmlDocument fault =
                        soapUtil.wrapBodyContent(faultEl);
                    context.setOutgoingMessage(faultEl);
                    return true;
                }
                Capability cap =
                    new Capability(builder.serializeToString(capEl));
                authorizer.isAuthorized(principal, cap, soapEnvelope);
                
                soapEnvelope.removeChild(capEl);
            }
            catch(CapabilityException e) {
                XmlElement faultEl = soapUtil
                    .generateSoapClientFault("unathorized access"
                                                 + e.getMessage(), null);
                XmlDocument fault =
                    soapUtil.wrapBodyContent(faultEl);
                context.setOutgoingMessage(faultEl);
                return true;
            }
        }
        
        return false;
    }
    
    private boolean needCapCheck(MessageContext context) {
        boolean needCapCheck =
            (context.element(MCtxConstants.NS,
                             MCtxConstants.NOCAPABILITYCHECK) == null &&
                 context.element(MCtxConstants.NS,
                                 MCtxConstants.CAPCHECKED) == null);
        return needCapCheck;
    }
}



