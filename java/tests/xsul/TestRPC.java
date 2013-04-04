/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/* Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 * This software is open source. See the bottom of this file for the licence.
 * $Id: TestRPC.java,v 1.5 2006/04/21 15:45:44 aslom Exp $ */

package xsul;

import java.io.IOException;
import java.lang.reflect.Proxy;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XsulTestCase;
import xsul.common_type_handler.CommonTypeHandlerRegistry;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.processor.MessageProcessor;
import xsul.processor.http.HttpDynamicInfosetProcessor;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.soaprpc_server.SoapRpcReflectionBasedService;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

/**
 * Tests for utility methods shared by all clases in package.
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestRPC extends XsulTestCase {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static HttpDynamicInfosetProcessor customizedProcessor;
    private String location;
    private int lastVal;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestRPC.class));
    }
    
    public TestRPC(String name) {
        super(name);
    }
    
    
    protected void setUp() throws IOException {
        FooService srv = new FooServiceImpl();
        
        TypeHandlerRegistry registry = CommonTypeHandlerRegistry.getInstance();
        
        final MessageProcessor refelctionDispatcher
            = new SoapRpcReflectionBasedService(srv, registry);
        
        customizedProcessor = new SoapHttpDynamicInfosetProcessor() {
            public XmlElement processMessage(XmlElement message) {
                XmlElement resp = refelctionDispatcher.processMessage(message);
                return resp;
            }
        };
        //customizedProcessor.setServerPort(port);
        
        customizedProcessor.start();
        location = customizedProcessor.getServer().getLocation();
        //System.err.println(getClass()+" started on "+location);
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
    
    public void testRPC() throws Exception
    {
        SoapHttpDynamicInfosetInvoker invoker = new SoapHttpDynamicInfosetInvoker(location);
        //invoker.setKeepAlive(false);
        
        SoapRpcInvocationHandler handler
            = new SoapRpcInvocationHandler(invoker, CommonTypeHandlerRegistry.getInstance());
        
        FooService service
            = (FooService) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { FooService.class },
            handler);
        
        String s = service.echo("test");
        assertEquals("test", s);
        
        String[] sa = new String[]{"test", "test2" };
        String[] got = service.echoStringArr( sa );
        assertEquals(2, got.length);
        assertEquals(sa[0], got[0]);
        assertEquals(sa[1], got[1]);
        
        
    }
}

