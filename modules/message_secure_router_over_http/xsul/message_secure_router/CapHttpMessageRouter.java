/**
 * CapabilityProcessingNode.java
 *
 * @author Liang Fang lifang@cs.indiana.edu
 */

package xsul.message_secure_router;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.SOAPEnvelopeSigner;
import xsul.dsig.SOAPEnvelopeVerifier;
import xsul.dsig.SignatureInfo;
import xsul.dsig.saml.CapSignatureInfo;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityAuthorizer;
import xsul.dsig.saml.authorization.CapabilityException;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;
import xsul.message_router.MessageContext;
import xsul.message_router.MessageProcessingException;
import xsul.message_router.MessageRouter;
import xsul.message_router.MessageRouterException;
import xsul.message_router_over_http.HttpMessageContext;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;


public abstract class CapHttpMessageRouter implements MessageRouter {
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder =  XmlConstants.BUILDER;
    private SOAPEnvelopeSigner signer;
    private SOAPEnvelopeVerifier verifier;
    private CapabilityAuthorizer authorizer;

    private HttpMiniServer server;
    private RouterServlet servlet;

    public HttpMiniServer getHttpServer() { return server; }

    public void startService() throws MessageRouterException {
        try {
            server.startServer();
        }
        catch (HttpServerException e) {
            throw new MessageRouterException("could not start router on "+server.getLocation(), e);
        }
    }

    public void stopService() throws MessageRouterException {
        try {
            server.stopServer();
        }
        catch (HttpServerException e) {
            throw new MessageRouterException("could not start router on "+server.getLocation(), e);
        }
    }

    public void shutdownService() throws MessageRouterException {
        try {
            server.shutdownServer();
        }
        catch (HttpServerException e) {
            throw new MessageRouterException("could not start router on "+server.getLocation(), e);
        }
    }

    /**
     * Constructor
     *
     * @param    verifier            a  SOAPEnvelopeVerifier
     * @param    authorizer          a  CapabilityAuthorizer
     * @param    signer              a  SOAPEnvelopeSigner
     */
    public CapHttpMessageRouter(int port,
                                SOAPEnvelopeSigner signer,
                                SOAPEnvelopeVerifier verifier,
                                CapabilityAuthorizer authorizer) {
        try {
            server = new HttpMiniServer(port);
        }
        catch (HttpServerException e) {
            throw new MessageRouterException("could not create router on TCP port "+port, e);
        }
        servlet = new RouterServlet();
        server.useServlet(servlet);

        this.signer = signer;
        this.verifier = verifier;
        this.authorizer = authorizer;
    }

    public CapHttpMessageRouter(int port,
                                SOAPEnvelopeSigner signer,
                                SOAPEnvelopeVerifier verifier) {
        try {
            server = new HttpMiniServer(port);
        }
        catch (HttpServerException e) {
            throw new MessageRouterException("could not create router on TCP port "+port, e);
        }
        servlet = new RouterServlet();
        server.useServlet(servlet);

        this.signer = signer;
        this.verifier = verifier;
    }

    public CapHttpMessageRouter() {
    }

    public SOAPEnvelopeVerifier getVerifier() {
        return verifier;
    }

    public void setVerifier(SOAPEnvelopeVerifier _verifier) {
        verifier = _verifier;
    }

    public CapabilityAuthorizer getAuthorizer() {
        return authorizer;
    }

    public void setAuthorizer(CapabilityAuthorizer _authorizer) {
        authorizer = _authorizer;
    }

    public SOAPEnvelopeSigner getSigner() {
        return signer;
    }

    public void setVerifier(SOAPEnvelopeSigner _signer) {
        signer = _signer;
    }

    public abstract boolean process(MessageContext context) throws MessageProcessingException;

