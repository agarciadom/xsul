/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XSoapDocLiteralService.java,v 1.4 2006/04/18 18:03:47 aslom Exp $
 */
package xsul.xservo_soap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.message_router.MessageContext;
import xsul.message_router.MessageProcessingException;
import xsul.message_router.MessageRouterException;
import xsul.util.XsulUtil;
import xsul.wsdl.WsdlBinding;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlMessage;
import xsul.wsdl.WsdlMessagePart;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlPortType;
import xsul.wsdl.WsdlPortTypeInput;
import xsul.wsdl.WsdlPortTypeOperation;
import xsul.wsdl.WsdlPortTypeOutput;
import xsul.wsdl.WsdlResolver;
import xsul.wsdl.WsdlService;
import xsul.xservo.XService;
import xsul.xservo.XServiceBase;

/**
 * This class provides simple doc/literal services over HTTP.
 */
public class XSoapDocLiteralService extends XServiceBase implements XService {
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private Map messageToMethod = new HashMap();
    private Map methodNameToMethod = new HashMap();
    
    protected Method lookupMethodWithQNameParameter(QName qn) {
        return (Method) messageToMethod.get(qn);
    }
    
    protected void setMethodWithQNameParameter(QName qn, Method m) {
        //logger.finest("qn "+qn+" -> "+m.getName()+" m="+m);
        messageToMethod.put(qn, m);
    }
    
    
    public XSoapDocLiteralService(String name) {
        super(name);
    }
    
    
    public XSoapDocLiteralService(String name, String wsdlLoc, Object serviceImpl) {
        super(name);
        WsdlDefinitions def = WsdlResolver.getInstance().loadWsdlFromPath(serviceImpl.getClass(), wsdlLoc);
        useWsdl(def);
        useServiceImpl(serviceImpl);
        //startService();
    }
    
    public void startService() throws MessageRouterException {
        super.startService();
        WsdlDefinitions wsdlDefs = getWsdl();
        if(wsdlDefs == null) {
            throw new MessageRouterException("missing WSDL definitions for service");
        }
        WsdlService service = (WsdlService) wsdlDefs.getServices().iterator().next();
        WsdlPort servicePort =  (WsdlPort) service.getPorts().iterator().next();
        WsdlBinding binding =  servicePort.lookupBinding();
        WsdlPortType pt = binding.lookupPortType();
        
        Object impl = getServiceImpl();
        Class implClass = impl.getClass();
        Method[] methods = implClass.getMethods();
        
        //gather list of potentially useful methods
        for (int pos = 0; pos < methods.length; pos++) {
            Method m = methods[pos];
            Class[] paramTypes = m.getParameterTypes();
            validateMethodParamTypes(m, paramTypes);
        }
        
        // check that every method form portType is in impl
        for(Iterator iter = pt.getOperations().iterator(); iter.hasNext(); ) {
            WsdlPortTypeOperation op = (WsdlPortTypeOperation) iter.next();
            String opName = op.getOperationName();
            WsdlPortTypeInput opInput = op.getInput();
            WsdlMessage inputMessage = opInput.lookupMessage();
            Iterator messageParts = inputMessage.getParts().iterator();
            if(!messageParts.hasNext()) {
                throw new MessageRouterException(
                    "there must be at least one message part (operation: "+opName+")");
            }
            WsdlMessagePart msgPart = (WsdlMessagePart) messageParts.next();
            if(messageParts.hasNext()) {
                throw new MessageRouterException(
                    "there can be only one message part (operation: "+opName+")");
            }
            QName partElementQn = msgPart.getPartElement();
            if(partElementQn == null) {
                throw new MessageRouterException(
                    "message part must have element attribute (operation: "+opName+")");
            }
            Method targetMethod = establishMethodFromOperationInputMessage(opName, partElementQn);
            
            WsdlPortTypeOutput opOutput = op.getOutput();
            
            Class returnType = targetMethod.getReturnType();
            if(opOutput != null) {
                // if return type xml object
                validateMethodOutput(returnType, opName);
            } else {
                if(!returnType.equals(Void.TYPE)) {
                    throw new MessageRouterException(
                        "method "+opName+" must be void if there is no output message");
                }
            }
        }
    }
    
