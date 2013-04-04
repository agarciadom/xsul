/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //- -----100-columns-wide------>*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XBeansDocumentDispatcher.java,v 1.5 2005/01/23 10:04:21 aslom Exp $
 */

package xsul.xbeans_document_dispatcher;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.processor.MessageProcessor;

/**
 * Maps document QName to a method in a java target. Invokes target when message with
 * document qname arrives.
 *
 * @version $Revision: 1.5 $
 * @author Yogesh L. Simmhan [mailto:ysimmhan@cs.indiana.edu]
 */
public class XBeansDocumentDispatcher implements MessageProcessor {

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    private final static MLogger l = MLogger.getLogger();

    // mapping from the document QName to the method which will be invoked with the param
    HashMap documentQNameToMethod;

    public XBeansDocumentDispatcher() {

        documentQNameToMethod = new HashMap();
    }

    /**
     * creates an invocation target to match the incoming doc QName against each method name in the
     * target interface and with the given NS.
     *
     * @param    namespace           the namespace of incoming doc to match
     * @param    targetObject        the impl on which to invoke the method
     * @param    targetInterface     the methods in the IMPL which are being exposed
     * @param    overwrite           forces overwrite of existing target object and method
     * with the same method name as one present in given interface.
     *
     * @return true if this was a unique mapping and no invokation target was overwritten --
     * i.e. no mapping NS+methodname combination already existed.
     * false if there was already a target for the NS+methodname combination.
     * If overwrite was false, this means that the operation was not performed.
     * @exception   XsulException
     *
     */
    public boolean setTarget(String namespace, Object targetObject,
                             Class targetInterface, boolean overwrite)
        throws XsulException {

        // check if impl not null
        if(targetObject == null){
            throw new XsulException("target object is null");
        }

        // check if imlp implements interface
        if(!targetInterface.isAssignableFrom(targetObject.getClass())){
            throw new XsulException("interface " + targetInterface.getName() +
                                        "not implemented by object: " + targetObject.getClass().getName());
        }

        // check if there are any methods in hte interface
        Method[] interfaceMethods = targetInterface.getMethods();
        if(interfaceMethods.length == 0){
            throw new XsulException("interface " + targetInterface.getName() +
                                        " has no method. Nothing to export");
        }

        // check if the documents being mapped do not already exist. if any already exist and
        // overwrite is false, then we cannot proceed
        boolean targetUnique = true;
        for(int i=0; i<interfaceMethods.length; i++){

            QName docQName = new QName(namespace, interfaceMethods[i].getName());
            targetUnique = documentQNameToMethod.get(docQName) == null;
            if(!overwrite && !targetUnique){
                return false;
            }

        }

        // check if the methods in the interface take and return xmlelement/xmlobject
        // if so, add mapping for them
        for(int i=0; i<interfaceMethods.length; i++){

            Class[] params = interfaceMethods[i].getParameterTypes();
            // check exactly one param as input
            if(params.length != 1) {
                throw new XsulException("interface " + targetInterface.getName() +
                                            " has method " + interfaceMethods[i].getName() +
                                            " that does not have return type XmlElement or XmlObject. " +
                                            " Number of params taken != 1 (" + params.length + ")");
            }
            // check type of param is xml object or xml element
            Class param1Type = params[0];
            Class returnType = interfaceMethods[i].getReturnType();
            int targetType = -1;
            if(XmlObject.class.isAssignableFrom(returnType) && XmlObject.class.isAssignableFrom(param1Type)){
                targetType = InvocationTarget.XML_OBJECT;
            }
            else
                if(XmlElement.class.isAssignableFrom(interfaceMethods[i].getReturnType()) &&
                       XmlElement.class.isAssignableFrom(params[0])){
                    targetType = InvocationTarget.XML_ELEMENT;
                }
                else {
                    throw new XsulException("interface " + targetInterface.getName() +
                                                " has method " + interfaceMethods[i].getName() +
                                                " that does not have param/return type XmlElement or XmlObject. " +
                                                " found param,return type = " +
                                                params[0].getName() + "," +
                                                interfaceMethods[i].getReturnType().getName());
                }

            // create invocation target struct
            InvocationTarget invk =
                new InvocationTarget(targetObject, interfaceMethods[i], targetType);
            QName docQName = new QName(namespace, interfaceMethods[i].getName());
            documentQNameToMethod.put(docQName, invk);
            l.finest("Added as invoker for QName: " + docQName + " method: " + invk.getMethod().getName());
        }

        // return true if all methods in interface were not previously mapped
        return targetUnique;
    }

