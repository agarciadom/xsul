/**
 * RequestSecurityTokenType.java
 *
 * @author Liang Fang
 * $Id: RequestSecurityTokenType.java,v 1.5 2004/11/16 03:44:59 lifang Exp $
 */

package xsul.secconv.token;

import java.net.URI;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.secconv.SCConstants;
import xsul.secconv.token.autha.ClientInitTokenType;
import xsul.DataValidationException;

public class RequestSecurityTokenType extends XmlElementAdapter
{
    private static final MLogger logger = MLogger.getLogger();
    
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static XmlNamespace aa = SCConstants.AANS;
    private final static XmlNamespace pki = SCConstants.PKINS;
    private final static XmlNamespace wst = SCConstants.WSTNS;
    
    //private String context; // attribute
    
    public final static String NAME = "RequestSecurityToken";
    
    public RequestSecurityTokenType() {
        super(builder.newFragment(wst, NAME));
    }
    
    public RequestSecurityTokenType(XmlElement target) {
        super(target);
    }
    
    public void setTokenType(URI uri) {
        XmlElement el = element(wst, "TokenType", true);
        el.removeAllChildren();
        el.addChild(uri.toString());
    }
    
    public void setRequestType(URI uri) {
        XmlElement el = element(wst, "RequestType", true);
        el.removeAllChildren();
        el.addChild(uri.toString());
    }
    
    public void setClaimsType(String claims) {
        XmlElement el = element(wst, "Claims", true);
        el.removeAllChildren();
        el.addChild(claims);
    }
    
    public String getClaims() {
        return element(wst, "Claims", false).requiredTextContent().trim();
    }
    
    public String getTokenType() {
        return element(wst, "TokenType", false).requiredTextContent().trim();
    }
    
    public XmlElement getClientInitToken() {
        XmlElement t = element(aa, "ClientInitToken", false);
        if(t == null)
            t = element(pki, "ClientInitToken", false);
        return t;
    }
    
    
    public void setClientInitToken(XmlElement xel) {
        String EL_CIT = "ClientInitToken";
        if((!xel.getNamespaceName().equals(aa) ||
                !xel.getNamespaceName().equals(pki)) && !xel.getName().equals(EL_CIT)) {
            throw new DataValidationException("expected element in "+aa+" namespace with name "+EL_CIT);
        }
        if(xel.getNamespaceName().equals(aa)) {
            XmlElement el = element(aa, EL_CIT);
            if(el != null) {
                removeChild(el);
            }
        }
        else {
            XmlElement el = element(pki, EL_CIT);
            if(el != null) {
                removeChild(el);
            }
        }
        addChild(xel);
    }
}


