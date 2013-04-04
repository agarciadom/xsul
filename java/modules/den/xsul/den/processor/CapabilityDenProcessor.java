/**
 * CapabilityDenProcessor.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: CapabilityDenProcessor.java,v 1.3 2005/03/19 18:02:56 lifang Exp $
 */

package xsul.den.processor;

import java.io.IOException;
import xsul.MLogger;
import xsul.den.v1.BytesHttpServerRequestImpl;
import xsul.den.DenRoutingTable;
import xsul.den.processor.DistributedProcessor;
import xsul.http_common.HttpConstants;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;

public class CapabilityDenProcessor extends DistributedProcessor {
    
    private final static MLogger logger = MLogger.getLogger();
    
    public CapabilityDenProcessor(DenRoutingTable routingTable) {
        super(routingTable);
    }
    
    protected HttpServerRequest process(HttpServerRequest req)
        throws HttpServerException {

        BytesHttpServerRequestImpl req2 = new BytesHttpServerRequestImpl(req);
        
        try {
            byte[] ibytes = req2.getRequestBytes();
            String requestBody =
                new String(ibytes, HttpConstants.ISO88591_CHARSET);
            logger.finest("ibytes::: " + requestBody);
        } catch (IOException e) {
            logger.severe("failed to get input stream from httprequest", e);
        }
        
        req2.reset();
        return req2;
    }
    
}

