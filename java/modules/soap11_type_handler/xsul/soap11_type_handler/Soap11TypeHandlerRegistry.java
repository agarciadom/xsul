/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Soap11TypeHandlerRegistry.java,v 1.9 2004/09/02 14:23:14 aslom Exp $
 */

package xsul.soap11_type_handler;

import java.util.Iterator;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;
import xsul.soap11_util.Soap11Util;
import xsul.type_handler.TypeHandlerException;
import xsul.type_handler.TypeHandlerRegistry;

//TODO: add support for more than just String[] ...

/**
 * Support SOAP 1.1 encoding of arrays (currently only String[],int[],double[]).
 *
 * @version $Revision: 1.9 $ $Date: 2004/09/02 14:23:14 $ (GMT)
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 * @author matt
 */
public class Soap11TypeHandlerRegistry extends TypeHandlerRegistry
{
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    //private final static XmlPullParserFactory factory;
    private final static Soap11TypeHandlerRegistry instance = new Soap11TypeHandlerRegistry();
    private final static XmlNamespace SOAP11_ENC_NS = Soap11Util.SOAP11_ENC_NS;
    private final static XmlNamespace SOAP12_ENC_NS = builder.newNamespace(
        "enc", "http://www.w3.org/2001/06/soap-encoding");
    
    private final static String ARRAY_TYPE = "arrayType";
    
    protected Soap11TypeHandlerRegistry() {
        super(null);
    }
    
    protected Soap11TypeHandlerRegistry(TypeHandlerRegistry parent) {
        super(parent);
    }
    
    public static Soap11TypeHandlerRegistry getInstance() {
        return instance;
    }
    
    public static Soap11TypeHandlerRegistry getInstance(TypeHandlerRegistry parent) {
        return new Soap11TypeHandlerRegistry(parent);
    }
    
    public XmlElement javaToXmlElement(Object javaValue,
                                       String elNamespaceName,
                                       String elName)
        
        throws TypeHandlerException
    {
        if(javaValue == null) {
            XmlElement el = builder.newFragment(elNamespaceName, elName);
            el.addAttribute(XmlConstants.XSI_NS, "nil", "1");
            return el;
        }
        assert javaValue != null;
        if(javaValue.getClass().isArray()) {
            Class compType = javaValue.getClass().getComponentType();
            XmlElement arrEl = builder.newFragment(elNamespaceName, elName);
            // jsut to be sure that nespace prefixes will exist and have expected values!
            arrEl.declareNamespace(XmlConstants.XS_NS);
            arrEl.declareNamespace(SOAP11_ENC_NS);
            //xsi:type='SOAP-ENC:Array'
            arrEl.addAttribute(XmlConstants.XSI_NS, "type", Soap11Util.SOAP11_ENC_PREFIX+":Array");
            if( compType.equals(String.class)) {
                //SOAP-ENC:arrayType='n1:string[1]'
                String[] arr = (String[]) javaValue;
                String arrayType = XmlConstants.XSD_PREFIX+":string["+arr.length+"]";
                arrEl.addAttribute(SOAP11_ENC_NS, ARRAY_TYPE, arrayType);
                //write array as elements
                for (int i = 0; i < arr.length; i++)
                {
                    arrEl.addElement("s").addChild(arr[i]);
                }
                return arrEl;
            } else if( compType.equals(Double.TYPE)) {
                double[] arr = (double[]) javaValue;
                String arrayType = XmlConstants.XSD_PREFIX+":double["+arr.length+"]";
                arrEl.addAttribute(SOAP11_ENC_NS, ARRAY_TYPE, arrayType);
                for (int i = 0; i < arr.length; i++)
                {
                    arrEl.addElement("d").addChild(Double.toString(arr[i]));
                }
                return arrEl;
            } else if( compType.equals(Integer.TYPE)) {
                int[] arr = (int[]) javaValue;
                String arrayType = XmlConstants.XSD_PREFIX+":int["+arr.length+"]";
                arrEl.addAttribute(SOAP11_ENC_NS, ARRAY_TYPE, arrayType);
                for (int i = 0; i < arr.length; i++)
                {
                    arrEl.addElement("i").addChild(Integer.toString(arr[i]));
                }
                return arrEl;
            }
        }
        return super.javaToXmlElement(javaValue, elNamespaceName, elName);
    }
    
