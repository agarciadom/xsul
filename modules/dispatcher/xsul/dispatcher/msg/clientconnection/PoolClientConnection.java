/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: PoolClientConnection.java,v 1.4 2004/11/30 16:40:25 aslom Exp $
 */
package xsul.dispatcher.msg.clientconnection;

import java.io.IOException;
import java.io.OutputStream;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dispatcher.http_util.SendError;
import xsul.dispatcher.msg.DispatcherMSG;
import xsul.dispatcher.msg.postmail.MailBox;
import xsul.dispatcher.msg.postmail.MailBoxImpl;
import xsul.dispatcher.routingtable.RoutingTable;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * Managing threads pool to serve clients requests and responses from WSs.
 *
 * @author Alexandre di Costanzo
 *
 */
public class PoolClientConnection extends HttpMiniServlet {
    private final static MLogger logger = MLogger.getLogger();

    private static PooledExecutor pool;

    protected static RoutingTable routingTable;

    protected static MailBox mailBox;

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    private final static SoapUtil[] soapFragrances = new SoapUtil[] {
            Soap11Util.getInstance(), Soap12Util.getInstance() };

    /**
     * Create and initialize a pool of threads to serve clients requests.
     *
     * @param routingTable
     *            the Routing Table used to find WS.
     */
    public PoolClientConnection(RoutingTable routingTable) {
        super();
        PoolClientConnection.routingTable = routingTable;
        PoolClientConnection.mailBox = new MailBoxImpl();

        // Pool creation and initialisation
        // Max threads
        int sizeMax = Integer.parseInt(DispatcherMSG.CONFIGURATION
                .getProperty("clients.max"));
        pool = new PooledExecutor(sizeMax);

        // Min threads
        int sizeMin = Integer.parseInt(DispatcherMSG.CONFIGURATION
                .getProperty("clients.min"));
        pool.setMinimumPoolSize(sizeMin);

        // Keep alive time
        long keepAliveTime = Long.parseLong(DispatcherMSG.CONFIGURATION
                .getProperty("clients.keepAliveTime"));
        pool.setKeepAliveTime(keepAliveTime);

        // Pre-start threads
        int preCreateThread = Integer.parseInt(DispatcherMSG.CONFIGURATION
                .getProperty("clients.preCreateThreads"));
        pool.createThreads(preCreateThread);

        // Set the policy for blocked execution to be to wait until a thread is
        // available
        pool.waitWhenBlocked();
    }

    /**
     * Check, parse, verify the <code>req</code>. Attribute a client thread
     * to serve the client.
     *
     * @see xsul.http_server.HttpMiniServlet#service(xsul.http_server.HttpServerRequest,
     *      xsul.http_server.HttpServerResponse)
     */
    public void service(HttpServerRequest req, HttpServerResponse resp)
            throws HttpServerException {
        // Thread allocation for the new client
        try {
            XmlElement el = builder.parseFragmentFromInputStream(req
                    .getInputStream());
            // FIXME XmlElement el =
            // builder.parseFragmentFromInputStream(req.getInputStream(),
            // req.getCharset());
            SoapUtil soapUtil = SoapUtil
                    .selectSoapFragrance(el, soapFragrances);
            String path = req.getPath();
            //          Path Treatment to get arguments
            String arguments = null;
            if (path != null) {
                // Analysiing path
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                String method = req.getMethod();
                if (method.equals("GET")) {
                    // Remove arguments from path
                    int argStart = path.indexOf('?');
                    if (argStart != -1) {
                        // There are some arguments
                        arguments = path.substring(argStart, path.length());
                        path = path.replaceAll("\\?.*", "");
                    }
                }
            }
            ClientConnection client = new ClientConnection(path, arguments, el,
                    soapUtil);
            pool.execute(client);  // NOTE: pool will now "execute" client (the sentence may be delayed ...)
            // Successful HTTP message transmission
            // Send Ok to the client
            this.sendHttpOk(resp);
        } catch (Exception e) {
            SendError.send(resp, "500", "Thread Interrupted Exception");
            logger.warning("Couldn't allocate a thread to the client", e);
        }
    }

    /**
     * Send a HTTP 202 status code.
     *
     * @param resp
     *            response from client.
     */
    private void sendHttpOk(HttpServerResponse resp) {
        try {
            resp.setReasonPhrase("OK");
            resp.setStatusCode("202");
            OutputStream outResp = resp.getOutputStream();
            outResp.close();
            logger.finest("Sended 202 response to the client");
        } catch (IOException e) {
            logger.warning(
                    "Couldn't send confirmation respsonse to the client", e);
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
