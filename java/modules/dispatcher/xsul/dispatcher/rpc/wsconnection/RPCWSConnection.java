/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: RPCWSConnection.java,v 1.3 2004/09/17 19:56:54 adicosta Exp $
 */
package xsul.dispatcher.rpc.wsconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import xsul.MLogger;
import xsul.dispatcher.http_util.SendError;
import xsul.dispatcher.routingtable.WS;
import xsul.dispatcher.routingtable.WSConnection;
import xsul.dispatcher.rpc.DispatcherRPC;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientRequest;
import xsul.http_client.HttpClientResponse;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;

/**
 * @author Alexandre di Costanzo
 *  
 */
public class RPCWSConnection extends WSConnection{
    private final static MLogger logger = MLogger.getLogger();

    private HttpClientRequest fwdRequest;

    private HttpClientConnectionManager cx = null;

    private WS wsHttp;

    /**
     * Creates a new <code>RPCWSConnection</code> with a new connection manager on the
     * specified Web Service.
     * 
     * @param wsHttp the specified Web Service.
     * 
     * @see WS
     */
    public RPCWSConnection(WS wsHttp) {
        this.wsHttp = wsHttp;

        // Connection creation of reuse
        if (cx == null) {
            cx = HttpClientReuseLastConnectionManager.newInstance();
        }
    }
    
    /**
     * <p>Forwards a request from a client to the Web Service and the response.</p>
     * <p>Structure of the parameter <code>request</code>
     * <ul>
     * 	<li><code>request[0]</code>: the HttpServerRequest from client</li>
     * 	<li><code>request[1]</code>: the HttpServerResponse from client</li>
     * 	<li><code>request[2]</code>: the arguments from client's http request</li>
     * </ul>
     * 
     * @param request the request, response and arguments.
     */
    public void forwards(Object[] request) {
        this.fwdRequest = cx.connect(this.wsHttp.getHost(), this.wsHttp
                .getPort(), Integer.parseInt(DispatcherRPC.CONFIGURATION
                .getProperty("webservices.timeout")));

        HttpServerRequest req = (HttpServerRequest) request[0];
        HttpServerResponse resp = (HttpServerResponse) request[1];
        String arguments = (String) request[2];

        //Forwarding request from queue
        String wsPath = this.wsHttp.getPath();

        // Arguments to add?
        if (arguments != null) {
            wsPath += arguments;
        }
        this.fwdRequest.setRequestLine(req.getMethod(), wsPath, "HTTP/1.0");
        this.fwdRequest.setContentType(req.getContentType());
        HttpClientResponse wsResp = this.fwdRequest.sendHeaders();

        //      Copy contents form req to fwd
        try {
            InputStream inReq = req.getInputStream();
            OutputStream outToWs = fwdRequest.getBodyOutputStream();
            this.copy(inReq, outToWs);
        } catch (IOException e) {
                SendError.send(resp, "500",
                        "Couldn't communicate with the Web service");
                logger.warning("Couldn't communicate with the Web service on: "
                        + wsHttp);
                return;
        }

        //      Send response to the client
        wsResp.readStatusLine();
        wsResp.readHeaders();
        resp.setContentType(wsResp.getContentType());
        resp.setReasonPhrase(wsResp.getReasonPhrase());
        resp.setStatusCode(wsResp.getStatusCode());
        OutputStream outResp = resp.getOutputStream();
        InputStream inResp = wsResp.getBodyInputStream();
        try {
            this.copy(inResp, outResp);
        } catch (IOException e) {
            logger.warning("Problem with the response", e);
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