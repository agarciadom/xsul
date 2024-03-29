/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: ServletClientConnection.java,v 1.2 2004/11/30 16:40:25 aslom Exp $
 */
package xsul.dispatcher.rpc.clientconnection;

import xsul.MLogger;
import xsul.dispatcher.routingtable.RoutingTable;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;

/**
 * This servlet serves request from client. The service is to forward the request to
 * the good Web Service.
 *
 * @author Alexandre di Costanzo
 *
 */
public class ServletClientConnection extends HttpMiniServlet {
    private final static MLogger logger = MLogger.getLogger();

    protected static RoutingTable routingTable;


    /**
     * Create a new <code>ServletClientConnection</code> with a specified Routing
     * Table.
     *
     * @param routingTable the Routing Table which this servlet will use to match
     * virtual paths with Web Services addresses
     */
    public ServletClientConnection(RoutingTable routingTable) {
        super();
        ServletClientConnection.routingTable = routingTable;
    }

    /**
     * Create a <code>ClientConnection</code> to serve this method.
     *
     * @see xsul.dispatcher.rpc.clientconnection.ClientConnection
     * @see xsul.http_server.HttpMiniServlet#service(xsul.http_server.HttpServerRequest, xsul.http_server.HttpServerResponse)
     */
    public void service(HttpServerRequest req, HttpServerResponse resp)
            throws HttpServerException {
        ClientConnection client = new ClientConnection();
        client.service(req, resp);
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

