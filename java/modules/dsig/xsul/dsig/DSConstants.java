/**
 * DSConstants.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DSConstants.java,v 1.2 2005/06/22 05:53:41 lifang Exp $
 */

package xsul.dsig;

import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;

public interface DSConstants {

    public final static XmlInfosetBuilder BUILDER = XmlConstants.BUILDER;

    public final static XmlNamespace DSIG = BUILDER.newNamespace(
        "fds", "http://extreme.indiana.edu/ws/2005/06/fdsig");

    public final static XmlNamespace WSSE = BUILDER.newNamespace(
        "fwsse", "http://extreme.indiana.edu/ws/2005/06/fsec");

}

