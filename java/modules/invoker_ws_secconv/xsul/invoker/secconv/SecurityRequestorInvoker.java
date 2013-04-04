/**
 * SecurityRequestorInvoker.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.invoker.secconv;

import java.lang.reflect.Proxy;
import java.security.Key;
import org.xmlpull.v1.builder.XmlDocument;
import sun.misc.BASE64Encoder;
import xsul.MLogger;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.secconv.ClientNegotiator;
import xsul.secconv.SCUtil;
import xsul.secconv.SecurityRequestorService;
import xsul.secconv.autha.AuthaClientNegotiator;
import xsul.secconv.dsig.SessionKeySOAPEnvelopeSigner;
import xsul.secconv.dsig.SessionKeySOAPEnvelopeVerifier;
import xsul.secconv.pki.GlobusCredClientNegotiator;
import xsul.secconv.pki.KeyStoreClientNegotiator;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class SecurityRequestorInvoker extends SoapHttpDynamicInfosetInvoker
{
    private static final MLogger logger = MLogger.getLogger();
    private static BASE64Encoder encoder = new BASE64Encoder();
    
    // location of the remote SecurityRequestor service
    private String sclocation = null;
    // session security context id
    private String contextId = null;
    
    public SecurityRequestorInvoker(String location,
                                    String scloc) {
        super(location);
        this.sclocation = scloc;
    }
    
    public void setSCLocation(String scloc) {
        this.sclocation = scloc;
    }
    
    public String getSCLocation() {
        return sclocation;
    }
    
    public XmlDocument invokeXml(XmlDocument request)
        throws DynamicInfosetInvokerException {
        
        if(contextId == null) {
            logger.finest("context id null.\n create a new security context...");
            // work as a SR service client to get a session key
            SoapHttpDynamicInfosetInvoker invoker =
                new SoapHttpDynamicInfosetInvoker(sclocation);
            
            SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
                invoker, XsdTypeHandlerRegistry.getInstance());
            invoker.setSoapFragrance(Soap12Util.getInstance());
            
            SecurityRequestorService ref =
                (SecurityRequestorService) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] { SecurityRequestorService.class },
                handler);
            
            try {
                String opt = System.getProperty("scprotocol");
                ClientNegotiator cn = null;
                if(opt.equals("autha")) {
                    try {
                        char[] pw = SCUtil.getPassword();
                        if(pw == null) {
                            throw new Exception("no password");
                        }
                        cn = new AuthaClientNegotiator(pw);
                    }
                    catch(Exception e) {
                        cn = new AuthaClientNegotiator();
                    }
                }
                else if(opt.equals("ks")) {
                    String alias = System.getProperty("alias");
                    String kspasswd = System.getProperty("kspasswd");
                    String password = System.getProperty("password");
                    if(alias == null || kspasswd == null || password == null)
                        throw new Exception("parameters (alias/password/keystorepassord) missing");
                    
                    cn = new KeyStoreClientNegotiator(alias, password, kspasswd);
                }
                else if(opt.equals("globus")) {
                    String proxycert = System.getProperty("proxy");
                    if(proxycert == null)
                        cn = new GlobusCredClientNegotiator();
                    else
                        cn = new GlobusCredClientNegotiator(proxycert);
                }
                else {
                    throw new Exception("Please specify a protocol!");
                }
                
                if(cn != null) {
                    cn.negotiate(ref);
                    contextId = cn.getContextId();
                    logger.finest("got contextId from autha: "+contextId);
                    // a SecrectKeySpec instance
                    Key skey = cn.getSessionKey();
                    SCUtil.saveSessionKey(contextId, skey, false);
                    logger.finest("encoded key: "+encoder.encode(skey.getEncoded()));
                    logger.finest("Context established");
                }
                else {
                    throw new Exception("failed to instantiate client negotiator");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.severe("failed to establish context: " + e.getMessage());
            }
        }
        
        logger.finest("contextididid: " + contextId);
        
        SessionKeySOAPEnvelopeSigner sksigner =
            new SessionKeySOAPEnvelopeSigner(contextId);
        XmlDocument signedRequest = sksigner.signSoapMessage(request);
        
        XmlDocument response = super.invokeXml(signedRequest);
        
        SessionKeySOAPEnvelopeVerifier skverifier =
            new SessionKeySOAPEnvelopeVerifier(contextId);
        skverifier.verifySoapMessage(response);
        
        return response;
        
    }
    
}




