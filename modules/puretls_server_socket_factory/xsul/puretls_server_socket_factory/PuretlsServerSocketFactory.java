/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: PuretlsServerSocketFactory.java,v 1.3 2005/07/13 16:47:59 aslom Exp $
 */

package xsul.puretls_server_socket_factory;


import COM.claymoresystems.ptls.SSLContext;
import COM.claymoresystems.ptls.SSLServerSocket;
import COM.claymoresystems.ptls.SSLSocket;
import COM.claymoresystems.sslg.SSLPolicyInt;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import xsul.MLogger;
import xsul.http_server.HttpServerException;
import xsul.http_server.ServerSocketFactory;
import xsul.http_server.plain_impl.PlainServerSocketFactory;
import xsul.http_server.plain_impl.TcpPortRange;

/**
 * Factory that uses SSL sockets provided by PureTLS.
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class PuretlsServerSocketFactory implements ServerSocketFactory {
    private final static MLogger logger = MLogger.getLogger();
        
    protected SSLContext ctx; // = new SSLContext();
    
    protected SSLServerSocket listenSocket;
    private static InetAddress myIp;
    protected int serverPort;
    
    public PuretlsServerSocketFactory(int serverPort,
                                      String rootfile,
                                      String keyfile,
                                      String password)
    {
        this(serverPort, rootfile, keyfile, password, false, false, null, null);
    }
    
    public PuretlsServerSocketFactory(int serverPort,
                                      String rootfile, // = "root.pem";
                                      String keyfile, // = "server.pem";
                                      String password, // = "password";
                                      boolean clientauth, // = false;
                                      //boolean renegotiate, // = false;
                                      boolean fakeseed, // = false;
                                      short[] cipherSuites, // = null
                                      String dhfile) //=null)
        throws HttpServerException
    {
        if(serverPort == -1) {
            serverPort = 443;
        }
        this.serverPort = serverPort;
        this.ctx = new SSLContext();
        if(fakeseed) {
            ctx.seedRNG(null);
        }
        try {
            ctx.loadRootCertificates(rootfile);
            ctx.loadEAYKeyFile(keyfile,password);
            if(dhfile != null) {
                ctx.loadDHParams(dhfile);
            }
            SSLPolicyInt policy=new SSLPolicyInt();
            
            if(cipherSuites != null) {
                policy.setCipherSuites(cipherSuites);
            }
            policy.requireClientAuth(clientauth);
            ctx.setPolicy(policy);
        } catch (IOException e) {
            throw new HttpServerException("could not crete PureTLS server socket context:"+e, e);
        }
        bindSocket(serverPort, ctx);
    }
    
    public PuretlsServerSocketFactory(int serverPort,
                                      SSLContext ctx)
        throws HttpServerException
    {
        bindSocket(serverPort, ctx);
    }
    
    public void bindSocket(int serverPort,
                           SSLContext ctx)
        throws HttpServerException
    {
        
        if(ctx == null) throw new IllegalArgumentException();
        this.ctx = ctx;
        try {
            TcpPortRange portRange = PlainServerSocketFactory.getPortRange();
            this.serverPort = serverPort;
            if(serverPort == 0 && portRange != null) {
                while(true) {
                    serverPort = portRange.getFreePort(serverPort);
                    try {
                        listenSocket = new SSLServerSocket(ctx, serverPort);
                        portRange.setUsed( serverPort );
                        break;
                    } catch(IOException e) {
                    }
                    ++serverPort;
                }
            } else {
                //listenSocket = new ServerSocket( serverPort );
                listenSocket = new SSLServerSocket(ctx, serverPort);
            }
        } catch(IOException ioe) {
            throw new HttpServerException(
                "could not create instance of server on port "+serverPort, ioe);
        }
        
        //        try {
        //          //ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
        //          //listenSocket = ssf.createServerSocket(9096);
        //          listenSocket = new SSLServerSocket(ctx, serverPort);
        //
        //        } catch(IOException ioe) {
        //          throw new HttpServerException(
        //              "could not create instance of server on port "+serverPort, ioe);
        //        }
        this.serverPort = listenSocket.getLocalPort();
    }
    
    
    //    public static ServerSocketFactory newInstance(int serverPort) throws HttpServerException {
    //
    //        return new PuretlsServerSocketFactory(serverPort);
    //    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    
    public Socket accept() throws IOException {
        return accept(null);
    }
    
    public Socket accept(Map connectionProps) throws IOException {
        SSLSocket socket = (SSLSocket)listenSocket.accept();
        return socket;
        //return listenSocket.accept();
    }
    
    public void shutdown() throws IOException {
        listenSocket.close();
        listenSocket = null;
    }
    
    public static synchronized InetAddress getMyIp() throws java.net.UnknownHostException {
        return PlainServerSocketFactory.getMyIp();
    }
    
    //    public static synchronized InetAddress getMyIp() throws java.net.UnknownHostException
    //    {
    //        if(myIp != null) {
    //            return myIp;
    //        }
    //        myIp = InetAddress.getLocalHost();
    //
    //        String overrideIp = null;
    //        try {
    //            overrideIp = (String) AccessController.doPrivileged(
    //                new PrivilegedAction() {
    //                    public Object run() {
    //                        return System.getProperty(HTTP_TRANSPORT_SERVER_HOST_IP_PROPERTY);
    //                    }
    //                });
    //        } catch(AccessControlException ace) {
    //
    //            logger.severe("could not read system property "+HTTP_TRANSPORT_SERVER_HOST_IP_PROPERTY , ace);
    //
    //        }
    //
    //
    //        if(overrideIp != null) {
    //            // parse into 4 bytes
    //            try {
    //                /*              byte b[] = new byte[4];
    //                 String s = overrideIp;
    //                 int i = s.indexOf('.');
    //                 String s0 = s.substring(0, i);
    //                 b[0] = (Byte) Integer.parseInt(s0);
    //                 int j = s.indexOf('.', i);
    //                 String s1 = s.substring(i, j);
    //                 b[1] = Byte.parseByte(s1);
    //                 i = j;
    //                 j = s.indexOf('.', i);
    //                 String s2 = s.substring(i, j);
    //                 b[2] = Byte.parseByte(s2);
    //                 String s3 = s.substring(j+1);
    //                 b[3] = Byte.parseByte(s3);
    //                 //myIp =  InetAddress.getByAddress(b); //only JDK 1.4
    //                 */
    //                myIp = InetAddress.getByName(overrideIp);
    //                logger.config(HTTP_TRANSPORT_SERVER_HOST_IP_PROPERTY+"="+myIp);
    //            } catch(Exception e) {
    //                logger.severe("user specified -D"+HTTP_TRANSPORT_SERVER_HOST_IP_PROPERTY+" is not IP address", e);
    //            }
    //        } else {
    //            logger.config("no "+HTTP_TRANSPORT_SERVER_HOST_IP_PROPERTY+" property");
    //        }
    //        return myIp;
    //    }
    
    public String getServerLocation() throws HttpServerException {
        try {
            //InetAddress addr = InetAddress.getLocalHost();
            //InetAddress addr = listenSocket.getInetAddress(); //wont work returns "0.0.0.0"
            InetAddress addr = getMyIp();
            return "https://" + addr.getHostAddress() + ':' + getServerPort();
        } catch(Exception ex) {
            throw new HttpServerException(
                "cant determine internet address of HTTP embedded web server", ex);
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


