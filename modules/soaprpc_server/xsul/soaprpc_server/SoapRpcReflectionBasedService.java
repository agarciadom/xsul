/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SoapRpcReflectionBasedService.java,v 1.8 2004/10/04 22:56:26 aslom Exp $
 */

package xsul.soaprpc_server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.processor.MessageProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.type_handler.TypeHandlerException;
import xsul.type_handler.TypeHandlerRegistry;

/**
 *
 *
 * @version $Revision: 1.8 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class SoapRpcReflectionBasedService implements MessageProcessor {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    //private final static XmlPullParserFactory factory;
    private Object target;
    private TypeHandlerRegistry registry;
    // ysimmhan: added interface of the target exported by this SOAP service
    private Class targetInterface = null;
    //private XmlElement lastRequest;
    private SoapUtil[] soapFragrances = new SoapUtil[] { Soap11Util.getInstance() };
    private Map name2Method;
    
    public SoapRpcReflectionBasedService(Object target, TypeHandlerRegistry thr) {
        if(target == null) throw new IllegalArgumentException();
        this.target = target;
        if(thr == null) throw new IllegalArgumentException();
        this.registry = thr;
        buildMethodNamesMap(target.getClass());
    }
    
    // ysimmhan: added constructor: enable SOAP service on only methods
    // defined in the given interface
    public SoapRpcReflectionBasedService(Object target,
                                         Class targetInterface,
                                         TypeHandlerRegistry thr) {
        
        if(target == null) throw new IllegalArgumentException();
        this.target = target;
        if(thr == null) throw new IllegalArgumentException();
        this.registry = thr;
        if(targetInterface == null) throw new IllegalArgumentException();
        
        Class[] tInterfaces = target.getClass().getInterfaces();
        boolean foundMatch = false;
        for(int i=0; i < tInterfaces.length; i++){
            if(tInterfaces[i] == targetInterface){
                foundMatch = true;
                break;
            }
        }
        if(!foundMatch) throw new IllegalArgumentException("target must implement targetInterface");
        this.targetInterface = targetInterface;
        buildMethodNamesMap(targetInterface);
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
    
    //    public XmlElement getLastResponse() {
    //        return lastResponse;
    //    }
    
    public Object getTarget() {
        return target;
    }
    
    public XmlElement processMessage(XmlElement message)
        throws DynamicInfosetProcessorException {
        //        XmlElement request = builder.parseFragmentFromReader(
        //            new StringReader("<getNode><path>/hello</path></getNode>"));
        // ysimmhan: added line: use the targetInterface given by user,
        // default to target object class
        Class klazz = targetInterface != null? targetInterface : target.getClass();
        // ysimmhan: removed line
        // Class klazz = target.getClass();
        XmlNamespace methodNamespace = message.getNamespace();
        String methodName = message.getName();
        
        Method method = findMethodWithName(methodNamespace, methodName, klazz);
        
        //TODO: deal with one-way messages
        Object[] values = mapXmlToJavaPrameters(message, method);
        
        //TODO: configurabel invokeOneWay
        return invokeWithResult(message, methodNamespace, methodName, method, values);
        
        
    }
    
    protected  XmlElement invokeWithResult(XmlElement inputMessage,
                                           XmlNamespace methodNamespace,
                                           String methodName,
                                           Method method,
                                           Object[] values)
        throws TypeHandlerException {
        Object result = null;
        try {
            result = method.invoke(target, values);
            //produce response message
            XmlElement responseMsg = produceSoapResponseMessage(methodNamespace, methodName, method, result);
            
            return responseMsg;
        } catch (IllegalAccessException e) {
            throw new DynamicInfosetProcessorException(
                "reflection based invocation failed: "+e, e);
        } catch (InvocationTargetException e) {
            throw new DynamicInfosetProcessorException(
                "reflection based invocation failed: "+e, e);
        } catch(Exception ex) { //user exception
            
            XmlElement faultMsg = produceSoapFault(inputMessage, ex, methodName);
            return faultMsg;
        }
    }
    
    protected XmlElement produceSoapFault(XmlElement inputMessage, Exception ex, String methodName)
    {
        // find SOAP Envelope for this message
        XmlElement root = inputMessage;
        while(root.getParent() instanceof XmlElement) {
            root = (XmlElement) root.getParent();
        }
        // find what SOAP fragrance message came and select utility class to deal with it
        SoapUtil soapUtil = null;
        for (int i = 0; i < soapFragrances.length; i++)
        {
            if(soapFragrances[i].isSoapEnvelopeSupported(root)){
                soapUtil = soapFragrances[i];
                break;
            }
        }
        if(soapUtil == null) {
            throw new DynamicInfosetProcessorException(
                "could not generate Fault for unsupported SOAP Envelope "
                    +builder.serializeToString(inputMessage));
        }
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        XmlElement faultMsg = soapUtil.generateSoapServerFault(
            "Exception when processing method "+methodName+": "+sw.toString(),ex);
        return faultMsg;
    }
    
    // return null to
    protected  Object[] mapXmlToJavaPrameters(XmlElement message, Method method)
        throws TypeHandlerException {
        Iterator params = message.requiredElementContent().iterator();
        Class[] paramTypes = method.getParameterTypes();
        Object[] values = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            XmlElement paramEl = (XmlElement) params.next();
            Class paramType  = paramTypes[i];
            if(XmlElement.class.isAssignableFrom(paramType)) {
                if(XmlElementAdapter.class.isAssignableFrom(paramType)) {
                    values[i] = XmlElementAdapter.castOrWrap(paramEl, paramType);
                } else {
                    values[i] = paramEl;
                }
            } else {
                values[i] = registry.xmlElementToJava(paramEl, paramTypes[i]);
            }
        }
        return values;
    }
    
    protected XmlElement produceSoapResponseMessage(XmlNamespace methodNamespace,
                                                    String methodName,
                                                    Method method,
                                                    Object result)
        throws XmlBuilderException {
        Class returnType = method.getReturnType();
        
        XmlElement xmlResult = null;
        if(returnType != null && !Void.TYPE.equals(returnType)) { // non void method
            //if(result != null) { //TODO should instead send xsi:nil='1' if returnType != Void.TYPE !!!
            if(result instanceof XmlElement && XmlElement.class.isAssignableFrom(returnType)) {
                xmlResult = (XmlElement) result;
            } else {
                xmlResult = registry.javaToXmlElement(result, null, "result");
            }
        }
        XmlElement responseMsg = builder.newFragment(methodNamespace, methodName+"Response");
        if(xmlResult != null) {
            responseMsg.addElement(xmlResult);
        }
        return responseMsg;
        
    }
    
    private void buildMethodNamesMap(Class klazz)
        throws DynamicInfosetProcessorException
    {
        Method[] methods = klazz.getMethods();
        name2Method = new HashMap(5 *methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if(m.getDeclaringClass().equals(Object.class)) {
                continue;
            }
            String methodName = m.getName();
            Method conflicM = (Method) name2Method.get(methodName);
            if(conflicM == null) {
                name2Method.put(methodName, m);
            } else {
                throw new DynamicInfosetProcessorException("overloaded methods are not supported: "
                                                               +methodName+" in "+klazz.getName());
            }
            //throw new DynamicInfosetProcessorException(
            //    "can not dispatch to overloaded method "+methodName);
        }
    }
    
    private Method findMethodWithName(XmlNamespace methodNamespace, String methodName, Class klazz)
        throws DynamicInfosetProcessorException
    {
        Method m = (Method) name2Method.get(methodName);
        if(m == null) {
            throw new DynamicInfosetProcessorException(
                "no method named "+methodName+" to dispatch");
            
        }
        return m;
        //
        //        //TODO: build HashMap<methodName, method>
        //        if(methodName == null) throw new IllegalArgumentException();
        //        if(klazz == null) throw new IllegalArgumentException();
        //        Method foundMethod = null;
        //        Method[] methods = klazz.getMethods();
        //        for (int i = 0; i < methods.length; i++) {
        //          Method m = methods[i];
        //          if(m.getName().equals(methodName)) {
        //              if(foundMethod != null) {
        //                  throw new DynamicInfosetProcessorException(
        //                      "can not dispatch to overloaded method "+methodName);
        //              }
        //              foundMethod = m;
        //          }
        //        }
        //        if(foundMethod == null) {
        //          throw new DynamicInfosetProcessorException(
        //              "could not find method named "+methodName);
        //        }
        //        return foundMethod;
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






