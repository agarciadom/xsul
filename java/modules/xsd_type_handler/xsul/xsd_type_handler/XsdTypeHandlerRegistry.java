/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsdTypeHandlerRegistry.java,v 1.8 2004/09/08 20:46:29 aslom Exp $
 */

package xsul.xsd_type_handler;

import xsul.XmlConstants;
import xsul.type_handler.TypeHandler;
import xsul.type_handler.TypeHandlerException;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.xsd_type_handler.util.Base64;

/**
 * Set of type handler for XSD simple types (currently int,booleans,string,double,byte[]/BASE64).
 *
 * @version $Revision: 1.8 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XsdTypeHandlerRegistry extends TypeHandlerRegistry{
    //private final static XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
    private final static XsdTypeHandlerRegistry instance = new XsdTypeHandlerRegistry(null);
    
    protected XsdTypeHandlerRegistry(TypeHandlerRegistry parent) {
        super(parent);
        enableTypeHandlers();
        
        TypeHandler stringHandler = new TypeHandler() {             //new XsdStringHandler();
            
            public Object javaToXml(Object javaValue) throws TypeHandlerException {
                return javaValue;
            }
            
            public Object xmlToJava(Object xmlValue) throws TypeHandlerException {
                if(xmlValue.getClass().equals(String.class)) {
                    return xmlValue;
                } else {
                    throw new TypeHandlerException(
                        "expected String not "+xmlValue.getClass()+" of XML "+xmlValue);
                }
            }
        };
        registerHandler(XmlConstants.NS_URI_XSD, "string", String.class, stringHandler);
        
        TypeHandler booleanHandler = new TypeHandler() {
            
            public Object javaToXml(Object javaValue) throws TypeHandlerException {
                return ((Boolean)javaValue).booleanValue() ? "1" : "0";
            }
            
            public Object xmlToJava(Object xmlValue) throws TypeHandlerException {
                if(xmlValue.getClass().equals(String.class)) {
                    String s = ((String)xmlValue);
                    char c = s.charAt(0);
                    return c == '1' || c =='t' ?  Boolean.TRUE : Boolean.FALSE;
                } else {
                    throw new TypeHandlerException(
                        "expected boolean as string not "+xmlValue.getClass()+" of XML "+xmlValue);
                }
            }
        };
        registerHandler(XmlConstants.NS_URI_XSD, "boolean", Boolean.TYPE, booleanHandler);
        
        TypeHandler intHandler = new TypeHandler() {
            
            public Object javaToXml(Object javaValue) throws TypeHandlerException {
                return ((Integer)javaValue).toString();
            }
            
            public Object xmlToJava(Object xmlValue) throws TypeHandlerException {
                if(xmlValue.getClass().equals(String.class)) {
                    String s = ((String)xmlValue);
                    try {
                        Integer i = Integer.valueOf(s);
                        return i;
                    } catch(NumberFormatException ex) {
                        throw new TypeHandlerException("can't parse int value"+s, ex);
                    }
                } else {
                    throw new TypeHandlerException(
                        "expected int as string not "+xmlValue.getClass()+" of XML "+xmlValue);
                }
            }
        };
        registerHandler(XmlConstants.NS_URI_XSD, "int", Integer.TYPE, intHandler);
        
        TypeHandler doubleHandler = new TypeHandler() {
            
            public Object javaToXml(Object javaValue) throws TypeHandlerException {
                return ((Double)javaValue).toString();
            }
            
            public Object xmlToJava(Object xmlValue) throws TypeHandlerException {
                if(xmlValue.getClass().equals(String.class)) {
                    String s = ((String)xmlValue);
                    try {
                        Double i = Double.valueOf(s);
                        return i;
                    } catch(NumberFormatException ex) {
                        throw new TypeHandlerException("can't parse double value"+s, ex);
                    }
                } else {
                    throw new TypeHandlerException(
                        "expected double as string not "+xmlValue.getClass()+" of XML "+xmlValue);
                }
            }
        };
        registerHandler(XmlConstants.NS_URI_XSD, "double", Double.TYPE, doubleHandler);
        
        // additional handlers to deal with Java Reflection type system mess ...
        registerHandler(XmlConstants.NS_URI_XSD, "boolean", Boolean.class, booleanHandler);
        registerHandler(XmlConstants.NS_URI_XSD, "int", Integer.class, intHandler);
        registerHandler(XmlConstants.NS_URI_XSD, "double", Double.class, doubleHandler);
        
        TypeHandler base64Handler = new TypeHandler() {
            
            public Object javaToXml(Object javaValue) throws TypeHandlerException {
                byte[] byteArr = (byte[])javaValue;
                char[] charArr = Base64.encode(byteArr);
                String s = new String(charArr);
                return s;
            }
            
            public Object xmlToJava(Object xmlValue) throws TypeHandlerException {
                if(xmlValue.getClass().equals(String.class)) {
                    String s = ((String)xmlValue);
                    try {
                        byte[] byteArr = Base64.decode(s.toCharArray());
                        return byteArr;
                    } catch(NumberFormatException ex) {
                        throw new TypeHandlerException("can't parse BASE64 value"+s, ex);
                    }
                } else {
                    throw new TypeHandlerException(
                        "expected BASE64 as string not "+xmlValue.getClass()+" of XML "+xmlValue);
                }
            }
        };
        registerHandler(XmlConstants.NS_URI_XSD, "base64Binary", byte[].class, base64Handler);
        
        
    }
    
    public static XsdTypeHandlerRegistry getInstance() {
        return instance;
    }
    
    // ysimmhan: added method: create a xsd type handler registry that delegates to
    // a parent registry if unable to process the type
    public static XsdTypeHandlerRegistry getInstance(TypeHandlerRegistry parent) {
        return new XsdTypeHandlerRegistry(parent);
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






