/**
 * PasswordEnforcer.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: PasswordEnforcer.java,v 1.3 2006/04/30 06:48:12 aslom Exp $
 */

package xsul.passwd;

import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.passwd.token.UsernameTokenType;
import xsul.soap11_util.Soap11Util;

public class PasswordEnforcer {
    
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    private String username;
    private byte[] password;
    
    public PasswordEnforcer(String username, byte[] password) {
        this.username = username;
        this.password = password;
    }
    
    public void enforceSoapMessage(XmlElement envelope)
        throws XsulException {
        
        UsernameTokenType utt = new UsernameTokenType();
        utt.setUsername(username);
        utt.setPassword(password);
        
        //XmlElement root = envelope.getDocumentElement();
        XmlElement header = envelope.findElementByName(XmlConstants.S_HEADER);
        if(header == null)
            header = envelope.addElement(0, builder.newFragment(Soap11Util.SOAP11_NS, XmlConstants.S_HEADER));
        XmlElement wssec = header.element(Constants.WSSENS, "Security", true);
        wssec.addElement(utt);
        
        //return envelope;
    }
}

