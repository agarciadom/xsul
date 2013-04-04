/**
 * ClientInitTokenType.java
 *
 * @author Liang Fang
 * $Id: ClientInitTokenType.java,v 1.2 2004/10/22 05:38:37 lifang Exp $
 */

package xsul.secconv.token.autha;

import java.math.BigInteger;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import xsul.DataValidationException;
import xsul.XmlConstants;
import xsul.secconv.SCConstants;
import java.io.IOException;

public class ClientInitTokenType extends XmlElementAdapter
{
    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static XmlNamespace aa = SCConstants.AANS;
    private final static XmlNamespace wsu = SCConstants.WSUNS;
    
    public final static String NAME = "ClientInitToken";
    
    public ClientInitTokenType() {
        super(builder.newFragment(aa, NAME));
    }
    
    public ClientInitTokenType(XmlElement target) {
        super(target);
    }
    
    public int getBitLength() {
        String t = element(aa, "BitLength", true).requiredTextContent().trim();
        if(t != null) {
            return Integer.parseInt(t);
        }
        throw new DataValidationException ("required aa:BitLength was not found in "+toString());
    }
    
    public void setBitLength(int len) {
        XmlElement el = element(aa, "BitLength", true);
        el.removeAllChildren();
        el.addChild(Integer.toString(len));
    }
    
    public BigInteger getP() {
        String t = element(aa, "P", true).requiredTextContent().trim();
        if(t != null) {
            try {
                byte[] b = decoder.decodeBuffer(t);
                return new BigInteger(b);
            } catch (IOException e) {
                throw new DataValidationException ("illegal format of P: "+t);
            }
        }
        throw new DataValidationException ("required aa:P was not found in "+toString());
    }
    
    public void setP(BigInteger p) {
        byte[] pbytes = p.toByteArray();
        String encodedp = encoder.encode(pbytes);
        XmlElement el = element(aa, "P", true);
        el.removeAllChildren();
        el.addChild(encodedp);
    }
    
    public BigInteger getG() {
        String t = element(aa, "G", true).requiredTextContent().trim();
        if(t != null) {
            try {
                byte[] b = decoder.decodeBuffer(t);
                return new BigInteger(b);
            } catch (IOException e) {
                throw new DataValidationException ("illegal format of G: "+t);
            }
        }
        throw new DataValidationException ("required aa:G was not found in "+toString());
    }
    
    public void setG(BigInteger g) {
        byte[] gbytes = g.toByteArray();
        String encodedg = encoder.encode(gbytes);
        XmlElement el = element(aa, "G", true);
        el.removeAllChildren();
        el.addChild(encodedg);
    }
    
    public byte[] getX() {
        String t = element(aa, "X", true).requiredTextContent().trim();
        if(t != null) {
            try {
                byte[] x = decoder.decodeBuffer(t);
                return x;
            } catch (IOException e) {
                throw new DataValidationException ("illegal format of X: "+t);
            }
        }
        throw new DataValidationException ("required aa:X was not found in "+toString());
    }
    
    public void setX(byte[] xbytes) {
        String encodedx = encoder.encode(xbytes);
        XmlElement el = element(aa, "X", true);
        el.removeAllChildren();
        el.addChild(encodedx);
    }
    
    public String getClientName() {
        String t = element(aa, "ClientName", true).requiredTextContent().trim();
        if(t != null) {
            return t;
        }
        throw new DataValidationException ("required aa:ClientName was not found in "+toString());
    }
    
    public void setClientName(String cname) {
        XmlElement el = element(aa, "ClientName", true);
        el.removeAllChildren();
        el.addChild(cname);
    }
    
    public String getServerName() {
        String t = element(aa, "ServerName", true).requiredTextContent().trim();
        if(t != null) {
            return t;
        }
        throw new DataValidationException ("required aa:ServerName was not found in "+toString());
    }
    
    public void setServerName(String sname) {
        XmlElement el = element(aa, "ServerName", true);
        el.removeAllChildren();
        el.addChild(sname);
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

