/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoClient.java,v 1.14 2004/03/30 22:14:27 aslom Exp $
 */

package xsul_perf;

import COM.claymoresystems.ptls.SSLDebug;
import java.lang.reflect.Proxy;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.ptls.PureTLSContext;
import xsul.common_type_handler.CommonTypeHandlerRegistry;
import xsul.http_client.ClientSocketFactory;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.puretls_client_socket_factory.PuretlsClientSocketFactory;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_client.SoapRpcInvocationHandler;

public class EchoClient {
    
    public static void main(String[] args) throws Exception
    {
        //int port = Integer.parseInt(args[0]);
        int port = 34321;
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
        System.out.println("connecting to "+location);
        SoapHttpDynamicInfosetInvoker invoker = new SoapHttpDynamicInfosetInvoker();
        //new Soap11HttpDynamicInfosetInvoker(HttpClientReuseLastConnectionManager.newInstance());
        //new Soap11HttpDynamicInfosetInvoker(HttpClientConnectionManager.newInstance());
        
        
        boolean usePureTLS = location.startsWith("https://");
        if(usePureTLS) {
            //SSLDebug.setDebug(SSLDebug.DEBUG_ALL);
            ClientSocketFactory secureSocketFactory;
            boolean useCogCredentials = false;
            if(useCogCredentials) {
                PureTLSContext ctx = new PureTLSContext();
                X509Certificate [] certs = TrustedCertificates.getDefaultTrustedCertificates().getCertificates();
                ctx.setTrustedCertificates(certs);
                GlobusCredential cred = GlobusCredential.getDefaultCredential();
                ctx.setCredential(cred);
                secureSocketFactory = new PuretlsClientSocketFactory(ctx);
            } else {
                secureSocketFactory = new PuretlsClientSocketFactory("root.pem",
                                                                     "client.pem",
                                                                     "random.pem",
                                                                     "password");
            }
            HttpClientConnectionManager secureConnMgr =
                HttpClientReuseLastConnectionManager.newInstance(secureSocketFactory);
            invoker.setSecureConnectionManager(secureConnMgr);
            
            System.out.println("native PureTLS provider "+COM.claymoresystems.ptls.LoadProviders.haveGoNativeProvider());
        } else {
            System.out.println("no security");
        }
        boolean keeapAlive = true;
        //if(!keeapAlive) {
        System.out.println("keepAlive "+(keeapAlive ? "enabled" : "disabled" ));
        //}
        invoker.setKeepAlive(keeapAlive);
        
        //to use SOAP 1.2
        invoker.setSoapFragrance(Soap12Util.getInstance());
        
        //invoker.setKeepAlive(false);
        
        //          {
        //                @Overrides public XmlDocument invokeXml(XmlDocument request)
        //                    throws DynamicInfosetInvokerException
        //                {
        //                    XmlDocument response = super.invokeXml(request);
        //                    return response;
        //                }
        //                @Overrides public void foo(XmlDocument request) {
        //                }
        //            };
        invoker.setLocation(location);
        
        
        SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
            invoker, CommonTypeHandlerRegistry.getInstance());
        
        EchoService ref = (EchoService) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { EchoService.class },
            handler);
        final int N = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        System.out.println("invoking "+location+" "+N+" times");
        long start = System.currentTimeMillis();
        for (int i = 0; i < N ; i++)
        {
            String arg = "echo"+i;
            String result = ref.echo(arg);
            //System.out.println(arg+" -> "+result);
            if(!arg.equals(result)) {
                throw new RuntimeException("service sent wrong answer");
            }
        }
        long end = System.currentTimeMillis();
        long total = end-start;
        double seconds = (total/1000.0);
        double invPerSecs = (double)N / seconds ;
        double avgInvTimeInMs = (double)total / (double)N;
        System.out.println("N="+N+" avg invocation:"+avgInvTimeInMs+" [ms] "+
                               //"total:"+seconds+" [s] "+
                               "throughput:"+invPerSecs+" [invocations/second]");
    }
}





