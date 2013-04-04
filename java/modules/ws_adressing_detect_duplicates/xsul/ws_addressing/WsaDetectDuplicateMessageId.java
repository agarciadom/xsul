/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/* Copyright (c) 2006 Extreme! Lab, Indiana University. All rights reserved.
 * This software is open source. See the bottom of this file for the licence.
 * $Id: WsaDetectDuplicateMessageId.java,v 1.3 2006/04/30 06:48:14 aslom Exp $ */

package xsul.ws_addressing;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;
import xsul.XsulException;

/**
 * Detect if a message if messages woth a duplicated WS-Addressing MessageId.
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsaDetectDuplicateMessageId {
    private final static String MESSAGE_ID_EL = "MessageID";
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    // It would nice ot add some cleanup to remove ids longer than N days (to free memory on busy server)
    private Set messageIds = new HashSet();
    
    public WsaDetectDuplicateMessageId() {
    }
    
    public boolean seenMessageId(XmlElement soapEnvelopeOrHeader) throws XsulException {
        if(soapEnvelopeOrHeader == null) throw new IllegalArgumentException();
        XmlElement header = soapEnvelopeOrHeader;
        if(header.getName().equals(XmlConstants.S_ENVELOPE)) {
            header = soapEnvelopeOrHeader.element(null, XmlConstants.S_HEADER );
            if(header == null) { // no SOAP::Header --> no way to detect duplicates
                return false;
            }
        } else  if(!header.getName().equals(XmlConstants.S_HEADER )) {
            throw new IllegalArgumentException("element passed must be SOAP Envelope or Header");
        }
        XmlElement messageIdEl = header.element(null, MESSAGE_ID_EL);
        if(messageIdEl != null) { // make sure we rcognize namespace
            XmlNamespace ns = messageIdEl.getNamespace();
            if( ns.equals(WsAddressing.NS_2005) || ns.equals(WsAddressing.NS_2004_08)
                   || ns.equals(WsAddressing.NS_2004_03)  )
            {
                String id = messageIdEl.requiredTextContent();
                boolean notSeenBefore = messageIds.add(id);
                return ! notSeenBefore;
            }
        }
        return false;
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

