/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: MeshInterfaceObject.java,v 1.1 2005/02/14 03:50:52 aslom Exp $
 */

package soap_bench.complex_types;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidation;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.DataValidationException;

/*
 This class impelemnts following XML schema
 <schema targetNamespace="urn:Benchmark1"
 xmlns:b1="urn:Benchmark1"
 xmlns="http://www.w3.org/2001/XMLSchema"
 elementFormDefault="unqualified"
 attributeFormDefault="unqualified">

 <xsd:complexType name="MeshInterfaceObject">
 <annotation><documentation xml:lang="en">
 Mesh object type
 </documentation></annotation>
 <xsd:all>
 <xsd:element name="x" type="xsd:int"/>
 <xsd:element name="y" type="xsd:int"/>
 <xsd:element name="value" type="xsd:double"/>
 </xsd:all>
 </xsd:complexType>

 </schema>
 */

/**
 * SimpleEvent from SOAP Benchmark
 *
 * @version $Revision: 1.1 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class MeshInterfaceObject extends XmlElementAdapter implements XmlElement, DataValidation
{
    
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static XmlNamespace NS = builder.newNamespace("b1", "urn:Benchmark1");
    public final static String NAME = "MeshInterfaceObject";
    public final static String X_ELEM = "x";
    public final static String Y_ELEM = "y";
    public final static String VALUE_ELEM = "value";
    
    public MeshInterfaceObject(int x, int y, double value) {
        super(builder.newFragment(NS, NAME));
        // set values
        setX(x);
        setY(y);
        setValue(value);
    }
    
    public MeshInterfaceObject(XmlElement target) {
        super(target);
        validateData();
    }
    
    public int getX() {
        String s = requiredElement(null, X_ELEM).requiredTextContent();
        return Integer.parseInt(s);
    }
    
    public void setX(int value) {
        XmlElement el = element(null, X_ELEM, true);
        String s = Integer.toString(value);
        el.replaceChildrenWithText(s);
    }
    
    public int getY() {
        String s = requiredElement(null, Y_ELEM).requiredTextContent();
        return Integer.parseInt(s);
    }
    
    public void setY(int value) {
        XmlElement el = element(null, Y_ELEM, true);
        String s = Integer.toString(value);
        el.replaceChildrenWithText(s);
    }
    
    public double getValue() {
        String s = requiredElement(null, VALUE_ELEM).requiredTextContent();;
        return Double.parseDouble(s);
    }
    
    public void setValue(double value) {
        XmlElement el = element(null, VALUE_ELEM, true);
        String s = Double.toString(value);
        el.replaceChildrenWithText(s);
    }
    
    
    public String toString() {
        return builder.serializeToString(this) ;
    }
    
    public void validateData() throws DataValidationException {
        //if(! NAME.equals( getName() )) {
        //validate that <all> elements are present
        getX();
        getX();
        getValue();
    }
    
    public boolean equals(Object obj) {
        // compares only value
        if(obj == null) return false;
        if(!(obj instanceof MeshInterfaceObject)) return false;
        MeshInterfaceObject other = (MeshInterfaceObject)obj;
        boolean comp = getX() == other.getX()
            && getY() == other.getY()
            && getValue() == other.getValue();
        return comp;
    }
    
    public int hashCode() {
        // hashes only value
        int h = 0; //getName().hashCode();
        h ^= getX() ^ getY();
        h ^= (int) getValue();
        return h;
    }
    
    
    
    
}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (C) 2005 The Trustees of Indiana University.
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