    //NOTE: allows extending so for example HTTP Basic Auth check can be added easily!
    public void service(HttpServerRequest req, HttpServerResponse res)
        throws HttpServerException {
        String encoding = req.getCharset();
        InputStream is = req.getInputStream();

        XmlDocument incomingXmlDoc = builder.parseInputStream(is, encoding);

        HttpMessageContext ctx = new HttpMessageContext(req);

        XmlElement incomingXml = incomingXmlDoc.getDocumentElement();

        XmlElement envelope = incomingXml;
        SignatureInfo si = verifier.verifySoapMessage(envelope);
        logger.finest("Starting authorizatrion ...");
        boolean authorizationFailure = false;
        try {
            Principal pr = si.getSubjectDn();

            if(pr == null){
                throw new CapabilityException("principal null");
            }

            if(si instanceof CapSignatureInfo) {

                if(authorizer == null){
                    throw new CapabilityException("Authorizer null");
                }

                // Authorize the Capability
                Capability apc = ((CapSignatureInfo)si).getCapability();
                if(apc == null) {
                    throw new CapabilityException("Capability null");
                }
                logger.finest("authr=" + authorizer + "; cap=" + apc);
                authorizer.isAuthorized(pr, apc, envelope);
            }
            else {
                if(authorizer == null) {
                    logger.finest("Authorizer null");
                    isAuthorized(pr, envelope);
                }
                else {
                    logger.finest("Capability not found");
                    throw new CapabilityException("Capability not found");
                }
            }
        }
        catch(Exception e) {
            authorizationFailure = true;
            logger.finest("Exception while authorizing: " + e.getMessage());
            SoapUtil soapUtil = Soap11Util.getInstance();
            XmlDocument fault =
                soapUtil.wrapBodyContent(
                soapUtil.generateSoapClientFault(
                                            "unathorized access ",
                                            e)
            );
            ctx.setOutgoingMessage(fault.getDocumentElement());

        }

        logger.finest("message_after_processing_by_cap_router: \n" +
                          builder.serializeToString(incomingXml));
        String soaped = null;
        if(XmlConstants.S_ENVELOPE.equals(incomingXml.getName())) {
            String n = incomingXml.getNamespaceName();
            logger.finest("namespace: " + n);
            //ALEK fix it !!!!
            if(Soap11Util.NS_URI_SOAP11.equals(n)) {
                soaped = Soap11Util.NS_URI_SOAP11;
                // extract body content
                XmlElement body = incomingXml.element(Soap11Util.SOAP11_NS, Soap11Util.ELEM_BODY);
                incomingXml = (XmlElement) body.requiredElementContent().iterator().next();
            }
            else if(Soap12Util.NS_URI_SOAP12.equals(n)) {
                soaped = Soap12Util.NS_URI_SOAP12;
                // extract body content
                XmlElement body = incomingXml.element(Soap12Util.SOAP12_NS, Soap12Util.ELEM_BODY);
                incomingXml = (XmlElement) body.requiredElementContent().iterator().next();
            }
        }

        logger.finest("outgoing message from cap router: \n"+builder.serializeToString(incomingXml));

        ctx.setIncomingMessage(incomingXml);
        //XmlDocument xmlRes = processXml(xmlReq);

        XmlElement outgoingXml = null;

        OutputStream os = res.getOutputStream();
        String charset = "UTF-8";
        res.setContentType("text/xml");

        if(authorizationFailure || process(ctx)) { // actual processing
            outgoingXml = ctx.getOutgoingMessage();
            //TODO!!!!!!!!!!!!: handle faults (message.name()=="Fault" to send HTTP 500
            logger.finest("outgoing message after process: \n" +
                              builder.serializeToString(outgoingXml));

            if(outgoingXml != null) {
                if(outgoingXml.getParent() == null) {
                    logger.finest("outgoing xml getparent == null");
                    if(soaped != null) {
                        // wrap message in SOAP Envelope as it is not yet soap-ified :-)
                        XmlDocument doc = null;
                        SoapUtil soapUtil;
                        if(soaped == Soap11Util.NS_URI_SOAP11) {
                            soapUtil = Soap11Util.getInstance();
                        }
                        else if(soaped == Soap12Util.NS_URI_SOAP12) {
                            soapUtil = Soap12Util.getInstance();
                        }
                        else {
                            throw new IllegalStateException("unsupported SOAP "+soaped);
                        }
                        doc = soapUtil.wrapBodyContent(outgoingXml);
                        XmlDocument signedDoc = signer.signSoapMessage(doc);
                        builder.serializeToOutputStream(signedDoc, os, charset);
                    }
                    else {
                        logger.finest("outgoing xml soaped == null");
                        builder.serializeToOutputStream(outgoingXml, os, charset); //check efficiency!
                    }
                }
                else {
                    //                    String serialized = builder.serializeToString(outgoingXml);
                    //                    logger.finest("parent: " + serialized);
                    //                    XmlElement newElem = builder.newFragment(serialized);
                    outgoingXml.setParent(null);
                    XmlElement newElem = outgoingXml;
                    SoapUtil soapUtil;
                    if(soaped == Soap11Util.NS_URI_SOAP11) {
                        soapUtil = Soap11Util.getInstance();
                        XmlElement body = outgoingXml.element(Soap11Util.SOAP11_NS, "Body");
                        newElem = (XmlElement) body.requiredElementContent().iterator().next();
                    }
                    else if(soaped == Soap12Util.NS_URI_SOAP12) {
                        soapUtil = Soap12Util.getInstance();
                        XmlElement body = outgoingXml.element(Soap12Util.SOAP12_NS, "Body");
                        newElem = (XmlElement) body.requiredElementContent().iterator().next();
                    }
                    else {
                        throw new IllegalStateException("unsupported SOAP "+soaped);
                    }
                    // fixme: how to sign the XmlElement with parent?
                    //                    String serialized = builder.serializeToString(newElem);
                    //                    logger.finest("serialized: " + serialized);

                    //                    logger.finest("serialized2: " +builder.serializeToString(newElem));
                    newElem.setParent(null);
                    XmlDocument doc = soapUtil.wrapBodyContent(newElem);
                    XmlDocument signedDoc = signer.signSoapMessage(doc);
                    builder.serializeToOutputStream(signedDoc, os, charset);

                    //                  XmlContainer top = outgoingXml.getRoot();
                    //                    builder.serializeToOutputStream(top, os, charset);
                }
            }
        }
        else {
            final String msg = "could not find service to process message";
            //res.setStatus(500, msg);
            XmlDocument doc = null;
            SoapUtil soapUtil;
            if(soaped == Soap11Util.NS_URI_SOAP11) {
                soapUtil = Soap11Util.getInstance();
            }
            else if(soaped == Soap12Util.NS_URI_SOAP12) {
                soapUtil = Soap12Util.getInstance();
            }
            else {
                throw new MessageRouterException(msg);
            }
            doc = soapUtil.wrapBodyContent(
                soapUtil.generateSoapServerFault(msg, null));
            XmlDocument signedDoc = signer.signSoapMessage(doc);
            builder.serializeToOutputStream(signedDoc, os, charset);
        }

        try {
            os.close();
        }
        catch (IOException e) {
            logger.finest("Ignore: " + e.getMessage());
        }

    }

    private void isAuthorized(Principal prin, XmlElement envelope)
        throws Exception {
    }

    private class RouterServlet extends HttpMiniServlet {
        public void service(HttpServerRequest req, HttpServerResponse res)
            throws HttpServerException {
            CapHttpMessageRouter.this.service(req, res);
        }
    }
}

