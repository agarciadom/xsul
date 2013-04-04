/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpClientResponse.java,v 1.11 2006/04/18 18:03:46 aslom Exp $
 */

package xsul.http_client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import xsul.MLogger;
import xsul.http_common.FixedLengthInputStream;
import xsul.http_common.HttpConstants;
import xsul.http_common.NotifyCloseInputStream;
import xsul.util.XsulUtil;

/**
 * This class represents response from HTTP server.
 * It is ooptimzied for small memory footprint.
 *
 * @version $Revision: 1.11 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class HttpClientResponse {
    private static final MLogger httpInTracking = MLogger.getLogger("trace.xsul.http.client.in");
    private InputStream socketInputStream;
    //private DataInputStream lineInput;
    private int length = -1;
    private String statusLine;
    private String statusCode;
    private String reasonPhrase;
    private String httpVersion;
    private String contentType;
    private boolean hasReadHeaders;
    //private ClientSocketConnection conn;
    private HttpClientRequest req;
    private boolean tryReuseConnection = false;

    // user defined headers -- kept as array not hashtable to minmize meory usage
    protected int headersEnd;
    private static final int HEADERS_PREALLOC_SIZE = 16;
    protected String[] headerName = new String[HEADERS_PREALLOC_SIZE];
    protected String[] headerValue = new String[HEADERS_PREALLOC_SIZE];
    private boolean closed;
    
    /*package*/ HttpClientResponse(ClientSocketConnection conn,
                       HttpClientRequest req)
    {
        //this.conn = conn;
        this.req = req;
        //this.socketInputStream = socketInputStream;
        //this.lineInput = new DataInputStream(socketInputStream);
        //this.socketInputStream = new BufferedInputStream(socketInputStream, 8*1024);
        this.socketInputStream = conn.getInputStream();
    }
    
    // this is not multi-thread safe by design!
    private byte[] lineBuf = new byte[1024];
    
    private String readLine() throws IOException {
        int pos = 0;
        while(true) {
            int i = socketInputStream.read();
            if(i < 0) {
                throw new EOFException("no more data available while reading line");
            }
            if(i == '\n') {
                //ignore leading LF in CRLF
                //as described in http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.3
                if(pos > 0 && lineBuf[ pos - 1 ] == '\r') {
                    --pos;
                }
                //NOTE: uses deprecated hibytes==0 for fast conversion
                String line = new String(lineBuf, 0, 0, pos);
                return line;
            }
            if(pos >= lineBuf.length) {
                byte[] newLineBuf = new byte[2 * lineBuf.length ];
                System.arraycopy(lineBuf,0,newLineBuf,0,pos);
                lineBuf = newLineBuf;
            }
            lineBuf[ pos++ ] = (byte)i;
        }
    }
    
    public void readStatusLine() throws HttpClientException {
        if(statusLine != null) {
            throw new HttpClientException("status line can be only read once");
        }
        //if(TRACE_RECEIVING.isFinestEnabled()) TRACE_RECEIVING.finest(
        //      "TRACE waiting for first line of HTTP request");
        statusLine = null;
        try {
            // RFC 2616 says be generous about allowing extra \r\n between requests
            do {
                statusLine = readLine();
            } while ((statusLine != null) && (statusLine.length() == 0));
            
        } catch(IOException e) {
            throw new HttpClientException("could not read response line", e);
        }
        httpInTracking.finest(statusLine);
        assert statusLine != null;
        //"HTTP/1.0 200 OK"
        if(! statusLine.startsWith("HTTP")) {
            throw new HttpClientException("expected response starting with HTTP"
                                              +" but got "+XsulUtil.printable(statusLine));
        }
        if(statusLine.length() < (4+1+4+1+3)) {
            throw new HttpClientException("statusline is too short"
                                              +": "+XsulUtil.printable(statusLine));
        }
        if(statusLine.charAt(4) != '/') {
            throw new HttpClientException("expected / after HTTP "
                                              +" but got "+XsulUtil.printable(statusLine));
        }
        if(statusLine.charAt(5) != '1' || statusLine.charAt(6) != '.' ) {
            throw new HttpClientException("only major version 1 of HTTP supported"
                                              +" but got "+XsulUtil.printable(statusLine));
        }
        //TODO check if multiple spaces (or other WS) are allowed
        int spacePos = statusLine.indexOf(' ');
        if(spacePos == -1) {
            throw new HttpClientException("expected space after HTTP/1.minor"
                                              +" but got "+XsulUtil.printable(statusLine));
        }
        httpVersion = statusLine.substring(0, spacePos);
        tryReuseConnection = httpVersion.equals("HTTP/1.1");
        // check /version
        
        int secondSpacePos = statusLine.indexOf(' ', spacePos+1);
        if(secondSpacePos == -1) {
            statusCode = statusLine.substring(spacePos+1);
            reasonPhrase = "";
        } else {
            statusCode = statusLine.substring(spacePos+1, secondSpacePos);
            reasonPhrase = statusLine.substring(secondSpacePos + 1);
        }
        //check that staus code is 3 digits
        if(statusCode.length() != 3
               || !isDigit(statusCode.charAt(0))
               || !isDigit(statusCode.charAt(1))
               || !isDigit(statusCode.charAt(1))
          )
        {
            throw new HttpClientException("expected HTTP status code is 3 digisits not "
                                              +XsulUtil.printable(statusCode)
                                              +" obtained from staus "+XsulUtil.printable(statusLine));
        }
    }
    
    private boolean isDigit(char ch) {
        //Character.isDigit() allows more than ASCII
        return ch >= '0' && ch <= '9';
    }
    
    public String getHttpVersion() throws HttpClientException {
        if(statusLine == null) {
            throw new HttpClientException("must read staus line first");
        }
        return httpVersion;
    }
    
    public String getStatusCode() throws HttpClientException {
        if(statusLine == null) {
            throw new HttpClientException("must read staus line first");
        }
        return statusCode;
    }
    
    public String getReasonPhrase() throws HttpClientException {
        if(statusLine == null) {
            throw new HttpClientException("must read staus line first");
        }
        return reasonPhrase;
    }
    
    //TODO: should we allow to return raw byte[] or String with all headers content?
    public void readHeaders() throws HttpClientException {
        if(statusLine == null) {
            throw new HttpClientException("must read staus line first");
        }
        if(hasReadHeaders) {
            throw new HttpClientException("headers can only be read once");
        }
        hasReadHeaders = true;
        try {
            while(true) {
                String line = readLine();
                httpInTracking.finest(line);
                //if(TRACE_RECEIVING.isFinestEnabled())
                //    System.err.println(line);
                if(line == null || "".equals(line)) {
                    break;
                }
                int index = line.indexOf(':');
                if(index == -1)  //TODO: handle multiline MIME headers???
                    continue;
                String name = line.substring(0, index).toLowerCase();
                int len = line.length();
                for(++index;index < len;++index) {
                    if(line.charAt(index) != ' ')
                        break;
                }
                String value = line.substring(index);
                
                
                // if necessary prepare space for headers
                assert headersEnd >= 0;
                assert headersEnd <= headerName.length;
                if(headersEnd == headerName.length) {
                    int newSize = 2 * headerName.length;
                    String[] newHeaderName = new String[newSize];
                    String[] newHeaderValue = new String[newSize];
                    if(headersEnd > 0) {
                        assert headerName != null && headerName.length > 0;
                        System.arraycopy(headerName, 0, newHeaderName, 0, headersEnd);
                        System.arraycopy(headerValue, 0, newHeaderValue, 0, headersEnd);
                    }
                    headerName = newHeaderName;
                    headerValue = newHeaderValue;
                }
                assert headersEnd < headerName.length;
                
                //headers.put(name, value); //TODO
                headerName[ headersEnd ] = name;
                headerName[ headersEnd ] = value;
                
                if(name.equals("content-length")) {
                    try {
                        length = Integer.parseInt(value);
                    } catch(NumberFormatException ex) {
                        throw new HttpClientException(
                            "could not parse content-length of '"+value+"' for line '"+line+"'");
                    }
                }
                if(name.equals("content-type")) {
                    contentType = value;
                }
                if(name.equals("connection")) {
                    tryReuseConnection = value.indexOf ("keep-alive") > -1;
                }
            }
        } catch(IOException e) {
            throw new HttpClientException("could not read response HTTP headers", e);
        }
        
    }

    public int getHeaderCount() throws HttpClientException {
        if(false == hasReadHeaders) {
            throw new HttpClientException("headers must be read first");
        }
        return headersEnd;
    }
    
    // implementation detail
    private String getHeaderName(int i) throws HttpClientException {
        if(i >= 0 && i < headersEnd) {
            return headerName[ i ];
        } else {
            throw new HttpClientException("header index is out of range");
        }
    }

    // implementation detail
    private String getHeaderValue(int i) throws HttpClientException {
        if(false == hasReadHeaders) {
            throw new HttpClientException("headers must be read first");
        }
        if(i >= 0 && i < headersEnd) {
            return headerValue[ i ];
        } else {
            throw new HttpClientException("header index is out of range");
        }
    }
    
    /**
     * Return value of header with given name or null if such header does not exist.
     */
    public String getHeader(String name) throws HttpClientException {
        if(false == hasReadHeaders) {
            throw new HttpClientException("headers must be read first");
        }
        name = name.toLowerCase(); //all header names are always kept in lower case
        for (int i = 0; i < headersEnd; i++)
        {
            if(name == headerName[ i ] || name.equals(headerName[ i ])) {
                return headerValue[ i ];
            }
        }
        return null;
    }
    
    public Enumeration getHeaderNames() throws HttpClientException {
        if(false == hasReadHeaders) {
            throw new HttpClientException("headers must be read first");
        }
        return new Enumeration() {
            int pos = 0;
            public boolean hasMoreElements() {
                return pos < headersEnd;
            }
            public Object nextElement() {
                //return headerName[ pos++ ];
                return getHeaderName( pos++ );
            }
        };
    }
    
