/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WSIFRuntime.java,v 1.10 2006/08/29 18:25:58 aslom Exp $
 */
package xsul.xwsif_runtime;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlException;
import xsul.wsdl.WsdlResolver;
import xsul.wsdl.WsdlUtil;
import xsul.wsif.WSIFException;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif.spi.WSIFProviderManager;

/**
 * This class encapsulates WSIF functionality to make *really* easy to invoke service described in WSDL.
 */
public class WSIFRuntime {
    private final static WSIFServiceFactory defaultWsifServiceFactory;
    
    static {
        WSIFProviderManager.getInstance().addProvider( new xsul.wsif_xsul_soap_http.Provider() );
        defaultWsifServiceFactory = WSIFServiceFactory.newInstance();
    }
    
    private static WSIFRuntime instance = new WSIFRuntime();
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    //    private String portName;
    //    private TypeHandlerRegistry typeRegistry = XsdTypeHandlerRegistry.getInstance();
    //    private List globalHandlers = new ArrayList();
    
    public WSIFRuntime() {
    }
    
    public static WSIFRuntime getDefault() {
        return instance;
    }
    
    // convenience methodb
    public static WSIFClient newClient(String wsdlLoc) throws WSIFException {
        return getDefault().newClientFor(wsdlLoc, null);
    }
    
    // svcloc is a known service location to replace the one in wsdl doc
    public static WSIFClient newClient(String wsdlLoc,
                                       String svcloc)
        throws WSIFException {
        WsdlDefinitions def = getWsdlWithModifiedServiceLocation(wsdlLoc, svcloc);
        return getDefault().newClientFor(def, null);
    }

    protected static WsdlDefinitions getWsdlWithModifiedServiceLocation(String wsdlLoc, String svcloc)
        throws WsdlException, WSIFException
    {
        //throw new IllegalArgumentException();
        URI base = ((new File(".")).toURI());
        URI wsdlLoclUri;
        try {
            wsdlLoclUri = new URI(wsdlLoc);
        } catch (URISyntaxException e) {
            throw new WSIFException(
                "location of WSDL must be correct URI, could not parse '"
                    +wsdlLoc+"'", e);
        }
        WsdlDefinitions def =
            WsdlResolver.getInstance().loadWsdl(base, wsdlLoclUri);
        if(svcloc == null) {
            return def;
        }
        XmlElement svcElem = def.element(WsdlDefinitions.TYPE, "service", true);
        if(svcElem == null) {
            throw new WSIFException("could not find service in "+wsdlLoc);
        }
        XmlElement portElem =
            svcElem.element(WsdlDefinitions.TYPE, "port", true);
        if(portElem == null) {
            throw new WSIFException("could not find port in "+wsdlLoc);
        }
        XmlElement addrElem =
            portElem.element(WsdlUtil.WSDL_SOAP_NS, "address");
        if(addrElem == null) {
            addrElem = portElem.element(WsdlUtil.WSDL_SOAP12_NS, "address");
            if(addrElem == null) {
                throw new WSIFException("could not find address in "+wsdlLoc);
            }
        }
        String LOCATION = "location";
        XmlAttribute locationAttr = addrElem.attribute(null, LOCATION);
        if(locationAttr != null) {
            addrElem.removeAttribute(locationAttr);
        }
        addrElem.addAttribute(LOCATION, svcloc);
        return def;
    }
    
    public WSIFClient newClientFor(String wsdlLoc, String portName) throws WSIFException {
        URI base = ((new File(".")).toURI());
        URI wsdlLoclUri;
        try {
            wsdlLoclUri = new URI(wsdlLoc);
        } catch (URISyntaxException e) {
            throw new WSIFException(
                "location of WSDL must be correct URI, could not parse '"+wsdlLoc+"'", e);
        }
        WsdlDefinitions def = WsdlResolver.getInstance().loadWsdl(base, wsdlLoclUri);
        return newClientFor(def, portName);
    }
    
    public WSIFClient newClientFor(WsdlDefinitions def, String portName) throws WSIFException {
        WSIFService serv = defaultWsifServiceFactory.getService(def);
        WSIFPort port = serv.getPort(portName);
        return newClientFor(port);
    }
    
    public WSIFClient newClientFor(WSIFPort port) throws WSIFException {
        return new WSIFClient(port);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 *
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 *
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 *
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 *
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */

