/**
 * InteropSecureService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: InteropSecureService.java,v 1.12 2005/06/16 19:44:35 lifang Exp $
 */

package simplesoap.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.ptls.PureTLSContext;
import simplesoap.contract.CommonConstants;
import xsul.http_server.ServerSocketFactory;
import xsul.puretls_server_socket_factory.PuretlsServerSocketFactory;
import xsul.xhandler.WorkloadHandler;
import xsul.xhandler.server.ServerCapabilityHandler;
import xsul.xhandler.server.ServerFastDSigHandler;
import xsul.xhandler.server.ServerSecConvHandler;
import xsul.xhandler.server.ServerSignatureHandler;
import xsul.xservo.XService;
import xsul.xservo_soap.XSoapRpcBasedService;
import xsul.xservo_soap_http.HttpBasedServices;

public class InteropSecureService {
    private static HttpBasedServices httpServer;
    private static Hashtable htable = null;
    private static final String SERVICE_NAME = "interop";
    
    public static void main(String[] args) throws Exception {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        //        ServerSocketFactory secureSocketFactory;
        boolean usessl = (System.getProperty("ssl") == null) ? false : true;
        if(usessl) {
            httpServer = prepareSecureServer(tcpPort);
        } else {
            httpServer = new HttpBasedServices(tcpPort);
        }
        //httpServer.addGlobalHandler(new ServerSignatureHandler("sig-checker"));
        System.out.println("Server started on "+httpServer.getServerPort());
        String wsdlLoc =
            CommonConstants.class.getResource("InteropTest.wsdl").toString();
        System.out.println("Loading WSDL from "+wsdlLoc);
        XService service =
            httpServer.addService(
            new XSoapRpcBasedService(SERVICE_NAME,
                                     wsdlLoc,
                                     new InteropTestRpcImpl()));
        Collection keys = readConf();
        if(keys == null) {
            // by default
            service.addHandler(new ServerSignatureHandler("sig-checker"));
        }
        else {
            int iload = 0;
            int isig = 0;
            int icap = 0;
            int isecconv = 0;
            for(Iterator iter = keys.iterator(); iter.hasNext();) {
                String key = (String)iter.next();
                String val = (String)htable.get(key);
                int nload = Integer.parseInt(val);
                for (int i = 0; i < nload; i++) {
                    if(key.equalsIgnoreCase("load")) {
                        System.out.println("adding workload handler " + iload);
                        service.addHandler(
                            new WorkloadHandler("muchadoabtnothing "
                                                    + iload++));
                    }
                    else if (key.equalsIgnoreCase("sig")) {
                        System.out.println("adding signature handler " + isig);
                        service.addHandler(
                            new ServerSignatureHandler("sig handler "
                                                           + isig++));
                    }
                    else if (key.equalsIgnoreCase("fastsig")) {
                        System.out.println("adding fast signature handler " + isig);
                        service.addHandler(
                            new ServerFastDSigHandler("fast sig handler "
                                                          + isig++));
                    }
                    else if (key.equalsIgnoreCase("cap")) {
                        System.out.println("adding cap handler " + icap);
                        String myurl = "://localhost:"+tcpPort+"/"+SERVICE_NAME;
                        if(usessl) {
                            myurl = "https" + myurl;
                        }
                        else {
                            myurl = "http" + myurl;
                        }
                        service.addHandler(
                            new ServerCapabilityHandler("cap handler " + icap++,
                                                        myurl));
                    }
                    else if (key.equalsIgnoreCase("secconv")) {
                        System.out.println("adding secconv handler " + isecconv);
                        service.addHandler(
                            new ServerSecConvHandler("secconv handler "
                                                         + isecconv++));
                    }
                }
            }
        }
        service.startService();
    }
    
    private static Collection readConf() throws Exception {
        String conf = System.getProperty("conf");
        if(conf == null)
            conf = "server.properties";
        
        try {
            BufferedReader in =
                new BufferedReader(new FileReader(new File(conf)));
            String line = null;
            htable = new Hashtable();
            Vector keys = new Vector(11);
            while((line = in.readLine()) != null) {
                int idx = line.indexOf('=');
                String key = line.substring(0, idx);
                keys.add(key);
                String val = line.substring(idx+1);
                htable.put(key, val);
            }
            in.close();
            return keys;
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't found the default configuration file");
        }
        
        return null;
    }
    
    public static String getServiceWsdlLocation() {
        return httpServer.getServer().getLocation() + "/"+SERVICE_NAME+"?wsdl";
    }
    
    public static void shutdownServer() {
        httpServer.getServer().shutdownServer();
    }
    
    private static HttpBasedServices prepareSecureServer(int tcpPort)
        throws Exception {
        ServerSocketFactory secureSocketFactory;
        PureTLSContext ctx = new PureTLSContext();
        X509Certificate [] certs
            = TrustedCertificates.getDefaultTrustedCertificates().getCertificates();
        ctx.setTrustedCertificates(certs);
        GlobusCredential cred = GlobusCredential.getDefaultCredential();
        ctx.setCredential(cred);
        secureSocketFactory = new PuretlsServerSocketFactory(tcpPort, ctx);
        return new HttpBasedServices(secureSocketFactory);
    }
    
}


