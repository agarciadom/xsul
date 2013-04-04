/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: MsgBoxServlet.java,v 1.9 2006/05/01 21:48:09 aslom Exp $
 */
package xsul.msg_box.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;
import xsul.monitoring.XsulMonitoringStats;
import xsul.monitoring.XsulMonitoringUtil;
import xsul.msg_box.MsgBoxConstants;
import xsul.msg_box.storage.MsgBoxStorage;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.ws_addressing.WsaEndpointReference;
import xsul.ws_addressing.WsaMessageInformationHeaders;

//TODO: keep track of queue size and other statistics such as number of boxe created (total since last cleanup)

/**
 * This servlet takes incoming message to some location and executes operation on MsgBoxStorage.
 * In some sense it is META service or partial service - it work under level of typical service.
 *
 */
public class MsgBoxServlet extends HttpMiniServlet {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static SoapUtil[] soapFragrances = new SoapUtil[] {
        Soap11Util.getInstance(), Soap12Util.getInstance() }; //supports both SOAP 1.1 and 1.2
    
    private MsgBoxStorage msgBoxStorage;
    private String msgBoxServicePath;
    private URI location;
    
    
    private long startTime = System.currentTimeMillis();
    private long requestXmlMsgCount;
    private long countRequestRpc;
    private long countRequestMsg;
    private long numberOfConnections;
    
    /**
     * Create a servlet that uses specified storage.
     *
     * @param msgBoxStorage storage implementation to use
     */
    public MsgBoxServlet(MsgBoxStorage msgBoxStorage) {
        this.msgBoxStorage = msgBoxStorage;
    }
    
    
    public void setLocation(URI location) {
        this.location = location;
        this.msgBoxServicePath = location.getPath();
    }
    
    /**
     * Process HTTP request.
     *
     */
    public void service(HttpServerRequest req, HttpServerResponse resp)
        throws HttpServerException
    {
        ++numberOfConnections;
        String path = req.getPath();
        String method = req.getMethod();
        
        if (method.equals("POST")) {
            // XML messages are expected for POST
            processSoap(req, path, resp);
            
        } else if(method.equals("GET")) {
            if("/".equals(path)) {
                XsulMonitoringStats s = XsulMonitoringUtil.createStats(
                    "MsgBox", startTime, requestXmlMsgCount, numberOfConnections);
                //                XmlElement xhtml = XsulMonitoringUtil.createXhtmlStats(s);
                //                XmlDocument xhtmlDoc = builder.newDocument("1.0", Boolean.TRUE, XmlConstants.DEFAULT_CHARSET);
                //                xhtmlDoc.setDocumentElement(xhtml);
                XmlDocument xhtmlDoc = XsulMonitoringUtil.createXhtmlStatsDoc(
                    s, XmlConstants.DEFAULT_CHARSET);
                sendHttpOkResult(resp, xhtmlDoc, XmlConstants.CONTENT_TYPE_XML);
            } else if("/favicon.ico".equals(path)) {
                //send XSUL logo as favicon.ico bytes
                resp.setStatusCode("200");
                resp.setReasonPhrase("OK");
                //resp.setStatusCode("404");
                //resp.setReasonPhrase("No favicon");
                OutputStream outResp = resp.getOutputStream();
                //resp.setContentType("image/vnd.microsoft.icon");
                try {
                    outResp.write(XsulMonitoringUtil.getFaviconAsBytes());
                    outResp.close();
                } catch (IOException e) {}
                
            } else if(!path.startsWith(msgBoxServicePath)) {
                logger.warning("wrong path '"+path+"' expected path starting with '"+msgBoxServicePath+"'");
                throw new HttpServerException("wrong path '"+path+"'");
            }
            //TODO suport ?wsdl and maybe some overivew like how many mailboxes are here !
            // for example http://localhost:port/msgBoxService?wsdl
        } else {
            throw new HttpServerException("unsupported HTTP method "+method);
        }
    }
    
    private void processSoap(HttpServerRequest req, String path, HttpServerResponse resp)
        throws XmlBuilderException, IllegalStateException, XsulException
    {
        XmlElement requestXml = builder.parseFragmentFromInputStream(req.getInputStream());
        SoapUtil soapUtil = SoapUtil.selectSoapFragrance(requestXml, soapFragrances);
        // take soap message
        XmlElement incomingXml = soapUtil.requiredBodyContent(requestXml);
        XsulMonitoringStats stats = XsulMonitoringUtil.createStats(
            "MsgBox", startTime, requestXmlMsgCount, numberOfConnections);
        XmlDocument outgoingPingResponseDoc = XsulMonitoringUtil.processMonitoringRequest(
            stats,
            incomingXml, soapUtil);
        if(outgoingPingResponseDoc != null) {
            sendHttpOkResult(resp, outgoingPingResponseDoc);
        } else if(path.equals(msgBoxServicePath)) {
            ++requestXmlMsgCount;
            ++countRequestRpc;
            
            // meta operations on message box
            
            //determine what is operation -- get first child with actual message
            WsaMessageInformationHeaders wmih = new WsaMessageInformationHeaders(requestXml);
            XmlNamespace wsaNs = wmih.guessWsAddressingVersionUsedInHeaders();  // guess WSA version
            XmlElement requestMsg = soapUtil.requiredBodyContent(requestXml);
            String opName = requestMsg.getName();
            XmlElement responseMsg = builder.newFragment(requestMsg.getNamespace(), opName+"Response");
            XmlDocument responseEnv = soapUtil.wrapBodyContent(responseMsg);
            //THIS SHOULD BE GENERATED FROM WSDL ...
            if(MsgBoxConstants.OP_CREATE_BOX.equals(opName) ) {
                createBox(wsaNs, requestMsg, responseMsg);
            } else if(MsgBoxConstants.OP_DESTROY_BOX.equals(opName) ) {
                destroyBox(requestMsg,  responseMsg);
            } else if(MsgBoxConstants.OP_TAKE_MSG.equals(opName) ) {
                //take message from box
                takeMessages(requestMsg, responseMsg);
            } else {
                throw new IllegalStateException("unknown operation "+opName);
            }
            sendHttpOkResult(resp, responseEnv);
        } else if(path.startsWith(msgBoxServicePath)) {
            ++requestXmlMsgCount;
            ++countRequestMsg;
            
            String key = path.substring(msgBoxServicePath.length());
            if(key.startsWith("/")) {
                key = key.substring(1);
            }
            String s = builder.serializeToString(requestXml);
            msgBoxStorage.putMessageIntoMsgBox(key, s);
            sendHttpOneWayOk(resp); //message was accepted --- now processing is delegated to mesage box owner
        } else {
            sendHttError(resp, "Wrong path");
        }
    }
    
