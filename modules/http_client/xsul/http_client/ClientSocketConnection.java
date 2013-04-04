/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: ClientSocketConnection.java,v 1.5 2004/08/06 05:48:58 aslom Exp $
 */

package xsul.http_client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import xsul.MLogger;

/**
 * Represents client side socket connection. This class should not be used directly.
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class ClientSocketConnection extends HttpClientConnectionManager {
    private static final MLogger logger = MLogger.getLogger();
    private final static int BUF_SIZE = 4*1024;
    //protected boolean reuseLastConenction;
    private Socket socket;
    private String host;
    private int port;
    private int timeout = -1;
    private InputStream inputStream;
    private OutputStream outputStream;
    
    ClientSocketConnection(String host, int port, int timeout, Socket socket)
        throws HttpClientException
    {
        this.host = host;
        this.port = port;
        this.socket = socket;
        
        setTimeout(timeout);
        
        try {
            outputStream = new BufferedOutputStream(socket.getOutputStream(), BUF_SIZE);
        } catch (IOException e) {
            throw new HttpClientException("could not get output stream from socket", e);
        }
        try {
            inputStream = new BufferedInputStream(socket.getInputStream(), BUF_SIZE);
        } catch (IOException e) {
            throw new HttpClientException("could not get input stream from socket", e);
        }
    }
    
    /**
     * Forceful close of socket and associated streams
     */
    public void close() {
        try {
            if(outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
        }
        try {
            if(inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
        }
        try {
            if(socket != null) {
                //System.out.println("closing socket="+socket);
                logger.finest(""+socket);
                socket.close();
            }
        } catch (IOException e) {
        }
    }
    
    public void setTimeout(int timeout) throws HttpClientException {
        this.timeout = timeout;
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            throw new HttpClientException("could not set socket timeout to "+timeout+": "+e, e);
        }
        
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    
    public Socket getSocket() {
        return socket;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }
    
    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String toString() {
        String name = getClass().getName();
        int pos;
        if((pos = name.lastIndexOf('.')) != -1) {
            name = name.substring(pos+1);
        }
        return name+"{socket="+socket+" host="+host+" port="+port+" timeout="+timeout;
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