    protected void validateMethodOutput(Class returnType, String opName) throws MessageRouterException {
        if(!XmlElement.class.isAssignableFrom(returnType)){
            throw new MessageRouterException(
                "there must be method "+opName+" that takes one parameter derived from XmlElement");
        }
    }
    
    protected Method establishMethodFromOperationInputMessage(String opName, QName partElementQn)
        throws MessageRouterException
    {
        Method targetMethod = (Method) methodNameToMethod.get(opName);
        if(targetMethod == null) {
            Class implClass = getServiceImpl().getClass();
            throw new MessageRouterException(
                "missng Java methods with name "+opName+" in "+implClass);
        }
        setMethodWithQNameParameter(partElementQn, targetMethod);
        return targetMethod;
    }
    
    protected void validateMethodParamTypes(Method m, Class[] paramTypes)
        throws MessageRouterException
    {
        if(paramTypes.length == 1) {
            Class paramType = paramTypes[0];
            if(XmlElement.class.isAssignableFrom(paramType)){
                String methodName = m.getName();
                methodNameToMethod.put(methodName, m);
            }
        }
    }
    
    
    public void invoke(MessageContext ctx) throws MessageProcessingException {
        //TODO:  now do all reflection magic that is driven by WSDL metadatas
        //throw new XsulException("pika boo");
        
        XmlElement incomingMsg = ctx.getIncomingMessage();
        String msgName = incomingMsg.getName();
        String msgNs = incomingMsg.getNamespace().getNamespaceName();
        QName qn = new QName(msgNs, msgName);
        Method javaMethod = (Method) messageToMethod.get(qn);
        if(javaMethod == null) {
            throw new MessageProcessingException(
                "no operation found that takes message with "+qn);
        }
        
        if(logger.isFinestEnabled()) logger.finest("qn="+qn+" javaMethod="+javaMethod
                                                       +" incomingMsgAsText="+XsulUtil.safeXmlToString(incomingMsg));
        String methodName = javaMethod.getName();
        
        invokeMethod(incomingMsg, javaMethod, methodName, qn, ctx);
    }
    
    protected void invokeMethod(XmlElement incomingMsg, Method javaMethod, String methodName,
                                QName qn, MessageContext ctx)
        throws MessageProcessingException, IllegalArgumentException
    {
        XmlElement responseXmlElement;
        Object impl = getServiceImpl();
        if(impl == null) throw new IllegalArgumentException();
        try {
            // pivot happens here
            responseXmlElement = (XmlElement) javaMethod.invoke(impl, new Object[]{incomingMsg});
        } catch (InvocationTargetException e) {
            throw new MessageProcessingException(
                "could not invoke operation "+methodName+" with input "+qn, e);
        } catch (IllegalArgumentException e) {
            throw new MessageProcessingException(
                "could not invoke operation "+methodName+" with input "+qn, e);
        } catch (IllegalAccessException e) {
            throw new MessageProcessingException(
                "could not invoke operation "+methodName+" with input "+qn, e);
        }
        
        Class returnType = javaMethod.getReturnType();
        if(!returnType.equals(Void.TYPE)) { // expected to have response
            if(responseXmlElement == null) {
                throw new MessageProcessingException(
                    "XmlElement response message for "+methodName+" must be not null "
                        +"(input message "+qn+")");
            }
            assert responseXmlElement != null;
            if(logger.isFinestEnabled()) {
                logger.finest("responseXml="+builder.serializeToString(responseXmlElement));
            }
        } else {
            if(responseXmlElement != null) { // this should mever happen but ...
                throw new MessageProcessingException(
                    "INTERNAL: XmlElement response message for one-way "+methodName+" must be null "
                        +"(input message "+qn+")");
            }
        }
        ctx.setOutgoingMessage(responseXmlElement);
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


