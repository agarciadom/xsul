/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulDynamicInvoker.java,v 1.5 2006/05/24 06:50:10 aslom Exp $
 */

package xsul.dii;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.wsdl.WsdlBindingOperation;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlMessage;
import xsul.wsdl.WsdlPortTypeOperation;
import xsul.wsdl.WsdlResolver;
import xsul.wsif.WSIFException;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.wsif.spi.WSIFProviderManager;

public class XsulDynamicInvoker {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private static void usage(String errMsg) {
        System.err.println("Usage: {WSDL URL} {operation name} [parameters ...]");
    }
    
    public static void main(String[] args) throws Exception
    {
        System.err.println("Starting "+XsulDynamicInvoker.class.getName());
        WSIFProviderManager.getInstance().addProvider( new xsul.wsif_xsul_soap_http.Provider() );
        
        if(args.length > 0 && args[0].equals("selftest")) {
            String GOOGLE_KEY = "google.key";
            String googleKey = System.getProperty(GOOGLE_KEY);  //
            if(googleKey != null) {
                runClient(new String[]{
                            "http://api.google.com/GoogleSearch.wsdl",
                                "doGoogleSearch", googleKey, "alek",
                                "0", "10", "false", "", "false", "", "latin1", "latin1"});
            } else {
                System.err.println("Warning: pass your your google api key as -D"+GOOGLE_KEY+"=XXXXXX");
            }
            
            //2005-01-17 error 200 OK body="" Content-Type: text/html
            //runClient(new String[]{
            //            "http://www.ghettodriveby.com/soap/?wsdl", "getRandomGoogleSearch", "aleksander slominksi"});
            
            
            runClient(new String[]{
                        "http://www.xmethods.net/sd/2001/BabelFishService.wsdl", "BabelFish",
                            "en_fr", "I'm going to the beach."});
            runClient(new String[]{
                        "http://www.webservicex.net/ValidateEmail.asmx?wsdl", "IsValidEMail", "aleksander@example.com"});
            
            runClient(new String[]{
                        "http://www.xmethods.net/sd/2001/TemperatureService.wsdl", "getTemp", "02067"});
            // java.io.IOException: Server returned HTTP response code: 502 for URL: http://services.xmethods.net/soap/urn:xmethods-delayed-quotes.wsdl
            //          runClient(new String[]{
            //                        "http://services.xmethods.net/soap/urn:xmethods-delayed-quotes.wsdl",
            //                            "getQuote", "IBM"});
            runClient(new String[]{
                        "http://mssoapinterop.org/asmx/xsd/round4XSD.wsdl", "echoString", "Hello World!!!"});
            runClient(new String[]{
                        "http://samples.gotdotnet.com/quickstart/aspplus/samples/services/MathService/VB/MathService.asmx?WSDL",
                            "Add", "3", "4"});
            runClient(new String[]{  //sometimes timeout
                        "http://www.webservicex.net/stockquote.asmx?WSDL", "GetQuote", "IBM"});
            //            runClient(new String[]{  //FAILING - cant find WSDL
            //                        "http://developerdays.com/cgi-bin/tempconverter.exe/wsdl/ITempConverter", "CtoF", "32"});
            runClient(new String[]{
                        "http://www.webservicex.net/icd10.asmx?WSDL", "GetICD10", "foo"});
            //simple http://www.ghettodriveby.com/soap/?wsdl
            //wsdl:import test http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils.wsdl
            runClient(new String[]{
                        "http://ws.netviagens.com/webservices/AirFares.asmx?wsdl", "GetFares",
                            "", "JFK", "WAW", "2005-01-20", "2005-02-02" });
        } else {
            runClient(args);
        }
    }
    
