/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoClient.java,v 1.15 2005/02/08 19:29:50 aslom Exp $
 */

package xsul_sample_hello;

import java.lang.reflect.Proxy;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.common_type_handler.CommonTypeHandlerRegistry;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.invoker.DynamicInfosetInvokerException;
//import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoClient implements Runnable{
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static int DEFAULT_PORT = 34321;
    private String location;
    
    /**
     * Create a new <code>EchoClient</code>.
     *
     * @param location the location of the <code>EchoServer</code>
     *
     * @see EchoServer
     */
    public EchoClient(String location){
        this.location = location;
    }
    
    /**
     * This method is used only with <code>EchoMultiClients</code>.
     *
     * @see EchoMultiClients
     * @see java.lang.Runnable#run()
     */
    public void run() {
        runCount(3);
        
    }
    
    public void runCount(int count) {
        SoapHttpDynamicInfosetInvoker invoker = new SoapHttpDynamicInfosetInvoker(location);
        
        SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
            invoker, CommonTypeHandlerRegistry.getInstance()) {
            
            protected void processSoapFaultIfPresent(XmlElement possibleFault) throws XmlBuilderException, XsulException {
                // TODO
                if("Fault".equals(possibleFault.getName())) {
                    //ALEK: Leaky Abstraciton: now can process both SOAP 1.1 and 1.2 ...
                    if(XmlConstants.NS_URI_SOAP11.equals(possibleFault.getNamespaceName())
                           || XmlConstants.NS_URI_SOAP12.equals(possibleFault.getNamespaceName()) )
                    {
                        // TODO extract faultcode + faultstring + detail etc.
                        //StringWriter sw = new StringWriter();
                        String s = builder.serializeToString(possibleFault);
                        XmlElement detail = possibleFault.element(possibleFault.getNamespace(), "Detail");
                        
                        
                        throw new DynamicInfosetInvokerException("remote exception .... "+s, possibleFault);
                    }
                }
            }
            //XsdTypeHandlerRegistry.getInstance());
            
        };
        invoker.setSoapFragrance(Soap12Util.getInstance());
        
        // JDK 1.3+ magic to create dynamic stub
        EchoService ref = (EchoService) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { EchoService.class },
            handler);
        
        try {
            int sum = ref.echoIntPlusOne(1);
            System.out.println("1 + 1 = "+sum);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            Struct st = new Struct(1, 2, 3.0);
            System.out.println("st="+st);
            Struct st2 = ref.echoStruct(st);
            System.out.println("st2="+st);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        for (int i = 0; i < count; i++)
        {
            String arg = "echo"+i;
            String result = null;
            try {
                result = ref.echo(arg);
                System.out.println(this+" "+arg+" -> "+result);
                if(!arg.equals(result)) {
                    throw new RuntimeException("service sent wrong answer");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        //int port = Integer.parseInt(args[0]);
        int port = DEFAULT_PORT;
        String location = null;
        if(args.length > 0) {
            String s = args[0];
            if(s.startsWith("http")) {
                location = s;
            } else {
                port = Integer.parseInt(s);
            }
        }
        if(location == null) {
            location = "http://localhost:"+port;
        }
        
        EchoClient client = new EchoClient(location);
        int count = 3;
        if(args.length > 1) {
            count = Integer.parseInt(args[1]);
        }
        client.runCount(count);
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


