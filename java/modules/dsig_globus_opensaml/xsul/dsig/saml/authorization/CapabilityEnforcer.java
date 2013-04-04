
package xsul.dsig.saml.authorization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.opensaml.SAMLAction;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAuthorizationDecisionStatement;
import org.opensaml.SAMLException;
import org.opensaml.SAMLSubject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.apache.axis.uti.XMLUtils;
import xsul.dsig.globus.security.authentication.wssec.WSConstants;
import xsul.soap11_util.Soap11Util;

/**
 * CapabilityEnforcer is for enforcing the capability policy by inserting the
 * corresponding assertions into a SOAP message header. It usually runs at the
 * user's side.
 *
 * @author Liang Fang [lifang@cs.indiana.edu]
 * $Id: CapabilityEnforcer.java,v 1.19 2006/04/30 06:48:13 aslom Exp $
 */
public class CapabilityEnforcer
{
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static MLogger logger = MLogger.getLogger();
    
    private Capability cap;
    private String subject;
    
    // whether the client side should look into the decision;
    // otherwise, the server side has to do it.
    private boolean clientsideEnabled = true;
    
    protected CapabilityEnforcer(Capability _cap, String _subject)
    {
        cap = _cap;
        subject = _subject;
    }
    
    protected CapabilityEnforcer(Capability _cap, String _subject,
                                 boolean clientsideEnabled)
    {
        cap = _cap;
        subject = _subject;
        this.clientsideEnabled = clientsideEnabled;
    }
    
    public static CapabilityEnforcer newInstance(Capability _cap, String _subject)
        throws CapabilityException
    {
        if(_cap ==null)
        {
            throw new CapabilityException("Capability can not be null");
        }
        return new CapabilityEnforcer(_cap, _subject);
        
    }
    
    public void setClientsideEnabled(boolean clientsideEnabled) {
        this.clientsideEnabled = clientsideEnabled;
    }
    
    public boolean isClientsideEnabled() {
        return clientsideEnabled;
    }
    
    public XmlDocument addCapability(XmlDocument envelope)
        throws CapabilityException
    {
        XmlElement root = envelope.getDocumentElement();
        if (Soap11Util.NS_URI_SOAP11.equals(root.getNamespace().getNamespaceName())
            && XmlConstants.S_ENVELOPE.equals(root.getName()))
        {
            XmlElement body = root.element(null, "Body");
            if(body == null)
                throw new CapabilityException("No SOAP Body found");
            String serializedBody = builder.serializeToString(body);
            XmlElement header = root.element(null, "Header");
            if(header == null)
                header =
                    root.addElement(0, builder.newFragment(Soap11Util.SOAP11_NS,
                                                           "Header"));
            
            XmlNamespace WSSE_NS =
                builder.newNamespace("wsse", WSConstants.WSSE_NS);
            XmlElement wssec = header.addElement(WSSE_NS, "Security");
            if (cap == null)
                throw new CapabilityException("No capability found");
            
            SAMLAssertion[] sas = cap.getAllAssertions();
            if (sas == null)
                throw new CapabilityException("No assertions found");
            
            try
            {
                for(int i = 0; i < sas.length;i++)
                {
                    logger.finest("\nNode to string "+i+": " + sas[i].toString());
                    Iterator iter = sas[i].getStatements();
                    if(iter.hasNext())
                    {
                        Object o = iter.next();
                        if(o instanceof SAMLAuthorizationDecisionStatement)
                        {
                            SAMLAuthorizationDecisionStatement authorst =
                                (SAMLAuthorizationDecisionStatement)o;
                            logger.finest("type SAMLAuthorizationDecisionStatement");
                            SAMLSubject sub = authorst.getSubject();
                            String namequal = sub.getNameIdentifier().getName();
                            logger.finest("name qual: " + namequal);
                            if(namequal.equals(subject))
                            {
                                Iterator actions = authorst.getActions();
                                if(actions.hasNext())
                                {
                                    SAMLAction action = (SAMLAction)actions.next();
                                    String actioname = action.getData();
                                    //fixme: should have a nicer to match the action and decision
                                    if(serializedBody.indexOf(actioname) >= 0) {
                                        if(clientsideEnabled) {
                                            String decision = authorst.getDecision();
                                            if(decision.equals(CapConstants.DENY)) {
                                                logger.finest(actioname + "got denied");
                                                throw new CapabilityException(actioname + "got denied");
                                            }
                                            else if(decision.equals(CapConstants.PERMIT)) {
                                                Node node = sas[i].toDOM();
                                                String sanode = XMLUtils.ElementToString((Element)node);
                                                XmlElement sa = builder.parseFragmentFromReader(new StringReader(sanode));
                                                wssec.addElement(sa);
                                                logger.finest("assertion added for action name: " + actioname);
                                            }
                                            else {
                                                logger.finest("unknown decision: " + decision);
                                                throw new CapabilityException("unknown decision: " + decision);
                                            }
                                        }
                                        else {
                                            Node node = sas[i].toDOM();
                                            String sanode = XMLUtils.ElementToString((Element)node);
                                            XmlElement sa = builder.parseFragmentFromReader(new StringReader(sanode));
                                            wssec.addElement(sa);
                                            logger.finest("assertion added for action name: " + actioname);
                                        }
                                        break;
                                    }
                                    else {
                                        logger.finest("no " + actioname + "in the body");
                                    }
                                }
                            }
                            else {
                                logger.finest("subject does not match: "+subject);
                            }
                        }
                    }
                }
            }
            catch (DOMException e) {throw new CapabilityException(e.getMessage());}
            catch (SAMLException e) {throw new CapabilityException(e.getMessage());}
        }
        else
        {
            String str1 = root.getNamespace().getNamespaceName();
            String str2 = root.getName();
            String errmsg = "namespace: " + str1 + "\n" + "localname: " + str2 + "\n" + builder.serializeToString(envelope);
            throw new CapabilityException("empty envelope: " + errmsg);
        }
        
        return envelope;
        
        //      String soapmsg = builder.serializeToString(envelope);
        //      String processed = addCapability(soapmsg);
        //      XmlDocument processedDoc = builder.parseReader(new StringReader(processed));
//
        //      return processedDoc;
    }
    
