/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2006 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestDetectMessageIdDuplicates.java,v 1.2 2006/04/21 15:45:44 aslom Exp $
 */

package xsul.ws_addressing;

import java.io.StringReader;
import java.net.URI;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.XsulTestCase;

/**
 * Tests for detecting message that has duplicate WS-Addressing MessageId
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestDetectMessageIdDuplicates extends XsulTestCase {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestDetectMessageIdDuplicates.class));
    }
    
    public TestDetectMessageIdDuplicates(String name) {
        super(name);
    }
    
    
    public void testMessageIdDetection() throws Exception
    {
        WsaDetectDuplicateMessageId detector  = new WsaDetectDuplicateMessageId();
        XmlElement soapEnv = builder.parseFragmentFromReader(new StringReader(SAMPLE_SOAP12_WITH_MIH));
        XmlElement headers = (XmlElement) soapEnv.requiredElementContent().iterator().next();
        assertFalse(detector.seenMessageId(soapEnv));
        assertTrue(detector.seenMessageId(soapEnv));
        assertTrue(detector.seenMessageId(headers));
        XmlElement S2 = (XmlElement) soapEnv.clone();
        XmlElement H2 = (XmlElement) S2.requiredElementContent().iterator().next();
        assertTrue(detector.seenMessageId(H2));
        WsaMessageInformationHeaders mih = new WsaMessageInformationHeaders(H2);
        mih.setMessageId(URI.create("uuid:11112222-3333-4444-5555-666666666666"));
        assertFalse(detector.seenMessageId(H2));
        assertTrue(detector.seenMessageId(H2));
        // test that messages witout id are *never* duplicates
        XmlElement mId = H2.element(null, "MessageID");
        assertNotNull(mId);
        H2.removeChild(mId);
        assertFalse(detector.seenMessageId(H2));
        assertFalse(detector.seenMessageId(H2));
        assertFalse(detector.seenMessageId(S2));
    }
    
    final private static String SAMPLE_SOAP12_WITH_MIH =
        "<S:Envelope xmlns:S=\"http://www.w3.org/2002/12/soap-envelope\"\n"+
        //"    xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\""+
        "    xmlns:wsa=\"http://www.w3.org/2005/08/addressing\""+
        "    xmlns:f123=\"http://www.fabrikam123.com/svc53\""+
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
        "        <f123:Delete>\n"+
        "            <maxCount>42</maxCount>\n"+
        "        </f123:Delete>\n"+
        "    </S:Body>\n"+
        "</S:Envelope>";
    
}

