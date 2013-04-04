/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoPeer.java,v 1.14 2004/12/08 23:53:11 aslom Exp $
 */
package xsul_ping_stat.msg;

import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;

import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;

import xsul.msg_box.MsgBoxClient;

import xsul.processor.DynamicInfosetProcessorException;

import xsul.soap.SoapUtil;

import xsul.soap11_util.Soap11Util;

import xsul.soap12_util.Soap12Util;

import xsul.ws_addressing.WsaEndpointReference;
import xsul.ws_addressing.WsaInvoker;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.ws_addressing.WsaRelatesTo;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Vector;

// -port 3001 -target http://localhost:3002
// -port 3001 -target http://localhost:3002 -send 10

/**
 * Simpel peer-to-peer communication using WSA.
 */
public class EchoPeer implements Runnable {
    private final static MLogger logger = MLogger.getLogger();

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    private final static String PROPERTY_SERIALIZER_INDENTATION = "http://xmlpull.org/v1/doc/properties.html#serializer-indentation";

    private static WsaEndpointReference nodeEpr;

    private static class Args {
        private String target = "http://localhost:3001"; // get the location of

        // the service
        private long delay = 500; // set a timer to stop

        private int count = 2;

        private int port = 0;
    }

    private Args args = null;

    private EchoStat statTool = null;

    private int totalSentPackets = 0;

    private int lostPackets = 0;

    private int notSentPackets = 0;

    private long maxRoundTrip = 0;

    private long minRoundTrip = -1;

    private double average = 0;

    private Vector uuidSent = new Vector();

    private SoapHttpProcessor_Stat customizedProcessor;

    private Vector allTimes = new Vector();

    private String msg_box;

    private MsgBoxClient msgBoxClient;

    private WsaEndpointReference msgBoxId;

    private long takeMsgBoxTime = 0;

    private int lostMessages = 0;

    private int getMessages;

    private WsaInvoker wsaInvoker;
    
