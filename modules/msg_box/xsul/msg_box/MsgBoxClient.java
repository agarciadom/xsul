/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: MsgBoxClient.java,v 1.11 2006/05/01 22:08:33 aslom Exp $
 */
package xsul.msg_box;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.soap.SoapUtil;
import xsul.soap12_util.Soap12Util;
import xsul.util.XsulUtil;
import xsul.ws_addressing.WsaEndpointReference;
import xsul.ws_addressing.WsaInvoker;

/**
 * Starts message box service.
 *
 */
public class MsgBoxClient {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static SoapUtil soapFragrance = Soap12Util.getInstance(); //make it configurable?
    
    private WsaInvoker invoker = new WsaInvoker();

    private WsaEndpointReference msgBoxServiceAddr;
    
    private MsgBoxClient(WsaEndpointReference msgBoxServiceAddr) {
        this.msgBoxServiceAddr = msgBoxServiceAddr;
        invoker.setUseHttpKeepAlive(false);
    }
    
    public MsgBoxClient(URI location) {
        this(new WsaEndpointReference(location));
    }

    public MsgBoxClient(XmlNamespace wsaNs, URI location) {
        this(new WsaEndpointReference(wsaNs, location));
    }
    
    public WsaEndpointReference createMsgBox() throws XsulException {
        invoker.setDefaultAction(MsgBoxConstants.ACTION_CREATE_BOX);
        XmlElement message = builder.newFragment(MsgBoxConstants.MSG_BOX_NS,
                                                 MsgBoxConstants.OP_CREATE_BOX);
        //XmlDocument soap = soapFragrance.wrapBodyContent(message);
        invoker.setTargetEPR(msgBoxServiceAddr);
        XmlElement response = invoker.invokeMessage(message);
        if(response == null) {
            throw new XsulException("could not create message box at "+msgBoxServiceAddr+
                                        " invoked "+XsulUtil.safeXmlToString(message));
        }
        XmlElement el = response.requiredElement(MsgBoxConstants.MSG_BOX_NS,
                                                 MsgBoxConstants.EL_BOX_ADDR);
        WsaEndpointReference epr = new WsaEndpointReference(el);
        return epr;
    }
    
    public void destroyMsgBox(WsaEndpointReference msgBoxId) throws XsulException {
        invoker.setDefaultAction(MsgBoxConstants.ACTION_DESTROY_BOX);
        XmlElement message = builder.newFragment(MsgBoxConstants.MSG_BOX_NS,
                                                 MsgBoxConstants.OP_DESTROY_BOX);
        message.addChild(msgBoxId);
        msgBoxId.setNamespace(MsgBoxConstants.MSG_BOX_NS);
        msgBoxId.setName(MsgBoxConstants.EL_BOX_ADDR);
        //XmlDocument soap = soapFragrance.wrapBodyContent(message);
        invoker.setTargetEPR(msgBoxServiceAddr);
        XmlElement response = invoker.invokeMessage(message);
        if(response == null) {
            throw new XsulException("no response received");
        }
    }
    
    public XmlElement[] takeMessages(WsaEndpointReference msgBoxId
                                         //int maxToTake,
                                         //long waitInMillisecons
                                    )
        throws XsulException
    {
        invoker.setDefaultAction(MsgBoxConstants.ACTION_TAKE_MSG);
        msgBoxId.setNamespace(MsgBoxConstants.MSG_BOX_NS);
        msgBoxId.setName(MsgBoxConstants.EL_BOX_ADDR);
        //XmlDocument soap = soapFragrance.wrapBodyContent(message);
        invoker.setTargetEPR(msgBoxServiceAddr);
        XmlElement request = builder.newFragment(MsgBoxConstants.MSG_BOX_NS,
                                                 MsgBoxConstants.OP_TAKE_MSG);
        request.addChild(msgBoxId);
        XmlElement response = invoker.invokeMessage(request);
        // every child will be string with message
        List list = new ArrayList();
        for(Iterator i = response.elements(
                MsgBoxConstants.MSG_BOX_NS, MsgBoxConstants.EL_MSG_STR).iterator()
            ; i.hasNext();)
        {
            XmlElement container = (XmlElement) i.next();
            String s = container.requiredTextContent();
            XmlElement msg = builder.parseFragmentFromReader(new StringReader( s ));
            list.add(msg);
            
        }
        XmlElement[] arr = (XmlElement[]) list.toArray(new XmlElement[]{});
        return arr;
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



