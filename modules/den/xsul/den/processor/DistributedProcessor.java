/**
 * ServletClientConnection.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.den.processor;

import java.io.IOException;
import xsul.MLogger;
import xsul.den.v1.BytesHttpServerRequestImpl;
import xsul.den.DenRoutingTable;
import xsul.den.v1.RPCWSConnection;
import xsul.dispatcher.http_util.SendError;
import xsul.dispatcher.routingtable.WS;
import xsul.http_common.HttpConstants;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;

public abstract class DistributedProcessor extends HttpMiniServlet {
    private final static MLogger logger = MLogger.getLogger();
    
    protected static DenRoutingTable routingTable;
    
    public DistributedProcessor(DenRoutingTable routingTable) {
        super();
        DistributedProcessor.routingTable = routingTable;
    }
    
    public void service(HttpServerRequest req, HttpServerResponse resp)
        throws HttpServerException {
        String path = req.getPath();
        
        if (path == null) {
            SendError.send(resp, "404", "Web Services not found on this server");
            logger.warning("The asked path is null");
        } else {
            // Forwards the request to the good WSForwarder
            
            // Analysing path
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            String method = req.getMethod();
            String arguments = null;
            if (method.equals("GET")) {
                // Remove arguments from path
                int argStart = path.indexOf('?');
                if (argStart != -1) {
                    // There are some arguments
                    arguments = path.substring(argStart, path.length());
                    path = path.replaceAll("\\?.*", "");
                }
            }
            
            // Looking for the good WS
            WS wsHttp = (WS)DistributedProcessor.routingTable.get(path);
            
            logger.finest("wshttp host: " + wsHttp.getHost());
            logger.finest("wshttp port: " + wsHttp.getPort());
            logger.finest("wshttp path: " + wsHttp.getPath());
            if (wsHttp == null) {
                SendError.send(resp, "404",
                               "Web Services not found on this server");
                logger.warning("The Web Services " + path + " was not found");
            } else {
                HttpServerRequest req2 = process(req);
                // Forwards request to the Web service
                RPCWSConnection connection = new RPCWSConnection(wsHttp);
                connection.forwards(new Object[] { req2, resp, arguments });
            }
        }
        
    }
    
    abstract protected HttpServerRequest process(HttpServerRequest req)
        throws HttpServerException;
    
    protected HttpServerRequest processBydefault(HttpServerRequest req)
        throws HttpServerException {
        BytesHttpServerRequestImpl req2 = new BytesHttpServerRequestImpl(req);
        
        try {
            byte[] ibytes = req2.getRequestBytes();
            
            String requestBody =
                new String(ibytes, HttpConstants.ISO88591_CHARSET);
            
            logger.finest("ibytes::: " + requestBody);
//            int i = requestBody.indexOf("echoIntPlusOne");
//            if(i <= 0) {
//                logger.finest("let's quit!!!");
//                throw new HttpServerException("let's quit");
//            }
            
            req2.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return req2;
    }
    
}

