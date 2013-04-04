/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpClientRequest.java,v 1.19 2006/06/08 19:42:29 aslom Exp $
 */

package xsul.http_client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;
import xsul.MLogger;
import xsul.http_common.HttpConstants;
import xsul.http_common.NotifyCloseOutputStream;
import xsul.util.XsulUtil;

/**
 * This class represents client-side HTTP request.
 * It is ooptimzied for small memory footprint.
 *
 * @version $Revision: 1.19 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class HttpClientRequest {
    private static final MLogger httpOutTracing = MLogger.getLogger("trace.xsul.http.client.out");
    
    final static private String CRLF = "\r\n";
    
    // request line
    protected String method;
    protected String requestUri;
    protected String httpVersion;
    
    //predefined headers
    protected String connection;
    protected String contentType;
    protected String userAgent;
    //NOTE: header field names are case-insensitive: http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
    protected static final String[] RESERVED_HEADERS = {
        "connection", "content-length", "content-type", "host", "user-agent" };
    
    // user defined headers -- kept as array not hashtable to minmize meory usage
    protected int headersEnd;
    protected String[] headerName;
    protected String[] headerValue;
    
    // internal communication details
    //private Socket socket;
    private String hostValue;
    //private String host;
    //private int port;
    private ClientSocketConnection conn;
    private HttpClientConnectionManager mgr;
    private boolean closed;
    private HttpClientResponse resp;
    //private InputStream socketInputSteam;
    private OutputStream outputStream;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream(8 * 1024);
    private boolean requestReuse = false;
    
    public HttpClientRequest(ClientSocketConnection conn,
                             //ClientSocketFactory socketFactory,
                             HttpClientConnectionManager mgr)
        throws HttpClientException
    {
        //this.socket = socket;
        
        //System.out.println(getClass()+" "+conn.getSocket());
        
        this.conn = conn;
        this.mgr = mgr;
        //        this.host = host;
        //        this.port = port;
        if(conn.getPort() != 80) { //TODO handle SSL!
            hostValue = conn.getHost() + ":"+ conn.getPort();
        } else {
            hostValue = conn.getHost();
        }
        this.outputStream = conn.getOutputStream();
        //this.socketInputSteam = socketInputSteam;
        this.resp = new HttpClientResponse(conn, this);
        
    }
    
    public void setRequestLine(String method, String requestUri, String httpVersion)
        throws HttpClientException
    {
        if(closed) {
            throw new HttpClientException("internal error: request was alredy closed");
        }
        
        // 5.1.1 Method http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1.1
        if(method == null || method.length() == 0) {
            throw new IllegalArgumentException("HTTP method be non empty");
        }
        this.method = method;
        // 5.1.2 Request-URI http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1.2
        if(requestUri == null || requestUri.length() == 0) {
            throw new IllegalArgumentException("request URI must be non empty path");
        }
        this.requestUri = requestUri;
        // 3.1 HTTP Version http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.1
        if(!"HTTP/1.0".equals(httpVersion) && !"HTTP/1.1".equals(httpVersion)) {
            throw new IllegalArgumentException("HTTP version must be 1.0 or 1.1 not "+httpVersion);
        }
        //TODO: make httpVersion to use by default 1.1 ?!
        this.httpVersion = httpVersion;
    }
    
    // Connection http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.10
    // 19.6.1.1 Changes to Simplify Multi-homed Web Servers and Conserve IP
    // 19.6.2 Compatibility with HTTP/1.0 Persistent Connections
    //   http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.6.1
    public void setConnection(String connection) throws HttpClientException {
        if(closed) {
            throw new HttpClientException("internal error: request was alredy closed");
        }
        this.connection = connection.toLowerCase();
        if("close".equals(connection)) {
            requestReuse = false;
        } else if ("keep-alive".equals(connection)) {
            requestReuse = true;
        } else {
            throw new HttpClientException("unsupported connection type");
        }
    }
    
    public void setContentType(String contentType) throws HttpClientException {
        if(closed) {
            throw new HttpClientException("internal error: request was alredy closed");
        }
        this.contentType = contentType;
    }
    
    // 14.43 User-Agent http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43
    public void setUserAgent(String userAgent) throws HttpClientException {
        if(closed) {
            throw new HttpClientException("internal error: request was alredy closed");
        }
        this.userAgent = userAgent;
    }
    
    public void ensureHeadersCapacity(int capacity) throws HttpClientException {
        if(closed) {
            throw new HttpClientException("internal error: request was alredy closed");
        }
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
    
    public void setHeader(String headerName, String headerValue) throws HttpClientException {
        if(closed) {
            throw new HttpClientException("internal error: request was alredy closed");
        }
        //TODO: check that not overwrites any reserved header ...
        String name = headerName.toLowerCase().trim().intern();
        for (int i = 0; i < RESERVED_HEADERS.length; i++) {
            if(name == RESERVED_HEADERS[i]) {
                
            }
        }
        //TODO: replace header value if header already exsit and not just append ...
        if(headerName == null) throw new IllegalArgumentException();
        if(headerValue == null) throw new IllegalArgumentException();
        ensureHeadersCapacity(headersEnd + 1);
        this.headerName[headersEnd] = headerName;
        this.headerValue[headersEnd] = headerValue;
        headersEnd++;
    }
    
    
    public HttpClientResponse sendHeaders() throws HttpClientException {
        if(closed) {
            throw new HttpClientException("internal error: request was alredy closed");
        }
        return resp;
    }
    
    private void writeHeaders(int contentLength) throws HttpClientException {
        if(method == null) throw new HttpClientException("request method must be set");
        if(requestUri == null) throw new HttpClientException("request URI must be set");
        if(httpVersion == null) throw new HttpClientException("request HTTP version must be set");
        StringBuffer headerbuf = new StringBuffer(1024); //to minimize buffer copy
        headerbuf.append(method).append(' ')
            .append(requestUri).append(' ')
            .append(httpVersion).append(CRLF);
        
        //.append(httpProxyHost == null ? path : location)
        //.append(" HTTP/").append(HTTP_VERSION).append("\r\n")
        
        headerbuf.append("Host: ").append(hostValue).append(CRLF);
        if(userAgent != null) {
            headerbuf.append("User-Agent: ").append(userAgent).append(CRLF);
        }
        
        headerbuf.append("Content-Type: ").append(contentType).append(CRLF);
        
        if(contentLength > 0) {
            headerbuf.append("Content-Length: ").append(contentLength).append("\r\n");
        }
        
        // write user headers
        for (int i = 0; i < headersEnd; i++)
        {
            headerbuf.append(headerName[i]).append(": ").append(headerValue[i]).append(CRLF);
        }
        
        //write connection close
        if(connection != null) {
            headerbuf.append("Connection: ").append(connection).append("\r\n");
        }
        
        headerbuf.append(CRLF);
        
        //it is fine - we need octets so we are fine with "iso-8859-1" too
        final String ENC = HttpConstants.ISO88591_CHARSET;
        byte[] b;
        try {
            b = headerbuf.toString().getBytes(ENC);
        } catch (UnsupportedEncodingException e) {
            throw new HttpClientException("could nto serialize headers to "+ENC, e);
        }
        //TODO: is there better way then use StringBuffer -> maybe byte array?
        try {
            if(httpOutTracing.isFinestEnabled()) {
                String headers = new String(b, HttpConstants.ISO88591_CHARSET);
                httpOutTracing.finest("TRACE: sending request headers:"
                                          +"---\n"
                                          +XsulUtil.printable(headers, false)
                                          +"---\n");
            }
            
            // send headers
            outputStream.write(b);
            outputStream.flush();
            
        } catch (IOException e) {
            throw new HttpClientException("could not send HTTP request headers", e);
        }
    }
    
    public OutputStream getBodyOutputStream() throws HttpClientException
    {
        if(closed) {
            throw new HttpClientException("request was alredy closed");
        }
        return new NotifyCloseOutputStream( baos, new Observer() {
                    public void update(Observable o, Object arg) {
                        closeRequest();
                    }
                });
    }
    
    //private HttpClientResponse closeRequest() throws HttpClientException
    private void closeRequest() throws HttpClientException
    {
        if(closed) {
            throw new HttpClientException("internal error: request was alredy closed");
        }
        closed = true; //TODO: add checks for closed flag to ALL methods
        byte[] body = baos.toByteArray();
        int contentLength = body.length;
        writeHeaders(contentLength);
        try {
            if(httpOutTracing.isFinestEnabled()) {
                String bodyAsString = new String(body, HttpConstants.ISO88591_CHARSET);
                httpOutTracing.finest("TRACE: sending request body:"
                                          +"---\n"
                                          +XsulUtil.printable(bodyAsString, false)
                                          +"---\n");
            }
            outputStream.write(body);
            try {
                outputStream.flush();
            } catch(SocketException sox)  {
//                if(sox.getMessage().indexOf("Broken pipe") != -1) {
//                    //OK to ignore?
//                    //Caused by: java.net.SocketException: Broken pipe
//                    //        at java.net.SocketOutputStream.socketWrite0(Native Method)
//                    //        at java.net.SocketOutputStream.socketWrite(SocketOutputStream.java:92)
//                    //        at java.net.SocketOutputStream.write(SocketOutputStream.java:136)
//                    //        at java.io.BufferedOutputStream.flushBuffer(BufferedOutputStream.java:65)
//                    //        at java.io.BufferedOutputStream.flush(BufferedOutputStream.java:123)
//                    //        at xsul.http_client.HttpClientRequest.closeRequest(HttpClientRequest.java:290)
//
//                } else {
                    throw sox;
//                }
            }
            //socketOutputSteam.close(); //would KILL KeepAlive :(
        } catch (IOException e) {
            throw new HttpClientException("could not send HTTP request body", e);
        }
    }
    
    /** this should be called only be corespondig response object spawned in this request */
    void responseFinishedAndCanBeReused(boolean reuseConection) throws HttpClientException {
        if(!closed) {
            throw new HttpClientException("request must be closed before response is closed");
        }
        if(requestReuse && reuseConection) {
            mgr.notifyConnectionForReuse(conn);
        } else {
            conn.close();
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








