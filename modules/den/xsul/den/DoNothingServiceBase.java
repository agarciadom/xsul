/**
 * DoNothingService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DoNothingServiceBase.java,v 1.1 2005/03/23 18:06:09 lifang Exp $
 */

package xsul.den;

import xsul.xservo.XService;
import xsul.xservo.XServiceBase;
import xsul.message_router.MessageProcessingException;
import xsul.message_router.MessageContext;

public class DoNothingServiceBase extends XServiceBase implements XService {
    
    public DoNothingServiceBase(String name) {
        super(name);
    }
    
    public void invoke(MessageContext ctx) throws MessageProcessingException {
        // DO NOTHING!
    }
    
}

