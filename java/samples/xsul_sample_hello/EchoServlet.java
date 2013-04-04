/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoServlet.java,v 1.3 2004/08/23 23:24:23 aslom Exp $
 */

package xsul_sample_hello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.XmlConstants;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.processor.MessageProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

/**
 * Very simple implementation of echo service that demonstrates
 * multi-stage processing in XSUL frominside servlet.
 */
public class EchoServlet extends HttpServlet
{
    private final static boolean DEBUG = true;
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    //private static final XmlPullParserPool pool = new XmlPullParserPool(builder.getFactory());
    private final static  SoapUtil[] soapFragrances =  // list of supported SOAP versions
        new SoapUtil[]{ Soap11Util.getInstance(), Soap12Util.getInstance() };
    private static MessageProcessor msgProcessor; // XML message processor - only for this servlet
    
    
    /** Called when someone accesses the servlet. */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {
        // use this to add ?WSDL and whatnot
        Writer out = response.getWriter();
        response.setContentType("text/html");
        out.write("<html><head><title>XSUL Hello Sample</title></head>");
        out.write("<body bgcolor='white'><h1>This is XSUL Hello Sample!</h1>");
    }
    
    /** Execute SOAP RPC call thatis HTTP POST dispatching to message processor */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        if(msgProcessor == null) initMessageProcessor(req);
        if(DEBUG) System.err.println(getClass()+" entering doPost()");
        
        Reader reader = null;
        try {
            reader = req.getReader();
        } catch(IOException ioe) {
            System.err.println(getClass()+" got "+ioe);
            ioe.printStackTrace();
            throw ioe;
        }
        res.setContentType("text/xml");
        
        // nice ot see what is going on - in production DEBUG should be false
        if(DEBUG) {
            System.err.println(getClass()+" new request");
            int length = req.getContentLength();
            char[] cbuf = new char[length];
            int toRead = length;
            while(toRead > 0) {
                int received = reader.read(cbuf, length - toRead, toRead);
                toRead -= received;
            }
            System.err.println(getClass()+" request ="+new String(cbuf));
            
            BufferedReader sreader = new BufferedReader(
                new StringReader(new String(cbuf)));
            reader = sreader;
        }
        
        if(DEBUG) System.err.println(getClass()+" getting writer");
        Writer rwriter = res.getWriter();
        Writer writer = rwriter;
        if(DEBUG) {
            StringWriter swriter = new StringWriter();
            writer = swriter;
        }
        
        if(DEBUG) System.err.println(getClass()+" dispatching");
        
        
        //msgProcessor.processMessage(message);
        XmlDocument inputXml = builder.parseReader(reader);
        
        XmlDocument outputXml = processXml(inputXml);
        
        if(DEBUG) System.err.println(getClass()+" leaving doPost()");
        
        builder.serializeToWriter(outputXml, writer);
        
        if(DEBUG) {
            String s = writer.toString();
            System.err.println(getClass()+" response = "+s);
            rwriter.write(s);
            rwriter.close();
        }
        writer.close();
    }
    
    private XmlDocument processXml(XmlDocument input)
        throws ServletException, IOException, DynamicInfosetProcessorException
    {
        XmlElement root = input.getDocumentElement();
        SoapUtil soapFragrance = SoapUtil.selectSoapFragrance(root, soapFragrances);
        
//      SoapUtil soapFragrance = null;
//        for (int i = 0; i < soapFragrances.length; i++)
//        {
//          if(soapFragrances[i].isSoapEnvelopeSupported(root)){
//              soapFragrance = soapFragrances[i];
//              break;
//          }
//        }
//
//        if (soapFragrance == null)
//        {
//          throw new ServletException("unsupported XML input {"+root.getName()+"}"+root.getName());
//        }
        
        // extract XML message from SOAP Body content
        XmlElement firstChild = soapFragrance.requiredBodyContent(root);
        // process message
        XmlElement responseMessage = msgProcessor.processMessage(firstChild);
        // wrap message back in SOAP Envelope
        return soapFragrance.wrapBodyContent(responseMessage);
    }
    
    
    private void initMessageProcessor(HttpServletRequest req)
        throws ServletException, IOException
    {
        //        try {
        // how to figure out that it may be https:// ???
        String location = "http://"+req.getServerName()+":"+req.getServerPort()+req.getServletPath();
        
        /* Actual implementationof event receiver embedded in servlet. */
        EchoService srv = new EchoServiceImpl("HelloService");
        
        TypeHandlerRegistry registry = XsdTypeHandlerRegistry.getInstance();
        
        msgProcessor = new SoapRpcReflectionBasedService(srv, registry);
        
        ((SoapRpcReflectionBasedService)msgProcessor).setSupportedSoapFragrances( soapFragrances );
        
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

