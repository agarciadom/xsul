/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoClient.java,v 1.5 2004/12/06 16:28:58 adicosta Exp $
 */
package xsul_ping_stat.rpc;

import xsul.common_type_handler.CommonTypeHandlerRegistry;

import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;

import xsul.soap12_util.Soap12Util;

import xsul.soaprpc_client.SoapRpcInvocationHandler;

import xsul_sample_hello.EchoMultiClients;
import xsul_sample_hello.EchoServer;
import xsul_sample_hello.EchoService;

import java.lang.reflect.Proxy;

import java.util.Vector;


//import xsul.xsd_type_handler.XsdTypeHandlerRegistry;
public class EchoClient implements Runnable {
    public final static int DEFAULT_PORT = 34321;
    private String location;
    private long runningTime = -1;

    // Stat value
    private int totalSentPackets = 0;
    private long totalTime = 0;
    private int exceptions = 0;
    private int notSent = 0;
    private Vector roundTrips = new Vector();
    private long minRoundTrip = -1;
    private long maxRoundTrip = -1;
    private double average = 0;
    private EchoStat parent;

    /**
     * Create a new <code>EchoClient</code>.
     *
     * @param location the location of the <code>EchoServer</code>
     *
     * @see EchoServer
     */
    public EchoClient(String location, long runningTime, EchoStat parent) {
        this.location = location;
        this.runningTime = runningTime;
        this.parent = parent;
    }

    /**
     * This method is used only with <code>EchoMultiClients</code>.
     *
     * @see EchoMultiClients
     * @see java.lang.Runnable#run()
     */
    public void run() {
            // Statistical mode
            int numberPackets = 0;
            long startTime = System.currentTimeMillis();
            long estimateEnd = startTime + this.runningTime;
            while (System.currentTimeMillis() < estimateEnd) {
                try {
                    long rtStart = System.currentTimeMillis();
                    this.runCount(1);
                    long rtEnd = System.currentTimeMillis();
                    long roundTrip = rtEnd - rtStart;
                    this.roundTrips.add(new Long(roundTrip));
                    if ((this.minRoundTrip < 0) ||
                            (roundTrip < this.minRoundTrip)) {
                        this.minRoundTrip = roundTrip;
                    }
                    if (roundTrip > this.maxRoundTrip) {
                        this.maxRoundTrip = roundTrip;
                    }
                    numberPackets++;
                } catch (NotSendException e) {
                    notSent++;
                } catch (Exception e) {
                    exceptions++;
                }
            }
            long endTime = System.currentTimeMillis();
            this.totalSentPackets = numberPackets;
            this.totalTime = endTime - startTime;
            if (maxRoundTrip < 0) {
                maxRoundTrip = 0;
            }
            computeAverage();
//            System.out.println(Thread.currentThread().getName() + ": " +
//                this.totalSentPackets + " packets transmitted, " +
//                this.exceptions + " packets lost, " +
//                this.notSent + " packets not sent, runned in " +
//                this.totalTime + " ms\n" + ":   round-trip min/avg/max = " +
//                ((this.minRoundTrip < 0) ? 0 : this.minRoundTrip) + "/" +
//               this.average + "/" + this.maxRoundTrip + " ms");
            
        if (this.parent != null) {
            synchronized (this.parent.FINISHED_THREADS) {
                int sum = this.parent.FINISHED_THREADS.intValue() + 1;
                this.parent.FINISHED_THREADS  = new Integer(sum);
            }
        }
    }

    private double computeAverage() {
        double avg = 0;
        for (int i = 0; i < this.roundTrips.size(); i++)
            avg += ((Long) this.roundTrips.get(i)).longValue();
        if (this.roundTrips.size() > 0) {
            this.average = avg / this.roundTrips.size();
            return this.average;
        } else {
            this.average = 0;
            return 0;
        }
    }

    public void runCount(int count) throws NotSendException{
        SoapHttpDynamicInfosetInvoker invoker = new SoapHttpDynamicInfosetInvoker(location);

        SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(invoker,
                CommonTypeHandlerRegistry.getInstance()); //XsdTypeHandlerRegistry.getInstance());
        invoker.setSoapFragrance(Soap12Util.getInstance());

        // JDK 1.3+ magic to create dynamic stub
        EchoService ref = (EchoService) Proxy.newProxyInstance(Thread.currentThread()
                                                                     .getContextClassLoader(),
                new Class[] { EchoService.class }, handler);

        try {
            ref.echoIntPlusOne(1);

            //System.out.println("1 + 1 = " + sum);
        } catch (Exception e) {
          throw new NotSendException();
        }

        for (int i = 0; i < count; i++) {
            String arg = "echo" + i;
            String result = null;
            try {
                result = ref.echo(arg);
            } catch (Exception e) {
                throw new NotSendException();
            }

            //System.out.println(this + " " + arg + " -> " + result);
            if (!arg.equals(result)) {
                throw new RuntimeException("service sent wrong answer");
            }
        }
    }

    /**
     * @return Returns the maxRoundTrip.
     */
    public long getMaxRoundTrip() {
        return maxRoundTrip;
    }

    /**
     * @return Returns the minRoundTrip.
     */
    public long getMinRoundTrip() {
        return minRoundTrip;
    }

    /**
     * @return Returns the totalSentPackets.
     */
    public int getTotalSentPackets() {
        return totalSentPackets;
    }

    public int getLostPackets() {
        return this.exceptions;
    }

    /**
     * @return Returns the totalTime.
     */
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * @return Returns the average.
     */
    public double getAverage() {
        return average;
    }
    
    /**
     * @return Returns the notSent.
     */
    public int getNotSent() {
        return notSent;
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
