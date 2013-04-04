/**
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * Echo Server with capability support
 *
 * @author Liang Fang lifang@cs.indiana.edu
 * $Id: EchoCapServer.java,v 1.7 2005/05/04 20:52:17 lifang Exp $
 */

package xsul_sample_capability;


import java.security.Principal;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import xsul.dsig.SignatureInfo;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeSigner;
import xsul.dsig.saml.CapGlobusCredSOAPEnvelopeVerifier;
import xsul.dsig.saml.CapSignatureInfo;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityAuthorizer;
import xsul.dsig.saml.authorization.CapabilityException;
import xsul.processor.capability.CapabilityProcessor;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoCapServer {
    private static SoapHttpDynamicInfosetProcessor customizedProcessor;
    
    public static final String USAGE =
        "usage: java "+EchoCapServer.class.getName()+" \n" +
        "\t[--port port number of the service]\n" +
        "";
    
    private static final int PORT = 0;
    
    static String[] parseArgs(String[] args) throws IllegalArgumentException {
        
        // set arg names expected
        String[] argNames = new String[4];
        argNames[PORT] = "--port";
        
        // set defaults
        String[] defaultValues = new String[4];
        defaultValues[PORT] = null;
        
        // set required
        boolean[] requiredArgs = new boolean[4];
        requiredArgs[PORT] = true;
        
        return CapabilityUtil.parse(args, argNames, defaultValues, requiredArgs, USAGE);
    }
    
    public static void main(String[] args) throws Exception {
        String[] myArgs = parseArgs(args);
        int port = Integer.parseInt(myArgs[PORT]);
        
        System.err.println("port: " + port);
        //TODO: Dispatcher dsptr = new Soap11ReflectionBasedDispatcher(srv);
        EchoService srv = new EchoServiceImpl();
        
        TypeHandlerRegistry registry = XsdTypeHandlerRegistry.getInstance();
        
        SoapRpcReflectionBasedService service =
            new SoapRpcReflectionBasedService(srv, registry);
        String svc_uri = "";
        GlobusCredential cred = CapabilityUtil.getGlobusCredential();
        X509Certificate[] trustedCerts = CapabilityUtil.getTrustedCertificates().getCertificates();
        
        customizedProcessor =
            new CapabilityProcessor(cred, trustedCerts, service, port, svc_uri);
        customizedProcessor.start();
        System.out.println(customizedProcessor.getClass()+" running on "+customizedProcessor.getServer().getLocation());
    }
    
    public static void shutdown() {
        customizedProcessor.shutdown();
        
        try {
            Thread.currentThread().sleep(1000);
        }
        catch (InterruptedException e) {}
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



