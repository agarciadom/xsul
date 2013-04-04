/**
 * CapabilityExpirationException.java
 *
 * @author Liang Fang lifang@cs.indiana.edu
 * $Id: CapabilityExpirationException.java,v 1.3 2005/01/10 20:00:14 aslom Exp $
 */

package xsul.dsig.saml.authorization;

public class CapabilityExpirationException extends CapabilityException
{
    public CapabilityExpirationException(String msg) {
        super(msg);
    }

    public CapabilityExpirationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

