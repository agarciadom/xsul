/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsaUtil.java,v 1.5 2006/04/30 06:48:13 aslom Exp $
 */

package xsul.ws_addressing;

import java.net.URI;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.DataValidationException;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.soap.SoapUtil;
import xsul.util.QNameElText;

/**
 * Handy util methods right now helping to generate WSA comliant faults.
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsaUtil {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    
    public static boolean isAsyncReplyRequired(XmlElement soapEnvelopeOrHeader)
    {
        boolean sendAsyncResp = false;
        XmlElement soapEnv;
        if(soapEnvelopeOrHeader.getName().equals(XmlConstants.S_ENVELOPE)) {
            soapEnv = soapEnvelopeOrHeader;
        } else if(soapEnvelopeOrHeader.getName().equals("Body")) {
            soapEnv = (XmlElement) soapEnvelopeOrHeader.getParent();
        } else {
            XmlElement soapBody = (XmlElement) soapEnvelopeOrHeader.getParent();
            soapEnv = (XmlElement) soapBody.getParent();
        }
        WsaMessageInformationHeaders wsah = new WsaMessageInformationHeaders(soapEnv);
        if(wsah.getReplyTo() != null) {
            sendAsyncResp = !wsah.getReplyTo().equals(WsAddressing.URI_ROLE_ANONYMOUS);
        }
        if(!sendAsyncResp && wsah.getFaultTo() != null) {
            sendAsyncResp = !wsah.getFaultTo().equals(WsAddressing.URI_ROLE_ANONYMOUS);
        }
        return sendAsyncResp;
    }
    
    
    //    public static XmlElement generateSenderWsaFault(SoapUtil soapVersion,
    //                                              String subCode,
    //                                              String reasonTextEnglish,
    //                                              XmlElement detail)
    //        throws XsulException
    //    {
    //        return null;
    //    }
    
    
    /**
     * <a href="http://www.w3.org/Submission/2004/SUBM-ws-addressing-20040810/#_Toc77464329">
     * 4.1 Invalid Message Information Header</a>
     */
    public static XmlElement faultInvalidMessageInformationHeader(SoapUtil soapVersion,
                                                                  XmlElement invaildHeader)
        throws XsulException
    {
        String reason =
            "A message information header is not valid and the message cannot be processed. "+
            "The validity failure can be either structural or semantic, e.g. "+
            "a [destination] that is not a URI or a [relationship] to a [message id] that was never issued.";
        XmlElement fault = soapVersion.generateSoapClientFault(reason, null);
        XmlElement detail = findOrAddDetailIfAllowed(fault);
        if(detail != null) {
            detail.addChild(invaildHeader);
        }
        return fault;
    }
    
    /**
     * <a href="http://www.w3.org/Submission/2004/SUBM-ws-addressing-20040810/#_Toc77464330">
     * 4.2 Message Information Header Required</a>
     */
    public static XmlElement faultMessageInformationHeaderRequired(SoapUtil soapVersion,
                                                                   QName missingHeaderQname)
        throws XsulException
    {
        String reason = "A required message information header, To, MessageID, or Action, is not present.";
        XmlElement fault = soapVersion.generateSoapClientFault(reason, null);
        XmlElement detail = findOrAddDetailIfAllowed(fault);
        if(detail != null) {
            String ns = missingHeaderQname.getNamespaceURI();
            String name = missingHeaderQname.getLocalPart();
            detail.addChild(new QNameElText(detail, ns, name));
        }
        return fault;
    }
    
    /**
     * <a href="http://www.w3.org/Submission/2004/SUBM-ws-addressing-20040810/#_Toc77464331">
     * 4.3 Destination Unreachable</a>
     */
    public static XmlElement faultDestinationUnreachable(SoapUtil soapVersion,
                                                         QName missingHeaderQname)
        throws XsulException
    {
        String reason = "No route can be determined to reach the destination role defined by the WS-Addressing To.";
        XmlElement fault = soapVersion.generateSoapClientFault(reason, null);
        return fault;
    }
    
    /**
     * <a href="http://www.w3.org/Submission/2004/SUBM-ws-addressing-20040810/#_Toc77464331">
     * 4.4 Action Not Supported</a>
     */
    public static XmlElement faultDestinationUnreachable(SoapUtil soapVersion,
                                                         URI wsaAction)
        throws XsulException
    {
        String reason = "The "+wsaAction.toString()+" cannot be processed at the receiver.";
        XmlElement fault = soapVersion.generateSoapClientFault(reason, null);
        XmlElement detail = findOrAddDetailIfAllowed(fault);
        if(detail != null) {
            detail.addChild(wsaAction.toString());
        }
        return fault;
    }
    
    /**
     * <a href="http://www.w3.org/Submission/2004/SUBM-ws-addressing-20040810/#_Toc77464330
     * 4.5 Endpoint Unavailable</a>
     */
    public static XmlElement faultDestinationUnreachable(SoapUtil soapVersion,
                                                         Long minimumDurationInMillisecondsToWait)
        throws XsulException
    {
        String reason = "The endpoint is unable to process the message at this time.";
        XmlElement fault = soapVersion.generateSoapServerFault(reason, null);
        if(minimumDurationInMillisecondsToWait != null) {
            long value = minimumDurationInMillisecondsToWait.longValue();
            if(value < 0) {
                throw new IllegalArgumentException(
                    "minimumDurationInMillisecondsToWait must be non negative but got "+value);
            }
            XmlElement detail = findOrAddDetailIfAllowed(fault);
            if(detail != null) {
                XmlElement retryEl = detail.addElement(WsAddressing.getDefaultNs(),  "RetryAfter" );
                retryEl.addChild(minimumDurationInMillisecondsToWait.toString());
            }
        }
        return fault;
    }
    
    /**
     * <a href="http://www.w3.org/Submission/2004/SUBM-ws-addressing-20040810/#_Toc77464330">
     * WSA 4. Faults</a>
     */
    private static XmlElement findOrAddDetailIfAllowed(XmlElement fault) {
        XmlNamespace ns = fault.getNamespace();
        if(ns.equals(XmlConstants.SOAP11_NS)) {
            // WSA binding for SOAP 1.1 does nto allow
            return null;
        }
        if(!ns.equals(XmlConstants.SOAP12_NS)) {
            throw new IllegalArgumentException("unrecognized verion of SOAP "+ns.getNamespaceName());
        }
        XmlElement detail = fault.element(ns, "Detail", true); // create if does nto exsit
        return detail;
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


