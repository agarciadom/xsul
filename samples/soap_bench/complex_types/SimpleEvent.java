/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SimpleEvent.java,v 1.1 2005/02/14 03:50:52 aslom Exp $
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
 <xsd:complexType name="SimpleEvent">
 <annotation><documentation xml:lang="en">
 Simple event
 </documentation></annotation>
 <xsd:all>
 <xsd:element name="sequenceNumber" type="xsd:int"/>
 <xsd:element name="timestamp" type="xsd:double"/>
 <xsd:element name="message" type="xsd:string"/>
 </xsd:all>
 </xsd:complexType>
 </complexType>
 </schema>
 */

/**
 * SimpleEvent from SOAP Benchmark
 *
 * @version $Revision: 1.1 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class SimpleEvent extends XmlElementAdapter implements XmlElement, DataValidation
{
    
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static XmlNamespace NS = builder.newNamespace("b1", "urn:Benchmark1");
    public final static String NAME = "SimpleEvent";
    public final static String SEQUENCE_NUMBER_ELEM = "sequenceNumber";
    public final static String TIMESTAMP_ELEM = "timestamp";
    public final static String MESSAGE_ELEM = "message";
    
    public SimpleEvent(int sequenceNumber, double timestamp, String message) {
        super(builder.newFragment(NS, NAME));
        // set values
        setSequenceNumber(sequenceNumber);
        setTimestamp(timestamp);
        setMessage(message);
    }
    
    public SimpleEvent(XmlElement target) {
        super(target);
        validateData();
    }
    
    public int getSequenceNumber() {
        String s = requiredElement(null, SEQUENCE_NUMBER_ELEM).requiredTextContent();
        return Integer.parseInt(s);
    }
    
    public void setSequenceNumber(int value) {
        XmlElement el = element(null, SEQUENCE_NUMBER_ELEM, true);
        String s = Integer.toString(value);
        el.replaceChildrenWithText(s);
    }
    
    public double getTimestamp() {
        String s = requiredElement(null, TIMESTAMP_ELEM).requiredTextContent();;
        return Double.parseDouble(s);
    }
    
    public void setTimestamp(double value) {
        XmlElement el = element(null, TIMESTAMP_ELEM, true);
        String s = Double.toString(value);
        el.replaceChildrenWithText(s);
    }

    public String getMessage() {
        String s = requiredElement(null, MESSAGE_ELEM).requiredTextContent();;
        return s;
    }
    
    public void setMessage(String message) {
        if(message == null) throw new IllegalArgumentException();
        XmlElement el = element(null, MESSAGE_ELEM, true);
        el.replaceChildrenWithText(message);
    }
    
    public String toString() {
        return builder.serializeToString(this) ;
    }

    public void validateData() throws DataValidationException {
        //if(! NAME.equals( getName() )) {
        //validate that <all> elements are present
        getSequenceNumber();
        getTimestamp();
        getMessage();
    }
    
    public boolean equals(Object obj) {
        // compares only value
        if(obj == null) return false;
        if(!(obj instanceof SimpleEvent)) return false;
        SimpleEvent other = (SimpleEvent)obj;
        boolean comp = getSequenceNumber() == other.getSequenceNumber()
            && getTimestamp() == other.getTimestamp()
            && getMessage().equals(other.getMessage());
        return comp;
    }
    
    public int hashCode() {
        // hashes only value
        int h = 0; //getName().hashCode();
        h ^= getMessage().hashCode() ^ getSequenceNumber();
        h ^= (int) getTimestamp();
        return h;
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






