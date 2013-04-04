/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: PuretlsClientSocketFactory.java,v 1.2 2005/07/13 16:47:59 aslom Exp $
 */

package xsul.puretls_client_socket_factory;

import COM.claymoresystems.ptls.SSLConn;
import COM.claymoresystems.ptls.SSLContext;
import COM.claymoresystems.ptls.SSLSocket;
import COM.claymoresystems.sslg.SSLPolicyInt;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import xsul.MLogger;
import xsul.http_client.ClientSocketFactory;
import xsul.http_client.HttpClientException;

/**
 * PureTLS based SSL client socket factory.
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class PuretlsClientSocketFactory implements ClientSocketFactory {
    private final static MLogger logger = MLogger.getLogger();
    
    //private static ClientSocketFactory instance = new PuretlsClientSocketFactory();
    protected SSLContext ctx;
    
    public PuretlsClientSocketFactory(SSLContext ctx)
        throws HttpClientException
    {
        if(ctx == null) throw new IllegalArgumentException();
        this.ctx = ctx;
    }
    
    public PuretlsClientSocketFactory(String rootfile, //="root.pem";
                                      String keyfile, //="client.pem";
                                      String randomfile, //="random.pem"; //will be created if not exist
                                      String password) //="password";
    {
        this(rootfile, keyfile, randomfile, password, false, null);
    }
    
    public PuretlsClientSocketFactory(String rootfile, //="root.pem";
                                      String keyfile, //="client.pem";
                                      String randomfile, //="random.pem"; //will be created if not exist
                                      String password, //="password";
                                      boolean acceptunverified, //=false;
                                      //boolean fakeseed, //=false;
                                      short[] cipherSuites) //=null;
        throws HttpClientException
    {
        ctx = new SSLContext();
        
        SSLPolicyInt policy = new SSLPolicyInt();
        
        if(cipherSuites != null) {
            policy.setCipherSuites(cipherSuites);
        }
        
        policy.acceptUnverifiableCertificates(acceptunverified);
        boolean negotiateTLS = true;
        policy.negotiateTLS(negotiateTLS);
        ctx.setPolicy(policy);
        
        try {
            
            ctx.loadRootCertificates(rootfile);
            
            ctx.loadEAYKeyFile(keyfile, password);
            
            if(randomfile != null) {
                ctx.useRandomnessFile(randomfile, password);
            } else {
                ctx.seedRNG(null);
            }
            
        } catch(Exception e){
            throw new HttpClientException("could not set PureTLS context for client sockets: "+e, e);
        }
    }
    
    /**
     * Open socket to host:port that can be used ot transfer SOAP/HTTP request over TLS.
     */
    public Socket connect(String host, int port, int connectionTimeout) throws HttpClientException {
        try {
            logger.finest("connect "+host+":"+port+" timeout:"+connectionTimeout);
            Socket socket = new Socket();
            //NOTE: JDK 1.4 required to support timeout ... but it is worth it!
            SocketAddress addr = new InetSocketAddress(host, port);
            socket.connect(addr, connectionTimeout);
            //socket.setSoTimeout(connectionTimeout);
            //SSLSocket secureSocket = new SSLSocket(ctx, host, port);
            SSLSocket secureSocket = new SSLSocket(ctx, socket, host, port, SSLConn.SSL_CLIENT);
            secureSocket.setSoTimeout(connectionTimeout);
            return secureSocket;
        } catch (Exception e) {
            throw new HttpClientException("could not open connection to "+host+":"+port, e);
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