    private static void runClient(String[] args) throws Exception {
        //args[0] =
        URI base = ((new File(".")).toURI());
        
        if(args.length < 2) {
            usage("at least two argument required");
        }
        
        String wsdlLoc = args[0];
        //System.out.println("loaded def="+def);
        
        String opName = args[1];
        String portName = null;
        if(opName.charAt(0) == '{') {
            int pos = opName.indexOf('}');
            portName = opName.substring(1, pos);
            opName = opName.substring(pos+1);
        }
        
        //System.out.println("invoking Google WS with query '"+query+"' using WSDL from "+wsdlLoc);
        System.err.println("invoking operation '"+opName+"' using WSDL from "+wsdlLoc);
        
        //MLogger.setCmdNames(":ALL");
        WsdlDefinitions def = WsdlResolver.getInstance().loadWsdl(base, new URI(wsdlLoc));
        
        WSIFServiceFactory wsf = WSIFServiceFactory.newInstance();
        WSIFService serv = wsf.getService(def);
        
        WSIFPort port = serv.getPort(portName);
        
        WSIFOperation op = port.createOperation(opName);
        WSIFMessage in = op.createInputMessage();
        
        
        WsdlBindingOperation bindingOp =  op.getBindingOperation();
        WsdlPortTypeOperation portTypeOp = bindingOp.lookupOperation();
        WsdlMessage inputMsg = portTypeOp.getInput().lookupMessage();
        Iterator partNames = in.partNames().iterator();
        
        //WSDL URL = - ==> read from stdin
        //allow arguments:
        //1. [position=]value only allowed as first arguments if position not specified (this is POSITIONAL!)
        //    position 1...
        //2. name=value
        //4. [name]=<xml>value
        //
        
        //-Depr=- ==> read from stdin
        //-Dselect="XPATH filter"
        
        //process arguments
        // deal with stupid element= wrapping and other stuff (special case for one part hat is element) ...
        
        
        //        for (int i = 2; i < args.length; i++) {
        //            //determine type of argument
        //            String partName = (String) partNames.next(); //partNames.get(i-2);
        //            String partValue = args[i];
        //            System.err.println(partName+"="+partValue);
        //            in.setObjectPart(partName, partValue);
        //        }
        
        int count = 2;
        while(partNames.hasNext()) {
            //determine type of argument
            String partName = (String) partNames.next(); //partNames.get(i-2);
            if(count < args.length) {
                String partValue = args[count];
                System.err.println(partName+"="+partValue);
                in.setObjectPart(partName, partValue);
            } else {
                // will try to do some defaulting
                QName type = ((WSIFMessageElement)in).getPartType(partName);
                
                String localPart = type.getLocalPart();
                if("string".equals(localPart)) {
                    in.setObjectPart(partName, "foo string");
                    System.err.println("defaulting "+partName+"="+in.getObjectPart(partName));
                } else if("anyURI".equals(localPart)) {
                    in.setObjectPart(partName, "gsiftp://rainier//tmp/foo.txt");
                    System.err.println("defaulting "+partName+"="+in.getObjectPart(partName));
                } else if("URIArrayType".equals(localPart)) {
                    XmlElement arrayEl = builder.newFragment(partName);
                    arrayEl.addElement("value").addChild("gsiftp://rainier//tmp/foo.txt");
                     in.setObjectPart(partName, arrayEl);
                    System.err.println("defaulting "+partName+"="+in.getObjectPart(partName));
                }
            }
            
            ++count;
        }
        
        
        //        in.setObjectPart("key", key);
        //        in.setObjectPart("q", query);
        //        in.setObjectPart("start", "0");
        //        in.setObjectPart("maxResults", "10");
        //        in.setObjectPart("filter", "true");
        //        //restrict xsi:type="xsd:string"
        //        in.setObjectPart("safeSearch", "false");
        //        //lr xsi:type="xsd:string"
        //        in.setObjectPart("ie", "latin1");
        //        in.setObjectPart("oe","latin1");
        if(op.isRequestResponseOperation()) {
            WSIFMessage out = op.createOutputMessage();
            WSIFMessage fault = op.createFaultMessage();
            boolean succes = op.executeRequestResponseOperation(in, out, fault);
            if(succes) {
                System.err.println("received response "+out);
            } else {
                System.err.println("received fault "+fault);
            }
        } else {
            op.executeInputOnlyOperation(in);
            System.out.println("input only message was sent successfully");
        }
    }
    
    private static void addListOfElementNames(XmlElement sequenceEl, List partNames) {
        for(Iterator ei = sequenceEl.requiredElementContent().iterator(); ei.hasNext(); ) {
            XmlElement xsdSomething = (XmlElement) ei.next();
            if(! xsdSomething.getName().equals("element")) {
                throw new WSIFException("only xsd:element is supported in xsd:sequence");
            }
            String name = xsdSomething.getAttributeValue(null, "name");
            logger.fine("adding sequence element name="+name);
            partNames.add(name);
        }
        
    }
    
    
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2005 The Trustees of Indiana University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1) All redistributions of source code must retain the above
 *    copyright notice, the list of authors in the original source
 *    code, this list of conditions and the disclaimer listed in this
 *    license;
 *
 * 2) All redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the disclaimer
 *    listed in this license in the documentation and/or other
 *    materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include
 *    the following acknowledgement:
 *
 *      "This product includes software developed by the Indiana
 *      University Extreme! Lab.  For further information please visit
 *      http://www.extreme.indiana.edu/"
 *
 *    Alternatively, this acknowledgment may appear in the software
 *    itself, and wherever such third-party acknowledgments normally
 *    appear.
 *
 * 4) The name "Indiana University" or "Indiana University
 *    Extreme! Lab" shall not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission from Indiana University.  For written permission,
 *    please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana
 *    University" name nor may "Indiana University" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code
 * provided does not infringe the patent or any other intellectual
 * property rights of any other entity.  Indiana University disclaims any
 * liability to any recipient for claims brought by any other entity
 * based on infringement of intellectual property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH
 * NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA
 * UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT
 * SOFTWARE IS FREE OF INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR
 * OTHER PROPRIETARY RIGHTS.  INDIANA UNIVERSITY MAKES NO WARRANTIES THAT
 * SOFTWARE IS FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP
 * DOORS", "WORMS", OR OTHER HARMFUL CODE.  LICENSEE ASSUMES THE ENTIRE
 * RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS,
 * AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION GENERATED USING
 * SOFTWARE.
 */


