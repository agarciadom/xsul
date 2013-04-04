/**
 * RandomScheduler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: RandomScheduler.java,v 1.1 2005/03/19 18:02:56 lifang Exp $
 */

package xsul.den.scheduler;

import java.util.Random;
import java.util.Vector;
import xsul.dispatcher.routingtable.RoutingTable;
import xsul.dispatcher.routingtable.WS;

public class RandomScheduler  implements Scheduler {

    static final public String ALGORITHM = "Random";
    
    static private Random rand = new Random();
    private RoutingTable routingTable;
    
    public RandomScheduler(RoutingTable table) {
        this.routingTable = table;
    }
    
    public WS choose(String key) {
        Object val = routingTable.get(key);
        if(val instanceof WS) {
            return (WS)val;
        }
        else if(val instanceof Vector) {
            Vector vval = (Vector)val;
            int size = vval.size();
            if(size == 1) {
                return (WS)vval.elementAt(0);
            }
            else if(size > 1) {
                int i = rand.nextInt(size);
                return (WS)vval.elementAt(i);
            }
        }
        
        return null;
    }
    
}

