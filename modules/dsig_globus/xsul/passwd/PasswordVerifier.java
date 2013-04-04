/**
 * PasswordVerifier.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: PasswordVerifier.java,v 1.3 2006/04/30 06:48:12 aslom Exp $
 */

package xsul.passwd;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.passwd.token.UsernameTokenType;

public class PasswordVerifier {
    
    private String username;
    private byte[] password;
    
    public PasswordVerifier(String username, byte[] password) {
        this.username = username;
        this.password = password;
    }
    
    public void verifySoapMessage(XmlElement envelope)
        throws XsulException {
        
        XmlElement header = envelope.findElementByName(XmlConstants.S_HEADER);
        if(header == null) {
            throw new XsulException("SOAP Header with password is required");
        }
        XmlElement wssec = header.element(Constants.WSSENS, "Security");
        if(wssec == null) {
            throw new XsulException("Security header with password is required");
        }
        XmlElement untoken =
            wssec.element(Constants.WSSENS, "UsernameToken");
        if(untoken == null) {
            throw new XsulException("UsernameToken is required");
        }
        UsernameTokenType utt =
            (UsernameTokenType)XmlElementAdapter.castOrWrap(untoken, UsernameTokenType.class);
        String pusername = utt.getUsername();
        String ppassword = new String(utt.getPassword());
        
        if(!pusername.equalsIgnoreCase(username) ||
           !ppassword.equals(new String(password)))
            throw new XsulException("Password verification failed.");
    }
    
}

