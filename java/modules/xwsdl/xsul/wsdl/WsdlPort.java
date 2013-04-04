/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsdlPort.java,v 1.5 2006/04/18 18:03:48 aslom Exp $
 */

package xsul.wsdl;

import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidation;
import xsul.DataValidationException;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.util.XsulUtil;

/**
 * Implementation of WSLD 1.1 services/port
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsdlPort extends XmlElementAdapter implements DataValidation
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static String NAME = "port";
    
    //private final static XmlNamespace wsa = WsAddressing.NS;
    private final static XmlNamespace wsdl = WsdlDefinitions.TYPE;
    
    public WsdlPort() {
        super(builder.newFragment(wsdl, NAME));
        validateData();
    }
    
    public WsdlPort(XmlElement target) {
        super(target);
        validateData();
    }
        
    public WsdlService getService() {
        return (WsdlService) castOrWrap((XmlElement)getParent(), WsdlService.class);
    }
    
    public WsdlDefinitions getDefinitions() {
        return getService().getDefinitions();
    }
    
    public String getPortName() {
        String b = getAttributeValue(null, "name");
        return b;
    }
    
    public QName getBinding() {
        String b = getAttributeValue(null, "binding");
        return XsulUtil.toQName(this, b);
    }
    
    public WsdlBinding lookupBinding() {
        QName qbv = getBinding();
        if(qbv == null) {
            throw new WsdlException("could not find binding "+qbv+" in "+getDefinitions());
        }
        WsdlDefinitions def = getDefinitions();
        if(!qbv.getNamespaceURI().equals(def.getTargetNamespace())) {
            throw new WsdlException("currently only bindings in the same WSDL file are supported");
        }
        String bindingName = qbv.getLocalPart();
        WsdlBinding binding = def.getBinding(bindingName);
        if(binding == null) {
            throw new WsdlException("binding "+bindingName+" was not founnd in "+getDefinitions());
        }
        
        return binding;
    }
            
    public void validateData() throws DataValidationException {
        
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