//    public Enumeration getHeaderValues() throws HttpClientException {
//        if(false == hasReadHeaders) {
//            throw new HttpClientException("headers must be read first");
//        }
//        return new Enumeration() {
//            int pos = 0;
//            public boolean hasMoreElements() {
//                return pos < headersEnd;
//            }
//            public Object nextElement() {
//                //return headerValue[ pos++ ];
//                return getHeaderValue( pos++ );
//            }
//        };
//    }
    
    public String getContentType() throws HttpClientException {
        if(false == hasReadHeaders) {
            throw new HttpClientException("headers must be read first");
        }
        return contentType;
    }
    
    /**
     * Returns -1 if Content-Length header was not present in response.
     */
    public int getContentLength() throws HttpClientException {
        if(false == hasReadHeaders) {
            throw new HttpClientException("headers must be read first");
        }
        return length;
    }
    
    public InputStream getBodyInputStream() throws HttpClientException {
        if(false == hasReadHeaders) {
            throw new HttpClientException("headers must be read first");
        }
        if(closed) {
            throw new HttpClientException("cant get input stream to already closed response");
        }
        InputStream is = null;
        
        if(length > -1) {
            is = new FixedLengthInputStream(socketInputStream, length);
            //tryReuseConnection = true;
            //charset != null ? charset : DEFAULT_CHARSET);
        } else {
            is = socketInputStream;
            //                  charset != null ? charset : DEFAULT_CHARSET);
            tryReuseConnection = false;
        }
        if(httpInTracking.isFinestEnabled())
        {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(length > 0 ? length : 8*1024);
                byte[] buf = new byte[4*1024];
                while(true) { //copy input stream into byte array!
                    int received = is.read(buf);
                    if(received <= 0)
                        break;
                    baos.write(buf, 0, received);
                }
                byte[] responseBytes = baos.toByteArray();
                //used ISO8859-1 as full 8-bit charset for debugging!
                String responseBody = new String(responseBytes, HttpConstants.ISO88591_CHARSET);
                //System.err.println(
                //"TRACE: "+getClass()+" received request:---\n"+
                //TODO: escape all <32 and > 128 characters!
                httpInTracking.finest("TRACE: received response:---\n"
                                          +XsulUtil.printable(responseBody, false)
                                          +"---\n");
                //reader = new StringReader(requestBody);
                //is.close();
                is = new ByteArrayInputStream(responseBytes);
            } catch (IOException e) { //example of gow tracing can introduce new bugs ...
                throw new HttpClientException("could not read input stream with HTTP body", e);
            }
        }
        //assert is != null
        //return is;
        return new NotifyCloseInputStream( is, new Observer() {
                    public void update(Observable o, Object arg) {
                        closeResponse();
                    }
                });
    }
    
    private void closeResponse() {
        closed = true;
        if(!tryReuseConnection) {
            try {
                socketInputStream.close();
            } catch (IOException e) {}
        }
        req.responseFinishedAndCanBeReused(tryReuseConnection);
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



