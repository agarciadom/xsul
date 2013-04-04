/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: InteropService.java,v 1.7 2005/03/15 17:51:04 lifang Exp $
 */
package simplesoap.service;


import simplesoap.service.server_security_handler.PasswordProcessorHandler;
import xsul.xservo_soap.XSoapRpcBasedService;
import xsul.xservo_soap_http.HttpBasedServices;

/**
 * Start interop service
 */
public class InteropService {
    private static HttpBasedServices httpServer;
    private static final String SERVICE_NAME = "interop";
    
    /**
     * Simple command line
     */
    public static void main(String[] args) {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        httpServer = new HttpBasedServices(tcpPort);
        //httpServer.addGlobalHandler();
        System.out.println("Server started on "+httpServer.getServerPort());
        String wsdlLoc = args.length > 1 ? args[1] : "samples/simplesoap/contract/InteropTest.wsdl";
        System.out.println("Loading WSDL from "+wsdlLoc);
        //httpServer.newService("interop", wsdlLoc, new InteropTestRpcImpl());
        httpServer.addService(new XSoapRpcBasedService(SERVICE_NAME, wsdlLoc, new InteropTestRpcImpl()))
//            .addHandler(new PasswordProcessorHandler("password-checker"))
            .startService();
        //httpServer.shutdown();
        
//  httpServices.addService(new XmlBeansBasedService(SERVICE_NAME, wsdlLoc, new DecoderImpl())).
//                     .addHandler(new ServerSignatureHandler(...params if any...))
//                     .addHandler(new ServerCapabilityHandler(...params if any...))
//                     .startService();
        
        //HttpServo server = new HttpBasedServices(port);
        //VirtualService httpService = server.newService();
        //httpService.setName("interop");
        //httpService.useWsdl(wsdl);
        //httpService.setServiceImpl(impl, interface)
        //httpService.startService();
        
    }

    public static String getServiceWsdlLocation() {
        return httpServer.getServer().getLocation() + "/interop?wsdl";
    }
    public static void shutdownServer() {
        httpServer.getServer().shutdownServer();
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2005 The Trustees of Indiana University. All rights
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


