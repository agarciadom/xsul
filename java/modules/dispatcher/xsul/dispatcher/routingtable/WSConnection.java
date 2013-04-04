/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WSConnection.java,v 1.2 2004/09/07 21:00:05 adicosta Exp $
 */
package xsul.dispatcher.routingtable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import xsul.MLogger;

/**
 * @author Alexandre di Costanzo
 *  
 */
public abstract class WSConnection {

    private final static MLogger logger = MLogger.getLogger();

    /**
     * <p>
     * Forwards a request from a client to the Web Service and the response.
     * </p>
     * <p>
     * Structure of the parameter <code>request</code>
     * <ul>
     * <li><code>request[0]</code>: the HttpServerRequest from client</li>
     * <li><code>request[1]</code>: the HttpServerResponse from client</li>
     * <li><code>request[2]</code>: the arguments from client's http request
     * </li>
     * </ul>
     * 
     * @param request
     *            the request, response and arguments.
     */
    public abstract void forwards(Object[] request);

    /**
     * Copy the content of an <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * 
     * @param in
     *            the <code>InputStream</code> to read.
     * @param out
     *            the <code>OutputStream</code> to write.
     */
    protected void copy(InputStream in, OutputStream out) throws IOException {
        logger.entering(new Object[] { in, out });
        byte[] buffer = new byte[1024];
        int lastRead;
        do {
            lastRead = in.read(buffer);
            logger.finest("got " + lastRead + " bytes");
            if (lastRead != -1) {
                out.write(buffer, 0, lastRead);
            } else {
                break;
            }
        } while (lastRead != -1);
        in.close();
        out.close();
        logger.exiting();
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2002-2004 The Trustees of Indiana University. All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */