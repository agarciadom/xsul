/**
 * @author Liang Fang lifang@cs.indiana.edu
 * $Id: CapabilityException.java,v 1.3 2005/01/10 20:00:14 aslom Exp $
*/

package xsul.dsig.saml.authorization;

import xsul.XsulException;

public class CapabilityException extends XsulException {

    public CapabilityException(String msg) {
        super(msg);
    }

    public CapabilityException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
