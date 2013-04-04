/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestAsyncWithMsgBox.java,v 1.1 2005/11/16 02:49:09 aslom Exp $
 */

package xsul.async_msg;

import java.io.IOException;
import java.net.URI;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulTestCase;
import xsul.msg_box.AddOneService;
import xsul.msg_box.MsgBoxServer;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;

/**
 * Test async messaging for client that uses WS-MsgBox.
 *
 * @version $Revision: 1.1 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestAsyncWithMsgBox extends XsulTestCase {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

//    private String webServiceLocation;
//    private static MsgBoxServer msgBoxServer;
//
//    private URI messageBoxLocation;
//
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestAsyncWithMsgBox.class));
    }

    public TestAsyncWithMsgBox(String name) {
        super(name);
    }

//    public void testClientUsingMB() throws Exception
//    {
//
//    }
//
//    protected void setUp() throws IOException {
//
//        msgBoxServer = new MsgBoxServer();
//        msgBoxServer.start();
//        messageBoxLocation = msgBoxServer.getLocation();
//
//        //TypeHandlerRegistry registry = CommonTypeHandlerRegistry.getInstance();
//
//        //
//        // This is super generic echo service tah can support RPC, one-way messaging,
//        // and request-response with messaging and sending redirected response
//        //
//        addOneService = new AddOneService();
//        //customizedProcessor.setServerPort(port);
//
//
//        addOneService.start();
//
//        webServiceLocation = addOneService.getServer().getLocation();
//        //System.err.println(getClass().getName()+" started on "+location);
//        logger.finest("started service on "+webServiceLocation);
//    }
//
//    protected void tearDown() {
//        addOneService.stop();
//        addOneService.shutdown();
//        msgBoxServer.stop();
//        msgBoxServer.shutdown();
//    }
//
}



