/**
 * SecureXBeansDocumentInvoker.java
 *
 * @author Liang Fang lifang@cs.indiana.edu
 */

package xsul.xbeans_secure_document_invoker;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.dsig.saml.authorization.Capability;
import xsul.invoker.capability.CapabilityInvoker;
import xsul.soap11_util.Soap11Util;
import xsul.ws_addressing.WsaEndpointReference;

public class CapXBeansDocumentInvoker extends CapabilityInvoker
{
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    protected static HashMap invokerMap = new HashMap();
    private static final MLogger logger = MLogger.getLogger();
    
    public static CapXBeansDocumentInvoker getInvoker(GlobusCredential cred,
                                                         X509Certificate[] trustedCerts,
                                                         Capability cap,
                                                         String locationUrl) throws URISyntaxException
    {
        
        return getInvoker(cred, trustedCerts, cap, new WsaEndpointReference(new URI(locationUrl)));
    }
    
    public static CapXBeansDocumentInvoker getInvoker(GlobusCredential cred,
                                                         X509Certificate[] trustedCerts,
                                                         Capability cap,
                                                         WsaEndpointReference serviceEpr)
    {
        
        String locationUrl = serviceEpr.getAddress().toString();
        //SecureXBeansDocumentInvoker invoker = (SecureXBeansDocumentInvoker)invokerMap.get(locationUrl);
        
        //if(invoker == null)
        //{
            CapXBeansDocumentInvoker invoker = new CapXBeansDocumentInvoker(cred, trustedCerts, cap, locationUrl);
            //invokerMap.put(locationUrl, invoker);
        //}
        
        return invoker;
    }
    
    protected CapXBeansDocumentInvoker(GlobusCredential cred,
                                          X509Certificate[] trustedCerts,
                                          Capability cap,
                                          String serviceLocation)
    {
        
        super(cred, trustedCerts, cap, serviceLocation);
    }
    
    /**
     * sends the given xml object
     *
     * @param    param               a  XmlObject
     *
     * @return   a XmlObject
     *
     * @exception   XmlException
     *
     */
    public XmlObject invoke(XmlObject param) throws XsulException
    {
        
        String param1Xml = param.xmlText();
        XmlElement requestEl = builder.parseFragmentFromReader(new StringReader(param1Xml));
        
        XmlElement responseEl = super.invokeMessage(requestEl);
        
        processSoap11FaultIfPresent(responseEl);
        String responseXml = builder.serializeToString(responseEl);
        logger.finest("response xml: " + responseXml);
//        if(logger.getLevel().equals(MLogger.Level.ALL))
//            throw new XsulException("whatzzup!");
        try
        {
            
            XmlObject response = XmlObject.Factory.parse(responseXml);
            return response;
            
        }
        catch (XmlException e)
        {
            throw new XsulException("error parsing response for the request: " + param +
                                        "\nreceived : " + responseXml, e);
        }
        
    }
    
    protected void processSoap11FaultIfPresent(XmlElement response)
        throws XmlBuilderException, XsulException
    {
        if("Fault".equals(response.getName())  //ALEK generalize
           && Soap11Util.NS_URI_SOAP11.equals(response.getNamespaceName()) )
        {
            // TODO extract faultcode + faultstring + detail etc.
            //StringWriter sw = new StringWriter();
            String s = builder.serializeToString(response);
            throw new XsulException("remote exception ...."+s);
        }
    }
    
}

