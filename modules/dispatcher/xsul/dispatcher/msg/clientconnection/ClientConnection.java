/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: ClientConnection.java,v 1.8 2005/03/19 18:06:43 lifang Exp $
 */
package xsul.dispatcher.msg.clientconnection;

import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dispatcher.http_util.SendError;
import xsul.dispatcher.msg.DispatcherMSG;
import xsul.dispatcher.msg.wsconnection.MSGWSConnection;
import xsul.dispatcher.routingtable.WS;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientRequest;
import xsul.http_client.HttpClientResponse;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.soap.SoapUtil;
import xsul.ws_addressing.WsaEndpointReference;
import xsul.ws_addressing.WsaMessageInformationHeaders;

/**
 * <p>Thread assigned to a client.</p>
 * <p>Algorithm:</p>
 * <ul>
 *  <li>if it is client message</li>
 *  <ul>
 *      <li>Find the real WS designed by the HTTP path or the WS-Addressing element <i>To</i></li>
 *      <li>Forward the message</li>
 *  </ul>
 *  <li>else if it is a WS response</li>
 *  <ul>
 *      <li>Try to contact the client with the WS-Addressing element <i>To</i> from
 *      the original message</li>
 *      <li>Else keep the WS message response in the PO Mail Box Service</li>
 *  </ul>
 *  <li>else</li>
 *  <ul>
 *      <li>nothing</li>
 *  </ul>
 * </ul>
 * @author Alexandre di Costanzo
 *
 */
class ClientConnection implements Runnable {
    private final static MLogger logger = MLogger.getLogger();

    private final static int TIMEOUT = Integer
            .parseInt(DispatcherMSG.CONFIGURATION
                    .getProperty("webservices.timeout"));

    private static String DEFAULT_REPLY_TO = null;

    private static WsaEndpointReference DEFAULT_FAULT_TO = null;

    private WsaEndpointReference WSA_FROM = null;

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    private final HttpClientConnectionManager cx = HttpClientReuseLastConnectionManager
            .newInstance();;

    private SoapUtil soapUtil = null;

    private String path;

    private XmlElement el;

    private String arguments;

    static {
        try {
            DEFAULT_REPLY_TO = DispatcherMSG.CONFIGURATION
                    .getProperty("wsa.replyTo");
            DEFAULT_FAULT_TO = new WsaEndpointReference(new URI(
                    DispatcherMSG.CONFIGURATION.getProperty("wsa.faultTo")));
        } catch (URISyntaxException e) {
            logger.warning("Couldn't initialise defaults values");
        }
    }