    public EchoPeer(String target, long delay, int port, String msg_box,
            EchoStat statTool) {
        this.args = new Args();
        this.args.target = target;
        this.args.delay = delay;
        this.args.port = port;
        this.msg_box = msg_box;
        this.statTool = statTool;
        wsaInvoker= new WsaInvoker();
        //wsaInvoker.setIndent("   ");
        wsaInvoker.setUseHttpKeepAlive(false);

    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // Msg Box creation
        if (this.msg_box != null) {
            try {
                this.msgBoxClient = new MsgBoxClient(new URI(this.msg_box));
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }
            msgBoxId = this.msgBoxClient.createMsgBox();
        }

        final String location;
        this.customizedProcessor = new SoapHttpProcessor_Stat() {
            public XmlElement processMessage(XmlElement message)
                    throws DynamicInfosetProcessorException {
                throw new DynamicInfosetProcessorException("not used");
            }

            public XmlDocument processSoapEnvelope(XmlElement envelope,
                    SoapUtil soapFragrance) {
                logger.finest("received envelope:\n"
                        + builder.serializeToString(envelope));
                this.responsePackets++;
                // this XML string could be convertedto DOM or whatever API one
                // preferes (like JDOM, DOM4J, ...)
                final WsaMessageInformationHeaders wsah = new WsaMessageInformationHeaders(
                        envelope);

                final XmlElement message = soapFragrance
                        .requiredBodyContent(envelope);

                // NOTE: retuning null as response means no response - async
                // messaging
                return null;
            }
        };
        customizedProcessor.setSupportedSoapFragrances(new SoapUtil[] {
                Soap12Util.getInstance(), Soap11Util.getInstance() });
        customizedProcessor.setServerPort(this.args.port);
        try {
            customizedProcessor.start();
        } catch (DynamicInfosetProcessorException e1) {
            this.notSentPackets++;
        } catch (IOException e1) {
            this.notSentPackets++;
        }

        location = customizedProcessor.getServer().getLocation();
        try {
            if (this.msgBoxId != null) {
                setMyNodeEpr(this.msgBoxId);
            } else {
                setMyNodeEpr(new WsaEndpointReference(new URI(location)));
            }
        } catch (URISyntaxException e2) {
            this.notSentPackets++;
        }

        WsaEndpointReference targetEpr;

        long startTime = System.currentTimeMillis();
        long estimateEnd = startTime + this.args.delay;

        while (System.currentTimeMillis() < estimateEnd) {
            // Sending message
            try {
                targetEpr = new WsaEndpointReference(new URI(this.args.target));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            XmlElement message = builder.newFragment("foo");
            message.addChild(Integer.toString(this.args.count));
            try {
                long sendStart = 0;
                long sendEnd = 0;
                sendStart = System.currentTimeMillis();
                sendTestMessage(targetEpr, getMyNodeEpr(), null, message);
                sendEnd = System.currentTimeMillis();
                this.allTimes.add(new Long(sendEnd - sendStart));
                this.totalSentPackets++;
            } catch (XsulException e) {
                this.notSentPackets++;
            } catch (XmlBuilderException e) {
                this.notSentPackets++;
            } catch (URISyntaxException e) {
                this.notSentPackets++;
            }
        }

        // Stop listen response !!
        customizedProcessor.stop();

        // Compute stat
        long totaltime = 0;
        for (int i = 0; i < this.allTimes.size(); i++) {
            long currentTime = ((Long) this.allTimes.get(i)).longValue();
            if ((this.minRoundTrip == -1) || (this.minRoundTrip > currentTime)) {
                this.minRoundTrip = currentTime;
            }
            if (currentTime > this.maxRoundTrip) {
                this.maxRoundTrip = currentTime;
            }
            totaltime += currentTime;
        }
        if (this.allTimes.size() != 0) {
            this.average = totaltime / this.allTimes.size();
        }

        // Get Msg from Box
        if (this.msgBoxClient != null) {
            long start = System.currentTimeMillis();
            int count = 0;
            while(true) {
                XmlElement[] messages = this.msgBoxClient.takeMessages(this.msgBoxId);
                if(messages == null || messages.length == 0) {
                    break;
                }
                count += messages.length;
            }
            long end = System.currentTimeMillis();
            this.takeMsgBoxTime = end - start;
            this.getMessages = count;
            if (count != this.totalSentPackets) {
                this.lostMessages = this.totalSentPackets - count;
            }
        }

        // Destroy Msg Box
        if (this.msgBoxClient != null) {
            this.msgBoxClient.destroyMsgBox(this.msgBoxId);
        }

        // End
        synchronized (this.statTool.FINISHED_THREADS) {
            int sum = this.statTool.FINISHED_THREADS.intValue() + 1;
            this.statTool.FINISHED_THREADS = new Integer(sum);
        }
    }

    private static void setMyNodeEpr(WsaEndpointReference myEpr) {
        nodeEpr = myEpr;
    }

    private static WsaEndpointReference getMyNodeEpr() {
        try {
            return (WsaEndpointReference) WsaEndpointReference.castOrWrap(
                    (XmlElementAdapter) nodeEpr.clone(),
                    WsaEndpointReference.class);
        } catch (CloneNotSupportedException e) {
            throw new XsulException("could not close replyTp EPR", e);
        }
    }

    public void sendTestMessage(WsaEndpointReference targetEpr,
            WsaEndpointReference replyToEpr, URI messageId, XmlElement message)
            throws XsulException, XmlBuilderException, URISyntaxException {
        XmlDocument soapEnvelope = wsaInvoker.getSoapFragrance()
                .wrapBodyContent(message);

        WsaMessageInformationHeaders wsah = new WsaMessageInformationHeaders(
                soapEnvelope);

        wsah.setReplyTo(replyToEpr);
        if (messageId != null) {
            wsah.setMessageId(messageId);
        }
        wsah.explodeEndpointReference(targetEpr);
        wsah.setAction(URI.create("test:echo"));
        // generate uuid
        String uuid = "uuid:" + System.currentTimeMillis();
        uuidSent.add(uuid);
        wsah.addRelatesTo(new WsaRelatesTo(new URI(uuid)));

        wsaInvoker.sendXml(soapEnvelope);
    }

    /**
     * @return
     */
    public int getTotalSentPackets() {
        return this.totalSentPackets;
    }

    /**
     * @return
     */
    public int getLostPackets() {
        return this.lostPackets;
    }

    /**
     * @return
     */
    public int getNotSent() {
        return this.notSentPackets;
    }

    /**
     * @return
     */
    public long getMaxRoundTrip() {
        return this.maxRoundTrip;
    }

    /**
     * @return
     */
    public long getMinRoundTrip() {
        return this.minRoundTrip;
    }

    /**
     * @return
     */
    public double getAverage() {
        return this.average;
    }

    /**
     * @return
     */
    public int getResponsePackets() {
        return this.customizedProcessor.getResponsePackets();
    }

    /**
     * @return Returns the takeMsgBoxTime.
     */
    public long getTakeMsgBoxTime() {
        return takeMsgBoxTime;
    }

    /**
     * @return Returns the lostMessages.
     */
    public int getLostMessages() {
        return lostMessages;
    }

    /**
     * @return Returns the getMessages.
     */
    public int getGetMessages() {
        return getMessages;
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



