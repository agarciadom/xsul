/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WSIFMessageElement.java,v 1.11 2006/08/16 20:30:18 aslom Exp $
 */

package xsul.wsif.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.Iterable;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidationException;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.util.XsulUtil;
import xsul.wsdl.WsdlMessage;
import xsul.wsdl.WsdlMessagePart;
import xsul.wsif.WSIFException;
import xsul.wsif.WSIFMessage;
import xsul.xs.XsComplexType;
import xsul.xs.XsSchema;

//WSIFMessageElement
/**
 * WSIF Message that is built on top of XmlElement
 *
 * @version $Revision: 1.11 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WSIFMessageElement extends XmlElementAdapter implements XmlElement, WSIFMessage  {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private Map mapPartToType = new HashMap();
    
    public WSIFMessageElement(String messageName) {
        super( builder.newFragment(messageName) );
        validateData();
    }
    
    
    public WSIFMessageElement(XmlElement target) {
        super(target);
        validateData();
    }
    
    
    public WSIFMessageElement(WsdlMessage message) {
        this(message.getMessageName());
        addPlaceholderForParameters(message);
        validateData();
    }
    
    private void addPlaceholderForParameters(WsdlMessage message) {
        List partNames = new ArrayList(8); //JDK15 ArrayList<String>
        for (Iterator i = message.getParts().iterator(); i.hasNext(); ) {
            WsdlMessagePart part = (WsdlMessagePart) i.next();
            partNames.add(part.getPartName());
        }
        String elementPartName = null;
        QName elementPartQName = null;
        if(partNames.size() == 1) {
            WsdlMessagePart part = (WsdlMessagePart) message.getParts().iterator().next();
            // part has @element or not ...
            elementPartQName = part.getPartElement();
            if(elementPartQName != null) {
                elementPartName = part.getPartName();
            }
        }
        
        if(elementPartName == null) {
            for(Iterator i = message.getParts().iterator(); i.hasNext(); ) {
                WsdlMessagePart part = (WsdlMessagePart) i.next();
                addElement( part.getPartName() );
            }
            return;
        }
        logger.info("elementPartQName="+elementPartQName);
        // OK let try and deal with most commnon use of <xs:sequence> for part element/doc/literal
        if(elementPartQName != null) {
            //FOXME better API
            XmlNamespace elementNamespace = builder.newNamespace( elementPartQName.getNamespaceURI() );
            setNamespace( elementNamespace );
            setName( elementPartQName.getLocalPart() ) ;
            
            removeAllChildren();
            partNames.clear();
            
            String desiredXsElementNs = elementPartQName.getNamespaceURI();
            assert desiredXsElementNs != null;
            String desiredXsElementName = elementPartQName.getLocalPart();
            // try to retrieve complexType from types section (somehow)
            //TODO: needs real XsResolver
            XmlElement schemaEl = message.getDefinitions().findXsSchemaInTypes(desiredXsElementNs);
            //create XmlElement extract list of elements/names to be used for message put inside!
            boolean formQualified = false;
            if(schemaEl != null) {
                
                //(type-cast) XML Infoset
                XsSchema xsSchema = (XsSchema) XsSchema.castOrWrap(schemaEl, XsSchema.class);
                
                formQualified = XsSchema.ENUM_ELEMENT_FORM_QUALIFIED.equals(xsSchema.getXsElementFormDefault());
                
                // first find element with input part QName
                XmlElement sElement = xsSchema.getXsElement(desiredXsElementName);
                
                
                //found It !!!!!
                // now find complexType xs:sequence or xs:all ...
                // complexType@name="desiredName"
                String sElType = sElement.getAttributeValue(null, "type");
                
                
                XmlElement complexTypeEl;
                if(sElType == null) {
                    // try for anonymous complexType
                    complexTypeEl = sElement.element(XmlConstants.XS_NS, XsComplexType.NAME);
                    if(complexTypeEl == null) {
                        if(!sElement.elements(null, null).iterator().hasNext()) {
                            throw new WSIFException(
                                "element "+desiredXsElementName
                                    +" may have complexContent with empty sequence but cannot be empty");
                        } else {
                            String s = XsulUtil.safeXmlToString(sElement);
                            throw new WSIFException(
                                "element "+desiredXsElementName
                                    +" schema content not supported "+s);
                        }
                    }
                } else {
                    QName typeQName = XsulUtil.toQName(sElement, sElType);
                    // handle simple types
                    if(typeQName.getNamespaceURI().equals( XsSchema.TYPE.getNamespaceName() )) {
                        if(typeQName.getLocalPart().equals("string") // i hate Xml Schemas ....
                               || typeQName.getLocalPart().equals("int")
                               || typeQName.getLocalPart().equals("double")
                               || typeQName.getLocalPart().equals("boolean")
                               || typeQName.getLocalPart().equals("integer")) // SWoM team has added this item
                            
                            
                        {
                            return;
                        }
                    }
                    
                    if(! typeQName.getNamespaceURI().equals( desiredXsElementNs ) ) {
                        throw new WSIFException("complext types from other schema namespaces are not supported: "+typeQName);
                    }
                    complexTypeEl = xsSchema.getXsComplexType(typeQName.getLocalPart());
                    if(complexTypeEl == null) {
                        throw new WSIFException("could not find complex type defintion for "+typeQName);
                    }
                    
                }
                // now we only support sequence == convert elements in sequence to list of names
                XmlElement sequenceEl = complexTypeEl.element(XmlConstants.XS_NS, "sequence");
                if(sequenceEl == null) {
                    return; // allow empty  <complexType />
                }
                //partNames.clear();
                addListOfElementNames(sequenceEl, partNames, elementNamespace.getNamespaceName(), mapPartToType);
                for (int i = 0; i < partNames.size(); i++) {
                    String name = (String) partNames.get(i);
                    logger.finest("name="+name);
                    if(formQualified) {
                        addElement(elementNamespace, name);
                    } else {
                        addElement(null, name);
                    }
                }
                logger.info("special messageIn="+builder.serializeToString(this));
            } else {
                throw new WSIFException(
                    "missing wsdl:schema declaration in wsdl:types for xsd:element "+elementPartQName);
            }
            //            } else {
            //                throw new XsulException(
            //                    "could not find wsdl:types to extract xsd:element "+elementPartQName);
            //            }
            
        }
        logger.info("messageIn="+builder.serializeToString(this));
        // populate input with elements and set right namespace ...
        
    }
    
    private static void addListOfElementNames(XmlElement sequenceEl,
                                              List partNames,
                                              String elementNamespace,
                                              Map mapPartToType)
    {
        for(Iterator ei = sequenceEl.requiredElementContent().iterator(); ei.hasNext(); ) {
            XmlElement xsdSomething = (XmlElement) ei.next();
            if(xsdSomething.getName().equals("annotation") || xsdSomething.getName().equals("any")){
                continue;
            }
            if(! xsdSomething.getName().equals("element")) {
                throw new WSIFException("only xsd:element or xsd:any is supported in xsd:sequence");
            }
            // check minOccurs / maxOccurs contraints
            {
                String maxOccursVal = xsdSomething.getAttributeValue(null, "maxOccurs");
                if(maxOccursVal != null) {
                    if(!"1".equals(maxOccursVal)) {
                        if("unbounded".equals(maxOccursVal)) {
                            // treat unbounded as epcial case of 1 - just put one and allow user to add more ...
                        } else {
                            throw new WSIFException(
                                "only maxOccurs='1' supported for elements in sequence in WSIF not "
                                    +maxOccursVal+" (on "+builder.serializeToString(xsdSomething)+")");
                    }
                }
            }
        }
        {
            String minOccursVal = xsdSomething.getAttributeValue(null, "minOccurs");
            if(minOccursVal != null) {
                if("0".equals(minOccursVal)) {
                    continue; //skip optional parameters
                }
                if(!"1".equals(minOccursVal)) {
                    throw new WSIFException(
                        "only minOccurs='0' or '1' is supported for elements in sequence in WSIF");
                }
            }
        }
        String name = xsdSomething.getAttributeValue(null, "name");
        String ref = xsdSomething.getAttributeValue(null, "ref");
        String typeVal = xsdSomething.getAttributeValue(null, "type");
        if(name != null && typeVal != null) {
            QName typeQName = XsulUtil.toQName(xsdSomething, typeVal);
            logger.fine("adding sequence element type name="+name+" typeQName="+typeQName);
            mapPartToType.put(name, typeQName);
        }
        if(name != null && ref != null) {
            String s = builder.serializeToString(sequenceEl);
            throw new WSIFException("name and ref can not be set at the same time on element "+s);
        } else if(name != null) {
            logger.fine("adding sequence element name="+name);
            partNames.add(name);
        } else if(ref != null) {
            QName refQName = XsulUtil.toQName(xsdSomething, ref);
            logger.fine("adding sequence element ref="+refQName);
            if(!refQName.getNamespaceURI().equals(elementNamespace)) {
                String s = builder.serializeToString(sequenceEl);
                throw new WSIFException(
                    "element ref='"+ref+"' must be in the namespace "+elementNamespace
                        +" not "+refQName+" (context: "+s+")");
            }
            partNames.add(refQName.getLocalPart());
        } else {
            assert (name == null && ref == null);
            String s = builder.serializeToString(sequenceEl);
            throw new WSIFException("missing name or ref on element in sequence "+s);
        }
    }
}

public QName getPartType(String name) {
    return (QName) mapPartToType.get(name);
}

public Iterable partNames() { // Iterable<String>
    //throw new IllegalStateException("not implemented"); //TODO
    final List partNames = new ArrayList(8); //JDK15 ArrayList<String>
    for (Iterator i = elements(null, null).iterator(); i.hasNext(); ) {
        //WsdlMessagePart part = (WsdlMessagePart) i.next();
        XmlElement el = (XmlElement) i.next();
        partNames.add(el.getName()); //part.getPartName());
    }
    return new Iterable() {
        public Iterator iterator() {
            return partNames.iterator();
        }
    };
}

public Iterable  parts() { // Iterable<Object>
    throw new IllegalStateException("not implemented"); //TODO
}

public Object getObjectPart(String name) throws WSIFException {
    XmlElement elPart = element(null, name);
    if(elPart == null) {
        throw new WSIFException("no part named '"+name+"'");
    }
    if(elPart.hasChildren()) {
        return elPart.children().next(); //return first child - aka element content ...
    } else {
        return null;
    }
}

public Object getObjectPart(String name, Class sourceClass) throws WSIFException {
    throw new IllegalStateException("not implemented"); //TODO
}

public void setObjectPart(String name, Object part) throws WSIFException {
    //XmlElement elPart = element(null, name, true);
    if(part ==null) throw new IllegalArgumentException();
    XmlElement elPart = element(null, name);
    if(elPart == null) {
        throw new WSIFException("no part with name '"+name+"' is allowed");
    }
    if(part instanceof XmlElement) {
        XmlElement partAsEl = (XmlElement) part;
        if(partAsEl.getName().equals(name)) {
            //                // transfer children children
            //                for (Iterator i = partAsEl.children(); i .hasNext(); ){
            //                    elPart.addChild(i.next());
            //                }
            // replace part in WSIF Message with user passed part
            replaceChild(partAsEl, elPart);
            partAsEl.setParent(this); //little of wiring
            return;
        }
        
    }
    elPart.removeAllChildren();
    elPart.addChild(part);
}

public char getCharPart(String name) throws WSIFException {
    throw new IllegalStateException("not implemented");
}

public byte getBytePart(String name) throws WSIFException {
    throw new IllegalStateException("not implemented");
}

public short getShortPart(String name) throws WSIFException {
    throw new IllegalStateException("not implemented");
}

public int getIntPart(String name) throws WSIFException {
    throw new IllegalStateException("not implemented");
}

public long getLongPart(String name) throws WSIFException {
    throw new IllegalStateException("not implemented");
}

public float getFloatPart(String name) throws WSIFException {
    throw new IllegalStateException("not implemented");
}

public double getDoublePart(String name) throws WSIFException {
    throw new IllegalStateException("not implemented");
}

public boolean getBooleanPart(String name) throws WSIFException {
    throw new IllegalStateException("not implemented");
}

public void setCharPart(String name, char charPart) {
    throw new IllegalStateException("not implemented");
}

public void setBytePart(String name, byte bytePart) {
    throw new IllegalStateException("not implemented");
}

public void setShortPart(String name, short shortPart) {
    throw new IllegalStateException("not implemented");
}

public void setIntPart(String name, int intPart) {
    throw new IllegalStateException("not implemented");
}

public void setLongPart(String name, long longPart) {
    throw new IllegalStateException("not implemented");
}

public void setFloatPart(String name, float floatPart) {
    throw new IllegalStateException("not implemented");
}

public void setDoublePart(String name, double doublePart) {
    throw new IllegalStateException("not implemented");
}

public void setBooleanPart(String name, boolean booleanPart) {
    throw new IllegalStateException("not implemented");
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

