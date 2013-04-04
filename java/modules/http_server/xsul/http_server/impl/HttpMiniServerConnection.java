/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpMiniServerConnection.java,v 1.13 2006/04/18 18:03:46 aslom Exp $
 */

package xsul.http_server.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import xsul.MLogger;
import xsul.http_common.HttpConstants;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.util.XsulUtil;

/**
 * Encapsulate one connection ot HTTP server.
 * One connection may be used to process multiple HTTP requests when Keep-Alive is used.
 *
 * @version $Revision: 1.13 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class HttpMiniServerConnection implements Runnable {
    private final static MLogger logger = MLogger.getLogger();
    final static MLogger TRACE_SENDING = MLogger.getLogger("trace.xsul.http.server.out");
    final static MLogger TRACE_RECEIVING = MLogger.getLogger("trace.xsul.http.server.in");
    //    private final static boolean TRACE_SENDING =
    //        SoapServices.TRACE_DISPATCH && SoapServices.TRACE_SENDING;
    //    private final static boolean TRACE_RECEIVING =
    //        SoapServices.TRACE_DISPATCH && SoapServices.TRACE_RECEIVING;
    
    
    private Map connectionProps; // NOTE: this is per connection!
    private Socket socket;
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;
    private HttpMiniServlet servlet;
    private boolean suppressSendingStackTraces;
    private boolean disableKeepAlive;
    
    
    public HttpMiniServerConnection() {
    }
    
    public void setDisableKeepAlive(boolean disableKeepAlive) {
        this.disableKeepAlive = disableKeepAlive;
    }
    
    public boolean isDisableKeepAlive() {
        return disableKeepAlive;
    }
    
    public void suppressSendingStackTraces(boolean isSuppressedSendingStackTraces) {
        this.suppressSendingStackTraces = isSuppressedSendingStackTraces;
    }
    
    public void useServlet(HttpMiniServlet servlet) {
        this.servlet = servlet;
    }
    
    public void setConnectionProps(Map connectioProps_) {
        this.connectionProps = connectioProps_;
    }
    
    public void setSocket(Socket s) {
        this.socket = s;
        try {
            socket.setTcpNoDelay(true);
            socket.setSoLinger(false, 0);
        } catch (SocketException e) {
            throw new HttpServerException(
                "TCP no delay (disabling Nagle algorithm ) support is good for performance");
        }
    }
    
    // simple automaton to process incoming requests
    public void run() {
        try {
            //ConnectionContext cctx = Services.getConnectionContext();
            //cctx.setIncomingSocket(socket);
            //cctx.setIncomingProps(connectionProps);
            //logger.finest("cctx="+cctx+" socket="+socket+" connectionProps="+connectionProps);
            
            
            
            //NOTE: buffering is *critical* for performance
            socketInputStream = new BufferedInputStream( socket.getInputStream() );
            socketOutputStream = new BufferedOutputStream( socket.getOutputStream() );
            //process(cctx);
            process();
            //        } catch(ServerException ex) {
            //            //ex.printStackTrace(System.err);
            //            logger.warning("server exception in HTTP connection, sending HTTP error ", ex);
            //            error(ex.toString(), ex); //500
        } catch(IOException ex) {
            // this can be safely dropped
            //- no much point in sending back HTTP error code!!!
            logger.warning("IO exception in HTTP connection, closing connection ", ex);
        } catch(RuntimeException ex) {
            //ex.printStackTrace(System.err);
            logger.warning("exception in HTTP connection, sending HTTP error "+
                               Thread.currentThread().getName(), ex);
            error("Runtime exception when processing request "+Thread.currentThread().getName(), ex);
        } catch(Throwable ex) {
            //ex.printStackTrace(System.err);
            logger.severe("unexpected exception in HTTP connection, sending HTTP error "+
                              Thread.currentThread().getName(), ex);
            error("Unexpected error when processing request "+Thread.currentThread().getName(), ex);
        } finally {
            try {
                if(socket != null) {
                    logger.finest("closing server "+socket);
                    socket.close();
                }
            } catch(IOException ex) {
                // CONSIDER additional tracing - but this is very common for close.socket()
                //} catch(NullPointerException ex) {
                // somehow happens in PureTLS not on Windows but Solaris...
                //java.lang.NullPointerException
                //        at COM.claymoresystems.ptls.SSLConn.getInStream(SSLConn.java:315)
                //        at COM.claymoresystems.ptls.SSLConn.recvClose(SSLConn.java:352)
                //        at COM.claymoresystems.ptls.SSLConn.close(SSLConn.java:368)
                //        at COM.claymoresystems.ptls.SSLSocket.close(SSLSocket.java:263)
                //        at xsul.http_server.impl.HttpMiniServerConnection.run(HttpMiniServerConnection.java:117)
                //        at java.lang.Thread.run(Thread.java:534)
                //
                
            }
        }
        logger.finest("connection thread finished");
    }
    
    //private void process(ConnectionContext cctx) throws IOException, ServerException {
    private void process() throws IOException, Exception {
        logger.entering();
        boolean keepalive = true;
        
        //NOTE: request/response objects are reused during lifeteime of conneciton
        HttpServerRequestImpl req = new HttpServerRequestImpl(socketInputStream,
                                                              socket.getInetAddress(),
                                                              socket.getPort());
        HttpServerResponseImpl res = new HttpServerResponseImpl(socketOutputStream);
        
        while(keepalive) {
            logger.finest("waiting for next request");
            
            // --- prepare output buffers
            if( req.readRequest() == false) {
                break;
            }
            keepalive = req.isKeepAlive();
            //  -- do processing
            if(disableKeepAlive) {
                keepalive = false;
            }
            
            res.reset();
            
            logger.finest("dispatching HTTP method '"+req.getMethod()+"' to servlet "+servlet);
            servlet.service(req, res);
            
            //TODO: check that "GET" should not have body (no InputStream?)
            
            
            //            if(req.method.equals("GET")) {
            //                doGet(reader, writer, bag, true);
            //            } else if(req.method.equals("HEAD") ) {
            //                doGet(reader, writer, bag, false);
            //            } else if("POST".equals(req.method) || "OST".equals(req.method)  ) {
            //                if(length > 0) {
            //                    errCode = doPost(reader, writer, bag, length);
            //                } else {
            //                    logger.warning(
            //                        "TRACE: skipping POST request, problem with length="+length);
            //                }
            //            } else {
            //                throw new HttpServerException("Unsupported method "+req.method);
            //            }
            
            //TODO drain input
            
            // --- send response
            //writer.flush();
            //writer.close();
            
            //          String contentType = (req.getMethod().equals("POST") ? "text/xml" : "text/html");
            //
            //          if(req.charset != null) {
            //              contentType += "; charset=\""+req.charset+"\"";
            //          } else {
            //              contentType += "; charset=\""+DEFAULT_CHARSET+"\"";
            //          }
            boolean sendBody = req.getMethod().equals("HEAD") == false;
            res.drainPipe(sendBody, keepalive);
            
        }
        logger.exiting();
    }
    
    
    //TODO: send nice HTTP errors
    private void error(String msg, Throwable thrw)
    {
        //System.err.println(getClass().getName()+" error: "+msg);
        //thrw.printStackTrace();
        logger.severe("error "+msg, thrw);
        String title = msg;
        if(thrw != null && !suppressSendingStackTraces) {
            title += ": "+thrw.getMessage();
        }
        StringBuffer body = new StringBuffer();
        body.append("<html>\n<head>\n<title>"+XsulUtil.escapeXml(title)+"</title>\n</head>\n<body>\n");
        body.append("<h1>"+XsulUtil.escapeXml(title)+"</h1>\n");
        if(thrw != null && !suppressSendingStackTraces) {
            StringWriter sw = new StringWriter();
            thrw.printStackTrace(new PrintWriter(sw));
            String trace = sw.toString();
            body.append("<p><pre>"+XsulUtil.escapeXml(trace, false, false)+"</pre></p>\n");
        }
        body.append("</body>\n</html>");
        try {
            byte[] bodyBytes = body.toString().getBytes("utf-8");
            int len = bodyBytes.length;
            String headers = HttpServerResponseImpl.getHeaders("500", msg, "text/html", len, false);
            byte[] headersBytes = headers.getBytes("utf-8");
            if(TRACE_SENDING.isFinestEnabled())
            {
                //NOITE: ISO-8859-1 charset used to show actual bytes even for UTF-8
                String responseAsString =
                    new String(headersBytes,HttpConstants.ISO88591_CHARSET)
                    +new String(bodyBytes,HttpConstants.ISO88591_CHARSET);
                TRACE_SENDING.finest("TRACE: sending error response:"
                                         +"---\n"
                                         +XsulUtil.printable(responseAsString, false)
                                         +"---\n");
            }
            socketOutputStream.write(headersBytes);
            socketOutputStream.write(bodyBytes);
            socketOutputStream.flush();
        } catch(IOException ex) {
            logger.warning("exception in sending HTTP error "+msg, ex);
        } //UnsupportedEncodingException
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





