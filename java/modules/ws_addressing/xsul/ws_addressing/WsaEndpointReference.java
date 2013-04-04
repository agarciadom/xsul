/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsaEndpointReference.java,v 1.17 2006/04/30 06:48:13 aslom Exp $
 */

package xsul.ws_addressing;

import java.net.URI;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.Iterable;
import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidation;
import xsul.DataValidationException;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.util.XsulUtil;

/* IDEA: WsaEndpointReference extends WsaEndpointReferenceReadOnly  {
 target.setReadOnly(false)
 //pass through setReadOnly
 }
 */

//<wsa:EndpointReference>
//    <wsa:Address>xs:anyURI</wsa:Address>
//    <wsa:ReferenceParameters>xs:any*</wsa:ReferenceParameters> ?
//    <wsa:Metadata>xs:any*</wsa:Metadata>?
//</wsa:EndpointReference>//</wsa:EndpointReference>

/**
 * Implementation of
 * <a href="http://www.w3.org/TR/2006/PR-ws-addr-core-20060321/#eprs">Endpoint Reference</a>
 * from Web Services Addressing 1.0 (WS-Addressing)
 *
 * @version $Revision: 1.17 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsaEndpointReference extends XmlElementAdapter implements DataValidation
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private XmlNamespace wa; // = WsAddressing.NS;
    private final static XmlNamespace wsp = WsAddressing.POLICY_NS_2002_12;
    
    public final static String NAME = "EndpointReference";
    public final static String ADDRESS_EL = "Address";
    public final static String METADATA_EL = "Metadata";
    public final static String REFERENCE_PARAMETERS_EL = "ReferenceParameters";
    
    public WsaEndpointReference(URI address) {
        this(WsAddressing.getDefaultNs(), address);
    }
    
    public WsaEndpointReference(XmlNamespace wa, URI address) {
        super(builder.newFragment(wa, NAME));
        this.wa = wa;
        setAddress(address);
        validateData();
    }
    
    public WsaEndpointReference(XmlElement target) {
        super(target);
        XmlNamespace ns = null;
        //        XmlNamespace ns = target.getNamespace();
        //        if(! (ns.equals(WsAddressing.NS_2005))  //HACKish
        //               || (ns.equals(WsAddressing.NS_2004_08))
        //               || (ns.equals(WsAddressing.NS_2004_03)) ) {
        //            ns = null; //failed
        //        }
        //        if(ns == null) {
        { // try to guess namespace based on presence of required "Address" element
            XmlElement addressEl = element(null, ADDRESS_EL);
            if(addressEl != null) {
                ns = addressEl.getNamespace();
            }
        }
        if(ns != null) {
            this.wa = ns;
        } else {
            WsAddressing.getDefaultNs();
        }
        validateData();
    }
    
    public XmlNamespace getWsAddressingVersion() {
        return wa;
    }
    
    //bloc requireChildrenModel
    public URI getAddress() {
        XmlElement el = element(wa, ADDRESS_EL);
        if(el == null) {
            return null;
        }
        String t = el.requiredTextContent().trim();
        if(t != null) {
            try {
                return new URI(t);
            } catch(Exception e) {
                throw new DataValidationException ("wsa:Address mut be of type xs:anyURI "+toString(), e);
            }
        }
        throw new DataValidationException ("required wsa:Address was not found in "+toString());
    }
    
    public void setAddress(URI uri) {
        XmlElement el = element(wa, ADDRESS_EL, true);
        el.removeAllChildren();
        el.addChild(uri.toString());
    }
    
    
    public XmlElement getReferenceParameters() {
        return element(wa, REFERENCE_PARAMETERS_EL, true);
    }
    
    public XmlElement getReferenceParametersFor(XmlNamespace ns, String name) {
        return getReferenceProperties().element(ns, name);
    }
    
    public XmlElement getMetadata() {
        return element(wa, METADATA_EL , true);
    }
    
    public XmlElement getMetadataFor(XmlNamespace ns, String name) {
        return getMetadata().element(ns, name);
    }
    
    // ------------------ OLD STUFF: to be removed soon!  ----
    
    /**
     * No longer supoprted in WSA 1.0.     *
     * @deprecated
     */
    public XmlElement getReferenceProperties() {
        return element(wa, "ReferenceProperties", true);
    }
    
    /**
     * No longer supoprted in WSA 1.0.     *
     * @deprecated
     */
    public XmlElement getReferenceProperty(XmlNamespace ns, String name) {
        return getReferenceProperties().element(ns, name);
    }
    
    //    public List/*<XmlElement>*/ getPolicy() {
    //      return elements(wsp,"Policy");
    //    }
    /**
     * No longer supported in WSA 1.0.     *
     * @deprecated
     */
    public Iterable getPolicy() {
        return elements(wsp, "Policy");
    }
    
    /**
     * No longer supported in WSA 1.0.     *
     * @deprecated
     */
    public QName getPortType() {
        XmlElement el = element(wa, "PortType");
        if(el != null) {
            return XsulUtil.getQNameContent(el);
        } else {
            return null;
        }
    }
    
    /**
     * No longer supported in WSA 1.0.     *
     * @deprecated
     */
    public QName getServiceName() {
        // <wsa:ServiceName PortName="xs:NCName"?>xs:QName</wsa:ServiceName> ?
        XmlElement el = element(wa,"ServiceName");
        if(el != null) {
            return XsulUtil.getQNameContent(el);
        } else {
            return null;
        }
    }
    
    /**
     * No longer supported in WSA 1.0.     *
     * @deprecated
     */
    public String getServicePortName() {
        // <wsa:ServiceName PortName="xs:NCName"?>xs:QName</wsa:ServiceName> ?
        XmlElement el = element(wa,"ServiceName");
        if(el != null) {
            XmlAttribute portNameAttr  = el.attribute("PortName");
            if(portNameAttr != null) {
                return XsulUtil.validateNcName(portNameAttr.getValue());
            }
        }
        return null;
    }
    
    public void validateData() throws DataValidationException {
        if(getAddress() == null) {
            throw new DataValidationException("wsa:Address is required in "+this);
        }
        //      // selected portType one and it is QName!
        //getPortType();
        // check only one PortType
        //       elements(wsa,"PortType").size() < 2
        //service port max one
        //      elements(wsa,"ServiceName").size() < 2
        //getServiceName();
        //getServicePortName();
    }
    
    public String toString() {
        return builder.serializeToString(this) ;
    }
}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (C) 2004 The Trustees of Indiana University.
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





