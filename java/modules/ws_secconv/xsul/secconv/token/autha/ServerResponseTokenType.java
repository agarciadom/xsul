/**
 * ServerResponseTokenType.java
 *
 * @author Liang Fang
 * $Id: ServerResponseTokenType.java,v 1.3 2004/11/16 03:44:59 lifang Exp $
 */

package xsul.secconv.token.autha;

import java.io.IOException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import xsul.DataValidationException;
import xsul.XmlConstants;
import xsul.secconv.SCConstants;

public class ServerResponseTokenType extends XmlElementAdapter
{
    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static XmlNamespace aa = SCConstants.AANS;
    private final static XmlNamespace wsu = SCConstants.WSUNS;
    
    public final static String NAME = "ServerResponseToken";
    
    public ServerResponseTokenType() {
        super(builder.newFragment(aa, NAME));
    }
    
    public ServerResponseTokenType(XmlElement target) {
        super(target);
    }
    
    public byte[] getY() {
        String t = element(aa, "Y", true).requiredTextContent().trim();
        if(t != null) {
            try {
                byte[] x = decoder.decodeBuffer(t);
                return x;
            } catch (IOException e) {
                throw new DataValidationException ("illegal format of Y: "+t);
            }
        }
        
        throw new DataValidationException ("required aa:Y was not found in "+toString());
    }
    
    public void setY(byte[] y) {
        String encodedy = encoder.encode(y);
        XmlElement el = element(aa, "Y", true);
        el.removeAllChildren();
        el.addChild(encodedy);
    }
    
    public byte[] getAuthB() {
        String t = element(aa, "AuthB", true).requiredTextContent().trim();
        if(t != null) {
            try {
                byte[] x = decoder.decodeBuffer(t);
                return x;
            } catch (IOException e) {
                throw new DataValidationException ("illegal format of Y: "+t);
            }
        }
        throw new DataValidationException ("required aa:AuthB was not found in "+toString());
    }
    
    public void setAuthB(byte[] authb) {
        String enc_authb = encoder.encode(authb);
        XmlElement el = element(aa, "AuthB", true);
        el.removeAllChildren();
        el.addChild(enc_authb);
    }
    
    public String getId() {
        String t = element(wsu, "Id", true).requiredTextContent().trim();
        if(t != null) {
            return t;
        }
        throw new DataValidationException ("required wsu:Id was not found in "+toString());
    }
    
    public void setId(String id) {
        XmlElement el = element(wsu, "Id", true);
        el.removeAllChildren();
        el.addChild(id);
    }
    
}

