/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpClientReuseLastConnectionManager.java,v 1.10 2006/02/07 18:11:09 aslom Exp $
 */

package xsul.http_client;

import xsul.MLogger;

/**
 * Manages connection.
 * <br />NOTE: this class is multi-thread safe however for good performance is shoul d
 * used from only one thread that needs to conenct multiple time to the smae host:port
 * as this manager will reuse such connection.
 *
 * @version $Revision: 1.10 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class HttpClientReuseLastConnectionManager extends HttpClientConnectionManager {
    private static final MLogger logger = MLogger.getLogger();
    private final int REUSE_TIME_WINDOWS_IN_MS = 3 * 60 * 1000;
    //protected boolean reuseLastConenction;
    protected ClientSocketConnection lastConn;
    protected long lastConnTimestamp;
    protected Object lock = new Object();
    
    protected HttpClientReuseLastConnectionManager()
    {
    }
    
    protected HttpClientReuseLastConnectionManager(ClientSocketFactory socketFactory)
    {
        super(socketFactory);
    }
    
    public static HttpClientConnectionManager newInstance() {
        return new HttpClientReuseLastConnectionManager();
    }
    
    public static HttpClientConnectionManager newInstance(ClientSocketFactory socketFactory) {
        return new HttpClientReuseLastConnectionManager(socketFactory);
    }
    
    /**
     * As defiend in Socket a timeout of zero is interpreted as an infinite timeout
     */
    public HttpClientRequest connect(String host, int port, int timeout)
        throws HttpClientException
    {
        if(host == null) throw new IllegalArgumentException();
        ClientSocketConnection conn = null;
        ClientSocketConnection oldConn = null;
        synchronized(lock) {
            if(lastConn != null) {
                oldConn = lastConn;
                if(lastConn.getHost().equals(host) && lastConn.getPort() == port) {
                    if(lastConnTimestamp > 0
                           && (System.currentTimeMillis() - lastConnTimestamp) < REUSE_TIME_WINDOWS_IN_MS)
                    {
                        conn = lastConn;
                        oldConn = null;
                    }
                }
                lastConn = null;
                lastConnTimestamp = -1;
            }
        }
        if(oldConn != null) {
            try {
                oldConn.close();
            } catch(Exception e) {
            }
        }
        if(conn != null) {
            if(conn.getTimeout() != timeout) {
                conn.setTimeout(timeout);
            }
            logger.finest("reusing "+conn);
        }
        if(conn == null) {
            //            Socket socket = socketFactory.connect(host, port, timeout);
            //            try {
            //                socket.setTcpNoDelay(true);
            //                socket.setSoLinger(false, 0);
            //            } catch (SocketException e) {
            //                throw new HttpClientException(
            //                    "TCP no delay (disabling Nagle algorithm ) support is good for performance");
            //            }
            //            conn = new ClientSocketConnection(host, port, timeout, socket);
            //            logger.finest("using new connecttion conn="+conn);
            return super.connect(host, port, timeout);
            
            
        }
        return new HttpClientRequest(conn, this);
        
    }
    
    public void notifyConnectionForReuse(ClientSocketConnection conn) throws HttpClientException
    {
        ClientSocketConnection oldConn;
        synchronized(lock) {
            logger.finest("may reuse "+conn);
            oldConn = lastConn;
            lastConn = conn;
            lastConnTimestamp = System.currentTimeMillis();
        }
        // forceful closing of old socket
        if(oldConn != null) {
            oldConn.close();
        }
    }
    
    public void shutdownAndReclaimResources() throws HttpClientException {
        ClientSocketConnection oldConn;
        synchronized(lock) {
            oldConn = lastConn;
            lastConn = null;
        }
        // forceful closing of old socket
        if(oldConn != null) {
            logger.finest("requested connection closing "+oldConn);
            oldConn.close();
        }
    }
    
    //    public void setReuseLastConenction(boolean enable) {
    //        this.reuseLastConenction = enable;
    //    }
    
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




