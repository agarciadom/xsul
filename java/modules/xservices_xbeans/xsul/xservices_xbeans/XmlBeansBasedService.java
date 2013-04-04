/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XmlBeansBasedService.java,v 1.11 2006/02/20 08:35:40 aslom Exp $
 */
package xsul.xservices_xbeans;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.message_router.MessageContext;
import xsul.message_router.MessageProcessingException;
import xsul.message_router.MessageRouterException;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlResolver;
import xsul.xbeans_util.XBeansUtil;
import xsul.xservo_soap.XSoapDocLiteralService;

/**
 * This class provides services over HTTP.
 */
public class XmlBeansBasedService extends XSoapDocLiteralService {
    private static final MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public XmlBeansBasedService(String name) {
        super(name);
    }
    
    
    public XmlBeansBasedService(String name, String wsdlLoc, Object serviceImpl) {
        super(name, wsdlLoc, serviceImpl);
    }
    
    protected void validateMethodOutput(Class returnType, String opName) throws MessageRouterException {
        if(!XmlObject.class.isAssignableFrom(returnType)){
            throw new MessageRouterException(
                "there must be method "+opName+" that takes one parameter derived from XmlElement");
        }
    }
    
    protected Method establishMethodFromOperationInputMessage(String opName, QName partElementQn)
        throws MessageRouterException
    {
        Method targetMethod = lookupMethodWithQNameParameter(partElementQn);
        if(targetMethod == null) {
            Class implClass = getServiceImpl().getClass();
            throw new MessageRouterException(
                "missng Java methods with name "+opName+" in "+implClass);
        }
        return targetMethod;
    }
    
    protected void validateMethodParamTypes(Method m, Class[] paramTypes)
        throws MessageRouterException
    {
        if(paramTypes.length == 1) {
            Class paramType = paramTypes[0];
            if(XmlObject.class.isAssignableFrom(paramType)){
                // type.getName
                QName qn = null;
                try {
                    Field f = paramType.getField("type");
                    
                    SchemaType type = (SchemaType) f.get(null);
                    qn = type.getDocumentElementName();
                } catch (Exception e) {
                    Class implClass = getServiceImpl().getClass();
                    if(logger.isFineEnabled()) {
                        logger.fine("could not get 'type' form XmlObject for method "+m.getName()
                                        +" in "+implClass+" : "+e, e);
                    }
                }
                if(qn != null) {
                    //if(messageToMethod.containsKey(qn)) {
                    if(lookupMethodWithQNameParameter(qn) != null) {
                        //Method existingMethod = (Method) messageToMethod.get(qn);
                        Method existingMethod = lookupMethodWithQNameParameter(qn);
                        throw new MessageRouterException(
                            "there are two methods "+m+" and "+existingMethod
                                +" that takes message with "+qn);
                    }
                    
                    //messageToMethod.put(qn, m);
                    setMethodWithQNameParameter(qn, m);
                }
            }
        }
    }
    
    protected void invokeMethod(XmlElement incomingMsg, Method javaMethod, String methodName,
                                QName qn, MessageContext ctx)
        throws MessageProcessingException, IllegalArgumentException
    {
        XmlObject requestXmlObj = XBeansUtil.xmlElementToXmlObject(incomingMsg);
        if(requestXmlObj == null) throw new IllegalArgumentException();
        
        XmlObject responseXmlObj;
        Object impl = getServiceImpl();
        if(impl == null) throw new IllegalArgumentException();
        try {
            // pivot happens here
            responseXmlObj = (XmlObject) javaMethod.invoke(impl, new Object[]{requestXmlObj});
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
            
            if(responseXmlObj == null) {
                throw new MessageProcessingException(
                    "XmlBeans response message for "+methodName+" must be not null "
                        +"(input message "+qn+")");
            }
            assert responseXmlObj != null;
            if(logger.isFinestEnabled()) {
                logger.finest("responseXml="+responseXmlObj.xmlText());
            }
            try {
                XmlElement outgoingMsg = XBeansUtil.xmlObjectToXmlElement(responseXmlObj);
                
                ctx.setOutgoingMessage(outgoingMsg);
            } catch (XmlBuilderException xbe) {
                throw new MessageProcessingException(
                    "could not convert XmlBeans into XML:"+xbe, xbe);
            }
        } else {
            if(responseXmlObj != null) { // this should mever happen but ...
                throw new MessageProcessingException(
                    "INTERNAL: XmlElement response message for one-way "+methodName+" must be null "
                        +"(input message "+qn+")");
            }
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


