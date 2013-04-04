/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpServerResponseImpl.java,v 1.13 2006/04/18 18:03:46 aslom Exp $
 */

package xsul.http_server.impl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import xsul.MLogger;
import xsul.http_common.HttpConstants;
import xsul.http_server.HttpServerResponse;
import xsul.util.XsulUtil;
import xsul.XsulVersion;

/**
 * Base exception thrown by all HTTP server side operations.
 *
 * @version $Revision: 1.13 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class HttpServerResponseImpl implements HttpServerResponse {
    private final static String CRLF = "\r\n";
    private final static String UTF8_CHARSET = "utf-8";
    private final static MLogger logger = MLogger.getLogger();
    final static MLogger TRACE_SENDING = HttpMiniServerConnection.TRACE_SENDING;
    
    //status code + status line
    private ByteArrayOutputStream baos;
    private OutputStream os;
    private String statusCode;
    private String reasonPhrase;
    private String httpVersion;
    
    //predefined headers
    private String connection;
    private String contentType;
    private String charset;
    private String userAgent;
    //NOTE: header field names are case-insensitive: http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
    protected static final String[] reservedHeaders = {
        "connection", "content-length", "content-type", "host", "user-agent" };
    
    // user defined headers
    private  int headersEnd;
    private  String[] headerName;
    private  String[] headerValue;
    
    private OutputStream socketOutputStream;
    
    HttpServerResponseImpl(OutputStream socketOutputStream) {
        this.socketOutputStream = socketOutputStream;
    }
    
    void reset() {
        //this is required to be able to compute Content-Length
        
        //this is full buffering - not too good -- needs to use ChunkedOutputStream if HTTP/1.1!!!
        // TODO: needs to discover that client supports chunked encoding
        
        baos = new ByteArrayOutputStream(512); //hard to predict initial size ..  //length > 0 ? length : 1024);
        os = baos;
        
        //TODO use UTF-8 for default output!!!!! -- leave it to client????
        //Writer writer = new OutputStreamWriter(baos,
        //                                       req.charset != null ? req.charset : DEFAULT_CHARSET);
        statusCode = "200";
        reasonPhrase = null;
        httpVersion = "1.0";
        
        connection = null;
        contentType = null;
        userAgent = XsulVersion.getUserAgent();
        
        headersEnd = 0;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    public String getCharset() {
        return charset;
    }
    
    public OutputStream getOutputStream() {
        return os;
    }
    
    //    public void setHttpVersion(String httpVersion) {
    //      this.httpVersion = httpVersion;
    //    }
    //
    //    public String getHttpVersion() {
    //      return httpVersion;
    //    }
    
    public void setReasonPhrase(String reasonPhrase) {
        if(reasonPhrase == null) throw new IllegalArgumentException(
                "HTTP reason phrase can not be null");
        this.reasonPhrase = reasonPhrase;
    }
    
    public String getReasonPhrase() {
        return reasonPhrase;
    }
    
    public void setStatusCode(String statusCode) {
        if(statusCode.length() != 3
               || !isDigit(statusCode.charAt(0))
               || !isDigit(statusCode.charAt(1))
               || !isDigit(statusCode.charAt(2))
          )
        {
            throw new IllegalArgumentException("status code must contain exactly 3 digits");
        }
        this.statusCode = statusCode;
    }
    
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    public String getStatusCode() {
        return statusCode;
    }
    
    public void ensureHeadersCapacity(int capacity) {
        if(headerName == null || headerName.length < capacity) {
            String[] newHeaderName = new String[capacity];
            String[] newHeaderValue = new String[capacity];
            // now copy exisiting headers
            if(headersEnd > 0) {
                assert headerName != null && headerName.length > 0;
                System.arraycopy(headerName, 0, newHeaderName, 0, headersEnd);
                System.arraycopy(headerValue, 0, newHeaderValue, 0, headersEnd);
            }
            headerName = newHeaderName;
            headerValue = newHeaderValue;
        }
    }
    
    public void setHeader(String headerName, String headerValue) {
        //TODO: check that not overwrites any reserved header ...
        //TODO: replace header value if header already exsit and not just append ...
        if(headerName == null) throw new IllegalArgumentException();
        if(headerValue == null) throw new IllegalArgumentException();
        if(this.headerName == null || this.headerName.length <= headersEnd) {
            ensureHeadersCapacity(2 * headersEnd + 1);
        }
        this.headerName[headersEnd] = headerName;
        this.headerValue[headersEnd] = headerValue;
        headersEnd++;
    }
    
    void drainPipe(boolean sendBody, boolean keepalive) throws Exception {
        os.flush();
        os.close();
        byte [] responseBytes = baos.toByteArray();
        int responseLength = responseBytes.length;
        
        String headersOut = getHeaders(responseLength, keepalive);
        if(TRACE_SENDING.isFinestEnabled()) {
            String body = "";
            if(sendBody) {
                body = new String(responseBytes, HttpConstants.ISO88591_CHARSET);
            }
            TRACE_SENDING.finest("TRACE: sending response (sendBody="+sendBody+"):"
                                     +"---\n"
                                     +XsulUtil.printable(headersOut+body, false)
                                     +"---\n");
            //TRACE_SENDING.finest("TRACE: sending response headers");
        }
        if(contentType != null && responseLength == 0) {
            throw new IllegalStateException(
                "response contentType was set to "+contentType+" but content length is zero");
        }
        
        //BufferedWriter output = new BufferedWriter(
        //  new OutputStreamWriter(outputStream(),"8859_1"));
        //TODO: check result encoding for HTTP headers????
        //TODO: use ISO-8859-1 for heacders and detect if non LATIN1 is used???
        socketOutputStream.write(headersOut.getBytes(UTF8_CHARSET));
        
        if(sendBody) {
            logger.finest("sending response body");
            socketOutputStream.write(responseBytes);
        }
        socketOutputStream.flush();
        logger.finest("sending response finished");
    }
    
    private String getHeaders(int responseLength,
                              boolean keepalive)
        throws Exception
    {
        StringBuffer buf = new StringBuffer();
        getHeadersInternal(buf,
                           httpVersion,
                           statusCode,
                           reasonPhrase,
                           responseLength,
                           contentType,
                           charset,
                           keepalive);
        
        for (int i = 0; i < headersEnd; i++)
        {
            buf.append(headerName[i]);
            buf.append(": ");
            buf.append(headerValue[i]);
            buf.append(CRLF);
        }
        
        buf.append(CRLF);
        return buf.toString();
    }
    
    /*package*/ static String getHeaders(String statusCode,
                                         String statusLine,
                                         String contentType,
                                         int contentLen,
                                         boolean keepalive)
    {
        StringBuffer buf = new StringBuffer();
        getHeadersInternal(buf,
                           "1.0",
                           statusCode,
                           statusLine,
                           contentLen,
                           contentType,
                           null,
                           keepalive);
        
        buf.append(CRLF);
        return buf.toString();
    }
    
    
    private static void getHeadersInternal(StringBuffer buf,
                                           String httpVersion,
                                           String statusCode,
                                           String reasonPhrase,
                                           int responseLength,
                                           String contentType,
                                           String charset,
                                           boolean keepalive)
    {
        if(reasonPhrase == null) {
            if("200".equals(statusCode) || "202".equals(statusCode)) {
                reasonPhrase = "OK";
            } else if("500".equals(statusCode)) {
                reasonPhrase = "Internal Server Error";
            } else {
                reasonPhrase = "Error processing request";
            }
        }
        
        
        buf.append("HTTP/").append(httpVersion).append(" ").append(statusCode)
            .append(" ").append(reasonPhrase).append(CRLF);
        
        
        //TODO check that no outHeaders overwrite those ...
        buf.append("Date: ").append((new Date()).toGMTString()).append(CRLF);
        buf.append("Server: ").append(XsulVersion.getUserAgent()).append(CRLF);
        
        
        if(contentType != null) {
            String c = contentType;
            String cset = charset ;
            if(charset != null) {
                c += "; charset=\""+charset+"\"";
            }
            
            buf.append("Content-Type: ").append(c).append(CRLF);
        }
        
        if(responseLength >= 0) {
            buf.append("Content-Length: ").append(responseLength).append(CRLF);
        } else  {
            //if(responseLength != 0) {
            //    throw new IllegalStateException("response contentType was not set and length not zero "+responseLength);
            //}
            keepalive = false;
        }
        
        
        if(keepalive) {
            buf.append("Connection: keep-alive\r\n");
            buf.append("Keep-Alive: 300\r\n");
        } else {
            buf.append("Connection: close\r\n");
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


