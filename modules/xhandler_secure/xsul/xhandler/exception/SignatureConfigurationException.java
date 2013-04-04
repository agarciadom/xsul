/**
 * SignatureConfigrationRequiredException.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: SignatureConfigurationException.java,v 1.1 2005/02/01 00:50:06 lifang Exp $
 */

package xsul.xhandler.exception;

public class SignatureConfigurationException
    extends ConfigurationException{
    
    public SignatureConfigurationException(String s) {
        super(s);
    }
    
    
    public SignatureConfigurationException(String s,
                                           Throwable thrwble) {
        super(s, thrwble);
    }
    
}

