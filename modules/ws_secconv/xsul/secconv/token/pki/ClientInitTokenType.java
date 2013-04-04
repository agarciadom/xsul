/**
 * ClientInitTokenType.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.secconv.token.pki;

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

public class ClientInitTokenType extends XmlElementAdapter
{
    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static XmlNamespace pki = SCConstants.PKINS;
    private final static XmlNamespace wsu = SCConstants.WSUNS;
    
    public final static String NAME = "ClientInitToken";
    
    public ClientInitTokenType() {
        super(builder.newFragment(pki, NAME));
    }
    
    public ClientInitTokenType(XmlElement target) {
        super(target);
    }
    
    public byte[] getPublicKey() {
        String t = element(pki, "PublicKey", true).requiredTextContent().trim();
        if(t != null) {
            try {
                byte[] pub = decoder.decodeBuffer(t);
                return pub;
            } catch (IOException e) {
                throw new DataValidationException ("illegal format of public key: "+t);
            }
        }
        
        throw new DataValidationException ("required pki:PublicKey was not found in "+toString());
    }
    
    public void setPublicKey(byte[] pub) {
        String encodedy = encoder.encode(pub);
        XmlElement el = element(pki, "PublicKey", true);
        el.removeAllChildren();
        el.addChild(encodedy);
    }

}

