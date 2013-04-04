/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestUtil.java,v 1.6 2006/04/30 03:22:04 aslom Exp $
 */

package xsul.util;

import java.io.StringReader;
import javax.xml.namespace.QName;
import junit.framework.TestSuite;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.XsulTestCase;
import xsul.util.XsulUtil;

/**
 * Tests for utility methods shared by all clases in package.
 *
 * @version $Revision: 1.6 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestUtil extends XsulTestCase {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestUtil.class));
    }
    
    public TestUtil(String name) {
        super(name);
    }
    
    protected void setUp() {
    }
    
    protected void tearDown() {
    }
    
    
    public void testQName() throws Exception
    {
        XmlElement el = builder.parseFragmentFromReader(new StringReader(
                                                            "<t xmlns='urn:n1'>test</t>"));
        QName q = XsulUtil.getQNameContent(el);
        //System.out.println(getClass()+" q="+q);
        
        el = builder.parseFragmentFromReader(new StringReader(
                                                 "<t xmlns:n='urn:n2'>n:test</t>"));
        q = XsulUtil.getQNameContent(el);
        //System.out.println(getClass()+" q="+q);
        
        //        el = builder.parseFragmentFromReader(new StringReader(
        //                                                 "<t xmlns:m='urn:n3'>n:test</t>"));
        //        q = Util.getQNameContent(el);
        //        System.out.println("q="+q);
        
        XmlPullParser pp = builder.getFactory().newPullParser();
        pp.setInput(new StringReader("<Rq><Rpc>x\t\t\n</Rpc></Rq>")); //"<Rq><Rpc>x\r\n</Rpc></Rq>"));
        pp.next();
        pp.next();
        //System.out.println(getClass()+" "+pp.getPositionDescription());
        pp.next();
        //System.out.println(getClasS()+" "+pp.getPositionDescription());
    }
    
    private final String XML = "<fileConsumed wor:topic=\"somerandomtopic\"\n"+
        "xmlns=\"http://lead.extreme.indiana.edu/namespaces/2005/06/workflow_tracking\"\n"+
        "xmlns:wor=\"http://lead.extreme.indiana.edu/namespaces/2005/06/workflow_tracking\">\n"+
        "       <workflowID>wf100201</workflowID>\n"+
        "       <nodeID>N1</nodeID>\n"+
        "       <wfTimeStep>1</wfTimeStep>\n"+
        "       <serviceID>{http://foobar.org}FuBar1</serviceID>\n"+
        "       <timestamp>2006-04-21T10:03:49.906+05:30</timestamp>\n"+
        "       <file>\n"+
        "               <fileUUID>leadId-001</fileUUID>\n"+
        "               <localFilePath>/6+data/tmp/foo.dat</localFilePath>\n"+
        "               <fileSizeBytes>0</fileSizeBytes>\n"+
        "               <timestamp>2006-04-21T10:03:49.921+05:30</timestamp>\n"+
        "       </file>\n"+
        "       <file>\n"+
        "               <fileUUID>leadId-002</fileUUID>\n"+
        "               <localFilePath>/etc/app/config/foo.cfg</localFilePath>\n"+
        "               <fileSizeBytes>0\n"+
        "</fileSizeBytes>\n"+
        "               <timestamp>2006-04-21T10:03:49.921+05:30</timestamp>\n"+
        "       </file>\n"+
        "</fileConsumed>\n";
    
    public void testParsingRegression() throws Exception {
        //System.err.println(getClass()+" before builder="+XML);
        XmlElement messageEl = builder.parseFragmentFromReader(new StringReader(XML));
        final String XML2 = builder.serializeToString(messageEl);
        //System.err.println(getClass()+"after Builder="+XML2);
        XmlElement el2 = builder.parseFragmentFromReader(new StringReader(XML2));

    }
}