    private void createBox(XmlNamespace wsaNs, XmlElement requestMsg, XmlElement responseMsg) throws IllegalStateException, XsulException {
        String key = msgBoxStorage.createMsgBox();
        if(key == null) throw new IllegalStateException();
        WsaEndpointReference epr = new WsaEndpointReference(wsaNs, URI.create(location + "/" +  key));
        epr.setNamespace(MsgBoxConstants.MSG_BOX_NS);
        epr.setName(MsgBoxConstants.EL_BOX_ADDR);
        // create response SOAP message with epr inside
        responseMsg.addChild(epr);
    }
    private void destroyBox(XmlElement requestMsg, XmlElement responseMsg) throws XmlBuilderException, XsulException {
        XmlElement el = requestMsg.requiredElement(
            MsgBoxConstants.MSG_BOX_NS, MsgBoxConstants.EL_BOX_ADDR);
        WsaEndpointReference epr = new WsaEndpointReference(el);
        String addr = epr.getAddress().toString();
        if(addr.startsWith(location.toString())) {
            String key = addr.substring(location.toString().length()+1);
            msgBoxStorage.destroyMsgBox(key);
        } else {
            throw new XsulException("unrecognized msg box");
        }
    }
    
    private void takeMessages(XmlElement requestMsg, XmlElement responseMsg) throws XmlBuilderException, XsulException {
        XmlElement el = requestMsg.requiredElement(
            MsgBoxConstants.MSG_BOX_NS, MsgBoxConstants.EL_BOX_ADDR);
        WsaEndpointReference epr = new WsaEndpointReference(el);
        String addr = epr.getAddress().toString();
        if(addr.startsWith(location.toString())) {
            String key = addr.substring(location.toString().length());
            if(key.startsWith("/")) {
                key = key.substring(1);
            }
            List list = msgBoxStorage.takeMessagesFromMsgBox(key);
            if(list != null && !list.isEmpty()) {
                //                try {
                //                    Thread.currentThread().sleep(1000L); //avoid flooding by clients - block them...
                //                } catch (InterruptedException e) {}
                //            } else {
                for(Iterator i = list.iterator(); i.hasNext(); ) {
                    XmlElement c = responseMsg.addElement(
                        MsgBoxConstants.MSG_BOX_NS, MsgBoxConstants.EL_MSG_STR);
                    String s = (String) i.next();
                    c.addChild(s);
                }
            }
        } else {
            throw new XsulException("unrecognized msg box");
        }
    }
        
    private void sendHttpOkResult(HttpServerResponse resp, XmlDocument responseEnv) {
        sendHttpOkResult(resp, responseEnv, XmlConstants.CONTENT_TYPE_XML);
    }
    
    private void sendHttpOkResult(HttpServerResponse resp, XmlDocument responseEnv, String contentType) {
        try {
            resp.setReasonPhrase("OK");
            resp.setStatusCode("200");
            OutputStream outResp = resp.getOutputStream();
            resp.setCharset("UTF8");
            resp.setContentType(contentType);
            builder.serializeToOutputStream(responseEnv, outResp, "UTF8");
            outResp.close();
            logger.finest("Sent 200 response to the client");
        } catch (IOException e) {
            logger.warning(
                "Couldn't send confirmation respsonse to the client", e);
        }
    }
    
    private void sendHttpOneWayOk(HttpServerResponse resp) {
        try {
            resp.setReasonPhrase("OK");
            resp.setStatusCode("202");
            OutputStream outResp = resp.getOutputStream();
            outResp.close();
            logger.finest("Sent 202 response to the client");
        } catch (IOException e) {
            logger.warning(
                "Couldn't send confirmation respsonse to the client", e);
        }
    }
    
    private void sendHttError(HttpServerResponse resp, String reason) {
        try {
            resp.setStatusCode("500");
            if(reason != null) {
                resp.setReasonPhrase(reason);
            } else {
                resp.setReasonPhrase("Internal Server Error");
            }
            OutputStream outResp = resp.getOutputStream();
            outResp.close();
            logger.finest("Sent 500 response '"+reason+"' to the client");
        } catch (IOException e) {
            logger.warning(
                "Couldn't send confirmation respsonse to the client", e);
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



