/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Client.java,v 1.3 2004/06/07 22:51:30 aslom Exp $
 */

package xsul_sample_google;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import org.xmlpull.v1.builder.XmlElement;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlResolver;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif.spi.WSIFProviderManager;

public class Client {
    
    public static void main(String[] args) throws Exception
    {

        String key = System.getProperty("key");
        if(key == null) {
            throw new RuntimeException("system property 'key' with google key is required");
        }

        //args[0] =
        URI base = ((new File(".")).toURI());
        
        String wsdlLoc = args[0];
        WsdlDefinitions def = WsdlResolver.getInstance().loadWsdl(base, new URI(wsdlLoc));
        //System.out.println("loaded def="+def);
        
        String opName = "doGoogleSearch";
        String query = "test";
        if(args.length > 1) {
            query = args[1];
        }
        System.out.println("invoking Google WS with query '"+query+"' using WSDL from "+wsdlLoc);
        
        WSIFProviderManager.getInstance().addProvider( new xsul.wsif_xsul_soap_http.Provider() );
        
        WSIFServiceFactory wsf = WSIFServiceFactory.newInstance();
        WSIFService serv = wsf.getService(def);
        
        WSIFPort port = serv.getPort();
        
        WSIFOperation op = port.createOperation(opName);
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        in.setObjectPart("key", key);
        in.setObjectPart("q", query);
        in.setObjectPart("start", "0");
        in.setObjectPart("maxResults", "10");
        in.setObjectPart("filter", "true");
        //restrict xsi:type="xsd:string"
        in.setObjectPart("safeSearch", "false");
        //lr xsi:type="xsd:string"
        in.setObjectPart("ie", "latin1");
        in.setObjectPart("oe","latin1");
        
        boolean succes = op.executeRequestResponseOperation(in, out, fault);
        if(succes) {
            //System.out.println("received response "+out);
            XmlElement response = (XmlElement) out;
            // return/resultElements
            XmlElement resultElements = response
                .requiredElement(null, "return")
                .requiredElement(null, "resultElements");
            // print URLs of all */URL/text()
            int count = 1;
            for (Iterator i = resultElements.elements(null, null).iterator(); i.hasNext(); ) {
                XmlElement item = (XmlElement) i.next();
                System.out.println((count++)+". "+item
                                       .element(null, "URL")
                                       .requiredTextContent());
            }
        } else {
            System.out.println("received fault "+fault);
        }
    }
}




