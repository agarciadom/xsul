/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: RoutingTable.java,v 1.4 2005/03/23 02:07:19 lifang Exp $
 */
package xsul.dispatcher.routingtable;

import java.net.URI;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

/**
 *
 * <p>
 * The Routing Table interface.
 * </p>
 *
 * <p>
 * Implements it to create your own Routing Table.
 * </p>
 *
 * <p>
 * The keys are <code>String</code> which represent the virtual path of the
 * WS. This virtual path must not start with a / but with the first virtual
 * directory.
 * </p>
 *
 * @author Alexandre di Costanzo
 *
 */
public interface RoutingTable {
    // TODO WSDL Import
    // TODO Yellow pages server

    /**
     * Clears the Routing Table so that it contains no keys.
     *
     */
    public void clear();

    /**
     * Tests if some key maps into the specified value in this Routing Table.
     *
     * @param value
     *            a value to search for.
     * @return <code>true</code> if and only if some key maps to the
     *         <code>value</code> argument in this Routing Table as determined
     *         by the <tt>equals</tt> method; <code>false</code> otherwise.
     * @exception NullPointerException
     *                if the value is <code>null</code>.
     * @see #containsKey(String)
     * @see #containsValue(Object)
     */
    public boolean contains(Object value);

    /**
     * Tests if the specified object is a key in this Routing Table.
     *
     * @param key
     *            possible key.
     * @return <code>true</code> if and only if the specified object is a key
     *         in this Routing Table, as determined by the <tt>equals</tt>
     *         method; <code>false</code> otherwise.
     * @throws NullPointerException
     *             if the key is <code>null</code>.
     * @see #contains(Object)
     */
    public boolean containsKey(String key);

    /**
     * Tests if the specified object is a key in this Routing Table.
     *
     * @param key
     *            possible key.
     * @return <code>true</code> if and only if the specified object is a key
     *         in this Routing Table, as determined by the <tt>equals</tt>
     *         method; <code>false</code> otherwise.
     * @throws NullPointerException
     *             if the key is <code>null</code>.
     * @see #contains(Object)
     */
    public boolean containsKey(URI key);

    /**
     * Returns true if this Routing Table maps one or more keys to this value.
     *
     * @param value
     *            value whose presence in this Routing Table is to be tested.
     * @return <tt>true</tt> if this Routing Table maps one or more keys to
     *         the specified value.
     * @throws NullPointerException
     *             if the value is <code>null</code>.
     */
    public boolean containsValue(Object value);

    /**
     * Returns an enumeration of the values in this Routing Table. Use the
     * Enumeration methods on the returned object to fetch the elements
     * sequentially.
     *
     * @return an enumeration of the values in this Routing Table.
     * @see java.util.Enumeration
     * @see #keys()
     * @see #values()
     */
    public Enumeration elements();

    /**
     * Returns the value to which the specified key is mapped in this Routing
     * Table.
     *
     * @param key
     *            a key in the Routing Table.
     * @return the value to which the key is mapped in this Routing Table;
     *         <code>null</code> if the key is not mapped to any value in this
     *         Routing Table.
     * @throws NullPointerException
     *             if the key is <code>null</code>.
     * @see #put(String, Object)
     */
    public Object get(String key);

    /**
     * Returns the value to which the specified key is mapped in this Routing
     * Table.
     *
     * @param key
     *            a key in the Routing Table.
     * @return the value to which the key is mapped in this Routing Table;
     *         <code>null</code> if the key is not mapped to any value in this
     *         Routing Table.
     * @throws NullPointerException
     *             if the key is <code>null</code>.
     * @see #put(URI, Object)
     */
    public Object get(URI Key);

    /**
     * Tests if this Routing Table maps no keys to values.
     *
     * @return <code>true</code> if this Routing Table maps no keys to values;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty();

    /**
     * Returns an enumeration of the keys in this Routing Table.
     *
     * @return an enumeration of the keys in this Routing Table.
     * @see Enumeration
     * @see #elements()
     */
    public Enumeration keys();

    /**
     * Maps the specified <code>key</code> to the specified <code>value</code>
     * in this Routing Table. Neither the key nor the value can be
     * <code>null</code>.
     * <p>
     *
     * The value can be retrieved by calling the <code>get</code> method with
     * a key that is equal to the original key.
     *
     * @param key
     *            the Routing Table key.
     * @param value
     *            the value.
     * @return the previous value of the specified key in this Routing Table, or
     *         <code>null</code> if it did not have one.
     * @exception NullPointerException
     *                if the key or value is <code>null</code>.
     * @see Object#equals(Object)
     * @see #get(String)
     */
    public Object put(String key, Object value);

    /**
     * Maps the specified <code>key</code> to the specified <code>value</code>
     * in this Routing Table. Neither the key nor the value can be
     * <code>null</code>.
     * <p>
     *
     * The value can be retrieved by calling the <code>get</code> method with
     * a key that is equal to the original key.
     *
     * @param key
     *            the Routing Table key.
     * @param value
     *            the value.
     * @return the previous value of the specified key in this Routing Table, or
     *         <code>null</code> if it did not have one.
     * @exception NullPointerException
     *                if the key or value is <code>null</code>.
     * @see Object#equals(Object)
     * @see #get(URI)
     */
    public Object put(URI key, Object value);

    /**
     * Copies all of the mappings from the specified Map to this Routing Table
     * These mappings will replace any mappings that this Routing Table had for
     * any of the keys currently in the specified Routing Table.
     *
     * @param t
     *            Mappings to be stored in this Routing Table.
     * @throws NullPointerException
     *             if the specified Routing Table is null.
     */
    public void putAll(RoutingTable t);

    /**
     * Removes the key (and its corresponding value) from this Routing Table.
     * This method does nothing if the key is not in the Routing Table.
     *
     * @param key
     *            the key that needs to be removed.
     * @return the value to which the key had been mapped in this routing Table,
     *         or <code>null</code> if the key did not have a mapping.
     * @throws NullPointerException
     *             if the key is <code>null</code>.
     */
    public Object remove(String key);

    /**
     * Removes the key (and its corresponding value) from this Routing Table.
     * This method does nothing if the key is not in the Routing Table.
     *
     * @param key
     *            the key that needs to be removed.
     * @return the value to which the key had been mapped in this routing Table,
     *         or <code>null</code> if the key did not have a mapping.
     * @throws NullPointerException
     *             if the key is <code>null</code>.
     */
    public Object remove(URI key);

    /**
     * Returns the number of keys in this Routing Table.
     *
     * @return the number of keys in this Routing Table.
     */
    public int size();

    /**
     * Returns a Collection view of the values contained in this Routing Table.
     * The Collection is backed by the Routing Table, so changes to the Routing
     * Table are reflected in the Collection, and vice-versa. The Collection
     * supports element removal (which removes the corresponding entry from the
     * Routing Table), but not element addition.
     *
     * @return a collection view of the values contained in this Routing Table.
     */
    public Collection values();

    /**
     * Returns the whole routing table.
     *
     * @return a map view of the whole Routing Table.
     */
    public Map getTable();
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
