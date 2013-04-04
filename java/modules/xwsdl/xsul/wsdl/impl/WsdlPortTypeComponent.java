/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsdlPortTypeComponent.java,v 1.4 2006/04/18 18:03:48 aslom Exp $
 */

package xsul.wsdl.impl;

import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidation;
import xsul.DataValidationException;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.util.XsulUtil;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlException;
import xsul.wsdl.WsdlMessage;
import xsul.wsdl.WsdlPortTypeOperation;



/**
 * Base class for WSDL portType input/output/fault.
 *
 * @version $Revision: 1.4 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public abstract class WsdlPortTypeComponent extends XmlElementAdapter implements DataValidation
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
           
    public WsdlPortTypeComponent(XmlElement target) {
        super(target);
        validateData();
    }
    
    public WsdlDefinitions getDefinitions() {
        return getPortTypeOperation().getDefinitions();
    }
    
    public WsdlPortTypeOperation getPortTypeOperation() {
        return (WsdlPortTypeOperation) castOrWrap((XmlElement)getParent(), WsdlPortTypeOperation.class);
    }

    public QName getMessage() throws DataValidationException {
        String b = getAttributeValue(null, "message");
        if(b == null) {
            throw new DataValidationException(
                "missing message on input element in operation "+getPortTypeOperation());
        }
        return XsulUtil.toQName(this, b);
    }

    public WsdlMessage lookupMessage() {
        QName messageQName = getMessage();
        assert messageQName != null;
        WsdlDefinitions def = getDefinitions();
        if(!messageQName.getNamespaceURI().equals(def.getTargetNamespace())) {
            throw new WsdlException("currently only messages in the same WSDL file are supported");
        }
        String messageName = messageQName.getLocalPart();
        WsdlMessage message = def.getMessage(messageName);
        if(message == null) {
            throw new WsdlException("message '"+messageName+"' was not found in "+getDefinitions());
        }
        return message;
    }
    
    public void validateData() throws DataValidationException {
        getMessage();
        getDefinitions();
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





