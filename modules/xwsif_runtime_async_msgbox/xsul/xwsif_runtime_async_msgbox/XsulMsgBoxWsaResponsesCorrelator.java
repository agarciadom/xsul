/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulMsgBoxWsaResponsesCorrelator.java,v 1.2 2006/06/16 19:12:51 aslom Exp $
 */

package xsul.xwsif_runtime_async_msgbox;

import java.net.URI;
import java.util.Iterator;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.msg_box.MsgBoxClient;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.ws_addressing.WsAddressing;
import xsul.ws_addressing.WsaEndpointReference;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.ws_addressing.WsaRelatesTo;
import xsul.xwsif_runtime_async.WSIFAsyncResponseListener;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import xsul.xwsif_runtime_async.WSIFAsyncWsaResponsesCorrelatorBase;

/**
 * This is correlator that will set replyTo address of messages to message box
 * and retrieve messages from it to pass back to client.
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XsulMsgBoxWsaResponsesCorrelator extends WSIFAsyncWsaResponsesCorrelatorBase
    implements WSIFAsyncResponsesCorrelator, Runnable
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private String msgBoxServiceLoc;
    private MsgBoxClient msgBoxClient;
    WsaEndpointReference msgBoxAddr;
    private Thread messageBoxDonwloader;
    
    public XsulMsgBoxWsaResponsesCorrelator(String msgBoxServiceLoc)
        throws DynamicInfosetProcessorException
    {
        this.msgBoxServiceLoc = msgBoxServiceLoc;
        msgBoxClient = new MsgBoxClient(URI.create(msgBoxServiceLoc));
        msgBoxAddr = msgBoxClient.createMsgBox();
        setReplyTo(msgBoxAddr);
        messageBoxDonwloader = new Thread(this, Thread.currentThread().getName()+"-async-msgbox-correlator");
        messageBoxDonwloader.setDaemon(true);
        messageBoxDonwloader.start();
    }
    
//    public void setMsgBoxAddr(WsaEndpointReference msgBoxAddr) {
//      this.msgBoxAddr = msgBoxAddr;
//    }
    
    public WsaEndpointReference getMsgBoxAddr() {
        return msgBoxAddr;
    }
    
    public void run() {
        while(true) {
            try {
                XmlElement[] messages = msgBoxClient.takeMessages(msgBoxAddr);
                // now hard work: find callbacks
                for (int i = 0; i < messages.length; i++) {
                    XmlElement m = messages[i];
                    XmlDocument incomingXmlDoc;
                    if(m.getParent() != null) {
                        incomingXmlDoc = (XmlDocument) m.getParent();
                    } else {
                        incomingXmlDoc = builder.newDocument();
                        incomingXmlDoc.setDocumentElement(m);
                    }
                    if(logger.isFinestEnabled()) {
                        logger.finest("got message "+builder.serializeToString(m));
                    }
                    
                    WsaMessageInformationHeaders wsaHeaders = new WsaMessageInformationHeaders(m);
                    URI relatedMssageId = wsaHeaders.getRelatedRequestMessageId();
                    if(relatedMssageId != null) {
                        WSIFAsyncResponseListener callback = unregisterCallback(relatedMssageId);
                        if(callback != null) {
                            // NOTE: it is expected callback will do just notification (VERY FAST!!!)
                            if(logger.isFinestEnabled()) {
                                logger.finest("correlated "+relatedMssageId+" with "+callback);
                            }
                            callback.processAsyncResponse(incomingXmlDoc);
                        }
                    } else {
                        logger.info("dropped message "+builder.serializeToString(incomingXmlDoc));
                    }
                    
                }
                try {
                    Thread.currentThread().sleep(1000L); //do not overload msg box service ...
                } catch (InterruptedException e) {}
            } catch (XsulException e) {
                logger.info("could not retrieve messages", e);
            }
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

