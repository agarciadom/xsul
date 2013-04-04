/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //- -----100-columns-wide------>*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpEndpointReferenceBasedRouter.java,v 1.4 2004/03/08 21:39:25 ysimmhan Exp $
 */

package xsul.epr_router;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.xmlpull.v1.builder.XmlElement;
import xsul.MLogger;
import xsul.XsulException;
import xsul.message_router.MessageContext;
import xsul.message_router.MessageProcessingException;
import xsul.message_router.MessageProcessingNode;
import xsul.message_router_over_http.HttpMessageContext;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.xbeans_document_dispatcher.XBeansDocumentDispatcher;

/**
 * Routes based on WSA EPR to specific XBeans document dispatcher
 *
 * @version $Revision: 1.4 $
 * @author Yogesh L. Simmhan [mailto:ysimmhan@cs.indiana.edu]
 */
public class HttpEndpointReferenceBasedRouter implements MessageProcessingNode {

    private static final MLogger logger = MLogger.getLogger();

    HashMap endpointToProcessor;

    public HttpEndpointReferenceBasedRouter() throws DynamicInfosetProcessorException {

        this.endpointToProcessor = new HashMap();
    }

    /**
     * gets the document processor for the given endpoint. If none available, one will be created.
     *
     * @param    endpoint            an Endpoint
     *
     * @return   an EndpointDocumentProcessor
     *
     */
    public XBeansDocumentDispatcher getDocumentProcessor(HttpEndpointReference endpoint){

        XBeansDocumentDispatcher endpointDocProcessor =
            (XBeansDocumentDispatcher)endpointToProcessor.get(endpoint);

        if(endpointDocProcessor == null){
            endpointDocProcessor = new XBeansDocumentDispatcher();
            endpointToProcessor.put(endpoint, endpointDocProcessor);
        }

        return endpointDocProcessor;
    }

    /**
     * sets the document processot for the given endpoint, replacing the previous one.
     *
     * @param    endpointDocProcessoran EndpointDocumentProcessor
     *
     * @exception   IOException
     * @exception   DynamicInfosetProcessorException
     *
     */
    public void setDocumentProcessor(HttpEndpointReference endpoint,
                                     XBeansDocumentDispatcher endpointDocProcessor) {

        // check if doc processor already exists and is not this
        XBeansDocumentDispatcher existingEndpointDocProcessor =
            (XBeansDocumentDispatcher)endpointToProcessor.get(endpoint);
        if(existingEndpointDocProcessor != null &&
               endpointDocProcessor != existingEndpointDocProcessor){

            throw new XsulException("Cannot add document processor to HTTP EPR router. " +
                                        "There already exists another document processor for the endpoint: "
                                        + endpoint + "; Do a getDocumentProcessor to get " +
                                        "this processor and use it instead");
        }

        endpointToProcessor.put(endpoint, endpointDocProcessor);
        logger.fine("Adding mapping for endpoint: " + endpoint.toString() + " to " + endpointDocProcessor);
    }

    /**
     * Method removeDocumentProcessor. Removes the document processor for given EPR
     *
     * @param    endpoint            a  HttpEndpointReference
     *
     * @return   true if processor exited for EPR and was removed. False if processor dod not exist.
     *
     */
    public boolean removeDocumentProcessor(HttpEndpointReference endpoint){

        boolean exists = endpointToProcessor.get(endpoint) != null;
        if(exists){
            endpointToProcessor.remove(endpoint);
        }
        return exists;
    }

    /**
     * check if message context is http content.
     * parse message context to get the HTTP path, which corresponds
     * to the endpoint address in ws-addressing. locate the handler for this
     * endpoint and call the process method in the handler.
     * Return true to indicate that processingis finished
     * (no more links inchains will be called).
     *
     */
    public boolean process(MessageContext context) throws MessageProcessingException {

        // if not http, we no not handle this...parent should continue processing
        if(!(context instanceof HttpMessageContext)){

            return false;
        }

        HttpMessageContext httpContext = (HttpMessageContext)context;

        // create endpoint from context. later we will get the endpoint as part of context.
        String httpPath = httpContext.getHttpRequestPath();
        HttpEndpointReference endpoint = null;
        try {

            endpoint = new HttpEndpointReference("http://" + httpPath);

        } catch (URISyntaxException e) {
            throw new XsulException(e.getMessage(), e);
        }

        XBeansDocumentDispatcher docDispatcher =
            (XBeansDocumentDispatcher)endpointToProcessor.get(endpoint);

        logger.fine("routing message to endpoint=" + endpoint +" to dispatcher " + docDispatcher);

        if(docDispatcher == null){
            // could not find doc dispatcher for endpoint
            return false;
        }
        // found endpoint handler. extract request document and send to document handler
        XmlElement inXml = httpContext.getIncomingMessage();

        //ALEK
        SoapUtil soapUtil = Soap11Util.getInstance();
        //SoapEnvelope env = SoapUtil.castSoapEnvelope(inXml); //get envelope for SOAP 1.1 or 1.2
        //if(env != null) {
        //  //SoapUtil util = env.getSoapUtil(); //get utilities for SOAP 1.1 or SOAP 1.2
        //  inXml = env.getBody().firstChild();
        //}
        //XmlElement responseXml = docDispatcher.processMessage(inXml);
        //if(responseDoc != null){
        //  if(env != null) {
        //     SoapEnvelope responseEnv = env.newNevelope();
        //     responseXml = responseEnv.getBody().addElement(responseDoc);
        //     //responseXml = responseEnv.getBody().firstChild());
        //   }
        //   context.setOutgoingMessage(responseXml);
        //   return true;
        // } else {
        //    return false;
        // }
        //ALEK

        XmlElement bodyPayload = inXml; //soapUtil.requiredBodyContent(inXml); // false);
        // call endpoint doc handler to handle the doc that arrived at this endpoint
        XmlElement responseDoc = docDispatcher.processMessage(bodyPayload);

        if(responseDoc == null){
            // could not find method in dispatcher that can handle this message
            return false;
        }

        context.setOutgoingMessage(soapUtil.wrapBodyContent(responseDoc).getDocumentElement());
        return true;
    }


}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1) All redistributions of source code must retain the above
 *    copyright notice, the list of authors in the original source
 *    code, this list of conditions and the disclaimer listed in this
 *    license;
 *
 * 2) All redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the disclaimer
 *    listed in this license in the documentation and/or other
 *    materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include
 *    the following acknowledgement:
 *
 *      "This product includes software developed by the Indiana
 *      University Extreme! Lab.  For further information please visit
 *      http://www.extreme.indiana.edu/"
 *
 *    Alternatively, this acknowledgment may appear in the software
 *    itself, and wherever such third-party acknowledgments normally
 *    appear.
 *
 * 4) The name "Indiana University" or "Indiana University
 *    Extreme! Lab" shall not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission from Indiana University.  For written permission,
 *    please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana
 *    University" name nor may "Indiana University" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code
 * provided does not infringe the patent or any other intellectual
 * property rights of any other entity.  Indiana University disclaims any
 * liability to any recipient for claims brought by any other entity
 * based on infringement of intellectual property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH
 * NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA
 * UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT
 * SOFTWARE IS FREE OF INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR
 * OTHER PROPRIETARY RIGHTS.  INDIANA UNIVERSITY MAKES NO WARRANTIES THAT
 * SOFTWARE IS FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP
 * DOORS", "WORMS", OR OTHER HARMFUL CODE.  LICENSEE ASSUMES THE ENTIRE
 * RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS,
 * AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION GENERATED USING
 * SOFTWARE.
 */