    public Object xmlElementToJava(XmlElement xmlElValue, Class expectedType) throws TypeHandlerException
    {
        String xsiNil = xmlElValue.getAttributeValue(XmlConstants.NS_URI_XSI, "nil");
        if(xsiNil != null && "1".equals(xsiNil)) {
            return null;
        }
        if(expectedType != null && expectedType.isArray()) {
            Class compType = expectedType.getComponentType();
            if(!compType.equals(Byte.TYPE)) {
                String arrayType = xmlElValue.getAttributeValue(SOAP11_ENC_NS.getNamespaceName(), ARRAY_TYPE);
                
                if(arrayType == null) {
                    arrayType = xmlElValue.getAttributeValue(SOAP12_ENC_NS.getNamespaceName(), ARRAY_TYPE);
                    //if(arrayType == null) {
                    //throw new TypeHandlerException("cant get arrayType attribute"
                    //                             +xmlElValue); //+pp.getPosDesc());
                    //kompType = Object.class;
                    //}
                }
                int arrLen = -1;
                if(arrayType != null) {
                    int idxBracket = arrayType.indexOf('[');
                    if(idxBracket == -1) {
                        throw new TypeHandlerException("array must have size specified"
                                                           +" arrayType='"+arrayType+"' in "+xmlElValue);
                    }
                    String arrayQname = arrayType.substring(0, idxBracket);
                    int idxBracket2 = arrayType.indexOf(']', idxBracket + 1);
                    if(idxBracket2 == -1) {
                        throw new TypeHandlerException("array must have size specified"
                                                           +" arrayType='"+arrayType+"' in "+xmlElValue);
                    }
                    String arraySize = arrayType.substring(idxBracket+1, idxBracket2);
                    try {
                        if(arraySize.length() > 0) {
                            arrLen = Integer.parseInt(arraySize);
                        } else {
                            // this is a special case not covered by SOAP 1.1 spec that
                            // allows empty array to be represented as [] instead of [0]
                            // from http://www.w3.org/TR/SOAP/#_Toc478383513 arrayTypeValue/asize defintions
                            // (MOTIVATION: improve interoperability with GLUE)
                            arrLen = 0;
                        }
                    } catch(NumberFormatException nfe) {
                        throw new TypeHandlerException("array size must be integer"
                                                           +" not '"+arraySize+"' from "+arrayType+" in "
                                                           +xmlElValue);
                    }
                    
                    //              String uri = pp.getQNameUri(arrayQname);
                    //              if(uri == null) {
                    //                  throw new TypeHandlerException(
                    //                      "cant determine array element type from '"+arrayQname+"' "
                    //                          +xmlElValue);
                    //              }
                    //              String localName = pp.getQNameLocal(arrayQname);
                } else {
                    
                    arrLen =    0; // number of element children
                    Iterator elements = xmlElValue.requiredElementContent().iterator();
                    while(elements.hasNext()) {
                        elements.next();
                        ++arrLen;
                    }
                }
                
                if( compType.equals(String.class)) {
                    String[] sarr = new String[arrLen];
                    //write array as elements
                    Iterator elements = xmlElValue.requiredElementContent().iterator();
                    for (int i = 0; i < sarr.length; i++)
                    {
                        XmlElement arrayEl = (XmlElement) elements.next();
                        sarr[i] = arrayEl.requiredTextContent();
                    }
                    return sarr;
                } else if( compType.equals(Double.TYPE)) {
                    double[] dArr = new double[arrLen];
                    Iterator elements = xmlElValue.requiredElementContent().iterator();
                    for (int i = 0; i < dArr.length; i++)
                    {
                        XmlElement arrayEl = (XmlElement) elements.next();
                        dArr[i] = Double.parseDouble(arrayEl.requiredTextContent());
                    }
                    return dArr;
                } else if( compType.equals(Integer.TYPE)) {
                    int[] iArr = new int[arrLen];
                    Iterator elements = xmlElValue.requiredElementContent().iterator();
                    for (int i = 0; i < iArr.length; i++)
                    {
                        XmlElement arrayEl = (XmlElement) elements.next();
                        iArr[i] = Integer.parseInt(arrayEl.requiredTextContent());
                    }
                    return iArr;
                }
            }
        }
        
        return super.xmlElementToJava(xmlElValue, expectedType);
    }
    
    //    private static class SoapArrayHandler implements TypeHandler
    //    {
    //        public Object xmlToJava(Object xmlValue) throws TypeHandlerException
    //        {
    //        }
    //
    //        public Object javaToXml(Object javaValue) throws TypeHandlerException
    //        {
    //        }
    
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
 * SOFTWARE IS FREE @FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP
 * DOORS", "WORMS", OR OTHER HARMFUL CODE.  LICENSEE ASSUMES THE ENTIRE
 * RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS,
 * AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION GENERATED USING
 * SOFTWARE.
 */

