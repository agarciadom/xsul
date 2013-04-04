/**
 * RequestedSecurityTokenType.java
 *
 * @author Liang Fang
 * $Id: RequestedSecurityTokenType.java,v 1.2 2004/10/22 05:38:37 lifang Exp $
 */

package xsul.secconv.token;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidationException;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.secconv.SCConstants;

public class RequestedSecurityTokenType extends XmlElementAdapter
{
    private static final MLogger logger = MLogger.getLogger();
    
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static XmlNamespace wsc = SCConstants.WSCNS;
    private final static XmlNamespace wst = SCConstants.WSTNS;
    
    public final static String NAME = "RequestedSecurityToken";
    
    public RequestedSecurityTokenType() {
        super(builder.newFragment(wst, NAME));
    }
    
    public RequestedSecurityTokenType(XmlElement target) {
        super(target);
    }
    
    public XmlElement getSecurityContextToken() {
        return element(wsc, "SecurityContextToken", false);
    }
    
    public void setSecurityContextToken(XmlElement xel) {
        String EL_SCT = "SecurityContextToken";
        if(!xel.getNamespaceName().equals(wsc) && !xel.getName().equals(EL_SCT)) {
            throw new DataValidationException("expected element in "+wsc+" namespace with name "+EL_SCT);
        }
        XmlElement el = element(wsc, EL_SCT);
        if(el != null) {
            removeChild(el);
        }
        addChild(xel);
    }
    
}

