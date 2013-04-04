/**
 * ServerResponseTokenType.java
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

public class ServerResponseTokenType extends XmlElementAdapter
{
    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    public final static String NAME = "ServerResponseToken";
    
    private final static XmlNamespace pki = SCConstants.PKINS;

    public ServerResponseTokenType() {
        super(builder.newFragment(pki, NAME));
    }
    
    public ServerResponseTokenType(XmlElement target) {
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

    public byte[] getSecret() {
        String t = element(pki, "Secret", true).requiredTextContent().trim();
        if(t != null) {
            try {
                byte[] sec = decoder.decodeBuffer(t);
                return sec;
            } catch (IOException e) {
                throw new DataValidationException ("illegal format of public key: "+t);
            }
        }
        
        throw new DataValidationException ("required pki:Secret was not found in "+toString());
    }
    
    public void setSecret(byte[] sec) {
        String encodedy = encoder.encode(sec);
        XmlElement el = element(pki, "Secret", true);
        el.removeAllChildren();
        el.addChild(encodedy);
    }

    public String getAlgorithm() {
        String t = element(pki, "Algorithm", true).requiredTextContent().trim();
        if(t != null) {
            return t;
        }
        
        throw new DataValidationException ("required pki:Algorithm was not found in "+toString());
    }
    
    public void setAlgorithm(String sec) {
        XmlElement el = element(pki, "Algorithm", true);
        el.removeAllChildren();
        el.addChild(sec);
    }

}


