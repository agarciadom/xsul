/**
 * SecurityContextTokenType.java
 *
 * @author Liang Fang
 * $Id: SecurityContextTokenType.java,v 1.2 2004/10/22 05:38:37 lifang Exp $
 */

package xsul.secconv.token;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidationException;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.secconv.SCConstants;

public class SecurityContextTokenType extends XmlElementAdapter
{
    private static final MLogger logger = MLogger.getLogger();
    
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static XmlNamespace wsc = SCConstants.WSCNS;
    private final static XmlNamespace wsu = SCConstants.WSUNS;
    
    public final static String NAME = "SecurityContextToken";
    
    public SecurityContextTokenType() {
        super(builder.newFragment(wsc, NAME));
    }
    
    public SecurityContextTokenType(URI uri) {
        super(builder.newFragment(wsc, NAME));
        setIdentifier(uri);
    }
    
    public SecurityContextTokenType(XmlElement target) {
        super(target);
    }
    
    public URI getIdentifier() {
        String t = element(wsc, "Identifier", true).requiredTextContent().trim();
        if(t != null) {
            try {
                return new URI(t);
            } catch(Exception e) {
                throw new DataValidationException ("wsc:Identifier mut be of type xs:anyURI "+toString(), e);
            }
        }
        throw new DataValidationException ("required wsc:Identifier was not found in "+toString());
    }
    
    public void setIdentifier(URI uri) {
        XmlElement el = element(wsc, "Identifier", true);
        el.removeAllChildren();
        el.addChild(uri.toString());
    }
    
    public String getId() {
        XmlAttribute att = findAttribute(wsu.getNamespaceName(), "Id");
        if(att != null) {
            return att.getValue();
        }
        //        String t = element(wsu, "Id", true).requiredTextContent().trim();
        //        if(t != null) {
        //            return t;
        //        }
        throw new DataValidationException ("required wsu:Id was not found in "+toString());
    }
    
    public void setId(String id) {
        XmlAttribute att = addAttribute(wsu, "Id", id);
        if(att == null)
            throw new DataValidationException ("failed to add Id: " + id);
        
    }
    
    public Element getElement(Document doc) {
        
        Element element =
            doc.createElementNS(
            SCConstants.WSC_NS, "wsc:SecurityContextToken"
        );
        
//        String id  = getId();
//        element.setAttributeNS(SCConstants.WSU_NS, "wsu:Id", id);
        
        Element iden_ele =
            doc.createElementNS(
            SCConstants.WSC_NS, "wsc:Identifier"
        );
        String idenstr = getIdentifier().toString();
        Text identxt = doc.createTextNode(idenstr);
        logger.finest("setting identifier vaule: " + idenstr);
        iden_ele.appendChild(identxt);
        element.appendChild(iden_ele);
        
        if(logger.isFinestEnabled()) {
            ByteArrayOutputStream rootElem = new ByteArrayOutputStream();
            XMLUtils.outputDOM(element, rootElem);
            logger.finest("SecurityContextToken Elemen=\n"+rootElem.toString());
        }

        return element;
    }
    
}

