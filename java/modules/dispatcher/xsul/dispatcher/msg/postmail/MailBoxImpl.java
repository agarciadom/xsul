/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: MailBoxImpl.java,v 1.2 2004/09/23 12:00:05 adicosta Exp $
 */
package xsul.dispatcher.msg.postmail;

import java.net.URI;

import org.xmlpull.v1.builder.XmlElement;

import xsul.MLogger;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * <p>
 * Implementation of <code>MailBox</code>.
 * </p>
 * 
 * <p>
 * This implementation uses 2 <code>ConcurrentReaderHashMap</code> to stock
 * responses and waiting responses.
 * </p>
 * 
 * <p>
 * <b>Actualy, there is no data persitence and it is not an independant process
 * or thread from the Dispatcher. </b>
 * </p>
 * 
 * @see xsul.dispatcher.msg.postmail.MailBox
 * @see EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap
 * 
 * @author Alexandre di Costanzo
 *  
 */
public class MailBoxImpl implements MailBox {
    private final static MLogger logger = MLogger.getLogger();

    private ConcurrentReaderHashMap waitingResponses = null;

    private ConcurrentReaderHashMap mailBox = null;

    /**
     * Construst a new PO Mail Box
     */
    public MailBoxImpl() {
        this.waitingResponses = new ConcurrentReaderHashMap();
        this.mailBox = new ConcurrentReaderHashMap();
    }

    /**
     * @see xsul.dispatcher.msg.postmail.MailBox#putForWaitingResponse(java.lang.String,
     *      java.net.URI)
     */
    public void putForWaitingResponse(String logicalPath, URI replyTo) {
        Object[] elements = { replyTo };
        this.waitingResponses.put(logicalPath, elements);
        logger.finest("Waiting response for " + logicalPath
                + " succefuly added with value: " + replyTo);
    }

    /**
     * @see xsul.dispatcher.msg.postmail.MailBox#isWaitingResponse(java.lang.String)
     */
    public boolean isWaitingResponse(String path) {
        return this.waitingResponses.containsKey(path);
    }

    /**
     * @see xsul.dispatcher.msg.postmail.MailBox#putResponse(java.lang.String,
     *      org.xmlpull.v1.builder.XmlElement)
     */
    public void putResponse(String logicalPath, XmlElement el) {
        // Modify message ID
        Object[] oldInformations = (Object[]) this.waitingResponses
                .remove(logicalPath);

        // Put new message with old id like key
        this.mailBox.put(logicalPath, el);
        logger.finest("Message added in PO Mailbox with key: " + logicalPath);
    }

    /**
     * @see xsul.dispatcher.msg.postmail.MailBox#getReplyToOf(java.lang.String)
     */
    public URI getReplyToOf(String logicalPath) {
        return (URI) ((Object[]) this.waitingResponses.get(logicalPath))[0];
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