/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: ClientConnection.java,v 1.5 2005/03/19 18:06:43 lifang Exp $
 */
package xsul.dispatcher.rpc.clientconnection;

import xsul.MLogger;
import xsul.dispatcher.http_util.SendError;
import xsul.dispatcher.routingtable.WS;
import xsul.dispatcher.rpc.wsconnection.RPCWSConnection;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;

/**
 * Looking in the Routing Table for find the real Web Service which match the
 * virtual path from the client's request.
 *
 * @author Alexandre di Costanzo
 *
 */
class ClientConnection {
    private final static MLogger logger = MLogger.getLogger();

    /**
     * Create a new <code>ClientConnection</code>.
     *
     */
    public ClientConnection() {
        // nothing to do
    }

    /**
     * Looking in the Routing Table with the path specified in the request for
     * the real Web Service. Give the request and the response to the
     * <code>RPCWSConnection</code> associated to the Web Service for
     * forwarding.
     *
     * @param req
     *            the request from the client.
     * @param resp
     *            the response for the client.
     *
     * @see RPCWSConnection
     * @see xsul.http_server.HttpMiniServlet#service(xsul.http_server.HttpServerRequest,
     *      xsul.http_server.HttpServerResponse)
     */
    public void service(HttpServerRequest req, HttpServerResponse resp)
            throws HttpServerException {
        String path = req.getPath();

        if (path == null) {
            SendError
                    .send(resp, "404", "Web Services not found on this server");
            logger.warning("The asked path is null");
        } else {
            // Forwards the request to the good WSForwarder

            // Analysing path
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            String method = req.getMethod();
            String arguments = null;
            if (method.equals("GET")) {
                // Remove arguments from path
                int argStart = path.indexOf('?');
                if (argStart != -1) {
                    // There are some arguments
                    arguments = path.substring(argStart, path.length());
                    path = path.replaceAll("\\?.*", "");
                }
            }

            // Looking for the good WS
            WS wsHttp = (WS)ServletClientConnection.routingTable.get(path);

            if (wsHttp == null) {
                SendError.send(resp, "404",
                        "Web Services not found on this server");
                logger.warning("The Web Services " + path + " was not found");
            } else {
                // Forwards request to the Web service
                RPCWSConnection connection = new RPCWSConnection(wsHttp);
                connection.forwards(new Object[] { req, resp, arguments });
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
