/**
 * SecConvConfigurationRequiredException.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: SecConvConfigurationException.java,v 1.1 2005/02/01 00:50:06 lifang Exp $
 */

package xsul.xhandler.exception;

public class SecConvConfigurationException
    extends ConfigurationException{
    
    public SecConvConfigurationException(String s) {
        super(s);
    }
    
    
    public SecConvConfigurationException(String s,
                                         Throwable thrwble) {
        super(s, thrwble);
    }
    
}

