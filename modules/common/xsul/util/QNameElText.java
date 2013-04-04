/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: QNameElText.java,v 1.1 2004/04/16 20:20:51 aslom Exp $
 */

package xsul.util;

import java.io.IOException;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.XmlSerializable;
import xsul.XmlConstants;
import xsul.XsulException;

/**
 * Make sure that when XmlElement with QName value is serialized it has <b>currently</b> in-scope prefix.
 *
 * @version $Revision: 1.1 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class QNameElText implements XmlSerializable {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private String namespaceName;
    
    private String name;
    
    private XmlElement parent;
    
    public QNameElText(XmlElement parent,
                       XmlNamespace ns,
                       String name)
    {
        this(parent, ns.getNamespaceName(), name);
    }
    
    public QNameElText(XmlElement parent,
                       String namespaceName,
                       String name)
    {
        this.parent = parent;
        this.namespaceName = namespaceName;
        this.name = name;
    }
    
    public void serialize(XmlSerializer ser) throws IOException {
        //ser.startTag(namespace,  name);
        //XmlNamespace ns = parent.lookupNamespaceByName(namespaceName);
        String prefix = ser.getPrefix(namespaceName, false);
        //if(ns == null || ns.getPrefix() == null) {
        if(prefix == null) {
            // declare prefix if does nto exist
            //final XmlNamespace faultNs = builder.newNamespace("ns", namespaceName);
            //ns = parent.declareNamespace(faultNs);
            throw new XsulException(
                "could not serialize QName value as namespace is not declared "+namespaceName);
        }
        String qname = prefix + ':' + name;
        
        
        ser.text(qname);
        //ser.endTag(namespace,  name);
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


