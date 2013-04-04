/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Struct.java,v 1.3 2005/02/08 19:31:35 aslom Exp $
 */

package xsul_sample_hello;

import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;

/*
 This class impelemnts following XML schema
 <schema targetNamespace="http://example.org/struct"
                xmlns="http://www.w3.org/2001/XMLSchema"
                elementFormDefault="unqualified">
        <complexType name="structType">
                <annotation><documentation xml:lang="en">
                  Simple struct (int,  int, double)
                </documentation></annotation>
       
                <sequence>
                        <element minOccurs="1" maxOccurs="1" name="intElem" type="xsd:int"/>
                        <element minOccurs="1" maxOccurs="1" name="doubleValue" type="xsd:double" default="0.0"/>
                </sequence>
                <attribute name="intAttr" type="xsd:int" default="-1"/>
        </complexType>
        <element name="struct" type="typens:structType"/>
  </schema>
 */

/**
 * Exampel of very simpel event ("struct")
 * Implements:

 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class Struct extends XmlElementAdapter implements XmlElement
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static XmlNamespace NS = builder.newNamespace("s", "http://example.org/struct");
    public final static String NAME = "struct";
    public final static String INT_ATTR = "intAttr";
    public final static int DEFAULT_VALUE_INT_ATTR = -1;
    public final static String INT_ELEM = "intElem";
    public final static String DOUBLE_ELEM = "doubleVale";
    public final static double DEFAULT_DOUBLE_ELEM = 0.0d;
    
    public Struct(int attrVal, int elemVal, double doubleVal) {
        super(builder.newFragment(NS, NAME));
        // create sequence of element
        addElement(null, INT_ELEM);
        addElement(null, DOUBLE_ELEM);
        // set values
        setIntAttr(attrVal);
        setIntElem(elemVal);
        setDoubleElem(doubleVal);
    }
    
    public Struct(XmlElement target) {
        super(target);
        //TODO: validation is required to check that sequence is OK
    }
    
    public int getIntAttr() {
        String s = getAttributeValue(null, INT_ATTR);
        if(s != null) {
            return Integer.parseInt(s);
        } else {
            return DEFAULT_VALUE_INT_ATTR;
        }
    }
    
    public void setIntAttr(int value) {
        XmlAttribute att = findAttribute(null, INT_ATTR);
        if(att != null) {
            removeAttribute(att);
        }
        String s = Integer.toString(value);
        addAttribute(null, INT_ATTR, s);
    }
    
    public int getIntElem() {
        String s = requiredElement(null, INT_ELEM).requiredTextContent();
        return Integer.parseInt(s);
    }
    
    public void setIntElem(int value) {
        XmlElement el = requiredElement(null, INT_ELEM);
        String s = Integer.toString(value);
        el.replaceChildrenWithText(s);
    }
    
    public double getDoubleElem() {
        XmlElement el = element(null, DOUBLE_ELEM);
        if(el != null) {
            String s = el.requiredTextContent();
            return Double.parseDouble(s);
        } else {
            return DEFAULT_DOUBLE_ELEM;
        }
    }
    
    public void setDoubleElem(double value) {
        XmlElement el = element(null, DOUBLE_ELEM, true);
        String s = Double.toString(value);
        el.replaceChildrenWithText(s);
    }
    
    public String toString() {
        return builder.serializeToString(this) ;
    }
}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (C) 2004 The Trustees of Indiana University.
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






