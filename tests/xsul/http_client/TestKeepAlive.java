/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestKeepAlive.java,v 1.6 2006/04/30 06:48:15 aslom Exp $
 */

package xsul.http_client;

import java.io.IOException;
import java.io.StringReader;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;
import xsul.XsulTestCase;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.processor.http.HttpDynamicInfosetProcessor;

/**
 * Tests for utility methods shared by all clases in package.
 *
 * @version $Revision: 1.6 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestKeepAlive extends XsulTestCase {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static HttpDynamicInfosetProcessor customizedProcessor;
    private String location;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestKeepAlive.class));
    }
    
    public TestKeepAlive(String name) {
        super(name);
    }
    
    
    protected void setUp() throws IOException {
        customizedProcessor = new HttpDynamicInfosetProcessor() {
            public  XmlDocument processXml(XmlDocument input) {
                //System.err.println(getClass()+" service got "+builder.serializeToString(input));
                XmlElement pingEl = input
                    .requiredElement(SOAP12_NS, XmlConstants.S_ENVELOPE)
                    .requiredElement(SOAP12_NS, XmlConstants.S_BODY)
                    .requiredElement(EXAMPLE_NS, "Ping");
                int val = Integer.parseInt(pingEl.requiredTextContent());
                pingEl.removeAllChildren();
                pingEl.addChild(Integer.toString(val + 1));
                return input;
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
    
    public void testSocketReuse() throws Exception
    {
        testSocketReuse(true);
        testSocketReuse(false);
    }
    
    public void testSocketReuse(boolean reuse) throws Exception
    {
        // start server
        
        //make invocations - check that socket is reused
        SoapHttpDynamicInfosetInvoker invoker = new SoapHttpDynamicInfosetInvoker();
        //new Soap11HttpDynamicInfosetInvoker(HttpClientReuseLastConnectionManager.newInstance());
        //new Soap11HttpDynamicInfosetInvoker(HttpClientConnectionManager.newInstance());
        invoker.setLocation(location);
        invoker.setKeepAlive(reuse);
        HttpClientReuseLastConnectionManager mgr =
            (HttpClientReuseLastConnectionManager) invoker.getConnectionManager();
        final int N = 4; //1000;
        ClientSocketConnection previousCon = null;
        for (int i = 0; i < N ; i++)
        {
            String msg = generateXmlDoc(i);
            XmlDocument requestDoc = builder.parseReader(new StringReader(msg));
            XmlDocument responseDoc = invoker.invokeXml(requestDoc);
            assertNotNull(responseDoc);
            //System.err.println(getClass()+" responseDoc="+builder.serializeToString(responseDoc));
            //XmlElement root = responseDoc.getDocumentElement();
            //assertEquals("Envelope", root.getName());
            //assertEquals(SOAP12_NS, root.getNamespaceName());
            String stringResult = responseDoc
                .requiredElement(SOAP12_NS, XmlConstants.S_ENVELOPE)
                .requiredElement(SOAP12_NS, XmlConstants.S_BODY)
                .requiredElement(EXAMPLE_NS, "Ping")
                .requiredTextContent();
            int result = Integer.parseInt(stringResult);
            //System.out.println(getClass()+" "+i+" -> "+result);
            if(i + 1 != result) {
                throw new RuntimeException("service sent wrong answer");
            }
            if(previousCon != null) {
                if(reuse) {
                    assertSame(previousCon, mgr.lastConn);
                } else {
                    assertNotSame(previousCon, mgr.lastConn);
                }
            }
            previousCon = mgr.lastConn;
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

