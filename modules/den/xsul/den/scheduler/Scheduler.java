/**
 * Scheduler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: Scheduler.java,v 1.1 2005/03/19 18:02:56 lifang Exp $
 */

package xsul.den.scheduler;

import xsul.dispatcher.routingtable.WS;

public interface Scheduler {
    
    public WS choose(String key);
    
}

