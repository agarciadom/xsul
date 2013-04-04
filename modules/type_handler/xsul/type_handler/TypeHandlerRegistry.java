/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TypeHandlerRegistry.java,v 1.13 2004/09/09 05:17:39 aslom Exp $
 */

package xsul.type_handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;


/**
 * This is abstract class that is basse to implement registry of type handlers.
 * Type handler registries can be chained.
 *
 * @version $Revision: 1.13 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TypeHandlerRegistry {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private List tuples  = null; //JDK15 List<Tuple>
    private TypeHandlerRegistry chained;
    private TypeHandlerRegistry top;
    
    public TypeHandlerRegistry(TypeHandlerRegistry chainedRegistry) {
        this.chained = chainedRegistry;
        setTopRegistry(this);
    }
    
    public TypeHandlerRegistry getTopRegistry() {
        if(top == null) {
            return this;
        } else {
            return top;
        }
    }
    
    public void setTopRegistry(TypeHandlerRegistry topRegistry) {
        this.top = topRegistry;
        if(chained != null) {
            chained.setTopRegistry(topRegistry);
        }
    }
    
    /**
     *
     */
    public Object xmlElementToJava(XmlElement xmlElValue, Class expectedType)
        throws TypeHandlerException
    {
        //TODO: use xsi:type
        String namespaceName = xmlElValue.getNamespaceName();
        String name = xmlElValue.getName();
        TypeHandler th = localLookupHandlerForXmlType(namespaceName, name);
        if(th == null) {
            th = localLookupHandlerForJavaType(expectedType);
        }
        if(th != null) {
            Iterator children = xmlElValue.children();
            Object firstChild;
            if(children.hasNext()) {
                firstChild = xmlElValue.children().next();
            } else {
                firstChild = ""; //empty content
            }
            return th.xmlToJava(firstChild);
        } else {
            if(chained != null) {
                return chained.xmlElementToJava(xmlElValue, expectedType);
            } else {
                if(XmlElement.class.isAssignableFrom(expectedType)) {
                    Iterator children = xmlElValue.children();
                    if(children.hasNext()) {
                        XmlElement firstChildElement =
                            (XmlElement) xmlElValue.requiredElementContent().iterator().next();
                        return firstChildElement;
                    } else {
                        throw new TypeHandlerException(
                            "could not find XML element in result "+xmlElValue
                                +" [XML:"+builder.serializeToString(xmlElValue));
                        
                    }
                    
                } else {
                    throw new TypeHandlerException("could not find type handler for "+xmlElValue
                                                       +" with expected type "+expectedType);
                }
            }
        }
    }
    
    /**
     *
     */
    public XmlElement javaToXmlElement(Object javaValue,
                                       String elNamespaceName,
                                       String elName)
        
        throws TypeHandlerException
    {
        if(javaValue == null) throw new IllegalArgumentException();
        Class klazz = javaValue.getClass();
        TypeHandler th = localLookupHandlerForJavaType(klazz);
        if(th != null) {
            XmlElement el = builder.newFragment(elNamespaceName, elName);
            Object child = th.javaToXml(javaValue);
            if(child instanceof XmlElement) {
                XmlElement childEl = (XmlElement)child;
                el.addElement(childEl); //make sure
            } else {
                el.addChild(child);
            }
            return el;
        } else {
            if(chained != null) {
                return chained.javaToXmlElement(javaValue, elNamespaceName, elName);
            } else {
                if(javaValue instanceof XmlElement) { // special ttreatment for XML elements
                    XmlElement el = builder.newFragment(elNamespaceName, elName);
                    XmlElement childEl = (XmlElement)javaValue;
                    el.addElement(childEl); //make sure
                    return el;
                } else {
                    throw new TypeHandlerException("could not find type handler for "+klazz);
                }
            }
        }
    }
    
    //    protected TypeHandler lookupHandlerForXmlType(String namespaceName, String name)
    //        throws TypeHandlerException
    //    {
    //        return getTopRegistry().downchainLookupHandlerForXmlType(namespaceName, name);
    //    }
    
    /**
     *
     */
    protected TypeHandler localLookupHandlerForXmlType(String namespaceName, String name)
        throws TypeHandlerException
    {
        //if(element == null) {
        //    throw new TypeHandlerException("no handler for null java value");
        //}
        if(namespaceName == null) throw new IllegalArgumentException();
        if(name == null) throw new IllegalArgumentException();
        if(tuples != null) {
            for (int i = 0; i < tuples.size(); i++)
            {
                Tuple t = (Tuple) tuples.get(i);
                if(t.name.equals(name) && t.namespaceName.equals(namespaceName)) {
                    return t.handler;
                }
            }
            //        if(chained != null) {
            //            return chained.downchainLookupHandlerForXmlType(namespaceName, name);
            //        } else {
        }
        return null;
        //        }
    }
    
    //    public TypeHandler lookupHandlerForJavaType(Class javaType) throws TypeHandlerException
    //    {
    //        return getTopRegistry().downchainLookupHandlerForJavaType(javaType);
    //    }
    
    /**
     *
     */
    protected TypeHandler localLookupHandlerForJavaType(Class javaType) throws TypeHandlerException
    {
        if(javaType == null) throw new IllegalArgumentException();
        //throw new TypeHandlerException("no handler for null java value");
        
        //Class klazz = javaValue.getClass();
        if(tuples != null) {
            for (int i = 0; i < tuples.size(); i++)
            {
                Tuple t = (Tuple) tuples.get(i);
                if(t.javaType.equals(javaType)) {
                    return t.handler;
                }
            }
        }
        //        if(chained != null) {
        //            return chained.downchainLookupHandlerForJavaType(javaType);
        //        } else {
        return null;
        //        }
    }
    
    protected void enableTypeHandlers() {
        if(tuples == null) tuples = new ArrayList();
    }
    
    public void registerHandler(String namespaceName,
                                String name,
                                Class javaType,
                                TypeHandler th)
        throws TypeHandlerException
    {
        if(tuples == null) {
            throw new TypeHandlerException("this registry does not support type handlers");
        }
        Tuple adding = new Tuple(namespaceName, name, javaType, th);
        for (int i = 0; i < tuples.size(); i++)
        {
            Tuple t = (Tuple) tuples.get(i);
            if(adding.namespaceName.equals(t.namespaceName)
                   && adding.name.equals(t.name)
                   && adding.javaType.equals(t.javaType)
              )
            {
                throw new TypeHandlerException(
                    "could not add "+adding+" as it has identical type mappings with "+t);
            }
            //          throw new TypeHandlerException(
            //              "could not add "+adding+" as its xml type conficts with "+t);
            //            if(adding.javaType.equals(t.javaType)) {
            //                throw new TypeHandlerException(
            //                    "could not add "+adding+" as its java type conficts with "+t);
            //            }
        }
        tuples.add(adding);
    }
    
    private static class Tuple {
        private String namespaceName;
        private String name;
        private Class javaType;
        private TypeHandler handler;
        
        Tuple(String namespaceName,
              String name,
              Class javaType,
              TypeHandler th)
        {
            if(javaType == null && name == null) {
                throw new IllegalArgumentException("either java type or XML type name mus tbe not null");
            }
            //if(namespaceName == null) throw new IllegalArgumentException();
            this.namespaceName = namespaceName;
            //if(name == null) throw new IllegalArgumentException();
            this.name = name;
            //if(javaType == null) throw new IllegalArgumentException();
            this.javaType = javaType;
            if(th == null) throw new IllegalArgumentException();
            this.handler = th;
        }
        
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("[Mapping:");
            buf.append("namespaceName="+namespaceName);
            buf.append(",name="+namespaceName);
            buf.append(",javaType="+javaType);
            buf.append(",handler="+handler.getClass());
            buf.append(']');
            return buf.toString();
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









