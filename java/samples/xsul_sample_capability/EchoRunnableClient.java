/**
 * EchoRunnableClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul_sample_capability;

import java.io.File;
import java.lang.reflect.Proxy;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import xsul.dsig.saml.CapGlobusCredSOAPEnvelopeSigner;
import xsul.dsig.saml.CapGlobusCredSOAPEnvelopeVerifier;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityEnforcer;
import xsul.dsig.saml.authorization.CapabilityException;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.capability.CapabilityInvoker;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoRunnableClient implements Runnable {
    private String certloc;
    private String keyloc;
    private String cacloc;
    private String svcurl;
    private String svccap;
    
    public EchoRunnableClient(String certloc, String keyloc, String cacloc,
                              String svcurl, String svccap) {
        this.certloc = certloc;
        this.keyloc = keyloc;
        this.cacloc = cacloc;
        this.svcurl = svcurl;
        this.svccap = svccap;
    }
    
    public void run() {
        try {
            Capability cap = new Capability(new File(svccap));
            GlobusCredential cred = CapabilityUtil.getGlobusCredential(keyloc, certloc);
            X509Certificate[] trustedCerts = CapabilityUtil.getTrustedCertificates(cacloc).getCertificates();
            
            SoapHttpDynamicInfosetInvoker invoker =
                new CapabilityInvoker(cred, trustedCerts, cap, svcurl);
            
            SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
                invoker, XsdTypeHandlerRegistry.getInstance());
            
            EchoService ref = (EchoService) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(), //XDirectoryService.class.getClassLoader(),
                new Class[] { EchoService.class },
                handler);
            
            long start = System.currentTimeMillis();
            String result = ref.sayHello("/hello");
            long end = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + " got back "+result + " " + (end-start));
            
        } catch (Exception e) {e.printStackTrace();}
        
    }
    
}

