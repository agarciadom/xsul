/**
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * Echo Service client with capability support
 *
 * @author Liang Fang lifang@cs.indiana.edu
 * $Id: EchoCapClient.java,v 1.10 2005/05/04 20:52:17 lifang Exp $
 */

package xsul_sample_capability;


import java.io.File;
import java.lang.reflect.Proxy;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.capability.CapabilityInvoker;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoCapClient {
    public static final String USAGE =
        "usage: java "+EchoCapClient.class.getName()+" \n" +
        "\t[--url url of the service]\n" +
        "\t[--cap location of your capability token for the service]\n"+
        "";
    
    private static final int SERVICE_URL = 0;
    private static final int SERVICE_CAP = 1;
    
    static String[] parseArgs(String[] args) throws IllegalArgumentException {
        // set arg names expected
        String[] argNames = new String[5];
        argNames[SERVICE_URL] = "--url";
        argNames[SERVICE_CAP] = "--cap";
        
        // set defaults
        String[] defaultValues = new String[5];
        defaultValues[SERVICE_URL] = null;
        defaultValues[SERVICE_CAP] = null;
        
        // set required
        boolean[] requiredArgs = new boolean[5];
        requiredArgs[SERVICE_URL] = true;
        requiredArgs[SERVICE_CAP] = true;
        
        return CapabilityUtil.parse(args, argNames, defaultValues, requiredArgs, USAGE);
    }
    
    public static void main(String[] args) {
        try {
            //EchoCapServer.main(new String[]{"--port", "8989"});
            
            String[] myArgs = parseArgs(args);
            String svcurl = myArgs[SERVICE_URL];
            String svccap = myArgs[SERVICE_CAP];
            
            System.err.println("svcurl: " + svcurl);
            System.err.println("svccap: " + svccap);
            
            Capability cap = new Capability(new File(svccap));
            GlobusCredential cred = CapabilityUtil.getGlobusCredential();
            X509Certificate[] trustedCerts = CapabilityUtil.getTrustedCertificates().getCertificates();
            
            // create XML service invoker and usgment it with dsig and header handlers
            SoapHttpDynamicInfosetInvoker invoker =
                new CapabilityInvoker(cred, trustedCerts, cap, svcurl);
            
            SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
                invoker, XsdTypeHandlerRegistry.getInstance());
            
            EchoService ref = (EchoService) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] { EchoService.class },
                handler);
            
            String result = ref.sayHello("/hello");
            System.out.println("got back "+result);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            EchoCapServer.shutdown();
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



