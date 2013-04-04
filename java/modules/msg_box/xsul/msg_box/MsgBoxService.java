/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: MsgBoxService.java,v 1.2 2005/06/16 03:17:11 aslom Exp $
 */
package xsul.msg_box;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import xsul.MLogger;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.HttpMiniServlet;
import xsul.msg_box.storage.MsgBoxStorage;

/**
 * Starts message box service.
 *
 */
public class MsgBoxService {
    private final static MLogger logger = MLogger.getLogger();

    private final static String DEFAULT_CONF_FILE_PATH = MsgBoxService.class
            .getResource("/xsul/msg_box/default.properties").getPath();
    private final static String USER_CONF_FILE_PATH = MsgBoxService.class
            .getResource("/xsul/msg_box/user.properties").getPath();

    private static Properties CONFIGURATION;

    private HttpMiniServer httpServer;

    private static HttpMiniServlet msgBoxServlet;

    private static MsgBoxStorage msgBoxStorage ;


    /**
     *
     */
    private MsgBoxService() {
        // Launch HTTP Server
        int port = Integer.parseInt(CONFIGURATION.getProperty("server.port"));
        this.httpServer = new HttpMiniServer(port);

        // select storage service
        msgBoxStorage  = null; //new InMemoryMsgBoxStorage();

        // Launch Servlets Pool
        msgBoxServlet = null; //new ServletClientConnection(routingTable);
        this.httpServer.useServlet(msgBoxServlet);
    }

    /**
     * <p>Start the MsgBox service.</p>
     * <p>To configure it, use the default.properties file.</p>
     * @param args no arguments used.
     */
    public static void main(String[] args) {
        // Load Configuration
        MsgBoxService.loadConfiguration();

        // DispatcherRPC Creation
        MsgBoxService boxService = new MsgBoxService();

        // Start DispatcherRPC with the mini server
        boxService.start();
    }

    /**
     * Start the Http Server.
     */
    private void start() {
        // Start HTTP Server
        this.httpServer.startServer();
    }

    /**
     *  Load the configuration file of the RPC Dispacher.
     */
    private static void loadConfiguration() {
        logger.finest("Loading default configuration");

        // create and load default properties
        Properties defaultProps = new Properties();
        loadProperties(defaultProps, DEFAULT_CONF_FILE_PATH);

        // create program properties with default
        CONFIGURATION = new Properties(defaultProps);

        // allow user to override them
        loadProperties(CONFIGURATION, USER_CONF_FILE_PATH);

        logger.finest("Configuration loaded");
    }

    private static void loadProperties(Properties defaultProps, String path) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(path);
            defaultProps.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            logger.finest("Couldn't found the configuration "+path, e);
        } catch (IOException e) {
            logger.finest("Couldn't found the configuration "+path, e);
        }
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1) All redistributions of source code must retain the above
 *    copyright notice, the list of authors in the original source
 *    code, this list of conditions and the disclaimer listed in this
 *    license;
 *
 * 2) All redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the disclaimer
 *    listed in this license in the documentation and/or other
 *    materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include
 *    the following acknowledgement:
 *
 *      "This product includes software developed by the Indiana
 *      University Extreme! Lab.  For further information please visit
 *      http://www.extreme.indiana.edu/"
 *
 *    Alternatively, this acknowledgment may appear in the software
 *    itself, and wherever such third-party acknowledgments normally
 *    appear.
 *
 * 4) The name "Indiana University" or "Indiana University
 *    Extreme! Lab" shall not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission from Indiana University.  For written permission,
 *    please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana
 *    University" name nor may "Indiana University" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code
 * provided does not infringe the patent or any other intellectual
 * property rights of any other entity.  Indiana University disclaims any
 * liability to any recipient for claims brought by any other entity
 * based on infringement of intellectual property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH
 * NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA
 * UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT
 * SOFTWARE IS FREE OF INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR
 * OTHER PROPRIETARY RIGHTS.  INDIANA UNIVERSITY MAKES NO WARRANTIES THAT
 * SOFTWARE IS FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP
 * DOORS", "WORMS", OR OTHER HARMFUL CODE.  LICENSEE ASSUMES THE ENTIRE
 * RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS,
 * AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION GENERATED USING
 * SOFTWARE.
 */
