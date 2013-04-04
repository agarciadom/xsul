/**
 * CapabilityAuthorizer is responsible for checking the authorization
 * policy in the capability token.
 *
 * @author    Liang Fang lifang@cs.indiana.edu
 * $Id: CapabilityAuthorizer.java,v 1.20 2006/04/30 06:48:13 aslom Exp $
 */


package xsul.dsig.saml.authorization;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xml.security.Init;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.apache.xml.security.signature.XMLSignature;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.opensaml.SAMLAction;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAuthorizationDecisionStatement;
import org.opensaml.SAMLDecision;
import org.opensaml.SAMLException;
import org.opensaml.XML;
import org.w3c.dom.Element;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.SignatureInfo;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeVerifier;

public class CapabilityAuthorizer {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static final MLogger logger = MLogger.getLogger();
    private static DocumentBuilderFactory dbfNonValidating;
    
    private String service_identifier = "";
    // service provider DN
    private String provider = "";
    
    static {
        Init.init();
        dbfNonValidating = DocumentBuilderFactory.newInstance();
        dbfNonValidating.setNamespaceAware(true);
    }
    
    protected CapabilityAuthorizer(String _service_uri, String _provider)
        throws CapabilityException {
        service_identifier = _service_uri;
        if(_provider == null) {
            try {
                GlobusCredential cred = GlobusCredential.getDefaultCredential();
                this.provider =
                    CapabilityUtil.canonicalizeSubject(cred.getSubject());
            }
            catch (GlobusCredentialException e) {
                throw new CapabilityException("could not get credential", e);
            }
        }
        else {
            this.provider = CapabilityUtil.canonicalizeSubject(_provider);
        }
    }
    
    protected CapabilityAuthorizer(String _service_uri)
        throws CapabilityException {
        service_identifier = _service_uri;
        try {
            GlobusCredential cred = GlobusCredential.getDefaultCredential();
            this.provider =
                CapabilityUtil.canonicalizeSubject(cred.getSubject());
        }
        catch (GlobusCredentialException e) {
            throw new CapabilityException("could not get credential", e);
        }
    }
    
    static public CapabilityAuthorizer newInstance(String _service_uri,
                                                   String _owner)
        throws CapabilityException {
        return new CapabilityAuthorizer(_service_uri, _owner);
    }
    
