/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2006 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestWsaConversion.java,v 1.4 2006/04/30 06:48:15 aslom Exp $
 */

package xsul.ws_addressing;

import java.io.StringReader;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.XsulTestCase;
import xsul.XsulVersion;
import xsul.util.XsulUtil;

/**
 * Tests for converting between different versions of WS-Addressing (mostly to and from WSA 1.0)
 *
 * @version $Revision: 1.4 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestWsaConversion extends XsulTestCase {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestWsaConversion.class));
    }
    
    public TestWsaConversion(String name) {
        super(name);
    }
    
    
    public void testWsaCoversion() throws Exception
    {
        XsulVersion.requireVersion("2.5.4");
        XmlElement soapEnv2005 = builder.parseFragmentFromReader(new StringReader(MSG_2005));
        XmlElement soapEnv2004 = builder.parseFragmentFromReader(new StringReader(MSG_2004_8));
        
        XmlElement soapEnv = builder.parseFragmentFromReader(new StringReader(MSG_2005));
        WsaConverter.convert(soapEnv, WsAddressing.NS_2004_08);
        assertXmlElements(soapEnv2004, soapEnv);
        //System.err.println(getClass()+" headers="+XsulUtil.safeXmlToString(soapEnv));
        WsaConverter.convert(soapEnv);
        //System.err.println(getClass()+" headers="+XsulUtil.safeXmlToString(soapEnv));
        assertXmlElements(soapEnv2005, soapEnv);
    }
    
    final private static String MSG_2005 =
        "<S:Envelope xmlns:S=\"http://www.w3.org/2002/12/soap-envelope\"\n"+
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
        "            <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n"+
        "        </wsa:FaultTo>\n"+
        "        <wsa:To S:mustUnderstand=\"1\">http://www.w3.org/2005/08/addressing/anonymous</wsa:To>\n"+
        "        <wsa:Action>http://fabrikam123.com/mail#Delete</wsa:Action>\n"+
        "    </S:Header>\n"+
        "    <S:Body>\n"+
        "        <f123:Delete>\n"+
        "            <maxCount>42</maxCount>\n"+
        "        </f123:Delete>\n"+
        "    </S:Body>\n"+
        "</S:Envelope>";

        final private static String MSG_2004_8 =
        "<S:Envelope xmlns:S=\"http://www.w3.org/2002/12/soap-envelope\"\n"+
        "    xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\""+
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
        "            <wsa:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:Address>\n"+
        "        </wsa:FaultTo>\n"+
        "        <wsa:To S:mustUnderstand=\"1\">http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:To>\n"+
        "        <wsa:Action>http://fabrikam123.com/mail#Delete</wsa:Action>\n"+
        "    </S:Header>\n"+
        "    <S:Body>\n"+
        "        <f123:Delete>\n"+
        "            <maxCount>42</maxCount>\n"+
        "        </f123:Delete>\n"+
        "    </S:Body>\n"+
        "</S:Envelope>";
    

}

