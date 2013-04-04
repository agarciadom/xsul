/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: MsgBoxServer.java,v 1.6 2006/07/20 15:54:27 cherath Exp $
 */
package xsul.msg_box;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import xsul.MLogger;
import xsul.http_server.HttpMiniServer;
import xsul.msg_box.servlet.MsgBoxServlet;
import xsul.msg_box.storage.MsgBoxStorage;
import xsul.msg_box.storage.db.DatabaseStorageImpl;
import xsul.msg_box.storage.memory.InMemoryStorageImpl;

/**
 * Starts message box service.
 *
 */
public class MsgBoxServer {
    private final static MLogger logger = MLogger.getLogger();
    
    private final static String DEFAULT_CONF_FILE_PATH;
    private final static String USER_CONF_FILE_PATH;
    private static Properties CONFIGURATION;
    
    private HttpMiniServer httpServer;
    private URI location;
    private MsgBoxServlet msgBoxServlet;
    private MsgBoxStorage msgBoxStorage ;
    
    static {
        {
            final String DPNAME = "/xsul/msg_box/default.properties";
            URL dp = MsgBoxServer.class.getResource(DPNAME);
            if(dp != null) {
                DEFAULT_CONF_FILE_PATH = dp.getPath();
            } else {
                DEFAULT_CONF_FILE_PATH = "missing resource "+DPNAME;
                logger.config("could not find "+DPNAME);
            }
        }

        {
            final String UPNAME = "/xsul/msg_box/user.properties";
            URL dp = MsgBoxServer.class.getResource(UPNAME);
            if(dp != null) {
                USER_CONF_FILE_PATH = dp.getPath();
            } else {
                USER_CONF_FILE_PATH = "missing resource "+UPNAME;
                logger.config("could not find "+UPNAME);
            }
        }
    }

    public MsgBoxServer(int port) {
        this.httpServer = new HttpMiniServer(port);
    }
    
    public MsgBoxServer() {
        // Launch HTTP Server
        MsgBoxServer.loadConfiguration();
        int port = Integer.parseInt(CONFIGURATION.getProperty("server.port", "0"));
        this.httpServer = new HttpMiniServer(port);
    }
    
    public static void main(String[] args) {
        // Load Configuration
        MsgBoxServer msgBoxServer;
        if(args.length > 0) {
            int port = Integer.parseInt(args[0]);
            msgBoxServer= new MsgBoxServer(port);
        } else {
         msgBoxServer= new MsgBoxServer();
        }
        msgBoxServer.start();
        System.out.println(msgBoxServer.getClass().getName()+" started on "+msgBoxServer.getLocation());
    }
    
    public void start() {
        // select storage service
        msgBoxStorage  = new DatabaseStorageImpl();
        
        // Launch Servlets Pool
        msgBoxServlet = new MsgBoxServlet(msgBoxStorage); //new ServletClientConnection(routingTable);
        this.httpServer.useServlet(msgBoxServlet);

        // Start HTTP Server
        this.httpServer.startServer();
        String loc = this.httpServer.getLocation() + "/MsgBox";
        location = URI.create(loc);
        msgBoxServlet.setLocation(location);
    }

    public void shutdown() {
        this.httpServer.shutdownServer();
    }
    
    public void stop() {
        this.httpServer.stopServer();
    }
    
    
    public URI getLocation() {
        return location;
    }
    
    /**
     *  Load the configuration resource.
     */
    private static void loadConfiguration() {
        logger.finest("Loading default configuration");
        CONFIGURATION = new Properties();
        
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



