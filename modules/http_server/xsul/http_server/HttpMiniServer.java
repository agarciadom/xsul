/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/

/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpMiniServer.java,v 1.13 2005/08/12 10:14:13 aslom Exp $
 */

package xsul.http_server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import xsul.MLogger;
import xsul.http_server.impl.HttpMiniServerConnection;
import xsul.http_server.plain_impl.PlainServerSocketFactory;

//TODO: use thread pool

/**
 * Very simple embeddable web server that is hosting web services.
 *
 * @version $Revision: 1.13 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class HttpMiniServer {
    private static int DEFAULT_TIMEOUT = 4 * 60 * 1000; // in [ms]
    private static boolean DEFAULT_DISABLE_KA = false;
    public final static String HTTP_TRANSPORT_SERVER_TIMEOUT_MS_PROPERTY =
        "http_transport.server.timeout.ms";
    public final static String HTTP_TRANSPORT_SERVER_DISABLE_KEEPALIVE_PROPERTY =
        "http_transport.server.disable.ka";
    private int soTimeout;
    private static MLogger logger = MLogger.getLogger();
    
    protected static int connectionNo;
    
    //protected SoapDispatcher dsptr = new HttpSocketSoapDispatcher(this);
    protected Thread listenThread ;
    protected HttpMiniServerMainLoop listenLoop;
    //private int port; // = 7777;
    //private String propertiesName = "soaprmi.properties";
    protected boolean running;
    protected boolean shutdown;
    
    protected ServerSocketFactory socketFactory;
    
    private HttpMiniServlet servlet;
    
    private boolean isDaemon;
    private boolean suppressSendingStackTraces;
    private boolean disableKeepAlive;
    
    static {
        
        String disableKaVal = null;
        try {
            disableKaVal = (String) AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return System.getProperty(
                                HttpMiniServer.HTTP_TRANSPORT_SERVER_DISABLE_KEEPALIVE_PROPERTY);
                        }
                    });
        } catch(AccessControlException ace) {
            logger.severe(
                "could not read system property "+HTTP_TRANSPORT_SERVER_DISABLE_KEEPALIVE_PROPERTY, ace);
        }
        if(disableKaVal != null) {
            DEFAULT_DISABLE_KA = true;
            logger.config("by default Keep-Alive will be disabled ("
                              +HTTP_TRANSPORT_SERVER_DISABLE_KEEPALIVE_PROPERTY+")");
        }
        
        String timeoutVal = null;
        try {
            timeoutVal = (String) AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return System.getProperty(HTTP_TRANSPORT_SERVER_TIMEOUT_MS_PROPERTY);
                        }
                    });
        } catch(AccessControlException ace) {
            logger.severe(
                "could not read system property "+HTTP_TRANSPORT_SERVER_TIMEOUT_MS_PROPERTY, ace);
        }
        if(timeoutVal != null) {
            try {
                DEFAULT_TIMEOUT = Integer.parseInt(timeoutVal);
                logger.config(HTTP_TRANSPORT_SERVER_TIMEOUT_MS_PROPERTY+"="+DEFAULT_TIMEOUT);
            } catch(Exception e) {
                logger.severe(
                    "user specified -D"+HTTP_TRANSPORT_SERVER_TIMEOUT_MS_PROPERTY
                        +" is not integer value", e);
            }
        }
    }
    
    
    /**
     * Creates mini server listening on default port for transport (ex. 80 for HTTP).
     * <br /> Note: only one server can be started to listen on one port.
     */
    public HttpMiniServer() throws HttpServerException {
        this(-1);
    }
    
    /**
     * Creates mini server listening on given port
     * <br /> Note: 0 means to use first available port (provided by operating system)
     * <br /> Note: -1 means to use default port for given transport (ex. 80 for HTTP)
     */
    public HttpMiniServer(int port) throws HttpServerException {
        this(PlainServerSocketFactory.newInstance(port));
    }
    
    public HttpMiniServer(ServerSocketFactory serverSocketFactory) throws HttpServerException
    {
        this.socketFactory = serverSocketFactory;
        // thread is started immediately to avoid JVM exiting when no running threads is around...
        //startServerNoChecks();
        setSoTimeout(DEFAULT_TIMEOUT);
        disableKeepAlive = DEFAULT_DISABLE_KA;
    }
    
    public void setSoTimeout(int socketTimeout)  throws HttpServerException {
        this.soTimeout = socketTimeout;
        //this.socketFactory.setSocketTimeout(socketTimeout);
    }
    
    public int getSoTimeout() {
        return soTimeout;
    }
    
    public void setDisableKeepAlive(boolean disableKeepAlive) {
        this.disableKeepAlive = disableKeepAlive;
    }
    
    public boolean isDisableKeepAlive() {
        return disableKeepAlive;
    } //default is false

    public void setDaemon(boolean isDaemon) {
        this.isDaemon = isDaemon;
    }
    
    public boolean isDaemon() {
        return isDaemon;
    }
    
    public void suppressSendingStackTraces(boolean suppressSendingStackTraces) {
        this.suppressSendingStackTraces = suppressSendingStackTraces;
    }
    
    public boolean isSuppressedSendingStackTraces() {
        return suppressSendingStackTraces;
    }
    
    
    public static HttpMiniServer newInstance(ServerSocketFactory socketFactory) {
        return new HttpMiniServer(socketFactory);
    }
    
    public int getServerPort() {
        return socketFactory.getServerPort();
    }
    
    public String getLocation() throws HttpServerException {
        return socketFactory.getServerLocation();
    }
    
    public void useServlet(HttpMiniServlet servlet) {
        this.servlet = servlet;
    }
    
    private void fireConnection(Socket socket, Map connectionProps) {
        //CONSIDER: pool threads and scalability when contending for pool resources
        HttpMiniServerConnection wsc = new HttpMiniServerConnection();
        wsc.setConnectionProps(connectionProps);
        wsc.setSocket(socket);
        wsc.suppressSendingStackTraces(isSuppressedSendingStackTraces());
        wsc.useServlet(servlet);
        wsc.setDisableKeepAlive(disableKeepAlive);
        //String name = getClass().getName();
        //name = name.substring(name.lastIndexOf('.') + 1);
        //System.err.println("creating new thread "+connectionNo);
        Thread connectionThread = new Thread(
            wsc, "connection"+(++connectionNo)+"on"+getServerPort());
        //so when conenciton is waiting keep-alive it is not blocking JVM from exit!
        connectionThread.setDaemon(true);
        connectionThread.start();
    }
    
    // -- more internal functions
    
    
    public void startServer() throws HttpServerException {
        if(shutdown) {
            throw new HttpServerException("already shutdown server can not started");
        }
        if(servlet == null) {
            throw new HttpServerException("before starting servlet must be passed to server");
        }
        startServerNoChecks();
    }
    
    private void startServerNoChecks() {
        //String name = getClass().getName();
        //name = name.substring(name.lastIndexOf('.') + 1);
        logger.config("starts listening on "+ getServerPort()+" :-)");
        if(listenThread == null) {
            //listenThread = new Thread(this, "listen"+getServerPort());
            listenLoop = new HttpMiniServerMainLoop();
            listenThread = new Thread(listenLoop, "listen"+getServerPort());
            if(isDaemon) {
                listenThread.setDaemon(isDaemon); //if thread is daemon it will not stop JVM from exit
            }
            listenThread.start();
        }
        running = true;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void stopServer() throws HttpServerException {
        if(shutdown) {
            throw new HttpServerException("already shutdown server can not stopped");
        }
        if(running) {
            running = false;
            if(listenThread != null) {
                listenThread.interrupt();
            }
            logger.config("stopping server on "+ getServerPort()+" :-(");
        }
    }
    
    public void shutdownServer() throws HttpServerException {
        try {
            stopServer();
            Thread.sleep(100); // give some time to server thread to notice shutdown request ....
        } catch(Exception e) {
        }
        if(listenThread != null) {
            try {
                socketFactory.shutdown();
            } catch(IOException ioe) {
                logger.config("socket shutdown", ioe);
            }
        }
        shutdown = true;
        logger.config("server shutdown is completed");
    }
    
    protected class HttpMiniServerMainLoop implements Runnable {
        
        public void run() {
            logger.entering();
            while(!shutdown) {
                try {
                    while(running) {
                        Map connectionProps = new HashMap();
                        Socket socket = null;
                        try {
                            try {
                                // properties will be populated by socket facotry implementation
                                socket = socketFactory.accept(connectionProps);
                            } catch(SocketException ex)  {
                                //NOTE handle when throws: "java.net.SocketException: socket closed"
                                if(running) {
                                    throw ex;
                                } else {
                                    break;
                                }
                            }
                            if(logger.isFinestEnabled()) {
                                logger.finest("received connection "+socket);
                            }
                            socket.setSoTimeout(soTimeout);
                            
                            // start proceshsing client data from socket
                            try {
                                //                            if(Log.ON) logger.fine(
                                //                                    "firing new connection socket="+socket
                                //                                        +" connectionProps="+connectionProps);
                                fireConnection(socket, connectionProps);
                            }catch(Exception ex) {
                                logger.fine("exception in processing connection", ex);
                            }
                            
                        } catch(InterruptedIOException se) {
                            //this is fine to ignore this exception here - in this case stopServer()
                            //may have requested stopping but this thread is still running
                            //and is blocked on socket.accept()
                            //so stopServer() calls listenThread.interrupt() that results
                            //in InterruptedIOException that leads to checking of
                            //loop condition in while(running) and as running==false
                            // this thread will cleanly exit :-)
                            
                            //of course the other possibility is that accept() timed out
                            //then running == true and we will just be back to accept() ...
                            
                            // but just to be sure we will close socket to notify client
                            if(socket != null) { try {socket.close();} catch(Exception ex){} }
                            
                        } catch(IOException se) {
                            logger.fine("exception in accepting socket connection", se);
                            // when SSL used it simportant to notify client that connection failed
                            if(socket != null) { try {socket.close();} catch(Exception ex){} }
                            //                    }catch(SocketException se) {
                            //                        l.fine("exception in accepting connection", se);
                            //                        if(s != null) { s.close();}
                            //continue LOOP;
                            //throw se;
                        }
                        
                    }
//                    //UNRESOLVED: seems to be required on Solaris or it goes into some kind
//                    //  of race condition and throws socket timeout after 200 seconds ...
//                    //Remains unresolved due to Heisenberg principle applied ot use of debugger :-)
//                    //  (using local or remote debugging over JDWP behavior could not be reproduced ...)
//                    //NOTE: it is doen outside main loop and only matter for switching on/off running
//                    try {
//                        Thread.currentThread().sleep(10 * 1);
//                    } catch(InterruptedException ie) {
//                    }
                    
                    // eliminate even remote possibility that this thread will be stopped accidentally..
                } catch(Exception ex) {
                    //ex.printStackTrace();  //good for now....
                    logger.severe("exception in embedded web server", ex);
                } finally {
                }
            }
            logger.exiting();
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




