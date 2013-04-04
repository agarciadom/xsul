/**
 * ChainedRoutingTable.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.den;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import xsul.MLogger;
import xsul.dispatcher.routingtable.RoutingTable;
import xsul.dispatcher.routingtable.WS;
import java.util.Map;

public class DenRoutingTable implements RoutingTable {
        
    private final static MLogger logger = MLogger.getLogger();
    public final static long RELOAD_INTERVAL = 5*60*1000;
    protected static ConcurrentReaderHashMap routingTable = null;
    private static TimerTask reloadTask = new TimerTask() {
        public void run() {
            fillTableWithFile(getRoutingFile());
        }
    };
    
    private static Timer timer = new Timer(true);
    static {
        timer.schedule(reloadTask, RELOAD_INTERVAL, RELOAD_INTERVAL);
    }
    
    public DenRoutingTable(int initialCapacity, float loadFactor) {
        routingTable = new ConcurrentReaderHashMap(initialCapacity,
                                                   loadFactor);
        fillTableWithFile(getRoutingFile());
    }
    
    public DenRoutingTable(int initialCapacity) {
        routingTable = new ConcurrentReaderHashMap(initialCapacity);
        fillTableWithFile(getRoutingFile());
    }
    
    public DenRoutingTable() {
        routingTable = new ConcurrentReaderHashMap();
        fillTableWithFile(getRoutingFile());
    }
    
    public DenRoutingTable(File data) {
        routingTable = new ConcurrentReaderHashMap();
        fillTableWithFile(data);
    }
    
    static private File getRoutingFile() {
        String tablefile = System.getProperty("table");
        if(tablefile == null) {
            tablefile = "table.properties";
        }
        File routingFile = new File(tablefile);
        return routingFile;
    }
    
    static private void fillTableWithFile(File data) {
        Properties dataProps = new Properties();
        FileInputStream in = null;
        
        try {
            in = new FileInputStream(data);
            dataProps.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            logger.finest("Couldn't found the default configuration file", e);
        } catch (IOException e) {
            logger.finest("Couldn't read the default configuration file", e);
        }
        
        Enumeration enumKey = dataProps.keys();
        Hashtable htable = new Hashtable();
        while (enumKey.hasMoreElements()) {
            String pathVirtual = (String) enumKey.nextElement();
            String pathWS = dataProps.getProperty(pathVirtual);
            
            String[] paths = pathWS.split(",");
            Vector wsv = new Vector(paths.length);
            for(int i = 0; i < paths.length;i++) {
                WS element = new WS();
                
                String proc = paths[i].replaceFirst("://.*", "");
                String host = paths[i].replaceFirst(".*://", "").replaceFirst(":.*", "");
                String path = paths[i].replaceFirst(".*/", "");
                String port = paths[i].replaceFirst(".*:", "").replaceFirst("/.*", "");
                element.setProtocol(proc);
                element.setHost(host);
                element.setPort(Integer.parseInt(port));
                element.setPath("/" + path);
                try {
                    element.setWsaElementTo(new URI(proc + "://" + host
                                                        + ":" + port
                                                        + "/" + path));
                } catch (URISyntaxException e) {
                    logger.warning("Couldn't create an URI", e);
                }
                wsv.add(element);
                logger.finest(pathVirtual + "->" + element
                                  + " added in the Routing Table");
            }
            
            htable.put(pathVirtual, wsv);
        }
        
        // do not want to be interrupted after clear()
        synchronized(routingTable) {
            routingTable.clear();
            routingTable.putAll(htable);
        }
    }
    
    
    public boolean contains(Object value) {
        for(Iterator iter = routingTable.values().iterator();
            iter.hasNext();) {
            Vector vec = (Vector)iter.next();
            if(vec.contains(value))
                return true;
        }
        
        return false;
    }
    
    public Enumeration elements() {
        return routingTable.elements();
    }
    
    public Enumeration keys() {
        return routingTable.keys();
    }
    
    public boolean isEmpty() {
        return routingTable.isEmpty();
    }
    
    public Object get(URI key) {
        Vector v = (Vector)routingTable.get(key);
        if(v == null)
            return null;
        
        return (WS[])v.toArray(new WS[0]);
    }
    
    public boolean containsValue(Object value) {
        // TODO
        return false;
    }
    
    public void clear() {
        routingTable.clear();
    }
    
    public Object put(String key, Object value) {
        // TODO
        return null;
    }
    
    public Collection values() {
        return routingTable.values();
    }
    
    public boolean containsKey(String key) {
        return routingTable.containsKey(key);
    }
    
    public void putAll(RoutingTable t) {
        // TODO
    }
    
    public Object put(URI key, Object value) {
        // TODO
        return null;
    }
    
    public boolean containsKey(URI key) {
        return routingTable.containsKey(key);
    }
    
    public int size() {
        return routingTable.size();
    }
    
    public Object remove(URI key) {
        // TODO
        return null;
    }
    
    public Object get(String key) {
        Vector vec = (Vector)routingTable.get(key);
        if(vec == null)
            return null;
        
        int size = vec.size();
        if(size == 0)
            return null;
        
        if(size == 1)
            return (WS)vec.firstElement();
        
        // randomly pick up one in the vector
        Random rand = new Random();
        int n = rand.nextInt(size);
        
        return (WS)vec.elementAt(n);
    }
    
    public Object remove(String key) {
        // TODO
        return null;
    }
    
    public Map getTable() {
        // TODO
        return routingTable;
    }
        
}

