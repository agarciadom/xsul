/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsdlDefinitions.java,v 1.3 2004/11/07 09:50:10 aslom Exp $
 */

package xsul.wsdl;

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
 * Implementation of WSLD 1.1 Definitions
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsdlDefinitions extends XmlElementAdapter implements DataValidation
{
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static String NAME = "definitions";
    
    //private final static XmlNamespace wsa = WsAddressing.NS;
    public final static XmlNamespace TYPE =
        builder.newNamespace("http://schemas.xmlsoap.org/wsdl/");
    
    public WsdlDefinitions(String targetNamespace) {
        super(builder.newFragment(TYPE, NAME));
        setTargetNamespace(targetNamespace);
        validateData();
    }
    
    public WsdlDefinitions(XmlElement target) {
        super(target);
        validateData();
    }

    public XmlElement getTypes() {
        // try to extract wsdl:types
        return element(TYPE, "types");
    }
    
    public XmlElement findXsSchemaInTypes(String schemaTargetNamespace) {
        XmlElement types = getTypes();
        if(types == null) {
            return null;
        }
        for(Iterator si = types.elements(XmlConstants.XS_NS, "schema").iterator(); si.hasNext(); ) {
            XmlElement schema = (XmlElement) si.next();
            String targetNs = schema.getAttributeValue(null, "targetNamespace");
            if(schemaTargetNamespace.equals(targetNs)) {
                return schema;
            }
        }
        return null;
    }
    
    
    public String getTargetNamespace() {
        return getAttributeValue(null, "targetNamespace");
    }
    
    public void setTargetNamespace(String targetNamespace) {
        XmlAttribute att = findAttribute(null, "targetNamespace");
        if(att != null) {
            removeAttribute(att);
        }
        addAttribute(null, "targetNamespace", targetNamespace);
    }
    
    public Iterable getMessages() { //JDK15 Iterable<WsdlMessage>
        final Iterable iterable = elements(TYPE, WsdlMessage.TYPE_NAME);
        return new IterableAdapter(iterable) {
            public Object next(Iterator iter) {
                XmlElement el = (XmlElement) iter.next();
                return castOrWrap(el, WsdlMessage.class);
            }
        };
    }
    
    public WsdlMessage getMessage(String messageName) {
        for(Iterator iter = getMessages().iterator(); iter.hasNext(); ) {
            WsdlMessage message = (WsdlMessage) iter.next();
            if(message.getMessageName().equals(messageName)) {
                return message;
            }
        }
        return null;
    }
    
    public Iterable getPortTypes() { //JDK15 Iterable<WsdlPortType>
        final Iterable iterable = elements(TYPE, WsdlPortType.NAME);
        return new IterableAdapter(iterable) {
            public Object next(Iterator iter) {
                XmlElement el = (XmlElement) iter.next();
                return castOrWrap(el, WsdlPortType.class);
            }
        };
    }

    public WsdlPortType getPortType(String portTypeName) {
        for(Iterator iter = getPortTypes().iterator(); iter.hasNext(); ) {
            WsdlPortType possiblePT = (WsdlPortType) iter.next();
            if(possiblePT.getAttributeValue(null, "name").equals(portTypeName)) {
                return possiblePT;
            }
        }
//        throw new WsdlException(
//            "PortType '"+portTypeName+ "' is not available and no alternative can be found in "+toString());
        return null;
    }
    
    public Iterable getBindings() { //JDK15 Iterable<WsdlBinding>
        final Iterable iterable = elements(TYPE, WsdlBinding.NAME);
        return new IterableAdapter(iterable) {
            public Object next(Iterator iter) {
                XmlElement el = (XmlElement) iter.next();
                return castOrWrap(el, WsdlBinding.class);
            }
        };
    }
    
    public WsdlBinding getBinding(String bindingName) {
        for(Iterator iter = getBindings().iterator(); iter.hasNext(); ) {
            WsdlBinding possibleBinding = (WsdlBinding) iter.next();
            if(possibleBinding.getAttributeValue(null, "name").equals(bindingName)) {
                return possibleBinding;
            }
        }
//        throw new WsdlException(
//            "Binding '"+bindingName+ "' is not available and no alternative can be found in "+toString());
        return null;
    }
    
    
    public Iterable getServices() { //JDK15 Iterable<WsdlService>
        final Iterable iterable = elements(TYPE, WsdlService.NAME);
        return new IterableAdapter(iterable) {
            public Object next(Iterator iter) {
                XmlElement el = (XmlElement) iter.next();
                return castOrWrap(el, WsdlService.class);
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
        if(getTargetNamespace() == null) {
            throw new DataValidationException("targetNamespace is required");
        }
        for(Iterator iter = getMessages().iterator(); iter.hasNext();) {
            iter.next();
        }
        for(Iterator iter = getPortTypes().iterator(); iter.hasNext();) {
            iter.next();
        }
        for(Iterator iter = getBindings().iterator(); iter.hasNext();) {
            iter.next();
        }
        for(Iterator iter = getServices().iterator(); iter.hasNext();) {
            iter.next();
        }
    }
    
    
    public String toString() {
        return builder.serializeToString(this) ;
    }
    
    
    //    private static class IteratorAdapter implements Iterator {
    //      final Iterator iter;
    //      IteratorAdapter(Iterator iter) {
    //          this.iter = iter;
    //      }
    //      public boolean hasNext() {
    //          return iter.hasNext();
    //      }
    //      public Object next() {
    //          iter.next();
    //      }
    //      public void remove() {
    //          iter.remove();
    //      }
    //
    //    }
    
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

