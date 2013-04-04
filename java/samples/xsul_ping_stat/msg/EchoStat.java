/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoStat.java,v 1.6 2005/01/10 20:00:14 aslom Exp $
 */
package xsul_ping_stat.msg;

import java.util.Vector;


/**
 * @author Alexandre di Costanzo
 *
 */
public class EchoStat {
    private int numberOfConnections;
    private long runningTime;
    private String location;
    private int port;
    private static final String USAGE = "java " + EchoStat.class.getName() +
        " location  number_of_connections   time_minuts port [msg_box]";
    protected Integer FINISHED_THREADS = new Integer(0);
    private String msg_box_adress;
    private boolean msgbox = false;

    public EchoStat(String[] params) {
        this.location = params[0];
        this.numberOfConnections = Integer.parseInt(params[1]);
        this.runningTime = Integer.parseInt(params[2]) * 60 * 1000;
        this.port = Integer.parseInt(params[3]);
        if (params.length == 5) {
            this.msgbox = true;
            this.msg_box_adress = params[4];
        }
    }

    private static EchoStat parseArgs(String[] args) {
        // Checks arguments
        if (args.length < 4) {
            System.err.println(USAGE);
            System.exit(69);
        }

        return new EchoStat(args);
    }

    private void start() {
        assert this.numberOfConnections > 0;
        assert this.runningTime > 0;

        Vector clients = new Vector();

        // Start clients
        for (int i = 0; i < this.numberOfConnections; i++) {
            EchoPeer peer = new EchoPeer(this.location, this.runningTime,
                    this.port++, this.msg_box_adress, this);
            new Thread(peer).start();
            clients.add(peer);
        }

        // Waiting results
        while (true) {
            synchronized (this.FINISHED_THREADS) {
                if (this.FINISHED_THREADS.intValue() == this.numberOfConnections) {
                    break;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Comupte total results
        int totalPackets = 0;
        int lostPackets = 0;
        int notSentPackets = 0;
        int responsePackets = 0;
        long minRT = -1;
        long maxRT = 0;
        double avg = 0;

        int messages = 0;
        int lostMessages = 0;
        long minTime = -1;
        long maxTime = 0;
        double avgMsg = 0;

        for (int i = 0; i < clients.size(); i++) {
            EchoPeer current = (EchoPeer) clients.get(i);
            totalPackets += current.getTotalSentPackets();
            lostPackets += current.getLostPackets();
            notSentPackets += current.getNotSent();
            responsePackets += current.getResponsePackets();
            if (maxRT < current.getMaxRoundTrip()) {
                maxRT = current.getMaxRoundTrip();
            }
            if ((minRT < 0) ||
                    ((current.getMinRoundTrip() < minRT) &&
                    (current.getMinRoundTrip() > 0))) {
                minRT = current.getMinRoundTrip();
            }
            avg += current.getAverage();
            // Stat Msg Box
            if (this.msgbox) {
                messages += current.getGetMessages();
                lostMessages += current.getLostMessages();
                long time = current.getTakeMsgBoxTime();
                if ((minTime == -1) || (minTime > time)) {
                    minTime = time;
                }
                if (maxTime < time) {
                    maxTime = time;
                }
                avgMsg += time;
            }
        }
        avg /= this.numberOfConnections;
        if (avgMsg != 0) {
            avgMsg /= this.numberOfConnections;
        }

        System.out.println(
            "-------------------------------------------------------------------------------------\n" +
            "Number of clients: " + this.numberOfConnections +
            " Running time: " + this.runningTime + " ms\n" + totalPackets +
            " packets transmitted, " + lostPackets + " packets lost, " +
            notSentPackets + " packets not sent \n" + responsePackets +
            " packets received\n" + "round-trip min/avg/max = " + minRT + "/" +
            avg + "/" + maxRT + " ms\n" +
            "-------------------------------------------------------------------------------------");

        if (this.msgbox) {
            System.out.println("Getting messages from MsgBox Stat:\n" +
                "Number of clients: " + this.numberOfConnections +
                " Running time: " + this.runningTime + " ms\n" + totalPackets +
                " messages transmitted, " + messages + " messages took, " +
                lostMessages + " messages lost \n" +
                "takeMessage min/avg/max = " + minTime + "/" + avgMsg + "/" +
                maxTime + " ms\n" +
                "-------------------------------------------------------------------------------------");
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        EchoStat statTool = parseArgs(args);

        statTool.start();
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

