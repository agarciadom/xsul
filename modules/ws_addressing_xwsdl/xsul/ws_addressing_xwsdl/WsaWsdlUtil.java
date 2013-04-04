/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsaWsdlUtil.java,v 1.6 2005/12/06 00:56:35 aslom Exp $
 */

package xsul.ws_addressing_xwsdl;

import java.util.Iterator;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.DataValidationException;
import xsul.XmlConstants;
import xsul.ws_addressing.WsAddressing;
import xsul.wsdl.WsdlBinding;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlMessage;
import xsul.wsdl.WsdlMessagePart;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlPortType;
import xsul.wsdl.WsdlPortTypeInput;
import xsul.wsdl.WsdlPortTypeOperation;
import xsul.wsdl.WsdlPortTypeOutput;

/**
 * Handy util methods to deal with WS-Addressing and WSDL 1.1
 *
 * @version $Revision: 1.6 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsaWsdlUtil {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public static String determineOutputWsaAction(WsdlPort wsdlPort, XmlElement incomingMsg) {
        //TODO: wsa:action should be read from WSDL
        WsdlBinding binding = wsdlPort.lookupBinding();
        WsdlPortType portType = binding.lookupPortType();
        XmlNamespace ns = incomingMsg.getNamespace();
        String name = incomingMsg.getName();
        QName inpQName = new QName(ns.getNamespaceName(), name);
        return determineOutputWsaAction(portType, inpQName);
    }
    
    public static String determineOutputWsaAction(WsdlPortType portType, QName inpQName) {
        String wsaAction = null;
        // find which operaiton took (ns, name) as input message
        WsdlPortTypeInput foundInput = findInputTakingQName(portType, inpQName);
        if(foundInput != null) {
            //if wsa:Action not present in output then use it
            WsdlPortTypeOperation op = foundInput.getPortTypeOperation();
            wsaAction = determineOutputWsaAction(op);
        }
        if(wsaAction == null) {
            wsaAction = "urn:async:response"; //action is required - so put "some" action ...
        }
        return wsaAction;
    }
    
    public static String determineOutputWsaAction(WsdlPortTypeOperation op) throws DataValidationException
    {
        String wsaAction =  null;
        WsdlPortTypeInput foundInput = op.getInput();
        WsdlPortTypeOutput outp = op.getOutput();
        wsaAction = outp.getAttributeValue(WsAddressing.getDefaultNs().getNamespaceName(), "Action");
        if(wsaAction == null) {
            WsdlPortType pt = foundInput.getPortTypeOperation().getPortType();
            String opInOutName = outp.getOutputName();
            wsaAction = generateDefaultWsaAction(pt, opInOutName);
        }
        return wsaAction;
    }
    
    public static String generateDefaultWsaAction(WsdlPortType pt, String opInOutName) {
        // follow http://www.w3.org/TR/2005/WD-ws-addr-wsdl-20050215/#defactionwsdl11
        // [target namespace]/[port type name]/[input|output name]
        WsdlDefinitions defs = pt.getDefinitions();
        StringBuffer buf = new StringBuffer(256);
        buf.append(defs.getTargetNamespace());
        buf.append('/');
        buf.append(pt.getPortTypeName());
        buf.append('/');
        buf.append(opInOutName);
        return  buf.toString();
    }
    
    public static WsdlPortTypeInput findInputTakingQName(WsdlPortType portType, QName inpQName)
        throws DataValidationException
    {
        WsdlPortTypeInput foundInput = null;
        for(Iterator i = portType.getOperations().iterator(); i.hasNext() ; ) {
            WsdlPortTypeOperation op = (WsdlPortTypeOperation) i.next();
            WsdlPortTypeInput inp = op.getInput();
            WsdlMessage inpMsg = inp.lookupMessage();
            WsdlMessagePart part = (WsdlMessagePart) inpMsg.getParts().iterator().next();
            QName partEl = part.getPartElement();
            if(partEl == null) {
                if(part.getPartType() != null) {
                    throw new DataValidationException(
                        "message type is not supported use element attribute instead for message"+inpQName);
                }
            } else if(partEl.equals(inpQName)) {
                foundInput = inp;
            }
        }
        return foundInput;
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



