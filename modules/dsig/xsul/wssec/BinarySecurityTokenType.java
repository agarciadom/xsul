/**
 * BinarySecurityToken.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: BinarySecurityTokenType.java,v 1.1 2005/06/16 19:48:50 lifang Exp $
 */

package xsul.wssec;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.XmlConstants;
import xsul.dsig.DSConstants;

public class BinarySecurityTokenType extends XmlElementAdapter {
    private final static XmlNamespace wsse = DSConstants.WSSE;
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static String NAME = "BinarySecurityToken";
    
    public BinarySecurityTokenType() {
        super(builder.newFragment(wsse, NAME));
    }
    
    public BinarySecurityTokenType(XmlElement target) {
        super(target);
    }
    
    public void setEncodingType(String encoding) {
    }
    
    public String getEncodingType() {
        return null;
    }

    public void setValueType(String valtype) {
    }
    
    public String getValueType() {
        return null;
    }

    public void setId(String id) {
    }
    
    public String getId() {
        return null;
    }
}

