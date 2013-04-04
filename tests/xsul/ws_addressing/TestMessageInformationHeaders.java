/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestMessageInformationHeaders.java,v 1.9 2006/04/30 06:48:15 aslom Exp $
 */

package xsul.ws_addressing;

import java.io.StringReader;
import java.net.URI;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.XsulTestCase;
import xsul.ws_addressing.WsaMessageInformationHeaders;

/**
 * Tests for utility methods shared by all clases in package.
 *
 * @version $Revision: 1.9 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestMessageInformationHeaders extends XsulTestCase {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestMessageInformationHeaders.class));
    }
    
    public TestMessageInformationHeaders(String name) {
        super(name);
    }
    
    
    public void testMIH() throws Exception
    {
        XmlElement el = builder.parseFragmentFromReader(new StringReader(SAMPLE_SOAP12_WITH_MIH));
        //        WsaEndpointReference ep = new WsaEndpointReference(new URI("http://test"));
        //        System.out.println("equals="+ep.getAddress().equals(new URI("http://test")));
        XmlElement headers = (XmlElement) el.requiredElementContent().iterator().next();
        
        WsaMessageInformationHeaders mih = new WsaMessageInformationHeaders(headers);
        
        
        assertEquals(new URI("mailto:joe@fabrikam123.com"), new URI("mailto:joe@fabrikam123.com"));
        //[destination] The URI mailto:joe@fabrikam123.com.
        assertEquals(new URI("mailto:joe@fabrikam123.com"), mih.getTo());
        //[reply endpoint] The endpoint with [address] http://business456.com/client1.
        assertEquals(new URI("http://business456.com/client1"), mih.getReplyTo().getAddress());
        //[fault endpoint] The endpoint with [address] http://business456.com/deadletters.
        assertEquals(new URI("http://business456.com/deadletters"), mih.getFaultTo().getAddress());
        //[action] http://fabrikam123.com/mail#Delete
        assertEquals(new URI("http://fabrikam123.com/mail#Delete"), mih.getAction());
        //[message id] uuid:aaaabbbb-cccc-dddd-eeee-ffffffffffff
        assertEquals(new URI("uuid:aaaabbbb-cccc-dddd-eeee-ffffffffffff"), mih.getMessageId());
        //[relationship] (wsa:Response,uuid:11112222-3333-4444-5555-666666666666)
        WsaRelatesTo relates = (WsaRelatesTo) mih.getRelatesTo().iterator().next();
        assertEquals(new URI("uuid:11112222-3333-4444-5555-666666666666"), relates.getRelationship());
        assertEquals(WsAddressing.URI_DEFAULT_REPLY_RELATIONSHIP_TYPE, relates.getRelationshipType());
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

