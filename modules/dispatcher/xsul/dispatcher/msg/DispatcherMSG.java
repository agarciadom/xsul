/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: DispatcherMSG.java,v 1.2 2004/09/23 12:26:54 adicosta Exp $
 */
package xsul.dispatcher.msg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import xsul.MLogger;
import xsul.dispatcher.msg.clientconnection.PoolClientConnection;
import xsul.dispatcher.msg.wsconnection.PoolWSConnection;
import xsul.dispatcher.routingtable.RoutingTable;
import xsul.dispatcher.routingtable.RoutingTableImpl;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.HttpMiniServlet;

/**
 * The main class to start the Messaging Dispatcher.
 * 
 * @author Alexandre di Costanzo
 *  
 */
public class DispatcherMSG {
    private final static MLogger logger = MLogger.getLogger();

    private final static String DEFAULT_CONF_FILE_PATH = DispatcherMSG.class
            .getResource("/xsul/dispatcher/msg/default.properties").getPath();

    public static Properties CONFIGURATION;

    private HttpMiniServer httpServer;

    private static HttpMiniServlet poolServlet;

    public static PoolWSConnection poolWSConnections;

    private static RoutingTable routingTable;

    /**
     * Create a new <code>DispatcherMSG</code>.
     */
    private DispatcherMSG() {
        // Launch HTTP Server
        int port = Integer.parseInt(CONFIGURATION.getProperty("server.port"));
        this.httpServer = new HttpMiniServer(port);

        // Launch Routing Table
        routingTable = new RoutingTableImpl();

        // Launch Servlets Pool
        poolServlet = new PoolClientConnection(routingTable);
        this.httpServer.useServlet(poolServlet);

        // Launch WS Connection Pool
        poolWSConnections = new PoolWSConnection(routingTable);
    }

    /**
     * <p>
     * Start the MSG Dispatcher.
     * </p>
     * <p>
     * To configure it, use the default.properties file.
     * </p>
     * 
     * @param args
     *            no arguments used.
     */
    public static void main(String[] args) {
        // Load Configuration
        DispatcherMSG.loadConfiguration();

        // DispatcherRPC Creation
        DispatcherMSG gateway = new DispatcherMSG();

        // Start DispatcherRPC with the mini server
        gateway.start();
    }

    /**
     * Start the HTTP Server;
     */
    private void start() {
        // Start HTTP Server
        this.httpServer.startServer();
    }

    /**
     * Load the configuration file of the MSG Dispatcher.
     */
    private static void loadConfiguration() {
        logger.finest("Loading default configuration");

        // create and load default properties
        Properties defaultProps = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(DEFAULT_CONF_FILE_PATH);
            defaultProps.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            logger.finest("Couldn't found the default configuration file", e);
        } catch (IOException e) {
            logger.finest("Couldn't read the default configuration file", e);
        }

        // create program properties with default
        CONFIGURATION = new Properties(defaultProps);

        logger.finest("Configuration loaded");
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