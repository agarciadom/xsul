/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestOneWayMessaging.java,v 1.9 2006/04/30 06:48:15 aslom Exp $
 */

package xsul;

import java.io.IOException;
import java.io.StringReader;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XsulTestCase;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.processor.http.HttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;

/**
 * Tests for utility methods shared by all clases in package.
 *
 * @version $Revision: 1.9 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestOneWayMessaging extends XsulTestCase {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static HttpDynamicInfosetProcessor customizedProcessor;
    private String location;
    private int lastVal;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestOneWayMessaging.class));
    }
    
    public TestOneWayMessaging(String name) {
        super(name);
    }
    
    
    protected void setUp() throws IOException {
        customizedProcessor = new HttpDynamicInfosetProcessor() {
            public  XmlDocument processXml(XmlDocument inputDoc) {
                XmlElement pingEl = inputDoc
                    .requiredElement(SOAP12_NS, XmlConstants.S_ENVELOPE)
                    .requiredElement(SOAP12_NS, XmlConstants.S_BODY)
                    .requiredElement(EXAMPLE_NS, "Ping");
                lastVal = Integer.parseInt(pingEl.requiredTextContent());
                
                //indicate that no response is returneds
                return null;
            }
        };
        customizedProcessor.start();
        location = customizedProcessor.getServer().getLocation();
        //System.err.println(getClass()+" started");
    }
    
    protected void tearDown() {
        //System.err.println(getClass()+" shutdown");
        customizedProcessor.stop();
        customizedProcessor.shutdown();
        //        try {
        //            Thread.currentThread().sleep(1000);
        //        } catch (InterruptedException e) {
        //        }
    }
    
    public void testOneWayMessaging() throws Exception
    {
        testOneWayMessaging(true);
        testOneWayMessaging(false);
    }
    
    public void testOneWayMessaging(boolean reuse) throws Exception
    {
        // start server
        
        //make invocations - check that socket is reused
        SoapHttpDynamicInfosetInvoker invoker = new SoapHttpDynamicInfosetInvoker();
        //new Soap11HttpDynamicInfosetInvoker(HttpClientReuseLastConnectionManager.newInstance());
        //new Soap11HttpDynamicInfosetInvoker(HttpClientConnectionManager.newInstance());
        invoker.setLocation(location);
        invoker.setKeepAlive(reuse);
        final int N = 4; //1000;
        for (int i = 0; i < N ; i++)
        {
            String msg = generateXmlDoc(i);
            XmlDocument requestDoc = builder.parseReader(new StringReader(msg));
            XmlDocument responseDoc = invoker.invokeXml(requestDoc);
            assertNull(responseDoc);
            //System.err.println(getClass()+" responseDoc="+builder.serializeToString(responseDoc));
            //XmlElement root = responseDoc.getDocumentElement();
            //assertEquals("Envelope", root.getName());
            //assertEquals(SOAP12_NS, root.getNamespaceName());
            //int result = Integer.parseInt(stringResult);
            //System.out.println(getClass()+" "+i+" -> "+result);
            //if(i + 1 != result) {
            //    throw new RuntimeException("service sent wrong answer");
            //}
            assertEquals(i, lastVal);
        }
        
        //shutdown server
    }
    private final static String SOAP12 =
        "http://www.w3.org/2002/12/soap-envelope";
    private final static XmlNamespace SOAP12_NS = builder.newNamespace(SOAP12);
    private final static String EXAMPLE =
        "http://www.example.com/ping-service";
    private final static XmlNamespace EXAMPLE_NS = builder.newNamespace(EXAMPLE);
    
    private static String generateXmlDoc(int value) {
        return
            "<S:Envelope xmlns:S=\""+SOAP12+"\"\n"+
            "    xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2003/03/addressing\""+
            "    xmlns:f123=\""+EXAMPLE+"\""+
            ">\n"+
            "    <S:Header>\n"+
            "        <wsa:MessageID>uuid:aaaabbbb-cccc-dddd-eeee-ffffffffffff\n"+
            "        </wsa:MessageID>\n"+
            "        <wsa:RelatesTo>uuid:11112222-3333-4444-5555-666666666666\n"+
            "        </wsa:RelatesTo>\n"+
            "        <wsa:ReplyTo>\n"+
            "            <wsa:Address>http://business456.com/client1</wsa:Address>\n"+
            "        </wsa:ReplyTo>\n"+
            "        <wsa:FaultTo>\n"+
            "            <wsa:Address>http://business456.com/deadletters</wsa:Address>\n"+
            "        </wsa:FaultTo>\n"+
            "        <wsa:To S:mustUnderstand=\"1\">mailto:joe@fabrikam123.com</wsa:To>\n"+
            "        <wsa:Action>http://fabrikam123.com/mail#Delete</wsa:Action>\n"+
            "    </S:Header>\n"+
            "    <S:Body>\n"+
            "        <f123:Ping>"+value+"</f123:Ping>\n"+
            "    </S:Body>\n"+
            "</S:Envelope>";
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

