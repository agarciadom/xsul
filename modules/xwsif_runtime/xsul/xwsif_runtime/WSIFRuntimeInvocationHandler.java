/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WSIFRuntimeInvocationHandler.java,v 1.14 2005/11/17 21:37:32 aslom Exp $
 */
package xsul.xwsif_runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.type_handler.TypeHandlerException;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.wsdl.WsdlPortType;
import xsul.wsdl.WsdlPortTypeOperation;
import xsul.wsdl.WsdlPortTypeOutput;
import xsul.wsif.WSIFException;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;

/**
 * This si dynamic stub implementating WSDL port/portType
 */
public class WSIFRuntimeInvocationHandler implements InvocationHandler
{
    
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static Method hashCodeMethod;
    private final static Method equalsMethod;
    private final static Method toStringMethod;
    private WSIFPort port;
    private XmlElement lastResponse;
    private TypeHandlerRegistry registry;
    private Map mapNameToOp = new HashMap();
    
    private List handlers;
    
    private boolean xmlElementBasedStub;
    
    private Class portTypeInterface;
    
    
    static {
        try {
            hashCodeMethod = Object.class.getMethod("hashCode", null);
            equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
            toStringMethod = Object.class.getMethod("toString", null);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
    
    public WSIFRuntimeInvocationHandler(
        Class portTypeInterface,
        WSIFPort port,
        TypeHandlerRegistry registry,
        final List handlers,
        final WSIFAsyncResponsesCorrelator correlator,
        final long asyncTimeoutInMs,
        boolean xmlElementBasedStub)
    {
        if(portTypeInterface == null) throw new IllegalArgumentException();
        this.portTypeInterface = portTypeInterface;
        if(port == null) throw new IllegalArgumentException();
        this.port = port;
        if(xmlElementBasedStub) {
            if(registry != null) throw new IllegalArgumentException();
        } else {
            if(registry == null) throw new IllegalArgumentException();
        }
        this.registry = registry;
        if(handlers == null) throw new IllegalArgumentException();
        this.handlers = handlers;
        this.xmlElementBasedStub = xmlElementBasedStub;
        
        
        init();
    }
    
    private void init() {
        // will go for all operaitons -- generates HashMap { Method --> opName }
        // for all creatOP_NAMEMessage will generate HashMap of some kind
        
        //method must not be duplicated
        
        //find corresponding wsdl:portType
        WsdlPortType pt = port.getWsdlServicePort().lookupBinding().lookupPortType();
        // get lsit of operations -- each operaton must map to one Java method
        // also check that number and types of arguments match
        Method[] methods = portTypeInterface.getMethods();
        for(Iterator iter = pt.getOperations().iterator(); iter.hasNext(); ) {
            WsdlPortTypeOperation ptOp = (WsdlPortTypeOperation) iter.next();
            String opName = ptOp.getOperationName();
            
            WSIFOperation op = port.createOperation(opName);
            //WsdlMessage inputMsg = ptOp.getInput().lookupMessage();
            
            //associate operation with Method
            Method m = null;
            String opNameLower = opName.toLowerCase();
            for (int pos = 0; pos < methods.length; pos++) {
                if(methods[pos].getName().toLowerCase().equals(opNameLower)) {
                    if(m != null) {
                        throw new WSIFException("for WSDL operation "+opName+" "+
                                                    portTypeInterface+" has duplicated methods "+m.getName()+
                                                    " and "+methods[pos].getName());
                        
                    }
                    m = methods[pos];
                    
                }
            }
            if(m == null) {
                throw new WSIFException("could not find in "+portTypeInterface
                                            +" method for WSDL operation "+opName);
            }
            mapNameToOp.put(m.getName(), op);
        }
    }
    
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        Class declaringClass = method.getDeclaringClass();
        if (declaringClass == Object.class) {
            if (method.equals(hashCodeMethod)) {
                return proxyHashCode(proxy);
            } else if (method.equals(equalsMethod)) {
                return proxyEquals(proxy, params[0]);
            } else if (method.equals(toStringMethod)) {
                return proxyToString(proxy);
            } else {
                throw new InternalError(
                    "unexpected Object method dispatched: " + method);
            }
        }
        return invokeRemote(proxy, method, params);
    }
    
    public WSIFPort getPort() {
        return port;
    }
    
    
    public WSIFOperation lookupOperationFromMethodName(String methodName) {
        return (WSIFOperation) mapNameToOp.get(methodName);
    }
    
    public Object invokeRemote(Object proxy, Method method, Object[] params) throws Throwable {
        String methodName = method.getName();
        WSIFOperation op = lookupOperationFromMethodName(methodName);
        if(op == null) {
            throw new IllegalStateException("no WSDL operation with name "+methodName);
        }
        
        WSIFMessage in;
        if(xmlElementBasedStub) {
            XmlElement el = (XmlElement) params[0];
            in = new WSIFMessageElement(el);
        } else {
            in = op.createInputMessage();
            //Iterator partNames = in.partNames().iterator();
            if(params != null) {
                Iterator els = ((XmlElement)in).elements(null, null).iterator();
                for (int i = 0; i < params.length; i++) {
                    //determine type of argument
                    //String partName = (String) partNames.next(); //partNames.get(i-2);
                    XmlElement paramContainer = (XmlElement) els.next();
                    String partName = paramContainer.getName();
                    XmlElement paramAsXml = prepareArgumentToSend(params[i], paramContainer);
                    logger.finest(paramAsXml+"="+paramAsXml);
                    in.setObjectPart(partName, paramAsXml.children().next()); //HACK HACK
                }
            }
        }
        
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        
        WsdlPortTypeOutput wsdlOutput = op.getBindingOperation().lookupOperation().getOutput();
        boolean success;
        //if(wsdlOutput != null) {
        success = executeRequestResponseOperation(op, in, out, fault);
        //} else {
        //success = op.executeInputOnlyOperation(in);
        //}
        
        if(success) {
            lastResponse = (XmlElement) out;
            logger.finest("received response XML "+out);
            if(xmlElementBasedStub) {
                return out;
            } else {
                Object value = processResponseMessage((XmlElement)out, method);
                return value;
            }
            
        } else {
            lastResponse = (XmlElement) fault;
            logger.finest("received fault "+fault);
            String s = builder.serializeToString(fault);
            throw new DynamicInfosetInvokerException("remote exception .... "+s, (XmlElement)fault);
        }
    }
    
    public boolean executeRequestResponseOperation(WSIFOperation op,
                                                   WSIFMessage in,
                                                   WSIFMessage out,
                                                   WSIFMessage fault) throws WSIFException
    {
        boolean success = op.executeRequestResponseOperation(in, out, fault);
        return success;
    }
    
    /** Return whole XML message that was received last as repsonse */
    public XmlElement getLastResponse() {
        return lastResponse;
    }
    
    protected void setLastResponse(XmlElement e) {
        lastResponse = e;
    }
    
    
    private XmlElement prepareArgumentToSend(Object param, XmlElement context)
        throws TypeHandlerException
    {
        XmlElement paramAsXml;
        if(param instanceof XmlElement) {
            paramAsXml = (XmlElement) param;
        } else {
            paramAsXml = registry.javaToXmlElement(
                param,
                context.getNamespaceName(),
                context.getName());
        }
        return paramAsXml;
    }
    
    
    /**
     * Extract fault or actual value and comnvert to target return type
     */
    private Object processResponseMessage(XmlElement response, Method method)
        throws XmlBuilderException, XsulException
    {
        Class returnType = method.getReturnType();
        Object value = null;
        if(returnType != null && !returnType.equals(Void.TYPE)) {
            //TODO better more careful extracting -- nextElement !!!!
            XmlElement firstChild = (XmlElement) response.requiredElementContent().iterator().next();
            if(XmlElement.class.isAssignableFrom(returnType)) {
                if(XmlElementAdapter.class.isAssignableFrom(returnType)) {
                    value = XmlElementAdapter.castOrWrap(firstChild, returnType);
                } else {
                    value = firstChild;
                }
            } else {
                value = registry.xmlElementToJava(firstChild, returnType);
            }
        }
        return value;
    }
    
    
    /** Maintain Java semantics over dynamic proxy */
    protected Integer proxyHashCode(Object proxy) {
        return new Integer(System.identityHashCode(proxy));
    }
    
    /** Maintain Java semantics over dynamic proxy */
    protected Boolean proxyEquals(Object proxy, Object other) {
        return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
    }
    
    /** Print simple debug info */
    protected String proxyToString(Object proxy) {
        return proxy.getClass().getName() + '@' +
            Integer.toHexString(proxy.hashCode())
            +" to "+port
            +" using registry "+registry
            +" lastResponse="
            +(lastResponse != null ? builder.serializeToString(lastResponse) : "null")
            ;
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



