/**
 * RPCWSConnection.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.den.v1;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import xsul.MLogger;
import xsul.dispatcher.http_util.SendError;
import xsul.dispatcher.routingtable.WS;
import xsul.dispatcher.routingtable.WSConnection;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientRequest;
import xsul.http_client.HttpClientResponse;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;

public class RPCWSConnection extends WSConnection{
    private final static MLogger logger = MLogger.getLogger();

    private HttpClientRequest fwdRequest;

    private HttpClientConnectionManager cx = null;

    private WS wsHttp;

    /**
     * Creates a new <code>RPCWSConnection</code> with a new connection manager on the
     * specified Web Service.
     *
     * @param wsHttp the specified Web Service.
     *
     * @see WS
     */
    public RPCWSConnection(WS wsHttp) {
        this.wsHttp = wsHttp;

        // Connection creation of reuse
        if (cx == null) {
            cx = HttpClientReuseLastConnectionManager.newInstance();
        }
    }
    
    /**
     * <p>Forwards a request from a client to the Web Service and the response.</p>
     * <p>Structure of the parameter <code>request</code>
     * <ul>
     *  <li><code>request[0]</code>: the HttpServerRequest from client</li>
     *  <li><code>request[1]</code>: the HttpServerResponse from client</li>
     *  <li><code>request[2]</code>: the arguments from client's http request</li>
     * </ul>
     *
     * @param request the request, response and arguments.
     */
    public void forwards(Object[] request) {
        this.fwdRequest = cx.connect(this.wsHttp.getHost(), this.wsHttp
                .getPort(), Integer.parseInt(DenRPC.config
                .getProperty("webservices.timeout")));

        HttpServerRequest req = (HttpServerRequest) request[0];
        HttpServerResponse resp = (HttpServerResponse) request[1];
        String arguments = (String) request[2];

        //Forwarding request from queue
        String wsPath = this.wsHttp.getPath();

        // Arguments to add?
        if (arguments != null) {
            wsPath += arguments;
        }
        this.fwdRequest.setRequestLine(req.getMethod(), wsPath, "HTTP/1.0");
        this.fwdRequest.setContentType(req.getContentType());
        HttpClientResponse wsResp = this.fwdRequest.sendHeaders();

        //      Copy contents form req to fwd
        try {
            InputStream inReq = req.getInputStream();
            OutputStream outToWs = fwdRequest.getBodyOutputStream();
            this.copy(inReq, outToWs);
        } catch (IOException e) {
                SendError.send(resp, "500",
                        "Couldn't communicate with the Web service");
                logger.warning("Couldn't communicate with the Web service on: "
                        + wsHttp);
                return;
        }

        //      Send response to the client
        wsResp.readStatusLine();
        wsResp.readHeaders();
        resp.setContentType(wsResp.getContentType());
        resp.setReasonPhrase(wsResp.getReasonPhrase());
        resp.setStatusCode(wsResp.getStatusCode());
        OutputStream outResp = resp.getOutputStream();
        InputStream inResp = wsResp.getBodyInputStream();
        try {
            this.copy(inResp, outResp);
        } catch (IOException e) {
            logger.warning("Problem with the response", e);
        }
    }

}

