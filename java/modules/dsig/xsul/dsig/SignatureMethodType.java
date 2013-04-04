/**
 * SignatureMethod.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: SignatureMethodType.java,v 1.1 2005/06/16 19:48:49 lifang Exp $
 */

package xsul.dsig;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.XmlConstants;
import xsul.dsig.DSConstants;

public class SignatureMethodType extends XmlElementAdapter {
    private final static XmlNamespace ds = DSConstants.DSIG;
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static String NAME = "SignatureMethod";
    
    public SignatureMethodType() {
        super(builder.newFragment(ds, NAME));
    }
    
    public SignatureMethodType(XmlElement target) {
        super(target);
    }

    public void setAlgorithm(String algo) {
    }
    
    public String getAlgorithm() {
        return null;
    }

}

