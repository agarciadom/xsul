/**
 * SecurityTokenReference.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: SecurityTokenReferenceType.java,v 1.1 2005/06/16 19:48:50 lifang Exp $
 */

package xsul.wssec;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.XmlConstants;
import xsul.dsig.DSConstants;

public class SecurityTokenReferenceType extends XmlElementAdapter {
    private final static XmlNamespace wsse = DSConstants.WSSE;
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static String NAME = "SecurityTokenReference";
    
    public SecurityTokenReferenceType() {
        super(builder.newFragment(wsse, NAME));
    }
    
    public SecurityTokenReferenceType(XmlElement target) {
        super(target);
    }
}

