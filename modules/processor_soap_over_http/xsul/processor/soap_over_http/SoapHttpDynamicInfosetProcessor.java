/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SoapHttpDynamicInfosetProcessor.java,v 1.7 2004/08/13 22:32:06 aslom Exp $
 */

package xsul.processor.soap_over_http;

import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.parser_pool.XmlPullParserPool;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.processor.MessageProcessor;
import xsul.processor.http.HttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;

//import static xsul.XmlConstants.*;

/**
 * This class allows to send XML to HTTP endpoint.
 * Simply set endpoint location and execute invoke*().
 *
 * @version $Revision: 1.7 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public abstract class SoapHttpDynamicInfosetProcessor extends HttpDynamicInfosetProcessor
    implements MessageProcessor
{
    private static final MLogger logger = MLogger.getLogger();
    private static final XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static final XmlPullParserPool pool = new XmlPullParserPool(builder.getFactory());
    
    private SoapUtil[] soapFragrances = new SoapUtil[] { Soap11Util.getInstance() };
    
    public SoapHttpDynamicInfosetProcessor() {
    }
    
    public SoapHttpDynamicInfosetProcessor(int tcpPort) throws DynamicInfosetProcessorException {
        setServerPort(tcpPort);
    }
    
    /**
     * Set the list of supported SOAP fragrances.
     */
    public void setSupportedSoapFragrances(SoapUtil[] soapFragrances) {
        if(soapFragrances == null) throw new IllegalArgumentException();
        this.soapFragrances = soapFragrances;
        
    }
    
    public SoapUtil[] getSupportedSoapFragrances() {
        return soapFragrances;
    }
    
    public XmlDocument processXml(XmlDocument input)
        throws DynamicInfosetProcessorException
    {
        XmlElement root = input.getDocumentElement();
        SoapUtil soapUtil = null;
        for (int i = 0; i < soapFragrances.length; i++)
        {
            if(soapFragrances[i].isSoapEnvelopeSupported(root)){
                soapUtil = soapFragrances[i];
                break;
            }
        }
        //        if(soapUtil == null) {
        //            throw new DynamicInfosetProcessorException(
        //                "unrecognized type of SOAP Envelope "+builder.serializeToString(root));
        //        }
        
        if (soapUtil != null)
        {
            return processSoapEnvelope(root, soapUtil);
        } else {
            return processUnknownXml(input);
        }
    }
    
    
    public XmlDocument processUnknownXml(XmlDocument input)
        throws DynamicInfosetProcessorException
    {
        String s = builder.serializeToString(input);
        String elName = input.getDocumentElement().getName();
        String elNs = input.getDocumentElement().getNamespace().getNamespaceName();
        throw new DynamicInfosetProcessorException(
            "incoming message XML root element "+elName+" (in namespace "+elNs+") is not supported "
                +" by this processorr. XML message was "+s);
    }
    
    public XmlDocument processSoapEnvelope(XmlElement envelope, SoapUtil soapFragrance)
        throws DynamicInfosetProcessorException
    {
        XmlElement firstChild = soapFragrance.requiredBodyContent(envelope);
        //XmlElement body = envelope.findElementByName(Soap11Util.NS_URI_SOAP11, "Body");
        //XmlElement firstChild = (XmlElement) body.requiredElementContent().iterator().next();
        return soapFragrance.wrapBodyContent(processMessage(firstChild));
    }
    
    //    public XmlDocument processSoap11Envelope(XmlElement envelope){
    //        XmlElement firstChild = Soap11Util.getInstance().requiredBodyContent(envelope);
    //        //XmlElement body = envelope.findElementByName(Soap11Util.NS_URI_SOAP11, "Body");
    //        //XmlElement firstChild = (XmlElement) body.requiredElementContent().iterator().next();
    //        return Soap11Util.getInstance().wrapBodyContent(processMessage(firstChild));
    //
    //    }
    
    //    //ALEK public XmlDocument processSoap11Envelope(SoapEnvelope envelope)
    //    private XmlDocument processSoapEnvelope(XmlDocument input,
    //                                            XmlElement envelope,
    //                                            SoapUtil soapUtil)
    //        throws DynamicInfosetProcessorException
    //    {
    //        // TODO find headers if any and process them to check MUST UNDERSTAND!!!!
    //
    //        //
    //
    //        XmlElement body =soapUtil.requiredBodyContent(envelope.getDocument());
    //        XmlElement firstChild = (XmlElement) body.requiredElementContent().iterator().next();
    //        return Soap11Util.getInstance().wrapBodyContent(processMessage(firstChild));
    //    }
    
    
    //    public void processSoap11Header(XmlElement header)
    //        throws DynamicInfosetProcessorException
    //    {
    //        // TODO check if MUST UNDERSTAND
    //        throw new DynamicInfosetProcessorException("not implemented");
    //    }
    
    public abstract XmlElement processMessage(XmlElement message)
        throws DynamicInfosetProcessorException;
    //    {
    //        throw new DynamicInfosetProcessorException("not implemented - this method must be overwriten with implemntation");
    //    }
    
