/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: RoutingTableImpl.java,v 1.7 2005/03/23 02:07:19 lifang Exp $
 */
package xsul.dispatcher.routingtable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import xsul.MLogger;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * An implementation of Routing Table. This implementation is based on a
 * Concurrent Reader Hash Map and could be use a simple properties file to
 * create its routing table.
 *
 * @see EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap
 * @see xsul.dispatcher.routingtable.RoutingTable
 *
 * @author Alexandre di Costanzo
 *
 */
public class RoutingTableImpl implements RoutingTable {
    private final static MLogger logger = MLogger.getLogger();

    private ConcurrentReaderHashMap routingTable = null;

    /**
     * Create an empty Routing table.
     *
     * @param initialCapacity
     *            the initial capacity of the routing table.
     * @param loadFactor
     *            the load factor of the routing table.
     */
    public RoutingTableImpl(int initialCapacity, float loadFactor) {
        this.routingTable = new ConcurrentReaderHashMap(initialCapacity,
                loadFactor);
    }

    /**
     * Create an empty Routing table.
     *
     * @param initialCapacity
     *            the initial capacity of the routing table.
     */
    public RoutingTableImpl(int initialCapacity) {
        this.routingTable = new ConcurrentReaderHashMap(initialCapacity);
    }

    /**
     * Create a Routing table from the
     * <code>/xsul/dispatcher/routingtable/table.properties</code> file.
     */
    public RoutingTableImpl() {
        this.routingTable = new ConcurrentReaderHashMap();
        String tablefile = System.getProperty("table");
        if(tablefile == null) {
            tablefile = RoutingTableImpl.class.getResource(
                "/xsul/dispatcher/routingtable/table.properties").getPath();
        }
        File routingFile = new File(tablefile);

        this.fillTableWithFile(routingFile);
    }

    /**
     * Create a Routing table from a specified properties file.
     *
     * @param data
     *            the specified file.
     */
    public RoutingTableImpl(File data) {
        this.routingTable = new ConcurrentReaderHashMap();
        this.fillTableWithFile(data);
    }

    private void fillTableWithFile(File data) {
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
        while (enumKey.hasMoreElements()) {
            String pathVirtual = (String) enumKey.nextElement();
            String pathWS = dataProps.getProperty(pathVirtual);
            if (!pathWS.startsWith("http")) {
                pathWS = "http://" + pathWS;
            }
            WS element = new WS();
            try {
                URI wsUrl = new URI (pathWS);
                element.setHost(wsUrl.getHost());
                element.setPort(wsUrl.getPort());
                element.setPath(wsUrl.getPath());
                element.setWsaElementTo(wsUrl);
            } catch (URISyntaxException e) {
                logger.warning("Couldn't create an URI", e);
            }
            this.routingTable.put(pathVirtual, element);
            logger.finest(pathVirtual + "->" + element
                    + " added in the Routing Table");
        }
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#clear()
     */
    public void clear() {
        this.routingTable.clear();
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#contains(xsul.dispatcher.routingtable.WS)
     */
    public boolean contains(Object value) {
        return this.routingTable.contains(value);
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#containsKey(java.lang.String)
     */
    public boolean containsKey(String key) {
        return this.routingTable.containsKey(key);
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#containsValue(xsul.dispatcher.routingtable.WS)
     */
    public boolean containsValue(Object value) {
        return this.routingTable.containsValue(value);
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#elements()
     */
    public Enumeration elements() {
        return this.routingTable.elements();
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#get(java.lang.String)
     */
    public Object get(String key) {
        try {
            return (WS) this.routingTable.get(key);
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#isEmpty()
     */
    public boolean isEmpty() {
        return this.routingTable.isEmpty();
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#keys()
     */
    public Enumeration keys() {
        return this.routingTable.keys();
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#put(java.lang.String,
     *      xsul.dispatcher.routingtable.WS)
     */
    public Object put(String key, Object value) {
        return (WS) this.routingTable.put(key, value);
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#putAll(xsul.dispatcher.routingtable.RoutingTableImpl)
     */
    public void putAll(RoutingTable t) {
        this.routingTable.putAll(t.getTable());
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#remove(java.lang.String)
     */
    public Object remove(String key) {
        return (WS) this.routingTable.remove(key);
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#size()
     */
    public int size() {
        return this.routingTable.size();
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#values()
     */
    public Collection values() {
        return this.routingTable.values();
    }

    public Map getTable() {
        return this.routingTable;
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#containsKey(java.net.URI)
     */
    public boolean containsKey(URI key) {
        return this.routingTable.containsKey(key);
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#get(java.net.URI)
     */
    public Object get(URI toKey) {
        return (WS) this.routingTable.get(toKey);
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#put(java.net.URI,
     *      xsul.dispatcher.routingtable.WS)
     */
    public Object put(URI key, Object value) {
        return (WS) this.routingTable.put(key, value);
    }

    /**
     * @see xsul.dispatcher.routingtable.RoutingTable#remove(java.net.URI)
     */
    public Object remove(URI key) {
        return (WS) this.routingTable.remove(key);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 *
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 *
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 *
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 *
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */

