/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Provider.java,v 1.1 2006/04/18 19:16:07 aslom Exp $
 */

package xsul.wsif_xsul_soap_gsi;

import org.ietf.jgss.GSSCredential;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.invoker.gsi.GsiInvoker;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.wsdl.WsdlBinding;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlUtil;
import xsul.wsif.WSIFException;
import xsul.wsif.spi.WSIFProvider;
import xsul.wsif_xsul_soap_http.XsulSoapPort;


/**
 * XSUL Soap/HTTP provider for XWSIF
 *
 * @version $Revision: 1.1 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class Provider extends xsul.wsif_xsul_soap_http.Provider implements WSIFProvider {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private SoapHttpDynamicInfosetInvoker secureInvoker;
    
    public Provider(GSSCredential cred) throws XsulException {
        this(new GsiInvoker (cred));
    }
    
    public Provider(GsiInvoker gsiInvoker) {
        this.secureInvoker = gsiInvoker;
    }
    
    public Provider(SoapHttpDynamicInfosetInvoker secureInvoker) {
        this.secureInvoker = secureInvoker;
    }
    
    protected void checkLocation(String location, WsdlPort port) throws WSIFException {
        if(! location.startsWith("https://")) {//
            throw new WSIFException("only HTTPS is suported but got "+location+errorContext(port));
        }
    }
    
    protected XsulSoapPort newPort(WsdlPort port, XmlElement soapAddr,
                                   String location,
                                   WsdlBinding binding,
                                   XmlElement soapBinding,
                                   String bindingStyle,
                                   TypeHandlerRegistry typeMap) {
        XsulSoapPort wsifPort = super.newPort(port, soapAddr, location, binding, soapBinding, bindingStyle, typeMap);
        wsifPort.setInvoker(secureInvoker);
        return wsifPort;
    }
    
}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University.
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

