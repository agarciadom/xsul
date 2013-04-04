/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SoapRpcInvocationHandler.java,v 1.10 2004/12/03 18:52:37 aslom Exp $
 */

package xsul.soaprpc_client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.MessageInvoker;
import xsul.type_handler.TypeHandlerException;
import xsul.type_handler.TypeHandlerRegistry;

/**
 * This is a dynamic stub implementation (actual stub can be created with Java Dynamic Proxy)
 * that understands generic SOAP invocation enveloping.
 *
 * @version $Revision: 1.10 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class SoapRpcInvocationHandler implements InvocationHandler {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static Method hashCodeMethod;
    private final static Method equalsMethod;
    private final static Method toStringMethod;
    //protected DynamicInfosetInvoker invoker;
    protected MessageInvoker invoker;
    protected XmlElement lastResponse;
    protected TypeHandlerRegistry registry;
    protected XmlNamespace targetServiceNamespace;
    
    
    static {
        try {
            hashCodeMethod = Object.class.getMethod("hashCode", null);
            equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
            toStringMethod = Object.class.getMethod("toString", null);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
    
    public SoapRpcInvocationHandler(MessageInvoker invoker,
                                    TypeHandlerRegistry registry)
    {
        this(invoker, registry, null);
    }
    
    public SoapRpcInvocationHandler(MessageInvoker invoker,
                                    TypeHandlerRegistry registry,
                                    XmlNamespace targetServiceNamespace)
    {
        if(invoker == null) throw new IllegalArgumentException();
        this.invoker = invoker;
        if(registry == null) throw new IllegalArgumentException();
        this.registry = registry;
        this.targetServiceNamespace = targetServiceNamespace;
    }
    
    
    public void setRegistry(TypeHandlerRegistry registry) {
        this.registry = registry;
    }
    
    public TypeHandlerRegistry getRegistry() {
        return registry;
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
        
        XmlElement requestMsg = prepareMessageToSend(method, params);
        
        Object value = null;
        if(requestMsg != null) {
            XmlElement responseMsg = invokeRemoteEndpoint(requestMsg);
            if(responseMsg != null) { // null means there was no response (one-way)
                value = processResponseMessage(responseMsg, method);
            }
        }
        
        return value;
    }
    
    /** Return whole XML message that was received last as repsonse */
    public XmlElement getLastResponse() {
        return lastResponse;
    }
    
    public void setTargetServiceNamespace(XmlNamespace targetServiceNamespace) {
        this.targetServiceNamespace = targetServiceNamespace;
    }
    
    public XmlNamespace getTargetServiceNamespace() {
        return targetServiceNamespace;
    }
    
    /* ----- Here are entry points to customzie any aspect of this RPCish invocaiton handler --- */
    
    protected String getArgumentName(int i, Method method, Object[] params) {
        return "p"+(i+1);
    }
    
    protected String getArgumentNamespace(int i, Method method, Object[] params) {
        return null;
    }
    
    protected XmlElement prepareArgumentToSend(int i, Method method, Object[] params)
        throws TypeHandlerException
    {
        XmlElement paramAsXml;
        if(params[i] instanceof XmlElement) {
            paramAsXml = (XmlElement) params[i];
        } else {
            paramAsXml = registry.javaToXmlElement(
                params[i],
                getArgumentNamespace(i, method, params),
                getArgumentName(i, method, params));
        }
        return paramAsXml;
    }
    
    /** When overriden return null to indicate that default action should not be executed */
    protected XmlElement prepareMessageToSend(Method method, Object[] params)
        throws TypeHandlerException, XmlBuilderException
    {
        String methodName = method.getName();
        XmlElement message = builder.newFragment( targetServiceNamespace, methodName );
        //This is place to add headers etc.
        
        // no way to determine parameter names ...
        if(params != null) {
            for (int i = 0; i < params.length; i++)
            {
                //XmlElement param = message.addElement("param"+i);
                //param.addChild(params[i]);
                XmlElement param = prepareArgumentToSend(i, method, params);
                message.addChild(param);
            }
        }
        return message;
    }
    
    
    
    /**
     * If null is returned it is assumend that one way operaiton was invoked
     * and RPC shoul dbe delcared void (as null will be returned)
     */
    protected XmlElement invokeRemoteEndpoint(XmlElement message)
        throws DynamicInfosetInvokerException
    {
        XmlElement response = lastResponse = invoker.invokeMessage(message);
        return response;
    }
    
    /**
     * Extract fault or actual value and comnvert to target return type
     */
    protected Object processResponseMessage(XmlElement response, Method method)
        throws XmlBuilderException, XsulException
    {
        processSoapFaultIfPresent(response);
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
    
    protected void processSoapFaultIfPresent(XmlElement possibleFault)
        throws XmlBuilderException, XsulException
    {
        if("Fault".equals(possibleFault.getName())) {
            //ALEK: Leaky Abstraciton: now can process both SOAP 1.1 and 1.2 ...
            if(XmlConstants.NS_URI_SOAP11.equals(possibleFault.getNamespaceName())
                   || XmlConstants.NS_URI_SOAP12.equals(possibleFault.getNamespaceName()) )
            {
                // TODO extract faultcode + faultstring + detail etc.
                //StringWriter sw = new StringWriter();
                String s = builder.serializeToString(possibleFault);
                throw new DynamicInfosetInvokerException("remote exception .... "+s, possibleFault);
                
                //throw new XsulException("remote exception .... "+s);
            }
        }
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
            +" to "+invoker
            +" using registry "+registry
            +" lastResponse="
            +(lastResponse != null ? builder.serializeToString(lastResponse) : "null")
            ;
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








