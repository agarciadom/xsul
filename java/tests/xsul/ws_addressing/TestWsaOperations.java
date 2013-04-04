/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestWsaOperations.java,v 1.3 2006/04/30 23:17:28 aslom Exp $
 */

package xsul.ws_addressing;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.XsulTestCase;
import xsul.XsulVersion;
import xsul.common_type_handler.CommonTypeHandlerRegistry;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.ws_addressing.WsaMessageInformationHeaders;

/**
 * Tests for utility methods shared by all clases in package.
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestWsaOperations extends XsulTestCase {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static String REF_PROPERTY_URI = "http://www.producer.org/RefProp";
    private final static XmlNamespace REF_PROPERTY_NS = builder.newNamespace(REF_PROPERTY_URI);
    private final static String REF_PROPERTY_NAME = "NSResourceId";
    private final static String REF_PROPERTY_VALUE = "uuid:8fefcd11-7d3d-66b344a2-ca44-9876bacd44e9";
    private final static String EXAMPLE_REF_PROPERTY =
        "<npex:"+REF_PROPERTY_NAME+" xmlns:npex='"+REF_PROPERTY_URI+"'>"+
        REF_PROPERTY_VALUE+"</npex:"+REF_PROPERTY_NAME+">";
    
    private static SoapHttpDynamicInfosetProcessor customizedProcessor;
    private String location;
    private XmlNamespace expectedVersionOfWsa;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestWsaOperations.class));
    }
    
    public TestWsaOperations(String name) {
        super(name);
    }
    
    protected void tearDown() {
        customizedProcessor.stop();
        customizedProcessor.shutdown();
    }
    
    public void testRefProperties2005() throws Exception {
        testRefProperties(WsAddressing.NS_WSA);
    }
    
    public void testRefProperties2004() throws Exception {
        testRefProperties(WsAddressing.NS_2004_08);
    }
    
    private void testRefProperties(XmlNamespace wsa) throws Exception
    {
        XsulVersion.requireVersion("2.5.3");
        
        //setAction("http://www.ibm.com/xmlns/stdwip/web-services/WSBaseNotification/PauseSubscription");
        
        WsaEndpointReference epr = new WsaEndpointReference(wsa, new URI(location));
        //setTo s12:mustUnderstand="1">
        
        XmlElement refParams = builder.parseFragmentFromReader(
            new StringReader(EXAMPLE_REF_PROPERTY));
        epr.getReferenceParameters().addElement(refParams);
        
        XmlElement message = builder.newFragment(
            "http://www.ibm.com/xmlns/stdwip/web-services/WS-BaseNotification", "PauseSubscription");
        
        expectedVersionOfWsa = wsa;
        
        WsaInvoker invoker = new WsaInvoker();
        //invoker.setTargetWsa(wsa);
        invoker.setTargetEPR(epr);
        invoker.setDefaultAction(new URI("http://www.ibm.com/Action"));
        assertNotNull(invoker.getDefaultAction());
        invoker.invokeMessage(message);
        
    }
    
    //    public void testRefPropertiesFromXml() throws Exception {
    //    }
    
    protected void setUp() throws IOException {
        
        TypeHandlerRegistry registry = CommonTypeHandlerRegistry.getInstance();
        
        customizedProcessor = new SoapHttpDynamicInfosetProcessor() {
            public XmlDocument processSoapEnvelope(XmlElement envelope, SoapUtil soapFragrance){
                // concert envelope to String
                //                System.err.println(getClass().getName()+" received envelope="
                //                                       +builder.serializeToString(envelope));
                if(logger.isFinestEnabled()) logger.finest("received envelope="+builder.serializeToString(envelope));
                // this XML string could be convertedto DOM ot whatever API one preferes (like JDOM, DOM4J, ...)
                
                //                XmlElement soapHeader = envelope.element(null, "Header");
                //                if(soapHeader == null) {
                //                    throw new XsulException("SOAP message must have headers");
                //                }
                WsaMessageInformationHeaders wsah = new WsaMessageInformationHeaders(envelope);
                //System.err.println(getClass().getName()+" message destinaiton="+wsah.getTo());
                assertEquals(location, wsah.getTo().toString());
                
                //
                XmlNamespace actionNs = wsah.getSoapHeaderElement().requiredElement(null, "Action").getNamespace();
                //System.err.println(getClass()+" actionNs="+actionNs);
                assertEquals(expectedVersionOfWsa, actionNs);
                
                //print reference properties
                //XmlElement refProp = soapHeader.requiredElement(REF_PROPERTY_NS, REF_PROPERTY_NAME);
                XmlElement refProp = wsah.getHeaderElement(REF_PROPERTY_NS, REF_PROPERTY_NAME);
                assertNotNull(refProp);
                //System.err.println(getClass().getName()+" get ref property "+refProp.requiredTextContent());
                String refPropContent = refProp.requiredTextContent();
                assertEquals(REF_PROPERTY_VALUE, refPropContent);
                
                XmlElement firstChild = soapFragrance.requiredBodyContent(envelope);
                
                XmlElement responseMessage = processMessage(firstChild);
                
                //to send back response wrapped in SOAP envelope
                // NOTE: if response is null no response - async messaging
                return soapFragrance.wrapBodyContent(responseMessage);
            }
            
            public XmlElement processMessage(XmlElement message) {
                if(logger.isFinestEnabled()) {
                    String msg = "received "+builder.serializeToString(message);
                    //System.err.println(getClass().getName()+" "+msg);
                    logger.finest(msg);
                }
                //XmlElement resp = null;
                //return resp;
                return null; //null means send no repsonse - one way
            }
        };
        //customizedProcessor.setServerPort(port);
        
        customizedProcessor.setSupportedSoapFragrances(new SoapUtil[]{
                    Soap12Util.getInstance(), Soap11Util.getInstance()});
        
        customizedProcessor.start();
        
        location = customizedProcessor.getServer().getLocation();
        //System.err.println(getClass().getName()+" started on "+location);
        if(logger.isFinestEnabled()) logger.finest("started on "+location);
    }
    
}




