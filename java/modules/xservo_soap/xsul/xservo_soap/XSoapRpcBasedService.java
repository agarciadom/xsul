/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XSoapRpcBasedService.java,v 1.6 2005/02/08 05:21:58 aslom Exp $
 */
package xsul.xservo_soap;

import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.message_router.MessageContext;
import xsul.message_router.MessageProcessingException;
import xsul.message_router.MessageRouterException;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlResolver;
import xsul.wsdl.WsdlService;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;
import xsul.xservo.XService;
import xsul.xservo.XServiceBase;
import xsul.common_type_handler.CommonTypeHandlerRegistry;

/**
 * This class provides services over HTTP.
 */
public class XSoapRpcBasedService extends XServiceBase implements XService {
    
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private TypeHandlerRegistry registry = CommonTypeHandlerRegistry.getInstance();
    
    //private static SoapHttpDynamicInfosetProcessor customizedProcessor;
    //private static HttpMiniServer miniMe;
    //private static HttpDynamicInfosetProcessor httpProcessor;
    private SoapRpcReflectionBasedService msgProcessor;
    
    public XSoapRpcBasedService(String name) {
        super(name);
    }
    
    public XSoapRpcBasedService(String name, String wsdlLoc, Object serviceImpl) {
        super(name);
        WsdlDefinitions def = WsdlResolver.getInstance().loadWsdlFromPath(serviceImpl.getClass(), wsdlLoc);
        useWsdl(def);
        useServiceImpl(serviceImpl);
        //startService();
    }
    
    private boolean rpcApi = true;
    
    public void invoke(MessageContext ctx) throws MessageProcessingException {
        //TODO:  now do all reflection magic that is druven by WSDL metadatas
        //throw new XsulException("pika boo");
        XmlElement incomingMsg = ctx.getIncomingMessage();
        // pivot happens here
        XmlElement outgoingMsg;
        //if(!rpcApi) {
        //    // if operation is taking XmlElement then pass whole thing ...
        //    //outgoingMsg = invokeMessage(incomingMsg);
        //    outgoingMsg = null;
        //} else {
        //otherwise use RPC
        outgoingMsg = msgProcessor.processMessage(incomingMsg);
        //}
        ctx.setOutgoingMessage(outgoingMsg);
    }
    
    
    
    public void startService() throws MessageRouterException {
        super.startService();
        if(getWsdl() == null) {
            throw new MessageRouterException("missing WSDL definitions for service");
        }
        WsdlService service = (WsdlService) getWsdl().getServices().iterator().next();
        WsdlPort servicePort =  (WsdlPort) service.getPorts().iterator().next();
        // figure out if it is doc/literal or rpc
        
        if(msgProcessor == null) {
            msgProcessor = new SoapRpcReflectionBasedService(getServiceImpl(), registry);
            msgProcessor.setSupportedSoapFragrances(
                new SoapUtil[]{ Soap11Util.getInstance(), Soap12Util.getInstance() });
        }
    }
    
    
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 *
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 *
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 *
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 *
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */


