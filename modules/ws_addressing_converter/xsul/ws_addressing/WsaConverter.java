/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/* Copyright (c) 2006 Extreme! Lab, Indiana University. All rights reserved.
 * This software is open source. See the bottom of this file for the licence.
 * $Id: WsaConverter.java,v 1.6 2006/04/30 06:48:14 aslom Exp $ */

package xsul.ws_addressing;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;
import xsul.XsulException;

/**
 * Convert between version of WS-Addressing.
 *
 * @version $Revision: 1.6 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsaConverter extends NamespaceConverter {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static XmlNamespace CONVERTER_EL_NS = builder.newNamespace(
        "xwsa", "http://www.extreme.indiana.edu/xgws/xsul/2006/");
    public final static String CONVERTER_EL_NAME = "message-wsa-namespace";
    public final static String CONVERTER_PREFIX_ATTR = "prefix";
    private final static String[] recognizedWsaNamespaces = new String[] {
        WsAddressing.NS_2005.getNamespaceName(),
            WsAddressing.NS_2004_08.getNamespaceName(),
            WsAddressing.NS_2004_03.getNamespaceName(),
    };
    
    private final static String[] wsaAnonAddr = new String[] {
        WsAddressing.URI_ROLE_ANONYMOUS.toString(),
            WsAddressing.URI_ROLE_ANONYMOUS_2004_08.toString(),
            WsAddressing.NS_2004_03.getNamespaceName(),
    };
    
    private int posOfRecognizedNamespace = -1; // important to get initialized correctly by recognizeNamespace()
    
    protected WsaConverter(String[] sourceNamespaces, XmlNamespace targetNamespace) {
        super(sourceNamespaces, targetNamespace);
        // make sure targetNs is supported
        posOfRecognizedNamespace = recognizeNamespace(targetNamespace);
        if(posOfRecognizedNamespace == -1) {
            throw new IllegalArgumentException("unsupported WSA namespace "+targetNamespace.getNamespaceName());
        }
    }
    
    protected void convertElementWithoutChildren(XmlElement el) {
        int lookupIndex = recognizeNamespace(el.getNamespace());
        if(lookupIndex != -1) { // may need conversion
            String name = el.getName();
            if(WsaMessageInformationHeaders.TO_EL.equals(name)
                   || WsaEndpointReference.ADDRESS_EL.equals(name))
            {
                // now we just need to check for special URI and replace it
                String s = el.requiredTextContent().trim();
                String a = wsaAnonAddr[ lookupIndex ];
                if(s.equals(a)) {
                    el.removeAllChildren();
                    el.addChild( wsaAnonAddr[ posOfRecognizedNamespace ]);
                }
            }
        }
        super.convertElementWithoutChildren(el);
    }
    
    private int recognizeNamespace(XmlNamespace n) {
        String ns = n.getNamespaceName();
        int index = 0;
        for (;index < recognizedWsaNamespaces.length; index++) {
            if(posOfRecognizedNamespace != index && ns.equals(recognizedWsaNamespaces[index])) {
                return index;
            }
        }
        return -1;
    }
    
    public static XmlNamespace convert(XmlElement soapEnvelopeOrHeader, XmlNamespace targetNamespace) {
        WsaConverter converter = new WsaConverter(recognizedWsaNamespaces, targetNamespace);
        //XmlElement headers = (XmlElement) soapEnv.requiredElementContent().iterator().next();
        if(soapEnvelopeOrHeader == null) throw new IllegalArgumentException();
        XmlElement header = soapEnvelopeOrHeader;
        if(soapEnvelopeOrHeader.getName().equals(XmlConstants.S_ENVELOPE)) {
            header = soapEnvelopeOrHeader.element(null, XmlConstants.S_HEADER );
            if(header == null) { // no SOAP::Header --> nothing to do ...
                return null;
            }
            // convert namespace declarations in the envelope
            converter.convertNamespaceDeclarations(soapEnvelopeOrHeader);
        } else if(!header.getName().equals(XmlConstants.S_HEADER )) {
            throw new IllegalArgumentException("element passed must be SOAP Envelope or Header");
        }
        XmlNamespace origWsaNamespace = converter.convertElement(header);
        return origWsaNamespace;
    }
    
    public static XmlNamespace getSavedWsaNamespaceFromContext(XmlElement context) {
        if(context == null) {
            return null;
        }
        XmlElement el = context.element(CONVERTER_EL_NS, CONVERTER_EL_NAME);
        if(el == null) {
            return null;
        }
        String prefix = el.getAttributeValue(null, CONVERTER_PREFIX_ATTR);
        if(prefix == null) {
            throw new XsulException("prefix is required in context element");
        }
        String ns = el.requiredTextContent();
        XmlNamespace n = builder.newNamespace(prefix, ns);
        return n;
    }
    
    public static void setSavedWsaNamespaceInContext(XmlElement context, XmlNamespace targetNs) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        XmlElement el = context.element(CONVERTER_EL_NS, CONVERTER_EL_NAME, true);
        String prefix = targetNs.getPrefix();
        if(prefix == null) {
            throw new IllegalArgumentException();
        }
        el.addAttribute(CONVERTER_PREFIX_ATTR, prefix);
        el.removeAllChildren();
        el.addChild(targetNs.getNamespaceName());
    }
    
    public static XmlNamespace convert(XmlElement soapEnvelopeOrHeader) {
        return convert(soapEnvelopeOrHeader, WsAddressing.NS_WSA);
    }
    
}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (C) 2006 The Trustees of Indiana University.
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

