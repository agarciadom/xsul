/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2006 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestXmlNameValueList.java,v 1.3 2006/08/29 18:25:58 aslom Exp $
 */

package xsul.lead;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import javax.xml.namespace.QName;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.XsulTestCase;
import xsul.XsulVersion;
import org.apache.xmlbeans.XmlObject;

/**
 * Tests for converting between different versions of WS-Addressing (mostly to and from WSA 1.0)
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestXmlNameValueList extends XsulTestCase {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestXmlNameValueList.class));
    }
    
    public TestXmlNameValueList(String name) {
        super(name);
    }
    
    
    public void testPropertiesCoversion() throws Exception
    {
        XsulVersion.requireVersion("2.7.4");
        Properties props = new Properties();
        //  properties.load(new FileInputStream("filename.properties"));
        props.load(new ByteArrayInputStream(PROPS.getBytes("UTF8")));
        //System.err.println(getClass()+" props ="+props);
        XmlNameValueList xmlProps = new XmlNameValueList(new QName("NameListPropertis"), props);
        //System.err.println(getClass()+" xmlProps="+builder.serializeToString(xmlProps));
        Properties props2 = xmlProps.toProperties();
        //System.err.println(getClass()+" props2="+props2);
        //props2.store(new FileOutputStream("filename.properties"), null)
    }

    public void testXmlObjectConversion() throws Exception
    {
        XsulVersion.requireVersion("2.7.4");
        XmlNameValueList xmlProps = new XmlNameValueList();
        xmlProps.setProperty("foo", "baz");
        xmlProps.setProperty("far", "10000000000000000");
        XmlObject xmlObjProps = xmlProps.xmlObject();
        //System.err.println(getClass()+" xmlObjProps="+xmlObjProps.toString());
        XmlNameValueList xmlProps2 = new XmlNameValueList(xmlObjProps);
        //System.err.println(getClass()+" xmlProps2="+builder.serializeToString(xmlProps2));
        //props2.store(new FileOutputStream("filename.properties"), null)
    }
    
    final private static String PROPS =
        "#Model Domain Configuration parameters\n"+
        "#Wed May 31 11:22:58 EDT 2006\n"+
        "nx=64\n"+
        "ctrlat=39.686\n"+
        "dy=5\n"+
        "ctrlon=-85.941\n"+
        "dx=5\n"+
        "ny=64 \n"+
        "";
    
}

