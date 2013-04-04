/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: TestEndpointReference.java,v 1.9 2006/04/30 03:22:04 aslom Exp $
 */

package xsul.ws_addressing;

import java.io.StringReader;
import java.net.URI;
import java.util.Iterator;
import javax.xml.namespace.QName;
import junit.framework.TestSuite;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;
import xsul.XsulTestCase;
import xsul.ws_addressing.WsaEndpointReference;

/**
 * Tests for handling of WSA EndpointReferences.
 *
 * @version $Revision: 1.9 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestEndpointReference extends XsulTestCase {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestEndpointReference.class));
    }
    
    public TestEndpointReference(String name) {
        super(name);
    }
    
    
    public void testEnpointReference() throws Exception
    {
        {
            WsaEndpointReference ep = new WsaEndpointReference(new URI("http://test"));
            //System.out.println(getClass()+" equals="+ep.getAddress().equals(new URI("http://test")));
        }
        {
            WsaEndpointReference sep = new WsaEndpointReference(
                builder.parseFragmentFromReader(new StringReader(SIMPLE_ENDPOINT_REF)));
            describeEp(sep);
            sep.validateData();
        }
        
        {
            WsaEndpointReference sep = new WsaEndpointReference(
                builder.parseFragmentFromReader(new StringReader(SOAP_ENDPOINT_REF)));
            describeEp(sep);
            sep.validateData();
        }
        
        {
            WsaEndpointReference sep = new WsaEndpointReference(
                builder.parseFragmentFromReader(new StringReader(SIMPLE_ENDPOINT_REF_2004)));
            describeEp(sep);
            sep.validateData();
        }
        
        {
            WsaEndpointReference fep = new WsaEndpointReference(
                builder.parseFragmentFromReader(new StringReader(FABRIKAM_ENDPOINT_REF_2004_08)));
            URI address = fep.getAddress();
            assertEquals(FABRIKAM_ADDRESS, address.toString());
            final QName portTypeQname = QName.valueOf("{"+NS_FABRIKAM.getNamespaceName()+"}"+FABRIKAM_PT);
            QName q = fep.getPortType();
            assertEquals(portTypeQname, q);
            describeEp(fep);
            fep.validateData();
        }
        
        {
            WsaEndpointReference firstEp = new WsaEndpointReference(
                builder.parseFragmentFromReader(new StringReader(SAMPLE_ENDPOINT_REF_2004_08)));
            describeEp(firstEp);
            firstEp.validateData();
        }
        
        {
            WsaEndpointReference secondEp = new WsaEndpointReference(
                builder.parseFragmentFromReader(new StringReader(SAMPLE_ENDPOINT_REF2_2004_08)));
            describeEp(secondEp);
            secondEp.validateData();
        }
    }
    
    private static void describeEp(WsaEndpointReference ep) {
        final boolean PRINT = false; //enable to print EPR descriptions (good for debugging)
        if(PRINT) System.out.println("\n"+TestEndpointReference.class.getName());
        if(PRINT) System.out.println("Endpoint Reference "+ep.getName());
        if(PRINT) System.out.println("address URI="+ep.getAddress());
        {
            XmlElement params = ep.getReferenceParameters();
            if(params != null) {
                for(Iterator i = params.requiredElementContent().iterator(); i.hasNext(); ) {
                    Object  o = i.next();
                    if(PRINT) System.out.println("reference parameter="+builder.serializeToString(o));
                }
            } else {
                if(PRINT) System.out.println("no reference parameters");
            }
        }
        {
            XmlElement meta = ep.getMetadata();
            if(meta != null) {
                for(Iterator i = meta.requiredElementContent().iterator(); i.hasNext(); ) {
                    Object  o = i.next();
                    if(PRINT) System.out.println("metadata="+builder.serializeToString(o));
                }
            } else {
                if(PRINT) System.out.println("no metadata");
            }
        }
        // deprecated -- to be deleted ...
        if(! WsAddressing.NS_WSA.equals( ep.getWsAddressingVersion() ))  {
            XmlElement props = ep.getReferenceProperties();
            if(props != null) {
                for(Iterator i = props.requiredElementContent().iterator(); i.hasNext(); ) {
                    Object  o = i.next();
                    if(PRINT) System.out.println("reference property="+builder.serializeToString(o));
                }
            } else {
                if(PRINT) System.out.println("no reference properties");
            }
            
            if(PRINT) System.out.println("selected port-type="+ep.getPortType());
            if(PRINT) System.out.println("service name="+ep.getServiceName());
            if(PRINT) System.out.println("service-port name="+ep.getServicePortName());
            {
                int pos = 1;
                for(Iterator i = ep.getPolicy().iterator(); i.hasNext(); ++pos) {
                    XmlElement policy = (XmlElement) i.next();
                    if(PRINT) System.out.println("policy["+pos+"]="+builder.serializeToString(policy));
                }
            }
        }
    }
    
    final private static String SIMPLE_ENDPOINT_REF =
        "<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n"+
        "   <wsa:Address>http://example.com/fabrikam/acct</wsa:Address>\n"+
        "</wsa:EndpointReference>";
    
    
    final private static String SOAP_ENDPOINT_REF =
        "<wsa:EndpointReference\n"+
        "     xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"\n"+
        "     xmlns:wsaw=\"http://www.w3.org/2005/03/addressing/wsdl\"\n"+
        "     xmlns:fabrikam=\"http://example.com/fabrikam\"\n"+
        "     xmlns:wsdli=\"http://www.w3.org/2006/01/wsdl-instance\"\n"+
        "     wsdli:wsdlLocation=\"http://example.com/fabrikam\n"+
        "       http://example.com/fabrikam/fabrikam.wsdl\">\n"+
        "   <wsa:Address>http://example.com/fabrikam/acct</wsa:Address>\n"+
        "   <wsa:Metadata>\n"+
        "       <wsaw:InterfaceName>fabrikam:Inventory</wsaw:InterfaceName>\n"+
        "   </wsa:Metadata>\n"+
        "   <wsa:ReferenceParameters>\n"+
        "       <fabrikam:CustomerKey>123456789</fabrikam:CustomerKey>\n"+
        "       <fabrikam:ShoppingCart>ABCDEFG</fabrikam:ShoppingCart>\n"+
        "   </wsa:ReferenceParameters>\n"+
        "</wsa:EndpointReference>";
    
    // --- deprecated -- tested only for backward compatibility
    final private static String SIMPLE_ENDPOINT_REF_2004 =
        "<SimpleReference"+
        " xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"+
        "<wsa:Address>http://www.producer.org/ProducerEndpoint</wsa:Address>"+
        "</SimpleReference>";
    
    final private static XmlNamespace NS_FABRIKAM  = builder.newNamespace(
        "http://www.fabrikam123.example");
    final private static String FABRIKAM_ADDRESS  = "http://www.fabrikam123.example/acct";
    final private static String FABRIKAM_PT = "InventoryPortType";
    
    final private static String FABRIKAM_ENDPOINT_REF_2004_08 =
        "<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2004/08/addressing\""+
        "xmlns:fabrikam=\""+NS_FABRIKAM.getNamespaceName()+"\">"+
        "   <wsa:Address>"+FABRIKAM_ADDRESS+"</wsa:Address>"+
        "   <wsa:PortType>fabrikam:"+FABRIKAM_PT+"</wsa:PortType>"+
        "</wsa:EndpointReference>";
    
    final private static String SAMPLE_ENDPOINT_REF_2004_08 =
        "<wsnt:ProducerReference"+
        " xmlns:wsa=\"http://www.w3.org/2004/08/addressing\""+
        " xmlns:wsnt=\"http://www.ibm.com/xmlns/stdwip/web-services/WS-Notification\""+
        " xmlns:npex=\"http://www.producer.org/RefProp\">"+
        "<wsa:Address>http://www.producer.org/ProducerEndpoint</wsa:Address>"+
        "<wsa:ReferenceProperties>\n"+
        "<npex:NPResourceId>uuid:84decd55-7d3f-65ad-ac44-675d9fce5d22"+
        "</npex:NPResourceId>\n"+
        "</wsa:ReferenceProperties>\n"+
        "</wsnt:ProducerReference>";
    
    final private static String SAMPLE_ENDPOINT_REF2_2004_08 =
        "<wsa:FilledEndpointReference"+
        " xmlns:wsa=\"http://www.w3.org/2004/08/addressing\""+
        " xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\""+
        " xmlns:npex=\"http://www.producer.org/RefProp\">"+
        "<wsp:Policy><wsp:TextEncoding wsp:Usage=\"wsp:Required\" Encoding=\"utf-8\"/></wsp:Policy>"+
        "<wsa:ReferenceProperties>\n"+
        "<npex:NPResourceId>uuid:84decd55-7d3f-65ad-ac44-67bbbbbbbbbb"+
        "</npex:NPResourceId>\n"+
        "<npex:NPResourceId>uuid:84decd55-7d3f-65ad-ac44-675d9faaaaaa"+
        "</npex:NPResourceId>\n"+
        "</wsa:ReferenceProperties>\n"+
        "<wsa:Address>http://www.producer.org/ProducerEndpoint</wsa:Address>"+
        "<wsa:PortType>npex:VerySpecialPortType</wsa:PortType>"+
        "<wsa:ServiceName PortName=\"httpPort\">npex:Service</wsa:ServiceName>"+
        "<wsp:Policy><wsp:SpecVersion wsp:Usage=\"wsp:Required\" URI=\"http://www.w3.org/TR/2000/NOTE-SOAP-20000508/\" /></wsp:Policy>"+
        "</wsa:FilledEndpointReference>";
    
}

