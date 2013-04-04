/**
 * Signature.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: SignatureType.java,v 1.1 2005/06/16 19:48:49 lifang Exp $
 */

package xsul.dsig;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidationException;
import xsul.XmlConstants;
import xsul.dsig.DSConstants;

public class SignatureType extends XmlElementAdapter {
    private final static XmlNamespace ds = DSConstants.DSIG;
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static String NAME = "Signature";
    
    public SignatureType() {
        super(builder.newFragment(ds, NAME));
    }
    
    public SignatureType(XmlElement target) {
        super(target);
    }
    
    public XmlElement getSignedInfo() {
        return element(ds, "SignedInfo", false);
    }
    
    public void setSignedInfo(XmlElement xel) {
        String EL_SI = "SignedInfo";
        if(!xel.getNamespaceName().equals(ds) && !xel.getName().equals(EL_SI)) {
            throw new DataValidationException("expected element in "+ds+" namespace with name "+EL_SI);
        }
        XmlElement el = element(ds, EL_SI);
        if(el != null) {
            removeChild(el);
        }
        addChild(xel);
    }
}

