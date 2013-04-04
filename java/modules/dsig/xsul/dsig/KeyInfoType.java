/**
 * KeyInfo.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: KeyInfoType.java,v 1.1 2005/06/16 19:48:49 lifang Exp $
 */

package xsul.dsig;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.XmlConstants;
import xsul.dsig.DSConstants;

public class KeyInfoType extends XmlElementAdapter {
    private final static XmlNamespace ds = DSConstants.DSIG;
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    public final static String NAME = "KeyInfo";
    
    public KeyInfoType() {
        super(builder.newFragment(ds, NAME));
    }
    
    public KeyInfoType(XmlElement target) {
        super(target);
    }
}

