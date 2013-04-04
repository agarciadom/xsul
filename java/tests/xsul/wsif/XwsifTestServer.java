/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * based on xsul_dii.XsulDynamicInvoker.java,v 1.8 2005/01/18 10:02:38 aslom Exp $
 */

package xsul.wsif;

import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulVersion;
import xsul.xservo.XService;
import xsul.xservo_soap.XSoapDocLiteralService;
import xsul.xservo_soap_http.HttpBasedServices;

public class XwsifTestServer {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static HttpBasedServices httpServices;
    private static final String SERVICE_NAME = XwsifTestService.XWSDLC_PORTTYPE_QNAME.getLocalPart();
    
    public static final String DEFAULT_WSDL_LOC = "tests/xsul/wsif/XwsifTestService.wsdl";
    
    private static void usage(String errMsg) {
        System.err.println("Usage: [port [wsdlLoc]] ");
        System.exit(1);
    }
 
    /**
     * Launch service from command line
     */
    public static void main(String[] args) {
        (new XwsifTestServer()).run(args);
    }
    
    private void run(String[] args) {
        XsulVersion.exitIfRequiredVersionMissing(XsulVersion.SPEC_VERSION); //sanity check
        XsulVersion.exitIfRequiredVersionMissing("2.1.17");
        if(args.length > 2) usage("");
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        httpServices = new HttpBasedServices(tcpPort);
        System.out.println("Server started on "+httpServices.getServerPort());
        
        String wsdlLoc = args.length > 1 ? args[1] : DEFAULT_WSDL_LOC;
        System.out.println("Using WSDL for service description from "+wsdlLoc);
        XService xsvc = httpServices.addService(
            //new XSoapRpcBasedService(SERVICE_NAME, wsdlLoc, new EchoPeerImpl()));
            new XSoapDocLiteralService(SERVICE_NAME, wsdlLoc, new XwsifTestServiceImpl()));
        xsvc.startService();
        System.out.println("Service "+SERVICE_NAME+" started");
        System.out.println("Service WSDL available at "+getServiceWsdlLocation());
    }
    
    public static String getServiceWsdlLocation() {
        return httpServices.getServer().getLocation() + "/"+SERVICE_NAME+"?wsdl";
    }
    public static void shutdownServer() {
        httpServices.getServer().shutdownServer();
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

