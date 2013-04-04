/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsAddressing.java,v 1.14 2006/04/30 06:48:13 aslom Exp $
 */

package xsul.ws_addressing;

import java.net.URI;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;

/**
 * This package contains implementation of
 * <a href="http://www-106.ibm.com/developerworks/webservices/library/ws-add/">WS-Addressing</a>
 * and this class contains some shared constants.
 *
 * @version $Revision: 1.14 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsAddressing {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    // "final" WSA 1.0 version from http://www.w3.org/2002/ws/addr/
    // http://www.w3.org/TR/2006/PR-ws-addr-soap-20060321/
    public final static XmlNamespace NS_2005 = builder.newNamespace(
        "wsa", "http://www.w3.org/2005/08/addressing");
    public final static XmlNamespace NS_WSA = NS_2005; // final and default version of WS-Addtressing
    //public final static XmlNamespace NS_WSA_1_0 = NS_WSA;
    
    public final static XmlNamespace NS_WSDL_WSA = builder.newNamespace(
        "wsaw", "http://www.w3.org/2005/03/addressing/wsdl");
    
    public final static URI URI_ROLE_ANONYMOUS = URI.create(
        "http://www.w3.org/2005/08/addressing/anonymous");
    
    public final static URI URI_ROLE_NONE = URI.create(
        "http://www.w3.org/2005/08/addressing/none");
    
    public final static URI URI_ACTION_FAULT = URI.create(
        "http://www.w3.org/2005/08/addressing/fault"); // Web Services Addressing 1.0 fault
    
    public final static URI URI_ACTION_SOAP_FAULT = URI.create(
        "http://www.w3.org/2005/08/addressing/soap/fault"); // Web Services Addressing 1.0 fault
    
    public final static URI URI_DEFAULT_REPLY_RELATIONSHIP_TYPE = URI.create(
        "http://www.w3.org/2005/08/addressing/reply");
    
    //private final static QName RESPONSE_REPLY_RELATIONSHIP = new QName(
    //    NS_2005.getNamespaceName(), "Reply");
    
    public final static QName FAULT_INVALID_ADRESS = createQName(NS_2005, "InvalidAddress");
    public final static QName FAULT_INVALID_EPR = createQName(NS_2005, "InvalidEPR");
    public final static QName FAULT_INVALID_CARDINALITY = createQName(NS_2005, "InvalidCardinality");
    public final static QName FAULT_MISSING_ADDRESS_IN_EPR = createQName(NS_2005, "MissingAddressInEPR");
    public final static QName FAULT_DUPLICATE_MESSAGE_ID = createQName(NS_2005, "DuplicateMessageID");
    public final static QName FAULT_ACTION_MISMATCH = createQName(NS_2005, "ActionMismatch");
    public final static QName FAULT_ONLY_ANON_ADDR_SUPPORTED = createQName(NS_2005, "OnlyAnonymousAddressSupported");
    public final static QName FAULT_ONLY_NON_ANON_ADDR_SUPPORTED = createQName(NS_2005, "OnlyNonAnonymousAddressSupported");
    
    // -----------------------------------------------------------------
    // -- old and non standard stuff
    
    //SPEC: http://www.w3.org/Submission/ws-addressing/
    public final static XmlNamespace NS_2004_08 = builder.newNamespace(
        "wa48", "http://schemas.xmlsoap.org/ws/2004/08/addressing");
    public final static XmlNamespace NS_2004_03 = builder.newNamespace(
        "wa43", "http://schemas.xmlsoap.org/ws/2004/03/addressing");
    //"wsa4", "http://schemas.xmlsoap.org/ws/2004/03/addressing");
    //"wsa", "http://schemas.xmlsoap.org/ws/2003/03/addressing");
    
    public final static XmlNamespace POLICY_NS_2002_12 = builder.newNamespace(
        "wsp", "http://schemas.xmlsoap.org/ws/2002/12/policy");
    
    
    public final static URI URI_ROLE_ANONYMOUS_2004_08 = URI.create(
        "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous");
    public final static URI URI_ROLE_ANONYMOUS_2004_03 = URI.create(
        "http://schemas.xmlsoap.org/ws/2004/03/addressing/role/anonymous");
    //"http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous");
    
    public final static URI URI_ACTION_FAULT_2004 = URI.create(
        "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault");
    //URI.create(WsAddressing.FAULT_ACTION_NS.getNamespaceName());
    
    //    private final static QName RESPONSE_REPLY_RELATIONSHIP_2004_03 = new QName(
    //        NS_2004_03.getNamespaceName(), "Reply");
    private final static QName RESPONSE_REPLY_RELATIONSHIP_2004_08 = new QName(
        NS_2004_08.getNamespaceName(), "Reply");
    //NS.getNamespaceName(), "Response");
    
    public final static URI URI_UNSPECIFIED_MESSAGE_ID_2004_08 = URI.create(
        "http://schemas.xmlsoap.org/ws/2004/08/addressing/id/unspecified");
    
    
    //private static XmlNamespace defaultNs = NS_2004_08;
    private static XmlNamespace defaultNs = NS_2005;
    //private static QName defaultResponseReplyRel = RESPONSE_REPLY_RELATIONSHIP_2004_08;
    //private static QName defaultResponseReplyRel = RESPONSE_REPLY_RELATIONSHIP;
    
    private static QName createQName(XmlNamespace ns, String ncname) {
        return new QName(ns.getNamespaceName(), ncname, ns.getPrefix());
    }
    
    public static XmlNamespace getDefaultNs() {
        return defaultNs;
    }
    
    public static void setDefaultNs(XmlNamespace ns) {
        if(ns == null) throw new IllegalArgumentException();
        defaultNs = ns;
        //defaultResponseReplyRel = new QName(defaultNs.getNamespaceName(), "Reply");
    }
    
//    public static QName getDefaultResponseReplyRel() {
//        return defaultResponseReplyRel;
//    }
    
    //    static  {
    //      try {
    //          ROLE_ANONYMOUS_URI = new ;
    //      } catch (URISyntaxException e) {
    //      }
    //    }
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