    static public CapabilityAuthorizer newInstance(String _service_uri)
        throws CapabilityException {
        return new CapabilityAuthorizer(_service_uri);
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setServiceIdentifier(String service_identifier) {
        this.service_identifier = service_identifier;
    }
    
    public String getServiceIdentifier() {
        return service_identifier;
    }
    
    /**
     * Method isAuthorized internally supports both capability-based or
     * ACL-based authorization. If capability is null here, we switch to
     * ACL.
     *
     * @param    principal           a  String
     * @param    cap                 a  Capability
     * @param    soapEnv             a  XmlElement of the whole soap message
     *
     * @exception   CapabilityException
     *
     */
    public void isAuthorized(String principal, Capability cap,
                             XmlElement soapEnv)
        throws CapabilityException {
        logger.entering();
        if(cap == null) {
            throw new CapabilityException("capability null");
        }
        
        Vector soapActions = getSoapActions(soapEnv);
        isAuthorized(cap,
                     new Object[]{provider, principal, soapActions});
        logger.exiting();
    }
    
    public void isAuthorized(Principal principal, Capability cap,
                             XmlElement soapEnv)
        throws CapabilityException {
        isAuthorized(principal.getName(), cap, soapEnv);
    }
    
    /**
     * Method isAuthorized verifies the authorization information
     * against the authorization policy, including the identifier, users, and
     * authz actions/decisions. It is agnostic of SOAP. For the time being,
     * because we are adopting SAML as the policy language, it is bound to SAML;
     * but it could be generalized and overloaded if necessary in the future.
     * Perhaps the current one should be extended as SAMLCapAuthorizer.
     *
     * @param    cap                 a  Capability
     * @param    principal           a  String
     * @param    actions             a  Vector of actions that are being taken
     * by the requester.
     *
     * @exception   CapabilityException
     *
     */
    public void isAuthorized(Capability cap,
                             Object[] members)
        throws CapabilityException {
        
        cap.verify();
        
        String provider = (String)members[0];
        String principal = (String)members[1];
        Collection actions = (Collection)members[2];
        // check whether the capability matches the soap call in the env
        SAMLAssertion[] sas = cap.getAllAssertions();
        if(sas == null)
            throw new CapabilityException("no capability available");
        
        for(int i = 0; i < sas.length;i++) {
            try {
                Element saElem = (Element)sas[i].toDOM();
                if(saElem == null) {
                    throw new CapabilityException("could not find corresponding assertion");
                }
                Principal subjectDn = getSubjectDN(saElem);
                
                if(subjectDn != null) {
                    logger.finest("subject DN= " + subjectDn.getName());
                    // check whether the cap owner is the service provider
                    checkIssuer(provider, subjectDn);
                }
            } catch (SAMLException e) {
                throw new CapabilityException(e.getMessage());
            }
            
            Iterator iter = sas[i].getStatements();
            while(iter.hasNext()) {
                SAMLAuthorizationDecisionStatement authorst=null;
                Object o = iter.next();
                logger.finest("class type: " + o.getClass());
                if(o instanceof SAMLAuthorizationDecisionStatement) {
                    authorst = (SAMLAuthorizationDecisionStatement)o;
                }
                else
                    throw new CapabilityException("unable to process: "
                                                      + o.getClass());
                
                if(authorst != null) {
                    checkIdentifier(authorst, service_identifier);
                    checkUserSubject(authorst, principal);
                    checkActions(authorst, actions);
                }
            }
        }
    }
    
    /**
     * Method isAuthorized. see isAuthorized(XmlElement envelope)
     *
     * @deprecated
     */
    public void isAuthorized(String envelope) throws Exception {
        XmlElement env =
            builder.parseFragmentFromReader(new StringReader(envelope));
        isAuthorized(env);
    }
    
    /**
     * Method isAuthorized actually does both signature and authorization
     * verification. It is deprecated as it is supposed to be the Signature
     * handlers' jobs on signatures.
     *
     * @deprecated
     */
    public void isAuthorized(XmlElement envelope) throws Exception {
        GlobusCredential credential = GlobusCredential.getDefaultCredential();
        X509Certificate[] trustedCerts =
            CapabilityUtil.getTrustedCertificates().getCertificates();
        SignatureInfo si =
            GlobusCredSOAPEnvelopeVerifier
            .getInstance(credential, trustedCerts)
            .verifySoapMessage(envelope);
        String principal = si.getSubjectDn().getName();
        
        Capability cap = getCapability(envelope);
        
        isAuthorized(principal, cap, envelope);
    }
    
    private void checkActions(SAMLAuthorizationDecisionStatement authorst,
                              Collection actions)
        throws CapabilityException {
        Iterator capactions = authorst.getActions();
        if(capactions == null)
            throw new CapabilityException("no actions!");
        // go through all the soap actions for their corresponding
        // assertions.
        for(Iterator iter = actions.iterator();iter.hasNext();) {
            Object o1 = iter.next();
            if(o1 instanceof String)
                logger.finest("o1 string: " + (String)o1);
            else
                continue;
            
            String name1 = (String)o1;
            while(capactions.hasNext()) {
                Object o2 = capactions.next();
                if(o2 instanceof SAMLAction) {
                    SAMLAction ac = (SAMLAction)o2;
                    logger.finest("SAMLAction namespace: "+ac.getNamespace());
                    String actionname = ac.getData();
                    logger.finest("SAMLAction data: "+actionname);
                    // fixme: here we have an issue with wsdl/soap style.
                    // doc/lit doesn't put the method name in the soap message,
                    // which brings problem comparing the method names. The
                    // ultimate solution should be that capabilityauthorizer
                    // has access to the method mapping table like a soap
                    // dispatcher. However, a makeshift can be that we make the
                    // name of the first parameter (wrapper) element begins with
                    // its method name explicitly.
                    // fixme: the qname should be compared eventually
                    if(!((name1.equalsIgnoreCase(actionname)
                            || name1.startsWith(actionname)) &&
                       authorst.getDecision().equals(SAMLDecision.PERMIT))) {
                        throw new CapabilityException(
                            "action: "
                                + actionname
                                + " is not authorized by the capability.\n");
                    }
                }
                else {
                    logger.finest("o2 class type: "+o2.getClass());
                }
            }
        }
    }
    
    private void checkIdentifier(SAMLAuthorizationDecisionStatement authorst,
                                 String identifier)
        throws CapabilityException {
        if(!authorst.getResource().equalsIgnoreCase(identifier)) {
            if(authorst.getResource().indexOf(identifier) == -1) {
                logger.finest("identifier: " + authorst.getResource());
                logger.finest("service uri:" + identifier);
                throw new CapabilityException("the identifier doesn't match!");
            }
        }
    }
    
    private void checkIssuer(String issuer, Principal subjectDn)
        throws CapabilityException {
        if(!CapabilityUtil.compareSubjects(issuer, subjectDn.getName())) {
            logger.finest("issuer: "+ issuer);
            throw new CapabilityException("the capability is not issued by the service owner");
        }
    }
    
    private void checkUserSubject(SAMLAuthorizationDecisionStatement authorst,
                                  String principal)
        throws CapabilityException {
        String name = authorst.getSubject().getNameIdentifier().getName();
        logger.finest("subject name: " + name);
        if(!CapabilityUtil.compareSubjects(principal, name)) {
            logger.finest("principal name: " + principal);
            throw new CapabilityException("the subject doesn't match!");
        }
    }
    
    private Capability getCapability(XmlElement envelope)
        throws Exception {
        
        XmlElement capEl =
            envelope.element(null, XmlConstants.S_HEADER)
            .element(null, "Security")
            .element(CapConstants.SAML_NS, "Assertion");
        Capability cap =
            new Capability(builder.serializeToString(capEl));
        return cap;
    }
    
    private Principal getSubjectDN(Element saElem)
        throws CapabilityException {
        X509Certificate[] certs = null;
        try {
            Element n =
                XML.getFirstChildElement(saElem, XML.XMLSIG_NS, "Signature");
            XMLSignature sig = new XMLSignature((Element)n,null);
            KeyInfo info = sig.getKeyInfo();
            if (info.containsX509Data()) {
                logger.info("keyinfo contains x509 data");
                int len = info.lengthX509Data();
                if(len!=1)
                    throw new CapabilityException("invalidX509Data: length="
                                                      + len);
                X509Data data = info.itemX509Data(0);
                int certLen = data.lengthCertificate();
                if (certLen <= 0)
                    throw new CapabilityException("invalidCertData: length="
                                                      + certLen);
                certs = new X509Certificate[certLen];
                XMLX509Certificate xmlCert;
                ByteArrayInputStream input;
                
                for (int j = 0; j < certLen; j++) {
                    xmlCert = data.itemCertificate(j);
                    input =
                        new ByteArrayInputStream(xmlCert.getCertificateBytes());
                    certs[j] = CertUtil.loadCertificate(input);
                }
            }
            else
                logger.info("try to get x509 data from security token");
        }
        catch (GeneralSecurityException e) {
            throw new CapabilityException("general security problem", e);
        }
        catch (XMLSecurityException e) {
            throw new CapabilityException("xml security problem", e);
        }
        
        return certs[0].getSubjectDN();
    }
    
    private Vector getSoapActions(XmlElement soapEnv)
        throws CapabilityException {
        if(soapEnv == null)
            throw new CapabilityException("SOAP Env null");
        
        XmlElement body = soapEnv.element(null, "Body");
        if(body == null)
            throw new CapabilityException("no SOAP body can be found");
        
        Iterator children = body.children();
        if(children == null)
            throw new CapabilityException("Body has no children");
        
        // Get all the soap actions
        // need to check again about the format issue: namespace ...
        Vector soapactions = new Vector(1);
        while(children.hasNext()) {
            XmlElement e = (XmlElement)children.next();
            soapactions.add(e.getName());
        }
        
        return soapactions;
    }
}

