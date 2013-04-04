/**
 * RoundRobinScheduler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: RoundRobinScheduler.java,v 1.1 2005/03/19 18:02:56 lifang Exp $
 */

package xsul.den.scheduler;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;
import java.util.Vector;
import xsul.dispatcher.routingtable.RoutingTable;
import xsul.dispatcher.routingtable.WS;


public class RoundRobinScheduler implements Scheduler {
    
    static final public String ALGORITHM = "Round Robin";
    
    private RoutingTable routingTable;
    private ConcurrentHashMap stateTable;
    
    public RoundRobinScheduler(RoutingTable table) {
        this.routingTable = table;
        stateTable = new ConcurrentHashMap(routingTable.size());
        for(Enumeration _enum = routingTable.keys(); _enum.hasMoreElements();) {
            Object key = _enum.nextElement();
            // begin from 0
            stateTable.put(key, new Integer(0));
        }
        
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
                int i = ((Integer)stateTable.get(key)).intValue();
                WS ws = (WS)vval.elementAt(i % size); // vector might have shrinked
                stateTable.put(key, new Integer(i++ % size));
                return ws;
            }
        }
        
        return null;
    }
    
    class State {
        
    }
}

