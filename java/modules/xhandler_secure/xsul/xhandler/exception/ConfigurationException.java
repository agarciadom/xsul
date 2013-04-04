/**
 * ConfigurationRequiredException.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ConfigurationException.java,v 1.1 2005/02/01 00:50:06 lifang Exp $
 */

package xsul.xhandler.exception;

import xsul.XsulException;

public class ConfigurationException extends XsulException {
    
    public ConfigurationException(String s) {
        super(s);
    }
    
    
    public ConfigurationException(String s, Throwable thrwble) {
        super(s, thrwble);
    }
    
}

