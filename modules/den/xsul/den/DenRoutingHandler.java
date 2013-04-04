/**
 * DenRoutingHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DenRoutingHandler.java,v 1.5 2006/04/30 06:48:12 aslom Exp $
 */

package xsul.den;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.parser_pool.XmlPullParserPool;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.den.DenRoutingTable;
import xsul.den.DenWSConnection;
import xsul.den.scheduler.RandomScheduler;
import xsul.den.scheduler.RoundRobinScheduler;
import xsul.den.scheduler.Scheduler;
import xsul.dispatcher.routingtable.RoutingTable;
import xsul.dispatcher.routingtable.WS;
import xsul.http_client.HttpClientResponse;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.message_router.MessageContext;
import xsul.message_router_over_http.HttpMessageContext;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.util.XsulUtil;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.xhandler.BaseHandler;

public class DenRoutingHandler extends BaseHandler {
    
    public static final String STATUS_CODE = "StatusCode";
    public static final String CONTENT_TYPE = "ContentType";
    public static final String REASON_PHRASE = "ReasonPhrase";
    public static final String RESPONSE_CONTENT = "ResponseContent";
    
    //    protected static DenRoutingTable routingTable;
    private Scheduler scheduler;
    
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static final XmlPullParserPool pool =
        new XmlPullParserPool(builder.getFactory());
    
    public DenRoutingHandler(String name) {
        super(name);
        // Launch Routing Table
        scheduler = new RoundRobinScheduler(new DenRoutingTable());
    }
    
    public DenRoutingHandler(String name, String schedulingAlg) {
        super(name);
        // Launch Routing Table
        if(schedulingAlg.equals(RandomScheduler.ALGORITHM)) {
            scheduler = new RandomScheduler(new DenRoutingTable());
        }
        else {
            scheduler = new RoundRobinScheduler(new DenRoutingTable());
        }
    }
    
    public DenRoutingHandler(String name, RoutingTable routingTable) {
        super(name);
        scheduler = new RoundRobinScheduler(routingTable);
    }
    
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    public Scheduler getScheduler() {
        return scheduler;
    }
    
    public boolean processIncomingXml(XmlElement soapEnvelope,
                                      MessageContext context)
        throws DynamicInfosetInvokerException {
        SoapUtil soapUtil =
            SoapUtil.selectSoapFragrance(soapEnvelope, new SoapUtil[]{
                    Soap12Util.getInstance(), Soap11Util.getInstance()});
        // it happens that "Header" doesn't ahve a namespace, which is supposed
        // to be a bug underlying somewhere. so here give it a last try
        XmlElement header = soapEnvelope.element(null, XmlConstants.S_HEADER, false);
        if(header == null) {
            if(soapUtil.getSoapVersion().equals("1.1")) {
                logger.finest("soap 1.1");
                //              if(logger.isFinestEnabled()) {
                //                  logger.finest("soap env ====>>>>\n"+
                //                                    builder.serializeToString(soapEnvelope));
                //              }
                header = soapEnvelope.element(Soap11Util.SOAP11_NS,
                                              Soap11Util.ELEM_HEADER,
                                              true);
            }
            else if(soapUtil.getSoapVersion().equals("1.2")) {
                logger.finest("soap 1.2");
                header = soapEnvelope.element(Soap12Util.SOAP12_NS,
                                              Soap12Util.ELEM_HEADER,
                                              true);
            }
        }
        // put messagecontext into soap header
        XmlElement ctx = header.element(MessageContext.XSUL_CTX_NS,
                                        MessageContext.CONTEXT, true);
        ctx.removeAllChildren();
        for(Iterator iter = context.children(); iter.hasNext();) {
            ctx.addChild(iter.next());
        }
                
        // forward it!
        WS wsHttp = null;
        String arguments = null;
        if(context instanceof HttpMessageContext) {
            String path = ((HttpMessageContext)context).getHttpRequestPath();
            if (path == null) {
                //SendError.send(resp, "404", "Web Services not found on this server");
                logger.warning("The asked path is null");
                // give a chance to the next handler?
                return false;
            }
            else {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                int argStart = path.indexOf('?');
                if (argStart != -1) {
                    // There might be some arguments
                    arguments = path.substring(argStart, path.length());
                    path = path.substring(0, argStart);
                    //                    path = path.replaceAll("\\?.*", "");
                }
                wsHttp = scheduler.choose(path);
                if (wsHttp == null) {
                    //SendError.send(resp, "404", "Web Services not found on this server");
                    logger.warning("The Web Services " + path + " was not found");
                    // give a chance to the next handler?
                    return false;
                }
            }
        }
        else {
            // TODO: handle with WS-Addressing instead of http path
            WsaMessageInformationHeaders wsaheader =
                new WsaMessageInformationHeaders(header);
        }
        
        logger.finest("wshttp host: " + wsHttp.getHost());
        logger.finest("wshttp port: " + wsHttp.getPort());
        logger.finest("wshttp path: " + wsHttp.getPath());
        DenWSConnection dwc = new DenWSConnection(wsHttp);
        Object[] fwdparams = new Object[]{soapEnvelope, arguments, ""};
        dwc.forwards(fwdparams);
        // how to process the response? -- put it into messagecontext
        // it is going to be processed by processOutgoingXml right
        // after finishing this method.
        HttpClientResponse wsResp = (HttpClientResponse)fwdparams[2];
        InputStream inResp = wsResp.getBodyInputStream();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            XsulUtil.copyInput2Output(inResp, bao);
        } catch (IOException e) {
            logger.warning("failed to get client response", e);
            return false;
        }
        String respSoap = bao.toString(); // utf-8?
        String contentType = wsResp.getContentType();
        String reasonPrase = wsResp.getReasonPhrase();
        String statusCode = wsResp.getStatusCode();
        
        // TODO: status code other than 200? -- mya not be further processed
        // put them into msgcontext and keep the handler stateless
        context.addElement(STATUS_CODE).addChild(statusCode);
        context.addElement(RESPONSE_CONTENT).addChild(respSoap);
        context.addElement(CONTENT_TYPE).addChild(contentType);
        context.addElement(REASON_PHRASE).addChild(reasonPrase);
        
        if(logger.isFinestEnabled()) {
            logger.finest("context: =====>\n"
                              + builder.serializeToString(context));
        }
        
        XmlElement outgoingMsg = null;
        XmlPullParser pp = null;
        try {
            pp = pool.getPullParserFromPool();
            pp.setInput(new StringReader(respSoap));
            XmlDocument soapdoc = builder.parse(pp);
            outgoingMsg = soapdoc.getDocumentElement();
        } catch (XmlPullParserException e) {
            throw new DynamicInfosetInvokerException("could not parse soap str", e);
        } finally {
            pool.returnPullParserToPool(pp);
        }
        
        context.setOutgoingMessage(outgoingMsg);
        
        return true;
    }
    
}

