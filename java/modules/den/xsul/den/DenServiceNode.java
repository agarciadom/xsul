/**
 * InteropSecureService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DenServiceNode.java,v 1.3 2005/05/16 03:46:03 lifang Exp $
 */

package xsul.den;

import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.ptls.PureTLSContext;
import xsul.den.DenRoutingHandler;
import xsul.http_server.ServerSocketFactory;
import xsul.puretls_server_socket_factory.PuretlsServerSocketFactory;
import xsul.xhandler.server.ServerCapabilityHandler;
import xsul.xhandler.server.ServerSignatureHandler;
import xsul.xservo_soap_http.HttpBasedServices;

public class DenServiceNode {
    private static HttpBasedServices httpServer;
    
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
    
    public static void main(String[] args) throws Exception {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        String svcname = args.length > 1 ? args[1] : "densvc";
        boolean usessl = (System.getProperty("ssl") == null) ? false : true;
        if(usessl) {
            httpServer = prepareSecureServer(tcpPort);
        } else {
            httpServer = new HttpBasedServices(tcpPort);
        }
        //httpServer.addGlobalHandler(new ServerSignatureHandler("sig-checker"));
        System.out.println("Server started on "+httpServer.getServerPort());
        String opt = System.getProperty("xh");
        if(opt.equals("sig")) {
            httpServer.addService(new DoNothingServiceBase(svcname))
                .addHandler(new ServerSignatureHandler("sig-checker"))
                .addHandler(new DenRoutingHandler("den processor for sig"))
                .startService();
        }
        else if(opt.equals("cap")) {
            httpServer.addService(new DoNothingServiceBase(svcname))
                .addHandler(new ServerCapabilityHandler("cap-checker", args[2]))
                .addHandler(new DenRoutingHandler("den processor for xpola"))
                .startService();
        }
        else if(opt.equals("dispatcher")) {
            httpServer.addService(new DoNothingServiceBase("dispatcher"))
                .addHandler(new DenRoutingHandler("dispatcher"))
                .startService();
        }
    }
    
}


