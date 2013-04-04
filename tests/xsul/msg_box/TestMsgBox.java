/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestMsgBox.java,v 1.14 2006/05/02 01:05:59 aslom Exp $
 */

package xsul.msg_box;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulTestCase;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.ws_addressing.WsAddressing;
import xsul.ws_addressing.WsaEndpointReference;
import xsul.ws_addressing.WsaInvoker;

/**
 * Tests simple interaction with WS-MsgBox.
 *
 * @version $Revision: 1.14 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestMsgBox extends XsulTestCase {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static SoapUtil[] soapFragrances = new SoapUtil[] {
        Soap12Util.getInstance(), Soap11Util.getInstance() };
    
    private final static String MESSAGE_URI = "http://example.com/AddOne";
    private final static XmlNamespace REF_PROPERTY_NS = builder.newNamespace(MESSAGE_URI);
    
    private static SoapHttpDynamicInfosetProcessor addOneService;
    private String webServiceLocation;
    private static MsgBoxServer msgBoxServer;
    
    private URI messageBoxLocation;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestMsgBox.class));
    }
    
    public TestMsgBox(String name) {
        super(name);
    }
    
    public void testClientUsingMB() throws Exception
    {
        testClientUsingMB(WsAddressing.NS_WSA);
        testClientUsingMB(WsAddressing.NS_2004_08);
    }
    
    private void testClientUsingMB(XmlNamespace wsaNs) throws Exception
    {
        // create mailbox
        MsgBoxClient msgBoxService = new MsgBoxClient(wsaNs, messageBoxLocation);
        
        WsaEndpointReference msgBoxAddr = msgBoxService.createMsgBox();
        //System.out.println(getClass()+" msgBoxId="+msgBoxAddr);
        
        {
            // this rpevents against chnages by anybody using reply address and chnaging its name to replyTo etc ...
            //WsaEndpointReference replyAddr = (WsaEndpointReference) msgBoxAddr.clone();
            // send echo mesage with replyTo using maiblox epr
            XmlElement message = builder.newFragment(
                MESSAGE_URI, "addOne");
            message.addChild("777");
            
            
            WsaEndpointReference serviceEpr = new WsaEndpointReference(wsaNs, new URI(webServiceLocation));
            WsaInvoker invoker = new WsaInvoker();
            invoker.setTargetEPR(serviceEpr);
            invoker.setDefaultAction(new URI(MESSAGE_URI));
            invoker.setDefaultReplyTo(msgBoxAddr); //replyAddr);
            assertNotNull(invoker.getDefaultAction());
            invoker.invokeMessage(message);

            //avoid possible reordering due toasync nature of calls
            Thread.currentThread().sleep(100L);
            
            message.replaceChildrenWithText("888");
            invoker.invokeMessage(message);
        }
        
        //wait for message to arrive to msg boox service
        Thread.currentThread().sleep(500L);
        
        {
            // retrieve message(s) from mailbox
            List messages = new ArrayList();
            for (int trying = 0; trying < 2; trying++)
            {
                XmlElement msg[] = msgBoxService.takeMessages(msgBoxAddr);
                for (int i = 0; i < msg.length; i++) {
                    messages.add(msg[i]);
                }
            }
            
            //System.out.println(getClass()+" got "+builder.serializeToString(msg[0]));
            assertEquals(2, messages.size());
            XmlElement response = (XmlElement) messages.get(0);
            SoapUtil soapFragrance = SoapUtil.selectSoapFragrance(response, soapFragrances);
            XmlElement responseMsg =  soapFragrance.requiredBodyContent(response);
            assertEquals("778", responseMsg.requiredTextContent());
            
            response = (XmlElement) messages.get(1);
            soapFragrance = SoapUtil.selectSoapFragrance(response, soapFragrances);
            responseMsg =  soapFragrance.requiredBodyContent(response);
            assertEquals("889", responseMsg.requiredTextContent());
            
        }
        //destroy mailbox
        msgBoxService.destroyMsgBox(msgBoxAddr);
        
    }
    
    protected void setUp() throws IOException {
        
        msgBoxServer = new MsgBoxServer();
        msgBoxServer.start();
        messageBoxLocation = msgBoxServer.getLocation();
        
        //
        // This is super generic echo service tah can support RPC, one-way messaging,
        // and request-response with messaging and sending redirected response
        //
        addOneService = new AddOneService();
        //customizedProcessor.setServerPort(port);
        
        addOneService.setSupportedSoapFragrances(new SoapUtil[]{
                    Soap12Util.getInstance(), Soap11Util.getInstance()});
        
        addOneService.start();
        
        webServiceLocation = addOneService.getServer().getLocation();
        //System.err.println(getClass().getName()+" started on "+location);
        logger.finest("started service on "+webServiceLocation);
    }
    
    protected void tearDown() {
        addOneService.stop();
        addOneService.shutdown();
        msgBoxServer.stop();
        msgBoxServer.shutdown();
    }
    
}




