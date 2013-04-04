/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsaRelatesTo.java,v 1.9 2006/04/30 06:48:13 aslom Exp $
 */

package xsul.ws_addressing;

import java.net.URI;
import javax.xml.namespace.QName;
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

//TODO: need clone() operaiton!


/*
/wsa:RelatesTo

 This optional (repeating) element information item contributes
 one abstract [relationship] property value, in the form of a (URI, QName) pair.
 The [children] property of this element (which is of type xs:anyURI) conveys the [message id] of the related message

 */

/**
 * Implementation of
 * <a href="http://www-106.ibm.com/developerworks/webservices/library/ws-add/">RelatesTo</a>
 * element
 * from Web Services Addressing (WS-Addressing) used in WSA MessageHeaders
 *
 * @version $Revision: 1.9 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsaRelatesTo extends XmlElementAdapter implements DataValidation
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static String REL_TYPE_ATTR = "RelationshipType";
    public final static String TYPE_NAME = "RelatesTo";
    
    //private final static XmlNamespace wsa = WsAddressing.getDefaultNs();
    
    public WsaRelatesTo(XmlNamespace wsa, URI relationship) {
        super(builder.newFragment(wsa, TYPE_NAME));
        setRelationship(relationship);
        validateData();
    }
    
    public WsaRelatesTo(URI relationship) {
        super(builder.newFragment(WsAddressing.getDefaultNs(), TYPE_NAME));
        setRelationship(relationship);
        validateData();
    }
    
    public WsaRelatesTo(XmlElement target) {
        super(target);
        validateData();
    }
    
    //bloc requireChildrenModel
    public URI getRelationship() {
        String t = requiredTextContent();
        if(t != null) {
            try {
                return new URI(t.trim());
            } catch(Exception e) {
                throw new DataValidationException ("wsa:RelatesTo content must be of type xs:anyURI in "+toString(), e);
            }
        }
        throw new DataValidationException ("wsa:RelatesTo is required to have text content with xs:anyUri in "+toString());
    }
    
    public void setRelationship(URI uri) {
        if(uri == null) throw new IllegalArgumentException();
        removeAllChildren();
        addChild(uri.toString());
    }
    
    /**
     * Encapsulate /wsa:RelatesTo/@RelationshipType
     *
     * This optional attribute (of type xs:QName) conveys the relationship type as a QName.
     */
    public URI getRelationshipType() {
        XmlAttribute relationshipTypeAttr = attribute(REL_TYPE_ATTR);
        if(relationshipTypeAttr != null) {
            return URI.create(relationshipTypeAttr.getValue()); //Util.validateNcName(portNameAttr.getValue());
        }
        //return WsAddressing.getDefaultResponseReplyRel();
        return WsAddressing.URI_DEFAULT_REPLY_RELATIONSHIP_TYPE;
    }
    
    public void validateData() throws DataValidationException {
        if(!getName().equals(TYPE_NAME)) {
            throw new DataValidationException("WSA relationship element name must be "+TYPE_NAME+" in "+this);
        }
        if(getRelationship() == null) {
            throw new DataValidationException("WSA relationship URI in wsa:RelatesTo is required in "+this);
        }
        //      // selected portType one and it is QName!
        getRelationshipType();
    }
    
    
    public String toString() {
        return XsulUtil.safeXmlToString(this) ;
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





