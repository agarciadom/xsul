/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Dispatcher.java,v 1.1 2004/12/04 07:52:41 aslom Exp $
 */
package xsul.dispatcher;

import java.net.URI;
import xsul.MLogger;
import xsul.dispatcher.msg.clientconnection.PoolClientConnection;
import xsul.dispatcher.msg.wsconnection.PoolWSConnection;
import xsul.dispatcher.routingtable.RoutingTable;
import xsul.dispatcher.routingtable.RoutingTableImpl;
import xsul.dispatcher.rpc.clientconnection.ServletClientConnection;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;
import xsul.msg_box.servlet.MsgBoxServlet;
import xsul.msg_box.storage.MsgBoxStorage;
import xsul.msg_box.storage.memory.InMemoryStorageImpl;

/**
 * THis class will start RPC and MSG dispatchers and MsgBox service as well
 * All services are started on the same port but on different paths.
 *
 */
public class Dispatcher extends HttpMiniServlet {
    private final static MLogger logger = MLogger.getLogger();
    private HttpMiniServer httpServer;
    
    //private static ;
    
    /**
     * <p>Start the RPC Dispacher.</p>
     * <p>To configure it, use the default.properties file.</p>
     * @param args no arguments used.
     */
    public static void main(String[] args) {
        
        // DispatcherRPC Creation
        int port = 0;
        Dispatcher gateway = new Dispatcher(port);
        
    }
    
    private HttpMiniServlet rpcServlet;
    private HttpMiniServlet msgServlet;
    private MsgBoxServlet msgBoxServlet;
    public Dispatcher(int port) {
        //this.httpServer.useServlet(servlet);
        this.httpServer = new HttpMiniServer(port);
        this.httpServer.useServlet(this);
        
        URI location;
        
        // shared? routing table
        RoutingTable routingTable = new RoutingTableImpl();
        
        //RPC
        rpcServlet = new ServletClientConnection(routingTable);
        
        //MSG
        msgServlet = new PoolClientConnection(routingTable);
        PoolWSConnection poolWSConnections = new PoolWSConnection(routingTable);
        
        //MsgBox
        MsgBoxStorage msgBoxStorage  = new InMemoryStorageImpl();
        msgBoxServlet = new MsgBoxServlet(msgBoxStorage); //new ServletClientConnection(routingTable);
        String loc = this.httpServer.getLocation() + "/MsgBox";
        location = URI.create(loc);
        msgBoxServlet.setLocation(location);
        
        this.httpServer.startServer();
        
    }
    
    public void service(HttpServerRequest req, HttpServerResponse resp) throws HttpServerException {
        String path = req.getPath();
        if(path.startsWith("/MsgBox")) {
            msgBoxServlet.service(req,resp);
        } else  if(path.startsWith("/dispatch/msg")) {
            msgServlet.service(req,resp);
        } else  if(path.startsWith("/dispatch/rpc")) {
            rpcServlet.service(req,resp);
        } else {
            // ignore if unknonw path or show some welcome string as HTML ???
            
            //EVEB better: show list of hosted services with links to WSDL files
            //i.e, it is like a mini UDDI registry :)
                
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