    /**
     * Method addCapability
     *
     * @param    envelope            a  String
     *
     * @return   a String
     *
     * @throws   CapabilityException
     *
     */
    public String addCapability(String envelope) throws CapabilityException
    {
        try
        {
            // TODO: take care of SOAP 1.2
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(envelope.getBytes()));
            
            Document env = addCapability(doc);
            
            ByteArrayOutputStream bf = new ByteArrayOutputStream();
            XMLUtils.DocumentToStream(env, bf);
            String soapenv = bf.toString();
            bf.close();
            logger.finest("\nenv with capability: " + soapenv);
            return soapenv;
        }
        catch (ParserConfigurationException e)
        {
            throw new CapabilityException("could not add capability "+e, e);
        }
        catch (SAXException e)
        {
            throw new CapabilityException("could not add capability "+e, e);
        }
        catch (IOException e)
        {
            throw new CapabilityException("could not add capability "+e, e);
        }
        catch (FactoryConfigurationError e)
        {
            throw new CapabilityException("could not add capability "+e);
        }
        
    }
    
    /**
     * Method addCapability
     *
     * @param    envelope            a  Document
     *
     * @return   a Document
     *
     * @throws   CapabilityException
     *
     */
    public Document addCapability(Document envelope) throws CapabilityException
    {
        Document doc = envelope;
        
        Element root = (Element)doc.getFirstChild();
        
        Element body = (Element)root.getFirstChild();
        
        Element header = doc.createElementNS(WSConstants.SOAP_NS, "S:Header");
        root.insertBefore(header, body);
        Element wssec = doc.createElementNS(WSConstants.WSSE_NS, "wsse:Security");
        header.appendChild(wssec);
        
        if (cap == null)
            throw new CapabilityException("No capability found");
        
        SAMLAssertion[] sas = cap.getAllAssertions();
        if (sas == null)
            throw new CapabilityException("No assertions found");
        
        try
        {
            for(int i = 0; i < sas.length;i++)
            {
                logger.finest("\nNode to string "+i+": " + sas[i].toString());
                //TODO: insert those assertions that are necessary according to
                // the actions/resources in the SOAP body.
                Element clonedNode = (Element)doc.importNode(sas[i].toDOM(),true);
                logger.finest("attribute just added: "+clonedNode.getAttributeNS(WSConstants.WSU_NS, "Id"));
                logger.finest("\nclonedNode to string "+i);
                wssec.appendChild(clonedNode);
            }
        }
        catch (DOMException e) {throw new CapabilityException(e.getMessage());}
        catch (SAMLException e) {throw new CapabilityException(e.getMessage());}
        return doc;
    }
    
}



