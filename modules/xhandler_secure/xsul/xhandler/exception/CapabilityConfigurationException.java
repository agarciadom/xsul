/**
 * CapabilityConfigurationRequiredException.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: CapabilityConfigurationException.java,v 1.1 2005/02/01 00:50:06 lifang Exp $
 */

package xsul.xhandler.exception;

public class CapabilityConfigurationException extends ConfigurationException{
    
    public CapabilityConfigurationException(String s) {
        super(s);
    }
    
    
    public CapabilityConfigurationException(String s,
                                            Throwable thrwble) {
        super(s, thrwble);
    }
}

