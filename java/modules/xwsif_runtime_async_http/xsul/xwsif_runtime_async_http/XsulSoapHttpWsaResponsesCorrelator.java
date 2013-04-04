/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulSoapHttpWsaResponsesCorrelator.java,v 1.1 2005/06/16 00:05:41 aslom Exp $
 */

package xsul.xwsif_runtime_async_http;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.parser_pool.XmlPullParserPool;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.util.FastUUIDGen;
import xsul.util.Utf8Reader;
import xsul.ws_addressing.WsAddressing;
import xsul.ws_addressing.WsaEndpointReference;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.ws_addressing.WsaRelatesTo;
import xsul.xwsif_runtime_async.WSIFAsyncResponseListener;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import xsul.xwsif_runtime_async.WSIFAsyncWsaResponsesCorrelatorBase;

/**
 * This is correlator that runs internally embedded webserver that will
 * receive asynchronous SOAP responses correlated by WS-Addressing messageId.
 *
 * @version $Revision: 1.1 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XsulSoapHttpWsaResponsesCorrelator  extends WSIFAsyncWsaResponsesCorrelatorBase
    implements WSIFAsyncResponsesCorrelator
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static final XmlPullParserPool pool = new XmlPullParserPool(builder.getFactory());
    
    private HttpMiniServer server;
    private HttpMiniServlet servlet;
    private int serverPort;
    
    private Map messageId2Callback = new HashMap();
    
    public XsulSoapHttpWsaResponsesCorrelator() throws DynamicInfosetProcessorException  {
        this(0);
    }
    
    public XsulSoapHttpWsaResponsesCorrelator(int tcpPort) throws DynamicInfosetProcessorException {
        setServerPort(tcpPort);
    }
    
    private void setServerPort(int tcpPort) throws DynamicInfosetProcessorException {
        if(tcpPort < 0 || tcpPort > 65535) {
            throw new IllegalArgumentException("TCP port must be between 0 and 65535");
        }
        this.serverPort = tcpPort;
        server = new HttpMiniServer(serverPort);
        
        //NOTE: setting to daemon will make server thread die when all other threads are dead
        //this is important so it does not bloc JVM from exiting afer main thread is finished
        server.setDaemon(true);
        
        servlet = new HdisServlet();
        server.useServlet(servlet);
        
        server.startServer();
        // create replyTo address from server host:port known after server is started
        String serverLoc = server.getLocation();
        setReplyTo(URI.create(serverLoc));
        
    }
    
    public String getServerLocation() {
        return server.getLocation();
    }
    
    //    public void registerCallback(Object messageId, WSIFAsyncResponseListener callback) {
    //        if(messageId == null) {
    //            throw new IllegalArgumentException();
    //        }
    //        synchronized(messageId2Callback) {
    //            messageId2Callback.put((URI)messageId, callback);
    //        }
    //    }
    //
    //    public WSIFAsyncResponseListener unregisterCallback(Object messageId) {
    //        synchronized(messageId2Callback) {
    //            WSIFAsyncResponseListener callback = (WSIFAsyncResponseListener) messageId2Callback.remove((URI)messageId);
    //            return callback;
    //        }
    //
    //    }
    //
    //    public Object addCorrelationToOutgoingMessage(XmlDocument outgoingDoc) {
    //        WsaMessageInformationHeaders  wsaHeaders = new WsaMessageInformationHeaders(outgoingDoc);
    //        wsaHeaders.setReplyTo(new WsaEndpointReference(replyToUri));
    //        URI messageId = URI.create("uuid:"+FastUUIDGen.nextUUID());
    //        wsaHeaders.setMessageId(messageId);
    //        return messageId;
    //    }
    
    // allow extending so for example HTTP Basic Auth check can be added easily!
    public void service(HttpServerRequest req, HttpServerResponse res)
        throws HttpServerException
    {
        String contentType = req.getContentType();
        String method = req.getMethod();
        if(method.equals("POST")) {
            if(contentType != null && contentType.indexOf("xml") != -1) {
                serviceXml(req, res);
                return;
            } else {
                throw new HttpServerException(
                    "could not process POST - unsupported Content-type '"+contentType+"'");
            }
        } else {
            throw new HttpServerException("unsupported HTTP method '"+method+"'");
        }
    }
    
    public void serviceXml(HttpServerRequest req, HttpServerResponse res)
        throws HttpServerException
    {
        final String UTF8 = "utf-8";
        // read XML request as XML doc
        
        if(req.getCharset() == null) {
            throw new HttpServerException("content type encoding is required");
        }
        String encoding = req.getCharset();
        InputStream is = req.getInputStream();
        
        XmlDocument incomingXmlDoc;
        XmlPullParser pp;
        try {
            pp = pool.getPullParserFromPool();
        } catch(XmlPullParserException e) {
            throw new HttpServerException("could not get XML pull parser from the pool", e);
        }
        Reader reader = null;
        try {
            
            try {
                String enc = encoding.toLowerCase();
                if(UTF8.equals(enc) || "utf8".equals(enc))  {
                    reader = new Utf8Reader(is, 8*1024);
                    pp.setInput(reader);
                } else {
                    pp.setInput(is, encoding);
                }
            } catch(XmlPullParserException e) {
                throw new HttpServerException("could not set parser input", e);
            }
            incomingXmlDoc = builder.parse(pp);
            
        } finally {
            pool.returnPullParserToPool(pp);
            // make sure we are finished with reading
            try {
                if(reader != null) {
                    reader.close();
                } else {
                    is.close();
                }
            } catch (IOException e) {
                throw new HttpServerException("could not close request input stream", e);
            }
        }
        
        
        //find callback and execute it
        
        WsaMessageInformationHeaders wsaHeaders = new WsaMessageInformationHeaders(incomingXmlDoc);
        //URI relatedMssageId = null;
        //        for(Iterator r = wsaHeaders.getRelatesTo().iterator(); r.hasNext();) {
        //            WsaRelatesTo rt = (WsaRelatesTo) r.next();
        //            if(rt.getRelationshipType().equals(WsAddressing.RESPONSE_REPLY_RELATIONSHIP)) {
        //                relatedMssageId = rt.getRelationship();
        //            }
        //        }
        URI relatedMssageId = wsaHeaders.getRelatedRequestMessageId();
        if(relatedMssageId != null) {
            WSIFAsyncResponseListener callback = unregisterCallback(relatedMssageId);
            if(callback != null) {
                //NOTE: callback is expetect to return immediately (just notification!!!)
                if(logger.isFinestEnabled()) {
                    logger.finest("correlated "+relatedMssageId+" with "+callback);
                }
                callback.processAsyncResponse(incomingXmlDoc);
            }
        } else {
            logger.info("dropped message "+builder.serializeToString(incomingXmlDoc));
        }
        
        res.setStatusCode("202");
        res.setReasonPhrase("OK");
        
        // as it is one-way messaging send 201 OK empty HTTP response
        
        
    }
    
    private class HdisServlet extends HttpMiniServlet {
        public void service(HttpServerRequest req, HttpServerResponse res)
            throws HttpServerException
        {   // NOTE: required casts for "this" to enclosing class !!!!
            XsulSoapHttpWsaResponsesCorrelator.this.service(req, res);
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

