/**
 * RequestSecurityTokenResponseType.java
 *
 * @author Liang Fang
 * $Id: RequestSecurityTokenResponseType.java,v 1.3 2004/11/16 03:44:59 lifang Exp $
 */

package xsul.secconv.token;

import java.net.URI;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidationException;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.secconv.SCConstants;

public class RequestSecurityTokenResponseType extends XmlElementAdapter {
    
    private static final MLogger logger = MLogger.getLogger();

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    private final static XmlNamespace aa = SCConstants.AANS;
    private final static XmlNamespace pki = SCConstants.PKINS;
    private final static XmlNamespace wst = SCConstants.WSTNS;

    public final static String NAME = "RequestSecurityTokenResponse";

    public RequestSecurityTokenResponseType() {
        super(builder.newFragment(wst, NAME));
    }

    public RequestSecurityTokenResponseType(XmlElement target) {
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
    
    public XmlElement getRequestedSecurityToken() {
        return element(wst, "RequestedSecurityToken", false);
    }
    
    public void setRequestedSecurityToken(XmlElement xel) {
//        XmlElement el = element(wst, "RequestedSecurityToken", true);
//        el.removeAllChildren();
//        el.addChild(xel);
        String EL_RST = "RequestedSecurityToken";
        if(!xel.getNamespaceName().equals(aa) && !xel.getName().equals(EL_RST)) {
            throw new DataValidationException("expected element in "+wst+" namespace with name "+EL_RST);
        }
        XmlElement el = element(wst, EL_RST);
        if(el != null) {
            removeChild(el);
        }
        addChild(xel);
    }
    
    public XmlElement getClientResponseToken() {
        return element(aa, "ClientResponseToken", false);
    }
    
    public void setClientResponseToken(XmlElement xel) {
        XmlElement el = element(aa, "ClientResponseToken", true);
        el.removeAllChildren();
        el.addChild(xel);
    }
    
    public XmlElement getServerResponseToken() {
        XmlElement el = element(aa, "ServerResponseToken", false);
        if(el == null)
            el = element(pki, "ServerResponseToken", false);
        return el;
    }
    
    public void setServerResponseToken(XmlElement xel) {
        String EL_SRT = "ServerResponseToken";
        if(!xel.getNamespaceName().equals(aa) && !xel.getName().equals(EL_SRT)) {
            throw new DataValidationException("expected element in "+aa+" namespace with name "+EL_SRT);
        }
        XmlElement el = element(aa, EL_SRT);
        if(el != null) {
            removeChild(el);
        }
        addChild(xel);
    }
}

