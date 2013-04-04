/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SendError.java,v 1.2 2004/09/17 19:56:54 adicosta Exp $
 */
package xsul.dispatcher.http_util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.MLogger;
import xsul.XmlConstants;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientRequest;
import xsul.http_client.HttpClientResponse;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.http_server.HttpServerResponse;
import xsul.soap.SoapUtil;
import xsul.ws_addressing.WsaMessageInformationHeaders;

/**
 * An util class to send http errors responses.
 *
 * @author Alexandre di Costanzo
 *
 */
public class SendError {
    private final static MLogger logger = MLogger.getLogger();

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    private final static HttpClientConnectionManager cx = HttpClientReuseLastConnectionManager
            .newInstance();

    /**
     * Send a message to <code>resp</code>.
     *
     * @param resp
     *            the response object.
     * @param statusCode
     *            the http status code for the response.
     * @param reasonPhrase
     *            the http reason phrase for the response.
     */
    public static void send(HttpServerResponse resp, String statusCode,
            String reasonPhrase) {
        try {
            resp.setStatusCode(statusCode);
            resp.setReasonPhrase(reasonPhrase);
            OutputStream out = resp.getOutputStream();
            out.close();
            logger.fine("Error message sended to " + resp
                    + " with status code " + statusCode);
        } catch (IOException e) {
            logger.warning("Couldn't send error message", e);
        }
    }

    /**
     * Send a WSAddressing fault message to <code>dest</code>.
     *
     * @param reason the English language reason element.
     * @param e the Exception for the detail element.
     * @param soapUtil to use the good verison of SOAP.
     * @param dest the URI of the destination of the message.
     */
    public static void sendWSAFault(String reason, Exception e,
            SoapUtil soapUtil, URI dest) {
        if (dest == null){
            return;
        }
        // Creation of the soap message: responseDoc
        XmlElement faultMsg = soapUtil.generateSoapServerFault(reason, e);
        XmlDocument responseDoc = soapUtil.wrapBodyContent(faultMsg);
        WsaMessageInformationHeaders responseWsaHeaders = new WsaMessageInformationHeaders(
                responseDoc);
        responseWsaHeaders.setTo(dest);

        // Open connection
        HttpClientRequest message = cx.connect(dest.getHost(), dest.getPort(),
                30000);
        String path = dest.getPath();
        message.setRequestLine("POST", path, "HTTP/1.0");
        message.setContentType("text/xml; "
                + responseDoc.getCharacterEncodingScheme());
        HttpClientResponse resp = message.sendHeaders();
        OutputStream out = message.getBodyOutputStream();
        // Copy soap message
        builder.serializeToOutputStream(responseDoc, out);
        try {
            // Message sending
            out.close();
        } catch (IOException e1) {
            logger.finest("Sending error problem", e1);
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
