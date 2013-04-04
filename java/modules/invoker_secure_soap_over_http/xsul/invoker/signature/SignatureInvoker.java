/**
 * SignatureInvoker.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.invoker.signature;

import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeSigner;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeVerifier;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;

public class SignatureInvoker extends SoapHttpDynamicInfosetInvoker
{
    private GlobusCredential cred;
    private X509Certificate[] trustedCerts;
    
    public SignatureInvoker(GlobusCredential cred,
                            X509Certificate[] trustedCerts,
                            String location) {
        super(location);
        this.cred = cred;
        this.trustedCerts = trustedCerts;
    }
    
    public XmlDocument invokeXml(XmlDocument request)
        throws DynamicInfosetInvokerException
    {
        //XmlDocument response = super.invokeXml(request);
        //WSRMHandler.getInstance().augmentRequest(request);
        XmlDocument signedRequest =
            GlobusCredSOAPEnvelopeSigner.getInstance(cred).signSoapMessage(request);
        //      System.out.println(builder.serializeToString(signedRequest));
        XmlDocument response = super.invokeXml(signedRequest);
        GlobusCredSOAPEnvelopeVerifier.getInstance(cred, trustedCerts).verifySoapMessage(response);
        //WSRMHandler.getInstance().processResponse(response);
        return response;
    }
    
}

