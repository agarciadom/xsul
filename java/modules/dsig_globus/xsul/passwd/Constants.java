/**
 * Constants.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: Constants.java,v 1.1 2005/01/11 01:59:17 lifang Exp $
 */

package xsul.passwd;

import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;

public class Constants {
    public final static XmlNamespace WSSENS = XmlConstants.BUILDER.newNamespace(
        "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
    
}

