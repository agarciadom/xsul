/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: MSGWSConnection.java,v 1.2 2004/09/23 13:31:59 adicosta Exp $
 */
package xsul.dispatcher.msg.wsconnection;

import java.io.IOException;
import java.io.OutputStream;

import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dispatcher.msg.DispatcherMSG;
import xsul.dispatcher.routingtable.WS;
import xsul.dispatcher.routingtable.WSConnection;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientRequest;
import xsul.http_client.HttpClientResponse;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.soap.SoapUtil;
import EDU.oswego.cs.dl.util.concurrent.BoundedLinkedQueue;

/**
 * <p>
 * Working like a FIFO queue to forward messages from clients to one WS.
 * </p>
 * 
 * @author Alexandre di Costanzo
 *  
 */
public class MSGWSConnection extends WSConnection implements Runnable {
    private final static MLogger logger = MLogger.getLogger();

    private BoundedLinkedQueue queue = null;

    private HttpClientRequest fwdRequest;

    private HttpClientConnectionManager cx = null;

    private WS wsHttp;

    private boolean keepAliveFlag = false;

    private static String KEEP_ALIVE = DispatcherMSG.CONFIGURATION
            .getProperty("webservices.keepAliveTime");

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    /**
     * Create a new connection with the specified WS by <code>wsHttp</code>;
     * 
     * @param wsHttp
     *            information about the WS.
     */
    public MSGWSConnection(WS wsHttp) {
        this.wsHttp = wsHttp;
        int bounded = Integer.parseInt(DispatcherMSG.CONFIGURATION
                .getProperty("webservices.queue"));
        this.queue = new BoundedLinkedQueue(bounded);

        // Connection creation of reuse
        cx = HttpClientReuseLastConnectionManager.newInstance();
    }

    /**
     * Forward message from the queue during all the time where the connection
     * is alive.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // Connection
        this.fwdRequest = cx.connect(this.wsHttp.getHost(), this.wsHttp
                .getPort(), Integer.parseInt(DispatcherMSG.CONFIGURATION
                .getProperty("webservices.timeout")));
        // Keep alive specification
        long endKeepAlive = System.currentTimeMillis()
                + Long.parseLong(KEEP_ALIVE);
        this.fwdRequest.setHeader("Keep-Alive", KEEP_ALIVE);
        this.fwdRequest.setConnection("keep-alive");
        this.keepAliveFlag = true;

        // While the connection is open
        while (System.currentTimeMillis() < endKeepAlive) {
            // I'm serving request in queue
            while (!this.queue.isEmpty()) {
                // Service is to forward the request to the WS
                try {
                    Object[] request = (Object[]) this.queue.take();
                    if (request != null) {
                        logger.finest("Get request from queue");
                        this.forwards(request);
                    }
                } catch (InterruptedException e) {
                    logger.warning("Couldn't get request from queue", e);
                    this.keepAliveFlag = false;
                }
            }
            try {
                // TODO Change it to improve performances
                synchronized (this) {
                    this.wait(100);
                }
            } catch (InterruptedException e) {
                logger.finest("Couldn't wait", e);
            }
        }
        // Close connection
        this.cx.shutdownAndReclaimResources();
        this.keepAliveFlag = false;
    }

    /**
     * 
     * @see xsul.dispatcher.routingtable.WSConnection#forwards(java.lang.Object[])
     */
    public void forwards(Object[] request) {
        logger.entering(request);
        // Get elements from request
        XmlElement message = (XmlElement) request[0];
        String arguments = (String) request[1];
        SoapUtil soapUtil = (SoapUtil) request[2];

        //   XmlDocument responseDoc = soapUtil.wrapBodyContent(message);
        XmlContainer top = message.getRoot();
        XmlDocument responseDoc;
        if (top instanceof XmlDocument) {
            responseDoc = (XmlDocument) top;
        } else {
            responseDoc = builder.newDocument();
            responseDoc.setDocumentElement(message);
        }

        //Forwarding request from queue
        String wsPath = this.wsHttp.getPath();

        // Arguments to add?
        if (arguments != null) {
            wsPath += arguments;
        }

        this.fwdRequest = cx.connect(this.wsHttp.getHost(), this.wsHttp
                .getPort(), Integer.parseInt(DispatcherMSG.CONFIGURATION
                .getProperty("webservices.timeout")));
        this.fwdRequest.setRequestLine("GET", wsPath, "HTTP/1.0");
        this.fwdRequest.setContentType("text/xml; charset='UTF-8'");

        // TODO What do with the response
        HttpClientResponse wsResp = this.fwdRequest.sendHeaders();

        OutputStream out = this.fwdRequest.getBodyOutputStream();
        // Copy soap message
        builder.serializeToOutputStream(responseDoc, out);
        try {
            // Message sending
            out.close();
        } catch (IOException e1) {
            logger.finest("Sending error problem", e1);
        }
        logger.exiting();
    }

    /**
     * Check if the conenction beetween the forwarder and the Web Sevice is keep
     * alive.
     * 
     * @return <code>true</code> if the connection is keep alive,
     *         <code>false</code> else.
     */
    public boolean isKeepAlive() {
        return this.keepAliveFlag;
    }

    /**
     * Put <code>outMessage</code> and <code>arguments</code> in the FIFO
     * queue to forward the message to the Web Service.
     * 
     * @param outMessage
     *            a valid WS-Addressing message.
     * @param arguments
     *            from the HTTP request.
     * @throws InterruptedException
     *             if problems occur.
     */
    public void put(XmlElement outMessage, String arguments, SoapUtil soapUtil)
            throws InterruptedException {
        this.queue.put(new Object[] { outMessage, arguments, soapUtil });
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