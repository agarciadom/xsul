/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: AddOneService.java,v 1.6 2006/04/30 06:48:15 aslom Exp $
 */

package xsul.msg_box;

import java.net.URI;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.ws_addressing.WsaInvoker;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.ws_addressing.WsaRelatesTo;
import xsul.ws_addressing.WsaUtil;

/**
 * Tests simple interaction with WS-MsgBox.
 *
 * @version $Revision: 1.6 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class AddOneService extends  SoapHttpDynamicInfosetProcessor{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    final static String MESSAGE_URI = "http://example.com/AddOne";
    private final static XmlNamespace MESSAGE_URI_NS = builder.newNamespace(MESSAGE_URI);
    
    
    public static void main (String[] args) {
        
    }
    
    public AddOneService() {
    }
    
    public XmlDocument processSoapEnvelope(XmlElement envelope, final SoapUtil soapFragrance){
        // concert envelope to String
        //                System.err.println(getClass().getName()+" received envelope="
        //                                       +builder.serializeToString(envelope));
        logger.finest("received envelope="+builder.serializeToString(envelope));
        // this XML string could be convertedto DOM ot whatever API one preferes (like JDOM, DOM4J, ...)
        
//        XmlElement soapHeader = envelope.element(null, XmlConstants.S_HEADER_EL);
//        if(soapHeader == null) {
//            throw new XsulException("SOAP message must have headers");
//        }
        final WsaMessageInformationHeaders wsah = new WsaMessageInformationHeaders(envelope);
        //System.err.println(getClass().getName()+" message destinaiton="+wsah.getTo());
        String location = getServer().getLocation();
        //assertEquals(location, wsah.getTo().toString());
        if(!location.equals(wsah.getTo().toString())) {
            throw new IllegalStateException();
        }
        
        final XmlElement message = soapFragrance.requiredBodyContent(envelope);
        
        final XmlElement responseMessage = processMessage(message);
        
        // TODO: fire new thread to send response if replyTo is to not use current HTTP connection
        if(wsah.getReplyTo() != null) {
            //if(wsah.getReplyTo().getAddress().equals(WsAddressing.URI_ROLE_ANONYMOUS)) {
            if(false == WsaUtil.isAsyncReplyRequired(envelope)) {
                //to send back response wrapped in SOAP envelope
                return soapFragrance.wrapBodyContent(responseMessage);
            } else {
                Runnable r = new Runnable() {
                    public void run() {
                        try {
                            XmlDocument responseEnvelope = soapFragrance.wrapBodyContent(responseMessage);
                            // now we need to add all WSA headers etc ...
                            WsaMessageInformationHeaders responseHeaders =
                                new WsaMessageInformationHeaders(responseEnvelope);
                            responseHeaders.explodeEndpointReference(wsah.getReplyTo());
                            URI messageId = wsah.getMessageId();
                            if(messageId != null) {
                                responseHeaders.addRelatesTo(new WsaRelatesTo(wsah.getMessageId()));
                            }
                            WsaInvoker invoker = new WsaInvoker();
                            invoker.setDefaultAction(URI.create(MESSAGE_URI+"Response"));
                            invoker.sendXml(responseEnvelope);
                        } catch (Exception e) {
                            logger.finest("could not send response to "+wsah.getReplyTo(), e);
                        }
                    }
                };
                //LATER: use Executor
                new Thread(r).start();
                // no response sent - actual response will be sent later over new connection
                return null;
            }
        } else {
            // no response needed
            return null;
        }
    }
    
    
    public XmlElement processMessage(XmlElement message) {
        //System.err.println(getClass().getName()+
        logger.finest("received message "+builder.serializeToString(message));
        final XmlElement response; //send no repsonse - one way
        try {
            response = (XmlElement) message.clone();
        } catch (CloneNotSupportedException e) {
            throw new XsulException("internal error", e);
        }
        int value = Integer.parseInt(message.requiredTextContent());
        response.setName(message.getName() + "Response");
        response.replaceChildrenWithText(Integer.toString(value + 1));
        return response;
    }
}






