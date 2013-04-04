/**
 * UsernameToken.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: UsernameTokenType.java,v 1.1 2005/01/11 01:59:18 lifang Exp $
 */

package xsul.passwd.token;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidationException;
import xsul.XmlConstants;
import xsul.passwd.Constants;

public class UsernameTokenType  extends XmlElementAdapter {
    
    public final static XmlNamespace wssens = Constants.WSSENS;
    
    public final static String NAME = "UsernameToken";
    
    public UsernameTokenType() {
        super(XmlConstants.BUILDER.newFragment(wssens, NAME));
    }
    
    public UsernameTokenType(XmlElement target) {
        super(target);
    }
    
    public String getUsername() {
        String t = element(wssens, "Username", true).requiredTextContent().trim();
        if(t != null) {
            return t;
        }
        
        throw new DataValidationException ("required wsse:Username was not found in "+toString());
    }
    
    public void setUsername(String sec) {
        XmlElement el = element(wssens, "Username", true);
        el.removeAllChildren();
        el.addChild(sec);
    }
    
    public byte[] getPassword() {
        String t = element(wssens, "Password", true).requiredTextContent().trim();
        if(t != null) {
            byte[] passwd = t.getBytes();
            return passwd;
        }
        
        throw new DataValidationException ("required wsse:Password was not found in "+toString());
    }
    
    public void setPassword(byte[] pub) {
        String encodedy = new String(pub);
        XmlElement el = element(wssens, "Password", true);
        el.removeAllChildren();
        el.addChild(encodedy);
    }
    
}

