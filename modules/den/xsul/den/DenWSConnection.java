/**
 * DenWSConnection.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DenWSConnection.java,v 1.3 2006/04/18 19:22:58 aslom Exp $
 */

package xsul.den;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.ptls.PureTLSContext;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dispatcher.routingtable.WS;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientRequest;
import xsul.http_client.HttpClientResponse;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.puretls_client_socket_factory.PuretlsClientSocketFactory;
import xsul.util.XsulUtil;

public class DenWSConnection {
    
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private HttpClientRequest fwdRequest;
    private HttpClientConnectionManager cx = null;
    private HttpClientConnectionManager securecx = null;
    private HttpClientConnectionManager nonsecurecx = null;
    private WS wsHttp;
    
    public DenWSConnection(WS wshttp) {
        this.wsHttp = wshttp;
        boolean secure =
            wshttp.getProtocol().equalsIgnoreCase("https") ? true : false;
        if(secure) {
            if(securecx == null) {
                try {
                    PureTLSContext ctx = new PureTLSContext();
                    X509Certificate [] certs =
                        TrustedCertificates.getDefaultTrustedCertificates().getCertificates();
                    ctx.setTrustedCertificates(certs);
                    ctx.setCredential(GlobusCredential.getDefaultCredential());
                    securecx = HttpClientReuseLastConnectionManager.
                        newInstance(new PuretlsClientSocketFactory(ctx));
                } catch(Exception e) {
                    logger.severe("failed to setup secure socket", e);
                }
            }
            cx = securecx;
        }
        else {
            if(nonsecurecx == null) {
                nonsecurecx = HttpClientReuseLastConnectionManager.newInstance();
            }
            cx = nonsecurecx;
        }
    }
    /**
     * <p>
     * Forwards a request from a client to the Web Service and the response.
     * </p>
     * <p>
     * Structure of the parameter <code>request</code>
     * <ul>
     * <li><code>request[0]</code>: the SOAP env</li>
     * <li><code>request[1]</code>: the arguments from client's http request
     * </li>
     * <li><code>request[2]</code>: return HttpClientResponse object for
     * further processing</li>
     * </ul>
     *
     * @param request
     *            the request, response and arguments.
     */
    public void forwards(Object[] request) {
        this.fwdRequest =
            cx.connect(this.wsHttp.getHost(), this.wsHttp.getPort(), 120000);
        
        //Forwarding request from queue
        String wsPath = this.wsHttp.getPath();
        XmlElement soapEnv = (XmlElement)request[0];
        String arguments = (String) request[1];
        if (arguments != null) {
            wsPath += arguments;
        }
        this.fwdRequest.setRequestLine("POST", wsPath, "HTTP/1.0");
        // fixme: supposed utf-8 now
        this.fwdRequest.setContentType("text/xml; charset='UTF-8'");
        HttpClientResponse wsResp = this.fwdRequest.sendHeaders();
        
        //      Copy soap env to fwd
        ByteArrayInputStream bai =
            new ByteArrayInputStream(builder.serializeToString(soapEnv).getBytes());
        OutputStream outToWs = fwdRequest.getBodyOutputStream();
        try {
            XsulUtil.copyInput2Output(bai, outToWs);
        } catch (IOException e) {
            logger.warning("Couldn't communicate with the Web service on: "
                               + wsHttp);
            return;
        }
        
        //      Send response to the client
        wsResp.readStatusLine();
        wsResp.readHeaders();
        request[2] = wsResp; // pass wsResp over
    }
    
}