    /**
     * Create a new Client Connection.
     *
     * @param path the path from the HTTP req.
     * @param arguments arguments from the HTTP req.
     * @param el WS-Addressing message from the HTTP req.
     * @param soapUtil SOAP version from the message.
     */
    public ClientConnection(String path, String arguments, XmlElement el,
            SoapUtil soapUtil) {
        try {
            WSA_FROM = new WsaEndpointReference(new URI(
                    DispatcherMSG.CONFIGURATION.getProperty("wsa.from")));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.path = path;
        this.arguments = arguments;
        this.el = el;
        this.soapUtil = soapUtil;
    }

    /**
     * Check if the message is from a client or it is WS response. Create logical path
     * to identify response from WS and modify WS-Addressing elements from the original
     * message before forwarding.
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        logger.finest("Start service for client");

        // Response for a client in mailbox?
        if (PoolClientConnection.mailBox.isWaitingResponse(path)) {
            this.putResponseInMailBoxService(path, el);
            return;
        }

        // Looking for WS with the path
        WS wsHttp = (WS)PoolClientConnection.routingTable.get(path);
        logger.finest("With path: " + path + " we found the WS at address: "
                + wsHttp);

        // WSA Informations
        WsaMessageInformationHeaders mih = null;
        URI fault = null;
        URI toKey = null;

        // WSA
        XmlElement headers = (XmlElement) el.requiredElementContent()
                .iterator().next();
        mih = new WsaMessageInformationHeaders(headers);

        // Get URI for fault message
        if (mih.getFaultTo() != null) {
            fault = mih.getFaultTo().getAddress();
        } else if (mih.getReplyTo() != null) {
            fault = mih.getReplyTo().getAddress();
        }

        URI messageID = mih.getMessageId();
        if (wsHttp == null) {
            // Looking for the good WS with WSA to
            // No use of the path. Keep only path arguments
            toKey = mih.getTo();
            wsHttp = (WS)PoolClientConnection.routingTable.get(toKey);
            logger.finest("With ws-a to element: " + toKey
                    + " we found the WS at address: " + wsHttp);
        }

        if (wsHttp == null) {
            // No WS found
            SendError.sendWSAFault("Web Services not found on this server",
                    null, this.soapUtil, fault);
            logger.warning("The Web Services was not found");
        }

        // Get the connection thread with the WS
        MSGWSConnection connection = wsHttp.getMsgConnection();

        XmlElement outMessage = null;
        try {
            outMessage = (XmlElement) el.clone();
        } catch (CloneNotSupportedException e) {
            SendError.sendWSAFault("Couldn't clone the message", null,
                    soapUtil, fault);
            logger.warning("Couldn't clone the message", e);
            return;
        }

        // Generate a logical path
        String logicalPath = "dispatcher" + System.currentTimeMillis()
                + wsHttp.getHost();

        // WS-Addressing Forwarding
        // Modification of WS-Addressing Message
        WsaMessageInformationHeaders outHeaders = new WsaMessageInformationHeaders(
                outMessage);
        outHeaders.setTo(wsHttp.getWsaElementTo());
        outHeaders.setFrom(WSA_FROM);
        if (wsHttp.getWsaElementReplyTo() != null) {
            outHeaders.setReplyTo(wsHttp.getWsaElementReplyTo());
        } else {
            String replyTo = DEFAULT_REPLY_TO + "";
            if (replyTo.endsWith("/")) {
                replyTo += logicalPath;
            } else {
                replyTo += "/" + logicalPath;
            }
            try {
                outHeaders
                        .setReplyTo(new WsaEndpointReference(new URI(replyTo)));
            } catch (URISyntaxException e) {
                logger.warning("Couldn't create the element replyTo", e);
            }
        }
        if (wsHttp.getWsaElementFaultTo() != null) {
            outHeaders.setFaultTo(wsHttp.getWsaElementFaultTo());
        } else {
            outHeaders.setFaultTo(DEFAULT_FAULT_TO);
        }

        // Keep Information for the response to the Client
        if (mih.getReplyTo() != null) {
            PoolClientConnection.mailBox.putForWaitingResponse(logicalPath, mih
                .getReplyTo().getAddress());
        }

        try {
            // Put the request in the FIFO queue of the WS
            // Connection thread
            connection.put(outMessage, arguments, soapUtil);
        } catch (InterruptedException e) {
            SendError.sendWSAFault("Couldn't forward the message", null,
                    soapUtil, fault);
            logger.warning("Couldn't put the message in the fowarder queue", e);
            return;
        }

        logger.finest("Finish service client");
    }

    /**
     * Before to put the message in the PO Mail Box try to send it to the client.
     *
     * @param logicalPath generated path before forwarding message.
     * @param el the response message from the WS.
     */
    private void putResponseInMailBoxService(String logicalPath, XmlElement el) {
        // It's a response from a WS for a client, put in the mailbox
        logger.finest("Response get from service:" + logicalPath);
        URI replyTo = PoolClientConnection.mailBox.getReplyToOf(logicalPath);
        if (replyTo == null) {
            logger.finest("No replyTo found for the message");
            PoolClientConnection.mailBox.putResponse(logicalPath, el);
        } else {
            logger.finest("Trying to send reponse from WS to " + replyTo);
            try {
                HttpClientRequest fwdRequest = cx.connect(replyTo.getHost(),
                        replyTo.getPort(), 240000);
                XmlContainer top = el.getRoot();
                XmlDocument responseDoc;
                if (top instanceof XmlDocument) {
                    responseDoc = (XmlDocument) top;
                } else {
                    responseDoc = builder.newDocument();
                    responseDoc.setDocumentElement(el);
                }

                String path = replyTo.getPath();
                if (path == null || path.length() == 0)
                    path = "/";
                fwdRequest.setRequestLine("POST", path, "HTTP/1.0");
                fwdRequest.setContentType("text/xml; charset='UTF-8'");

                // TODO What do with the response
                HttpClientResponse wsResp = fwdRequest.sendHeaders();

                OutputStream out = fwdRequest.getBodyOutputStream();
                // Copy soap message
                builder.serializeToOutputStream(responseDoc, out);
                // Message sending
                out.close();
                logger.finest("Message was put in msg_box " + replyTo);
            } catch (Exception e) {
                PoolClientConnection.mailBox.putResponse(logicalPath, el);
                logger.finest("The message was put in PO mailbox", e);
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
