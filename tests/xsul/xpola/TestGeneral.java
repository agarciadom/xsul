/**
 * TestGeneral.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: TestGeneral.java,v 1.7 2006/01/12 20:06:36 lifang Exp $
 */

package xsul.xpola;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.xpola.capman.CapabilityManager;
import xsul.xpola.util.XpolaUtil;

public class TestGeneral {
    
    final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private TestGeneral() {
    }
    
    public static void main(String[] args) throws Exception {
        
        String roster = "CN=Liang Fang, O=Indiana University;\nCN=Suresh Marru, O=OU;CN=Suresh Marru, O=IU";
        String[] students = roster.split(";[\\s]*");
        for (int i = 0; i < students.length; i++) {
            System.out.println("student " + i + ": " + students[i]);
        }
        String dn = "/C=US/O=National Center for Supercomputing Applications/ CN=Gopi Kandaswamy /CN=proxy";
        System.out.println("c14ned: "+CapabilityUtil.canonicalizeSubject(dn));
        dn = "/C=US/O=National Center for Supercomputing Applications/CN=Gopi Kandaswamy/CN=671463525";
        System.out.println("c14ned: "+CapabilityUtil.canonicalizeSubject(dn));
        
        GlobusCredential cred = CapabilityUtil.getGlobusCredential(null, null);
        System.out.println("c14ned: "+CapabilityUtil.canonicalizeSubject(cred.getIdentity()));
        testCase4();
        
        String cswsdlLoc =
            CapabilityManager.class.getResource("capman.wsdl").toString();
        
        System.out.println(cswsdlLoc);
//        String wsdlLoc = CommonConstants.class.getResource("InteropTest.wsdl").toString();
//        System.out.println(wsdlLoc);
        
        QName qname = XpolaUtil.string2QName("{http://www.extreme.indiana.edu/lead}Decoder3_Fri_Apr_01_14_18_42_EST_2005");
        System.out.println("qname: " + qname);
        //        testCase3();
    }
    
    private static void testCase4() throws Exception {
        String registryURL = "http://rainier.extreme.indiana.edu:20202/service-registry";
        String searchString = "/ogsi:entry/ogsi:memberServiceLocator/ogsi:reference/wsdl:definitions[@name=%22arps-trn_Fri_Apr_01_12_43_22_EST_2005%22%20and%20@targetNamespace=%22http://www.extreme.indiana.edu/lead%22]";
        String endpoint ="{http://www.extreme.indiana.edu/lead}Decoder_Thu_Apr_21_19_29_52_EST_2005";
        Vector ops = XpolaUtil.getEndpointOps(registryURL, XpolaUtil.string2QName(endpoint));
        for(Enumeration _enum = ops.elements(); _enum.hasMoreElements();) {
            QName qname = (QName)_enum.nextElement();
            System.out.println("qname: " + qname);
        }
    }
    
    private static void testCase3() throws Exception {
        String registryURL = "http://rainier.extreme.indiana.edu:20202/service-registry";
        String searchString = "/ogsi:entry/ogsi:memberServiceLocator/ogsi:reference/wsdl:definitions[@name=%22arps-trn_Fri_Apr_01_12_43_22_EST_2005%22%20and%20@targetNamespace=%22http://www.extreme.indiana.edu/lead%22]";
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        
        reader.setFeature("javax.wsdl.verbose", true);
        reader.setFeature("javax.wsdl.importDocuments", true);
        
        //        String wsdlloc = registryURL+"?xpath="
        //            + "/ogsi:entry/ogsi:memberServiceLocator/ogsi:reference/wsdl:definitions"
        //            + "[contains(@name,'" + "Decoder3" + "')and@targetNamespace=%22"
        //            + "http://www.extreme.indiana.edu/lead"+"%22]&ns_wsdl=http://schemas.xmlsoap.org/wsdl/&format=xml&index=0";
        //        Definition def = reader.readWSDL(null, wsdlloc);
        //        System.out.println(""+def.toString());
        Hashtable table =
            XpolaUtil.searchRegistryByServiceName(registryURL, XpolaConstants.LEADNAMESPACE, "Decoder");
        Enumeration e = table.keys();
        while(e.hasMoreElements()) {
            String str = (String)e.nextElement();
            QName qname = (QName)table.get(str);
            URI uri = XpolaUtil.qName2URI(qname);
            System.out.println("key: " + str);
            System.out.println("qname: " + XpolaUtil.uRI2QName(uri));
        }
        //        String searchString = registryURL + CapConstants.REGISTRY_XPATH_SEARCH_STRING +
        //            "Decoder3_Fri_Apr_01_14_18_42_EST_2005" +
        //            CapConstants.REGISTRY_XPATH_SEARCH_SUFFIX;
        //        "http://rainier.extreme.indiana.edu:20202/service-registry?xpath=/ogsi:entry/ogsi:memberServiceLocator/ogsi:reference/wsdl:definitions&ns_wsdl=http://schemas.xmlsoap.org/wsdl/&format=xml";
        //        Vector v = XpolaUtil.getWSDLOperations(searchString);
        //        for(Iterator i = v.iterator();i.hasNext();) {
        //            QName q = (QName)i.next();
        //            System.out.println("qname: " + q);
        //        }
    }
    
    private static void testCase2() throws URISyntaxException {
        URI uri = new URI("http://129.79.247.114:12351");
        uri = new URI("Decoder3_Fri_Apr_01_14_17_26_EST_2005:abc");
    }
    
    private static void testCase1() throws XmlBuilderException {
        String str = "<CapabilityRequest><User>my dad</User>\n<User>my mom</User><Id>3456456423</Id><Issuer>dda</Issuer><Action>cd</Action><Decision>Yes</Decision><Action>abc</Action><Decision>no</Decision></CapabilityRequest>";
        int idx = 0;
        idx = str.indexOf("<CapabilityRequest>");
        System.out.println(idx);
        if(str.startsWith("<CapabilityRequest>")) {
            String id = str.substring(str.indexOf("<Id>")+4, str.indexOf("</Id>"));
            System.out.println("id: " + id);
            String issuer = str.substring(str.indexOf("<Issuer>")+"<Issuer>".length(), str.indexOf("</Issuer>"));
            System.out.println("issuer: " + issuer);
            String substr =
                str.substring(str.indexOf("<Action>"),
                              str.lastIndexOf("</Decision>")+"</Decision>".length());
            //            String[] actions = substr.split("<Decision>[a-zA-Z]*</Decision>\\s*");
            String[] actions = substr.split("</Decision>\\s*");
            for (int i = 0; i < actions.length; i++) {
                System.out.println(actions[i]);
                actions[i] = actions[i].substring(
                    actions[i].indexOf("<Action>") + "<Action>".length(),
                    actions[i].indexOf("</Action>"));
                System.out.println(actions[i]);
            }
            
            substr =
                str.substring(str.indexOf("<User>"),
                              str.lastIndexOf("</User>")+"</User>".length());
            String[] users = substr.split("</User>\\s*");
            for (int i = 0; i < users.length; i++) {
                users[i] = users[i].substring("<User>".length());
                System.out.println(users[i]);
            }
        }
        
        XmlElement elem =
            builder.parseFragmentFromReader(new StringReader(str));
        XmlElement idelem = elem.element(null, "Id");
        System.out.println(idelem.toString());
    }
}

