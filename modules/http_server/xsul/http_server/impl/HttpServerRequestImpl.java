/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpServerRequestImpl.java,v 1.9 2006/04/18 18:03:46 aslom Exp $
 */

package xsul.http_server.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import xsul.MLogger;
import xsul.http_common.FixedLengthInputStream;
import xsul.http_common.HttpConstants;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.util.XsulUtil;

/**
 * Base exception thrown by all HTTP server side operations.
 *
 * @version $Revision: 1.9 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class HttpServerRequestImpl implements HttpServerRequest {
    
    
    private final static MLogger TRACE_RECEIVING = HttpMiniServerConnection.TRACE_RECEIVING;
    private final static MLogger logger = MLogger.getLogger();
    
    private String method;
    private String path;
    private String httpVersion;
    
    private String contentType;
    
    private Hashtable headers;
    
    private String charset;
    
    private InetAddress remotInetAddr;
    
    private int remotePort;
    
    private InputStream is;
    
    private boolean keepalive;
    
    private InputStream socketInputStream;
    
    
    HttpServerRequestImpl(InputStream socketInputStream,
                          InetAddress remotInetAddr,
                          int remotePort)
    {
        this.socketInputStream = socketInputStream;
        this.remotInetAddr = remotInetAddr;
        this.remotePort = remotePort;
    }
    
    public InputStream getInputStream() {
        return is;
    }
    
    
    public String getCharset() {
        return charset;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public boolean isKeepAlive() {
        return keepalive;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getPath() {
        return path;
    }

    public int getHeaderCount() throws HttpServerException {
        return headers.size();
    }
        
    /**
     * Return value of header with given name or null if such header does not exist.
     */
    public String getHeader(String name) throws HttpServerException {
        name = name.toLowerCase(); //all header names are always kept in lower case
        return (String) headers.get(name);
    }
    
    public Enumeration getHeaderNames() throws HttpServerException {
        return headers.keys();
    }
        
    private void reset() {
        method      = null;
        path        = null;
        httpVersion = null;
        contentType = null;
        headers = null;
        charset = null;
        is = null;
        keepalive = false;
    }
    
   
    private byte[] lineBuf = new byte[1024];
    
    private String readLine(InputStream socketInputStream) throws IOException {
        int pos = 0;
        while(true) {
            int i;
            try {
                i = socketInputStream.read();
            } catch(SocketTimeoutException ex) {
                if(pos == 0) {
                    return null;
                } else {
                    throw ex;
                }
            }
            if(i < 0) {
                //throw new EOFException("no more data avialble while reading line");
                return null;
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
    
    boolean readRequest() throws Exception {
        reset();
        int length = -1;

        //DataInputStream lineInput = new DataInputStream(socketInputStream);

        if(TRACE_RECEIVING.isFinestEnabled()) TRACE_RECEIVING.finest(
                "TRACE waiting for first line of HTTP request");
        String requestLine = null;
        try {
            // RFC 2616 says be generous about allowing extra \r\n between requests
            do {
                requestLine = readLine(socketInputStream);
            } while ((requestLine != null) && (requestLine.length() == 0));
        } catch(java.net.SocketException ex) {
            //skip typical  SocketException: Connection reset by peer:
            //JVM_recv in socket in put stream read
            //ex.printStackTrace();
        }
        if(requestLine==null) {
            //throw new ServerException("Invalid request");
            return false; //we are finished - client does not want to send any more requests
        }
        if(TRACE_RECEIVING.isFinestEnabled()) TRACE_RECEIVING.finest(
                "TRACE receiving first line with request:---\n"+requestLine+"---");
        
        //we have HTTP request - encapsulate it !
        
        try {
            //TODO remove Tokenizer
            StringTokenizer tokenizer=new StringTokenizer(requestLine," ");
            if(!tokenizer.hasMoreTokens()) {
                throw new HttpServerException("Wrong request: "+requestLine);
            }
            method      = tokenizer.nextToken();
            path        = tokenizer.nextToken();
            httpVersion = tokenizer.nextToken();
        } catch(Exception ex) {
            throw new HttpServerException("Malformed HTTP request: '"+requestLine+"'", ex);
        }
        
        keepalive  = "HTTP/1.1".equals(httpVersion);
        
        // CONSIDER refactor into readHeaders()
        headers = new Hashtable(); //CONSIDER reuse of hashtables!
        while(true) {
            String line= readLine(socketInputStream);
            if(TRACE_RECEIVING.isFinestEnabled()) {
                //System.err.println(line);
                TRACE_RECEIVING.finest(line);
            }
            if(line == null || "".equals(line)) {
                break;
            }
            int index = line.indexOf(':');
            if(index == -1) {  //TODO: handle multiline MIMEs???
                continue;
            }
            String name = line.substring(0, index).toLowerCase();
            int len = line.length();
            for(++index;index < len;++index) {
                if(line.charAt(index) != ' ')
                    break;
            }
            String value=line.substring(index);
            headers.put(name, value);
            if(name.equals("content-length")) {
                try {
                    length = Integer.parseInt(value);
                } catch(NumberFormatException ex) {
                    throw new HttpServerException(
                        "could not parse velue of content-length header of '"+line+"'");
                }
            }
            if(name.equals("content-type")) {
                contentType = value;
            }
            if(name.equals("connection")) {
                keepalive = value.indexOf ("keep-alive") > -1;
            }
        }
        
        // --- prepare input buffers for request
        
        //
        charset = XsulUtil.getContentTypeCharset(contentType);
        logger.finest("got charset="+charset+" from contentType="+contentType+" length="+length);
        
        //            Reader reader;
        //
        //            //if(length==0) length = 100;
        //            if(length > -1) {
        //                reader  = new InputStreamReader(
        //                    new FixedLengthInputStream(socketInputStream, length),
        //                    req.charset != null ? req.charset : DEFAULT_CHARSET);
        //            } else {
        //                reader  = new InputStreamReader(socketInputStream,
        //                                                req.charset != null ? req.charset : DEFAULT_CHARSET);
        //            }
        //            if(length < 0) {
        //                logger.warning("length="+length);
        //            }
        
        
        
        if(length > -1) {
            // wrap stream so request can not be read beyond content-length bytes!
            is  =  new FixedLengthInputStream(socketInputStream, length);
        } else {
            is = socketInputStream;
        }
        if(length < 0) {
            logger.warning("length="+length);
        }
        
        if("OST".equals(method)) {
            logger.warning(
                "processing call that required globus delegation but user did just POST!");
            method = "POST"; //little fixup :-)
        }
        
        
        //NOTE: this is expensive but sometimes necessary for debugging to see whole request!
        if(TRACE_RECEIVING.isFinestEnabled() && "POST".equals(method) )
        {
            //char[] cbuf = new char[length];
            //int toRead = length;
            //while(toRead > 0) {
            //  int received = reader.read(cbuf, length - toRead, toRead);
            //  toRead -= received;
            //}
            //StringWriter sw = new StringWriter(length > 0 ? length : 8*1024);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(length > 0 ? length : 8*1024);
            //char[] cbuf = new char[4*1024];
            byte[] buf = new byte[4*1024];
            //copy input stream inot byte array!
            while(true) {
                int received = is.read(buf);
                if(received < 0) {
                    break;
                }
                TRACE_RECEIVING.finest("recevied="+received+" '"+new String(buf, 0, received,  HttpConstants.ISO88591_CHARSET));
                if(received > 0) {
                    baos.write(buf, 0, received);
                }
            }
            byte[] requestBytes = baos.toByteArray();
            //used ISO8859-1 as full 8-bit charset for debugging!
            String requestBody = new String(requestBytes, HttpConstants.ISO88591_CHARSET);
            //System.err.println(
            //"TRACE: "+getClass()+" received request:---\n"+
            //TODO: escape all <32 and > 128 characters!
            TRACE_RECEIVING.finest("received request:---\n"
                                       +XsulUtil.printable(requestBody, false)
                                       +"---\n");
            //reader = new StringReader(requestBody);
            is = new ByteArrayInputStream(requestBytes);
        }
        
        //assert is != null;
        return true;
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