//    // allow extending so for example HTTP Basic Auth check can be added easily!
//    public void service(HttpServerRequest req, HttpServerResponse res)
//        throws HttpServerException
//    {
//        String encoding = req.getCharset().toLowerCase();
//        InputStream is = req.getInputStream();
//        //XmlDocument xmlReq = builder.parseInputStream(is, encoding);
//        Reader reader;
//        if(encoding.equals("utf-8") || encoding.equals("utf8")) {
//            reader = new Utf8Reader(is, 8*1024);
//        } else {
//            try {
//                reader = new InputStreamReader(is, encoding);
//            } catch (UnsupportedEncodingException e) {
//                throw new HttpServerException("could not read input with encoding "+encoding, e);
//            }
//        }
//
//        XmlPullParser pp;
//        try {
//            pp = pool.getPullParserFromPool();
//        } catch(XmlPullParserException e) {
//            throw new HttpServerException("could not get XML pull parser from the pool", e);
//        }
//
//        try {
//            pp.setInput(reader);
//        } catch(XmlPullParserException e) {
//            throw new HttpServerException("could not set parser input", e);
//        }
//
//        XmlDocument xmlReq;
//
//        try {
//            xmlReq = builder.parse(pp);
//        } finally {
//            pool.returnPullParserToPool(pp);
//        }
//        try {
//            reader.close();
//        } catch (IOException e) {
//            throw new HttpServerException("could not close request input stream", e);
//        }
//
//
//
//        XmlDocument xmlRes = processXml(xmlReq);
//
//        OutputStream os = res.getOutputStream();
//        String charset = "UTF-8";
//        res.setContentType("text/xml");
//
//        //builder.serializeToOutputStream(xmlRes, os, charset); //low performance as default UTF8 encoder is used ...
//        Writer utf8Writer = new Utf8Writer(os, 8*1024);
//        builder.serializeToWriter(xmlRes, utf8Writer);
//        try {
//            utf8Writer.close();
//        } catch (IOException e) {
//            throw new HttpServerException("problem when serializing XML result to "+charset);
//        }
//
//    }
//
//    /**
//     * Plumbing as in Java class can not extend two classes ...
//     */
//    private class PlumbingHdisServlet extends HttpMiniServlet {
//        //        private HttpDynamicInfosetProcessor processor;
//        //        public HdisServlet(HttpDynamicInfosetProcessor processor) {
//        //            if(processor == null) throw new IllegalArgumentException();
//        //            this.processor = processor;
//        //        }
//
//        public void service(HttpServerRequest req, HttpServerResponse res)
//            throws HttpServerException
//        {   // NOTE: this casts "this" to enclosing class !!!!
//            SoapHttpDynamicInfosetProcessor.this.service(req, res);
//        }
//    }
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





