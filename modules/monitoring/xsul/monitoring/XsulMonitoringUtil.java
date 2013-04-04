/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulMonitoringUtil.java,v 1.10 2006/06/06 20:17:23 aslom Exp $
 */
package xsul.monitoring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulVersion;
import xsul.soap.SoapUtil;

/**
 *
 */
public class XsulMonitoringUtil {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static byte[] FAVICON_ICO = new byte[]{0};
    
    
    static {
        init();
    }
    
    private static void init() {
        final String PATH = "/xsul/monitoring/favicon.ico";
        InputStream is = XsulMonitoringUtil.class.getResourceAsStream(PATH);
        if(is != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int count;
            try {
                while((count = is.read(buf)) > 0) {
                    baos.write(buf, 0 , count);
                }
                baos.close();
                is.close();
                FAVICON_ICO = baos.toByteArray();
                logger.config("loaded "+PATH);
            } catch (IOException e) {
                logger.config("failed loading "+PATH, e);
                
            }
        } else {
            logger.config("could not find "+PATH);
        }
    }
    
    public static byte[] getFaviconAsBytes() {
        return FAVICON_ICO;
    }
    
    //<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    public static XsulMonitoringStats createStats(String serverName, long startTime,
                                                  long requestXmlMsgCount, long numberOfConnections) {
        return new XsulMonitoringStats(serverName, startTime, requestXmlMsgCount, numberOfConnections);
    }
    
    
    public static XmlDocument createXhtmlStatsDoc(XsulMonitoringStats s, String encoding) {
        XmlDocument outgoingDoc = builder.newDocument("1.0", Boolean.TRUE, encoding);
        XmlElement xhtml = XsulMonitoringUtil.createXhtmlStats(s);
        outgoingDoc.setDocumentElement(xhtml);
        return outgoingDoc;
    }
    
    public static XmlElement createXhtmlStats(XsulMonitoringStats stats) {
        final XmlNamespace H = XmlConstants.XHTML_NS;
        XmlElement html = builder.newFragment(H, "html");
        html.declareNamespace(H);
        XmlElement head = html.addElement(H, "head");
        XmlElement titleEl = head.addElement(H, "title");
        //String version = " ("+XsulVersion.getUserAgent()+")";
        //String title = s.getServerName()+" Monitoring Stats "+version;
        titleEl.addChild(stats.getServerName());
        String pageHeader = "Monitoring stats for "+stats.getServerName();
        XmlElement body = html.addElement(H, "body");
        XmlElement h1 = body.addElement(H, "h1");
        h1.addChild(pageHeader);
        XmlElement div = body.addElement(H, "div");
        div.addAttribute(null, "class", "statistics");
        //defensive programming to avoid divide-per-zero when calculating uptime: starTime-1
        double uptimeInSeconds = (System.currentTimeMillis() - stats.getStarTimeInMs() - 1) / 1000.0;
        if (stats.getRequestXmlMsgsCount() > 0) {
            XmlElement p = div.addElement(H, "p");
            p.addChild("Number of XML requests: ");
            XmlElement spanMsgs = p.addElement(H, "span");
            spanMsgs.addAttribute(null, "class", XsulMonitoringStats.XML_REQUESTS_COUNT);
            spanMsgs.addChild(""+stats.getRequestXmlMsgsCount());
            p.addChild(" ");
        }
        if (stats.getConnectionsCount() > 0) {
            XmlElement p = div.addElement(H, "p");
            p.addChild("Total number of connections: ");
            XmlElement spanMsgs = p.addElement(H, "span");
            spanMsgs.addAttribute(null, "class", XsulMonitoringStats.CONNECTIONS_COUNT);
            spanMsgs.addChild(""+stats.getConnectionsCount());
            p.addChild(" ");
        }
        if (stats.getRequestXmlMsgsCount() > 0) {
            double avgPerSecond = stats.getRequestXmlMsgsCount() / uptimeInSeconds;
            if(avgPerSecond > 1.0) {
                XmlElement p = div.addElement(H, "p");
                p.addChild("average: ");
                XmlElement spanAvg = p.addElement(H, "span");
                spanAvg.addAttribute(null, "class", XsulMonitoringStats.XML_REQUESTS_PER_SECOND);
                spanAvg.addChild(""+avgPerSecond);
                p.addChild(" [requests/second]");
            }
        }
        {
            if(uptimeInSeconds < 1000) {
                XmlElement p = div.addElement(H, "p");
                p.addChild("Uptime: ");
                p.addChild(""+uptimeInSeconds);
                p.addChild(" [seconds]");
            }
            XmlElement p = div.addElement(H, "p");
            p.addChild("Service started: "+new Date(stats.getStarTimeInMs()) );
        }
        {
            XmlElement p = div.addElement(H, "p");
            p.addChild("Start time: ");
            XmlElement spanStartTime = p.addElement(H, "span");
            spanStartTime.addChild(""+stats.getStarTimeInMs());
            spanStartTime.addAttribute(null, "class", XsulMonitoringStats.STARTTIME_SECONDS);
            p.addChild(" [milliseconds] since UNIX epoch.");
        }
        {
            XmlElement p = div.addElement(H, "p");
            p.addChild("Service name/version: ");
            XmlElement spanVersion = p.addElement(H, "span");
            spanVersion.addAttribute(null, "class", XsulMonitoringStats.SERVER_VERSION);
            spanVersion.addChild("" + stats.getServerName());
        }

        {
            XmlElement p = div.addElement(H, "p");
            p.addChild("XSUL Version: ");
            XmlElement spanVersion = p.addElement(H, "span");
            spanVersion.addAttribute(null, "class", XsulMonitoringStats.XSUL_IMPL_VERSION);
            spanVersion.addChild("" + XsulVersion.getImplementationVersion());
        }
        
        return html;
    }
    
    public static XmlDocument processMonitoringRequest(XsulMonitoringStats s,
                                                       XmlElement incomingXml,
                                                       SoapUtil soapUtil)
    {
        XmlDocument outgoingDoc = null;
        if(XsulMonitoringStats.MONITORING_NS.equals(incomingXml.getNamespace())) {
            // we take over to do some monitoring tasks
            if("Ping".equals(incomingXml.getName())) {
                XmlElement bodyContent = builder.newFragment(XsulMonitoringStats.MONITORING_NS, "PingResponse");
                bodyContent.declareNamespace(XsulMonitoringStats.MONITORING_NS);
                //XsulMonitoringStats s = createStats(serviceName, startTime, requestXmlMsgCount);
                bodyContent.addElement(s);
                outgoingDoc = soapUtil.wrapBodyContent(bodyContent);
            }
            
        }
        return outgoingDoc;
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




