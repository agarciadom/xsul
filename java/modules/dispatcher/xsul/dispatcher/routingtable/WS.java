/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WS.java,v 1.4 2005/03/25 18:00:33 lifang Exp $
 */
package xsul.dispatcher.routingtable;

import java.net.URI;

import xsul.MLogger;
import xsul.dispatcher.msg.DispatcherMSG;
import xsul.dispatcher.msg.wsconnection.MSGWSConnection;
import xsul.ws_addressing.WsaEndpointReference;

/**
 * <p>
 * This class represents an element of the routing table.
 * </p>
 * <p>
 * Contains all informations for connecting and forwarding requests to a WS.
 * </p>
 *
 * @author Alexandre di Costanzo
 *
 */
public class WS {
    private final static MLogger logger = MLogger.getLogger();
    
    /**
     * WS's host.
     */
    private String host;
    
    /**
     * WS's port number.
     */
    private int port;
    
    /**
     * WS's path on the host.
     */
    private String path;
    
    /**
     * WS's protocol, currently support http and https.
     */
    private String protocol;
    /**
     * The connection with the WS.
     */
    private MSGWSConnection connection = null;
    
    /**
     * Create a default <code>WS</code> element with:
     * <ul>
     * <li>host = localhost</li>
     * <li>port = 80</li>
     * <li>path = /</li>
     * <li>and no pre-connection with the WS.</li>
     * </ul>
     */
    public WS() {
        this.host = "localhost";
        this.port = 80;
        this.path = "/";
        this.protocol = "http";
    }
    
    /**
     * Create a <code>WS</code> element with no pre-connection.
     *
     * @param host
     *            WS's host.
     * @param port
     *            WS's port number.
     * @param path
     *            WS's path on the host.
     */
    public WS(String host, int port, String path) {
        this.host = host;
        this.port = port;
        this.path = path;
        this.protocol = "http";
    }
    
    /**
     * Create a <code>WS</code> element with no pre-connection.
     *
     * @param host
     *            WS's host.
     * @param port
     *            WS's port number.
     * @param path
     *            WS's path on the host.
     */
    public WS(String host, int port, String path, String protocol) {
        this.host = host;
        this.port = port;
        this.path = path;
        this.protocol = protocol;
    }
    
    /**
     * @return Returns WS's host.
     */
    public String getHost() {
        return host;
    }
    
    /**
     * @return Returns WS's port number.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * @return Returns WS's path on the host.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * @param host
     *            WS's host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * @param path
     *            WS's path on the host to set.
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * @param port
     *            WS's port number to set.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    /**
     * Print the url of the WS.
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.protocol + "://" + this.host + ":" + this.port + this.path;
    }
    
    //-------------------------------------------------------------------------------
    //  Section for Messages version
    //-------------------------------------------------------------------------------
    
    private URI wsaTo;
    
    private WsaEndpointReference wsaReplyTo;
    
    private WsaEndpointReference wsaFaultTo;
    
    /**
     * @return the WS-Addressing element <i>To</i>.
     */
    public URI getWsaElementTo() {
        return this.wsaTo;
    }
    
    /**
     * @return the WS-Addressing element <i>Reply To</i>.
     */
    public WsaEndpointReference getWsaElementReplyTo() {
        return this.wsaReplyTo;
    }
    
    /**
     * @return the WS-Addressing element <i>Fault To</i>.
     */
    public WsaEndpointReference getWsaElementFaultTo() {
        return this.wsaFaultTo;
    }
    
    /**
     * @param wsaTo the WS-Addressing element <i>To</i>.
     */
    public void setWsaElementTo(URI wsaTo) {
        this.wsaTo = wsaTo;
    }
    
    /**
     * @param wsaReplyTo the WS-Addressing element <i>Reply To</i>.
     */
    public void setWsaElementReplyTo(WsaEndpointReference wsaReplyTo) {
        this.wsaReplyTo = wsaReplyTo;
    }
    
    /**
     * @param wsaFaultTo the WS-Addressing element <i>Fault To</i>
     */
    public void getWsaElementFaultTo(WsaEndpointReference wsaFaultTo) {
        this.wsaFaultTo = wsaFaultTo;
    }
    
    /**
     * Not a good implementation because use some methods in message package.
     *
     * @return the connection with the WS.
     */
    public MSGWSConnection getMsgConnection() {
        if (this.connection == null) {
            // Create a new Connection for the first time
            this.connection = new MSGWSConnection(this);
        }
        
        // Connection Steel alive ?
        if (this.connection.isKeepAlive()) {
            // Ok return it
            return this.connection;
        } else {
            try {
                // Use thread pool & Activate the thread
                DispatcherMSG.poolWSConnections.execute(this.connection);
                return this.connection;
            } catch (InterruptedException e) {
                logger.warning("Couldn't start a thread", e);
                return null;
            }
        }
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
