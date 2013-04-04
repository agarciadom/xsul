/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpDynamicInfosetProcessor.java,v 1.12 2004/04/27 20:12:45 aslom Exp $
 */

package xsul.processor.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.XmlBuilderException;
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
import xsul.http_server.ServerSocketFactory;
import xsul.processor.DynamicInfosetProcessor;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.util.Utf8Reader;
import xsul.util.Utf8Writer;

//import static xsul.XmlConstants.*;

/**
 * This class allows to send XML to HTTP endpoint.
 * Simply set endpoint location and execute invoke*().
 *
 * @version $Revision: 1.12 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public abstract class HttpDynamicInfosetProcessor implements DynamicInfosetProcessor {
    private static final MLogger logger = MLogger.getLogger();
    //private static final MLogger xmlOut = MLogger.getLogger("trace.xsul.xml.processor.out");
    //private static final MLogger xmlIn = MLogger.getLogger("trace.xsul.xml.processor.in");
    
    private static final XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static final XmlPullParserPool pool = new XmlPullParserPool(builder.getFactory());
    
    private HttpMiniServer server;
    private HttpMiniServlet servlet;
    private int serverPort;
    
    public HttpDynamicInfosetProcessor() {
    }
    
    public HttpDynamicInfosetProcessor(int tcpPort) throws DynamicInfosetProcessorException {
        setServerPort(tcpPort);
    }
    
    public HttpDynamicInfosetProcessor(ServerSocketFactory serverSocketFactory)
        throws DynamicInfosetProcessorException
    {
        this.server = new HttpMiniServer(serverSocketFactory);
    }
    
    public HttpDynamicInfosetProcessor(HttpMiniServer server)
        throws DynamicInfosetProcessorException
    {
        this.server = server;
    }
    
    
    public void setServerPort(int tcpPort) throws DynamicInfosetProcessorException {
        if(tcpPort < 0 || tcpPort > 65535) {
            throw new IllegalArgumentException("TCP port must be between 0 and 65535");
        }
        this.serverPort = tcpPort;
    }
    
    public int getServerPort() {
        if(server != null) {
            return server.getServerPort();
        } else {
            return this.serverPort;
        }
    }
    
    public HttpMiniServer getServer() { return server; }
    
    public void setServer(HttpMiniServer server) throws DynamicInfosetProcessorException {
        if(this.server != null) {
            throw new DynamicInfosetProcessorException("server is already set");
        }
        this.server = server;
    }
    
    public void start() throws IOException, DynamicInfosetProcessorException {
        if(server == null) {
            server= new HttpMiniServer(serverPort);
        }
        if(servlet == null) {
            servlet= new HdisServlet();
        }
        
        server.useServlet(servlet);
        
        server.startServer();
    }
    
    public void stop() throws DynamicInfosetProcessorException {
        if(server == null) {
            throw new DynamicInfosetProcessorException("server HTTP processor was not started");
        }
        server.stopServer();
    }
    
    public void shutdown() throws DynamicInfosetProcessorException {
        if(server == null) {
            throw new DynamicInfosetProcessorException("server HTTP processor was not started");
        }
        server.shutdownServer();
    }
    
    
    public abstract XmlDocument processXml(XmlDocument input)
        throws DynamicInfosetProcessorException;
    
    // allow extending so for example HTTP Basic Auth check can be added easily!
    public void service(HttpServerRequest req, HttpServerResponse res)
        throws HttpServerException
    {
        if(req.getCharset() == null) {
            throw new HttpServerException("content type encoding is required");
        }
        String encoding = req.getCharset().toLowerCase();
        InputStream is = req.getInputStream();
        
        //XmlDocument xmlReq = builder.parseInputStream(is, encoding);
        
        XmlDocument xmlReq;
        XmlPullParser pp;
        try {
            pp = pool.getPullParserFromPool();
        } catch(XmlPullParserException e) {
            throw new HttpServerException("could not get XML pull parser from the pool", e);
        }
        Reader reader = null;
        try {
            
            //        if(encoding.equals("utf-8") || encoding.equals("utf8")) {
            //            reader = new Utf8Reader(is, 8*1024);
            //        } else {
            //            try {
            //                reader = new InputStreamReader(is, encoding);
            //            } catch (UnsupportedEncodingException e) {
            //                throw new HttpServerException("could not read input with encoding "+encoding, e);
            //            }
            //        }
            
            try {
                String enc = encoding.toLowerCase();
                if("utf-8".equals(enc) || "utf8".equals(enc))  {
                    reader = new Utf8Reader(is, 8*1024);
                    pp.setInput(reader);
                } else {
                    pp.setInput(is, encoding);
                }
            } catch(XmlPullParserException e) {
                throw new HttpServerException("could not set parser input", e);
            }
            xmlReq = builder.parse(pp);
            
            
        } finally {
            pool.returnPullParserToPool(pp);
        }
        
        XmlDocument xmlRes = processXml(xmlReq);
        
        try {
            if(reader != null) {
                reader.close();
            } else {
                is.close();
            }
        } catch (IOException e) {
            throw new HttpServerException("could not close request input stream", e);
        }
        
        if(xmlRes != null) {
            OutputStream os = res.getOutputStream();
            //String charset = "UTF-8";
            String charset = "utf-8";
            res.setContentType(charset);
            res.setContentType("text/xml");
            //builder.serializeToOutputStream(xmlRes, os, charset); //check efficiency!
            Writer u8w = new Utf8Writer(os,8*1024);
            serializeXmlResponse(xmlRes, u8w);
            try {
                u8w.flush();
            } catch (IOException e) {}
            
            try {
                u8w.close();
            } catch (IOException e) {
                throw new HttpServerException("problem when serializing XML result to "+charset);
            }
            
        } else {
            //res.setContentType(
            res.setStatusCode("202");
            res.setReasonPhrase("OK");
        }
        //throw new HttpServerException("not implemented");
    }

    protected void serializeXmlResponse(XmlDocument xmlRes, Writer writer) throws XmlBuilderException {
        //builder.serializeToWriter(xmlRes, u8w);
        //builder.serializeToWriter(input, utf8Writer);
        XmlSerializer ser = null;
        try {
            ser = builder.getFactory().newSerializer();
            ser.setOutput(writer);
            final String SERIALIZER_ATTVALUE_USE_APOSTROPHE =
                "http://xmlpull.org/v1/doc/features.html#serializer-attvalue-use-apostrophe";
            ser.setFeature(SERIALIZER_ATTVALUE_USE_APOSTROPHE, true);
        } catch (Exception e) {
            throw new XmlBuilderException("could not serialize node to writer", e);
        }
        serializeXmlResponse(xmlRes, ser);
        try {
            ser.flush();
        } catch (IOException e) {
            throw new XmlBuilderException("could not flush output", e);
        }
    }
    
    protected void serializeXmlResponse(XmlDocument xmlRes, XmlSerializer ser) {
        builder.serialize(xmlRes, ser);
    }
    
    
    /**
     * Plumbing as in Java class can not extend two classes ...
     */
    private class HdisServlet extends HttpMiniServlet {
        //        private HttpDynamicInfosetProcessor processor;
        //        public HdisServlet(HttpDynamicInfosetProcessor processor) {
        //            if(processor == null) throw new IllegalArgumentException();
        //            this.processor = processor;
        //        }
        
        public void service(HttpServerRequest req, HttpServerResponse res)
            throws HttpServerException
        {   // NOTE: required casts for "this" to enclosing class !!!!
            HttpDynamicInfosetProcessor.this.service(req, res);
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






