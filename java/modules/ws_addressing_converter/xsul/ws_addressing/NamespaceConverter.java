/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/* Copyright (c) 2006 Extreme! Lab, Indiana University. All rights reserved.
 * This software is open source. See the bottom of this file for the licence.
 * $Id: NamespaceConverter.java,v 1.3 2006/04/30 06:48:14 aslom Exp $ */

package xsul.ws_addressing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;
import xsul.XsulException;

/**
 * Convert namespace declarations and preserve prefixes to maintain QName mappings.
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class NamespaceConverter {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    protected String[] sourceNamespaces;
    protected XmlNamespace targetNamespace;
    protected XmlNamespace firstConvertedNamespace;
    
    public NamespaceConverter(String[] sourceNamespaces, XmlNamespace targetNamespace) {
        this.sourceNamespaces = sourceNamespaces;
        this.targetNamespace = targetNamespace;
    }
    
    public XmlNamespace convertElement(XmlElement el) {
        firstConvertedNamespace = null;
        convertTree(el);
        return firstConvertedNamespace;
    }
    
    protected void convertTree(XmlElement el) {
        
        convertElementWithoutChildren(el);
        
        if(el.hasChildren()) { // walk tree revursively
            for(Iterator i = el.children(); i.hasNext(); ) {
                Object child = i.next();
                if(child instanceof XmlElement) {
                    convertTree((XmlElement) child);
                }
            }
        }
        
    }

    protected void convertElementWithoutChildren(XmlElement el) {
        convertElementNamespace(el);
        
        if(el.hasNamespaceDeclarations()) {
            convertNamespaceDeclarations(el);
        }
        
        if(el.hasAttributes()) {
            convertAttributeNamespaces(el);
        }
    }
    
    protected void convertAttributeNamespaces(XmlElement el) {
        // not exactly supper efficient ...
        List attributesToChange = new ArrayList();
        for(Iterator i = el.attributes(); i.hasNext(); ) {
            XmlAttribute attr =  (XmlAttribute) i.next();
            XmlNamespace n = shouldConvert(attr.getNamespace());
            if(n != null) {
                attributesToChange.add(attr) ;
                if(firstConvertedNamespace == null) {
                    firstConvertedNamespace = n;
                }
            }
        }
        for(Iterator i = attributesToChange.iterator(); i.hasNext(); ) {
            XmlAttribute attr =  (XmlAttribute) i.next();
            XmlNamespace n = shouldConvert(attr.getNamespace());
            el.removeAttribute(attr);
            el.addAttribute(attr.getType(), n.getPrefix(),n.getNamespaceName(), attr.getName(),
                            attr.getValue(), attr.isSpecified());
        }
    }
    
    protected void convertNamespaceDeclarations(XmlElement el) {
        // not exactly supper efficient
        List nsToAdd = new ArrayList();
        List nsToPreserve = new ArrayList();
        for(Iterator i = el.namespaces(); i.hasNext(); ) {
            XmlNamespace declaredNs =  (XmlNamespace) i.next();
            XmlNamespace n = shouldConvert(declaredNs);
            if(n != null) {
                nsToAdd.add(n) ;
                if(firstConvertedNamespace == null) {
                    firstConvertedNamespace = n;
                }
            } else {
                nsToPreserve.add(declaredNs);
            }
        }
        el.removeAllNamespaceDeclarations();
        for(Iterator i = nsToPreserve.iterator(); i.hasNext(); ) {
            XmlNamespace declaredNs =  (XmlNamespace) i.next();
            el.declareNamespace(declaredNs);
        }
        for(Iterator i = nsToAdd.iterator(); i.hasNext(); ) {
            XmlNamespace modifiedNs =  (XmlNamespace) i.next();
            el.declareNamespace(modifiedNs);
        }
    }
    
    protected void convertElementNamespace(XmlElement el) {
        XmlNamespace elNs = el.getNamespace();
        XmlNamespace n = shouldConvert(elNs);
        if(n != null) {
            el.setNamespace(n);
            if(firstConvertedNamespace == null) {
                firstConvertedNamespace = n;
            }
        }
    }

    protected XmlNamespace shouldConvert(XmlNamespace elNs) {
        String targetUri = targetNamespace.getNamespaceName();
        String elUri = elNs.getNamespaceName();
        for (int i = 0; i < sourceNamespaces.length; i++){
            String recUri = sourceNamespaces[i];
            if(elUri.equals(recUri)){
                if(elUri.equals(targetUri)) {
                    return null;
                } else {
                    // found -- now need to make sure prefix is preserved
                    String targetPrefix = targetNamespace.getPrefix();
                    String elPrefix = elNs.getPrefix();
                    if(targetPrefix.equals(elPrefix)) {
                        return targetNamespace;
                    } else {
                        if(elPrefix != null) {
                            return builder.newNamespace(elPrefix, targetUri);
                        } else {
                            return builder.newNamespace(targetUri); //CHECKME
                        }
                    }
                }
            }
        }
        return null;
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

