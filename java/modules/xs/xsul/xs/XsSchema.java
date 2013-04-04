/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsSchema.java,v 1.1 2004/11/07 09:51:45 aslom Exp $
 */

package xsul.xs;

import java.util.Iterator;
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



/**
 * XML Schemas:Schema
 *
 * @version $Revision: 1.1 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XsSchema extends XmlElementAdapter implements DataValidation
{
    public final static XmlNamespace TYPE = XmlConstants.XS_NS;
    private final static String NAME = "schema";
    
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static String ATTR_TARGET_NAMESPACE = "targetNamespace";
    private final static String ATTR_ELEMENT_FORM_DEFAULT = "elementFormDefault";
    public final static String ENUM_ELEMENT_FORM_QUALIFIED = "qualified"; //JDK15 enum
    
    public XsSchema(String targetNamespace) {
        super(builder.newFragment(TYPE, NAME));
        setXsTargetNamespace(targetNamespace);
        validateData();
    }
    
    public XsSchema(XmlElement target) {
        super(target);
        validateData();
    }
    
    public String getXsTargetNamespace() {
        return getAttributeValue(null, ATTR_TARGET_NAMESPACE);
    }
    
    public void setXsTargetNamespace(String targetNamespace) {
        XmlAttribute att = findAttribute(null, ATTR_TARGET_NAMESPACE);
        if(att != null) {
            removeAttribute(att);
        }
        addAttribute(null, ATTR_TARGET_NAMESPACE, targetNamespace);
    }
    
    public String getXsElementFormDefault() {
        return getAttributeValue(null, ATTR_ELEMENT_FORM_DEFAULT);
    }
    
    public void setXsElementFormDefault(String elementForm) {
        XmlAttribute attr = attribute(null, ATTR_ELEMENT_FORM_DEFAULT);
        if(attr != null) {
            removeAttribute(attr);
        }
        addAttribute(ATTR_ELEMENT_FORM_DEFAULT, elementForm);
    }

    public XmlElement getXsComplexType(String complexTypeName) {
        for(Iterator ei = elements(TYPE, XsComplexType.NAME).iterator(); ei.hasNext(); ) {
            XmlElement xsElement = (XmlElement) ei.next();
            //if(xsElement.getXsName().equals(globalElementName)) {
            String name = xsElement.getAttributeValue(null, "name");
            if(complexTypeName.equals(name)) {
                return xsElement;
            }
        }
        return null;
    }
    
    public XmlElement getXsElement(String globalElementName) {
        for(Iterator ei = elements(TYPE, XsElement.NAME).iterator(); ei.hasNext(); ) {
            XmlElement xsElement = (XmlElement) ei.next();
            //if(xsElement.getXsName().equals(globalElementName)) {
            String name = xsElement.getAttributeValue(null, "name");
            if(globalElementName.equals(name)) {
                return xsElement;
            }
        }
        return null;
    }
    
    
    public Iterable getXsElements() { //JDK15 Iterable<WsdlMessage>
        final Iterable iterable = elements(TYPE, XsElement.NAME);
        return new IterableAdapter(iterable) {
            public Object next(Iterator iter) {
                XmlElement el = (XmlElement) iter.next();
                return castOrWrap(el, XsElement.class);
            }
        };
    }
    
    
    public void validateData() throws DataValidationException {
        if(!TYPE.equals(getNamespace())) {
            throw new DataValidationException(
                "expected element to be in namespace "+TYPE.getNamespaceName()
                    +" not "+getNamespace().getNamespaceName());
        }
        if(!NAME.equals(getName())) {
            throw new DataValidationException(
                "expected element to have name "+NAME+" not "+getName());
        }
        if(getXsTargetNamespace() == null) {
            throw new DataValidationException("targetNamespace is required");
        }
        for(Iterator iter = getXsElements().iterator(); iter.hasNext();) {
            iter.next();
        }
    }
    
    
    public String toString() {
        return builder.serializeToString(this) ;
    }
    
}

// typical hockey-pockey exchange of control
class IterableAdapter implements Iterable {
    private final Iterable iterable;
    
    IterableAdapter(Iterable iterable) {
        this.iterable = iterable;
    }
    
    public Object next(Iterator iter) {
        return iter.next();
    }
    
    public Iterator iterator() {
        final Iterator iter = iterable.iterator();
        return new Iterator() {
            
            public boolean hasNext() {
                return iter.hasNext();
            }
            
            public void remove() {
                iter.remove();
            }
            
            public Object next() {
                return IterableAdapter.this.next(iter);
            }
        };
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

