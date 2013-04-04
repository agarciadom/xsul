/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulMonitoringStats.java,v 1.5 2006/06/06 20:17:23 aslom Exp $
 */
package xsul.monitoring;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;

/**
 *
 */
public class XsulMonitoringStats extends XmlElementAdapter {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static XmlNamespace MONITORING_NS = builder.newNamespace(
        "m", "http://www.extreme.indiana.edu/xgws/xsul/monitoring/2005");
    public final static String SERVICE_NAME = "service-name";
    public final static String XML_REQUESTS_COUNT = "xml-requests-count";
    public final static String STARTTIME_SECONDS = "starttime-seconds";
    public final static String XML_REQUESTS_PER_SECOND = "xml-requests-per-second";
    public final static String CONNECTIONS_COUNT = "connections-count";
    public final static String SERVER_VERSION = "server-version";
    public final static String XSUL_IMPL_VERSION = "impl-version";

    private String serverName;
    private long startTimeMs;
    private long requestXmlMsgsCount;

    protected XsulMonitoringStats(String serviceName, long startTimeInMs,
                                  long requestXmlMsgCount, long numberOfConnections) {
        super(builder.newFragment(MONITORING_NS, "service-statistics"));
        this.serverName = serviceName;
        this.startTimeMs = startTimeInMs;
        this.requestXmlMsgsCount = requestXmlMsgCount;
        XmlElement serviceNameEl = addElement(SERVICE_NAME);
        serviceNameEl.addChild(serviceName);
        XmlElement starttimeEl = addElement(STARTTIME_SECONDS);
        starttimeEl.addChild(""+startTimeInMs);
        XmlElement requestsEl = addElement(XML_REQUESTS_COUNT);
        requestsEl.addChild(""+requestsEl);
        XmlElement numberOfConnectionsEl = addElement(CONNECTIONS_COUNT);
        numberOfConnectionsEl.addChild(""+numberOfConnections);
    }

    /**
     * @deprecated
     */
    public String getSericeName() {
        return getServerName();
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public long getStarTimeInMs() {
        return startTimeMs;
    }
    
    public long getRequestXmlMsgsCount() {
        return requestXmlMsgsCount;
    }

    public long getConnectionsCount() {
        String s = requiredElement(null, CONNECTIONS_COUNT).requiredTextContent();
        return Long.parseLong(s);
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