    /**
     * Method addDocumentHandler. doc's qname matches with given qname causes
     * handler method to be called.
     * method must have input arg as XmlElement or XmlObject and similar
     * output arg.
     *
     * @param    docQName            a  QName
     * @param    targetObject        an Object
     * @param    targetMethod        a  String
     * @param overwrite forces overwrite of existing target object and method
     *
     * @return true if this was a unique mapping and no invokation target was overwritten.
     * false if there was already a target for the qname. If overwrite was false, this means
     * that the operation was not performed.
     *
     */
    public boolean setTarget(QName docQName, Object targetObject, String targetMethodName,
                             boolean overwrite) {

        boolean targetUnique = documentQNameToMethod.get(docQName) == null;
        if(!overwrite && !targetUnique){
            return false;
        }

        InvocationTarget invokationTarget = null;

        // check if method taking and returning XmlElement as param is present
        invokationTarget = getXmlElementInvocationTarget(targetObject, targetMethodName);
        if(invokationTarget != null){

            // add invokation target to map for that qname
            documentQNameToMethod.put(docQName, invokationTarget);

            return targetUnique;
        }

        // check if method taking and returning XmlObject as param is present
        invokationTarget = getXmlObjectMethod(targetObject, targetMethodName);
        if(invokationTarget != null){

            // add invokation target to map for that qname
            documentQNameToMethod.put(docQName, invokationTarget);

            return targetUnique;
        }

        throw new XsulException("Method with name: " + targetMethodName +
                                    " not found in " + targetObject +
                                    " with param and return type as XmlElement or XmlObject");
    }

    InvocationTarget getXmlElementInvocationTarget(Object targetObject, String targetMethodName){

        Method targetMethod = null;
        try {

            targetMethod = targetObject.getClass().getMethod(targetMethodName,
                                                             new Class[]{XmlElement.class});

        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }

        Class returnType = targetMethod.getReturnType();
        // if return type xml element
        if(XmlElement.class.isAssignableFrom(returnType)){
            // create invocation target
            return new InvocationTarget(targetObject, targetMethod, InvocationTarget.XML_ELEMENT);
        }
        // else fail
        return null;
    }

    InvocationTarget getXmlObjectMethod(Object targetObject, String targetMethodName){

        Method targetMethod = null;
        try {

            targetMethod = targetObject.getClass().getMethod(targetMethodName,
                                                             new Class[]{XmlObject.class});

        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }

        Class returnType = targetMethod.getReturnType();
        // if return type xml object
        if(XmlObject.class.isAssignableFrom(returnType)){
            // create invocation target
            return new InvocationTarget(targetObject, targetMethod, InvocationTarget.XML_OBJECT);
        }
        // else fail
        return null;
    }

    /**
     * Return xml element to indicate that processingis finished. else return null
     * (no more links inchains will be called).
     *
     */
    public XmlElement processMessage(XmlElement requestMsg) throws XsulException {

        // get the Qname of the document
        QName docQName = new QName(requestMsg.getNamespace().getNamespaceName(), requestMsg.getName());

        // check if the document has a method to handle it
        InvocationTarget target = (InvocationTarget)documentQNameToMethod.get(docQName);

        l.fine("Dispatching doc with qname=" + docQName.toString()+" via target " + target);

        // if method available, call method with the document, converting it to xmlobject if reqd.
        // response docuemnt returned as Xmlelement.
        // if no matching method found, return null;
        if(target == null){
            return null;
        }

        try {

            XmlElement responseEl = null;
            Method targetMethod = target.getMethod();

            switch(target.getReturnType()){

                case InvocationTarget.XML_ELEMENT:

                    responseEl = (XmlElement)targetMethod.invoke(target.getObject(),
                                                                 new Object[]{requestMsg});
                    return responseEl;

                case InvocationTarget.XML_OBJECT:

                    XmlObject requestXmlObj = XmlObject.Factory.parse(builder.serializeToString(requestMsg));
                    if(l.isFinerEnabled()) l.fine("input object type = ............. " + requestXmlObj.getClass().getName());
                    if(l.isFinerEnabled()) l.fine("input xml string=" + builder.serializeToString(requestMsg));
                    XmlObject responseXmlObj = (XmlObject)targetMethod.invoke(target.getObject(),
                                                                              new Object[]{requestXmlObj});
                    String responseXml = responseXmlObj.xmlText();
                    if(l.isFinerEnabled()) l.fine("output xml string=" + responseXml);

                    responseEl = builder.parseFragmentFromReader(new StringReader(responseXml));
                    if(l.isFinerEnabled()) l.fine("output xml string2=" + builder.serializeToString(responseEl));
                    return responseEl;

                default:
                    return null;
            }

        } catch (XmlException e) {
            throw new XsulException(e.getMessage(), e);
        } catch (XmlBuilderException e) {
            throw new XsulException(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new XsulException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new XsulException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new XsulException(e.getMessage(), e);
        }

    }

    private class InvocationTarget {

        public static final int XML_ELEMENT = 0;
        public static final int XML_OBJECT = 1;

        Object object;
        Method method;
        int returnType;

        public InvocationTarget(Object o, Method m, int pt){
            object = o;
            method = m;
            returnType = pt;
        }

        public Object getObject() { return object; }
        public Method getMethod() { return method; }
        public int getReturnType() { return returnType; }

        public String toString(){
            return '[' + method.getReturnType().getName() + ']' +
                object.getClass().getName() + ":" + method.getName() +
                '(' + method.getParameterTypes()[0].getName()+ ')' +
                "[#params=" + method.getParameterTypes().length + ']';
        }
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


