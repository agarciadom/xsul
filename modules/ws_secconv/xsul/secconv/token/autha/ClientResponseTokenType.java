/**
 * ClientResponseTokenType.java
 *
 * @author Liang Fang
 * $Id: ClientResponseTokenType.java,v 1.1 2004/09/23 06:29:45 lifang Exp $
 */

package xsul.secconv.token.autha;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import sun.misc.BASE64Encoder;
import xsul.DataValidationException;
import xsul.XmlConstants;
import xsul.secconv.SCConstants;

public class ClientResponseTokenType extends XmlElementAdapter
{
	private static BASE64Encoder encoder = new BASE64Encoder();

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

	private final static XmlNamespace aa = SCConstants.AANS;
	private final static XmlNamespace wsu = SCConstants.WSUNS;
	
    public final static String NAME = "ClientResponseToken";

    public ClientResponseTokenType() {
        super(builder.newFragment(aa, NAME));
    }

	public ClientResponseTokenType(XmlElement target) {
		super(target);
	}
	
	public byte[] getAuthA() {
		String t = element(aa, "AuthA", true).requiredTextContent().trim();
        if(t != null) {
			return t.getBytes();
		}
        throw new DataValidationException ("required aa:AuthA was not found in "+toString());
	}
		
	public void setAuthA(byte[] autha) {
		String enc_autha = encoder.encode(autha);
        XmlElement el = element(aa, "AuthA", true);
        el.removeAllChildren();
        el.addChild(enc_autha);
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

