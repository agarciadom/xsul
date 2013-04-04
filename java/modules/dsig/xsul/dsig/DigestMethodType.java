/**
 * DigestMethod.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DigestMethodType.java,v 1.1 2005/06/16 19:48:49 lifang Exp $
 */

package xsul.dsig;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.XmlConstants;

public class DigestMethodType  extends XmlElementAdapter {
    
    public final static String NAME = "DigestMethod";
    private final static XmlNamespace ds = DSConstants.DSIG;
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public DigestMethodType() {
        super(builder.newFragment(ds, NAME));
    }
    
    public DigestMethodType(XmlElement target) {
        super(target);
    }
    
    public void setAlgorithm(String algo) {
    }
    
    public String getAlgorithm() {
        return null;
    }
}

