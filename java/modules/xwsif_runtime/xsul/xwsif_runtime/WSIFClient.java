/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WSIFClient.java,v 1.12 2006/04/21 20:05:32 aslom Exp $
 */
package xsul.xwsif_runtime;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.common_type_handler.CommonTypeHandlerRegistry;
import xsul.invoker.soap.SoapDynamicInfosetInvoker;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.wsdl.WsdlPort;
import xsul.wsif.WSIFException;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif_xsul_soap_http.XsulSoapPort;
import xsul.xhandler.XHandler;
import xsul.xhandler.XHandlerContext;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;

/**
 * This class encapsulates WSIF functionality to make *really* easy to invoke service described in WSDL.
 */
public class WSIFClient {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private String portName;
    protected TypeHandlerRegistry typeRegistry = CommonTypeHandlerRegistry.getInstance();
    protected List chainOfHandlers = new ArrayList();
    private WSIFService serv;
    //private Class portTypeInterface;
    protected WSIFPort port;
    private XHandlerContext handlerContext;
    protected boolean xmlElementBasedStub;
    protected long asyncResponseTimeoutInMs = 0; //4 * 60 * 1000L; // 0 = wait indefinitely
    
    protected  WSIFAsyncResponsesCorrelator correlator;
    
    private WSIFAsyncSoapHttpDiiWithHandlers customInvoker;
    
    private SoapDynamicInfosetInvoker portInvoker;
    
    public WSIFClient(WSIFPort port) throws WSIFException {
        //this.port = port;
        //this.portTypeInterface = portTypeInterface;
        WsdlPort wsdlPort = port.getWsdlServicePort();
        this.handlerContext = new XHandlerContext(wsdlPort);
        //overrideAndRegenerateInvoker(port);
        setPort(port);
    }
    
    public void setPort(WSIFPort port) {
        this.port = port;
        if(port instanceof XsulSoapPort) {
            this.portInvoker = ((XsulSoapPort)port).getInvoker();
            overrideAndRegenerateInvoker(port);
            //            customInvoker = new WSIFAsyncSoapHttpDiiWithHandlers(
            //                portInvoker, chainOfHandlers, correlator, asyncResponseTimeoutInMs);
            //            ((XsulSoapPort)port).setInvoker(customInvoker);
            
        }
    }
    
    public WSIFPort getPort() {
        return port;
    }
    
    protected void overrideAndRegenerateInvoker(WSIFPort port) {
        if(port instanceof XsulSoapPort) {
            customInvoker = new WSIFAsyncSoapHttpDiiWithHandlers(
                portInvoker, chainOfHandlers, correlator, asyncResponseTimeoutInMs);
            ((XsulSoapPort)port).setInvoker(customInvoker);
        }
    }
    
    public WSIFClient setAsyncResponseTimeoutInMs(long asyncTimeoutInMs) {
        this.asyncResponseTimeoutInMs = asyncTimeoutInMs;
        if(customInvoker != null) {
            customInvoker.setAsyncTimeoutInMs(asyncTimeoutInMs);
        }
        return this;
    }
    
    public long getAsyncResponseTimeoutInMs() {
        return asyncResponseTimeoutInMs;
    }
    
    
    public WSIFClient useAsyncMessaging(WSIFAsyncResponsesCorrelator correlator) {
        this.correlator = correlator;
        overrideAndRegenerateInvoker(port); //VERY UGLY ...
        return this;
    }
    
    public WSIFClient addHandler(XHandler handler) {
        chainOfHandlers.add(handler);
        handler.init(handlerContext);
        return this;
    }
    
    //JDK15 T createStubFor<T>(WsdlDefinitions def, T portTypeInterface) see castOrWrap<T>
    //public Object createStubFor(WsdlDefinitions def, Class portTypeInterface) throws WSIFException {
    //JDK15 T createStubFor<T>(WsdlDefinitions def, T portTypeInterface) see castOrWrap<T>
    public Object generateDynamicStub(Class portTypeInterface) throws WSIFException {
        
        
        //        List copyOfHandlers = new ArrayList(); //(List) globalHandlers.clone();
        //        for (int i = 0; i < handlers.size(); i++) {
        //            copyOfHandlers.add(handlers.get(i));
        //        }
        if(XmlElementBasedStub.class.isAssignableFrom( portTypeInterface ) ) {
            xmlElementBasedStub = true;
            typeRegistry = null;
        }
        
        WSIFRuntimeInvocationHandler handler = createInvocationHandler(portTypeInterface);
        
        Object ref = generateProxy(handler, portTypeInterface);
        
        return ref;
        
    }
    
    protected Object generateProxy(WSIFRuntimeInvocationHandler handler, Class portTypeInterface)
        throws IllegalArgumentException
    {
        Object ref = Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { portTypeInterface },
            handler);
        return ref;
    }
    
    //        WSIFPort port,
    //        TypeHandlerRegistry typeRegistry,
    //        List chainOfHandlers,
    //        final WSIFAsyncResponsesCorrelator correlator,
    //        final long asyncTimeoutInMs
    protected WSIFRuntimeInvocationHandler createInvocationHandler(
        Class portTypeInterface)
    {
        WSIFRuntimeInvocationHandler invocationHandler = new WSIFRuntimeInvocationHandler(
            portTypeInterface, port, typeRegistry, chainOfHandlers, correlator, asyncResponseTimeoutInMs, xmlElementBasedStub);
        ////do magic and override provider port -- could be more elegant !!!!
        //overrideAndRegenerateInvoker(port); //UGLY ...
        return invocationHandler;
    }
    
    
    //    public void setPortName(String portName) {
    //        this.portName = portName;
    //    }
    //
    //    public String getPortName() {
    //        return portName;
    //    }
    
    public void setRegistry(TypeHandlerRegistry registry) {
        this.typeRegistry = registry;
    }
    
    public TypeHandlerRegistry getRegistry() {
        return typeRegistry;
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


