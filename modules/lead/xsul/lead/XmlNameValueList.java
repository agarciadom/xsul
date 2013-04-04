/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/* Copyright (c) 2002-2005 Extreme! Lab, Indiana University. All rights reserved.
 * This software is open source. See the bottom of this file for the licence.
 * $Id: XmlNameValueList.java,v 1.3 2006/08/29 18:47:05 aslom Exp $ */
package xsul.lead;

import java.util.Iterator;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlObject;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.xbeans_util.XBeansUtil;

/**
 * Represents list of name-values and allows to easily convert it to/from Properties
 * as seqeunce of XML elements (key names) with text content (values).
 */
public class XmlNameValueList extends XmlElementAdapter {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    private final static QName DEFAULT_TYPE = new QName(
        "http://lead.extreme.indiana.edu/namespaces/2006/06/xml-name-values-list/", "xml-name-values-list");
    private final static XmlNamespace NS = builder.newNamespace("xnvl", DEFAULT_TYPE.getNamespaceURI());
    
    public XmlNameValueList(XmlObject xmlObjectToWrap) {
        this(XBeansUtil.xmlObjectToXmlElement(xmlObjectToWrap));
    }
    
    public XmlNameValueList(XmlElement elementToWrap) {
        super(elementToWrap);
        if(elementToWrap == null) throw new IllegalArgumentException("null");
    }
    
    public XmlNameValueList() {
        this(DEFAULT_TYPE);
        declareNamespace(NS);
    }
    
    public XmlNameValueList(QName topElQName) {
        super(builder.newFragment(topElQName.getNamespaceURI(), topElQName.getLocalPart()));
    }
    
    public XmlNameValueList(QName elQName, Properties props) {
        this(elQName);
        setFromProperties(props);
    }

    public XmlNameValueList(Properties props) {
        this();
        setFromProperties(props);
    }
    
    public void setFromProperties(Properties props) {
        removeAllChildren();
        for(Iterator i = props.keySet().iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = props.getProperty(key);
            setString(key, value);
        }
    }
    
    public Properties toProperties() {
        Properties props = new Properties();
        for(Iterator i = requiredElementContent().iterator(); i.hasNext(); ) {
            XmlElement el = (XmlElement) i.next();
            String key = el.getName();
            String value = el.requiredTextContent();
            props.setProperty(key, value);
        }
        return props;
    }
    
    public String getProperty(String key) {
        return getString(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        String value = getString(key);
        if(value == null) {
            value = defaultValue;
        }
        return defaultValue;
    }
    
    public void setProperty(String key, String value) {
        setString(key, value);
    }
    
    
    public void setString(String name, String value) {
        XmlElement el = element(null, name, true);
        if(value == null) throw new IllegalArgumentException();
        el.replaceChildrenWithText(value);
    }
    
    public String getString(String name) {
        XmlElement el = element(null, name);
        if(el == null) {
            return null;
        }
        return el.requiredTextContent();
    }
    
    public XmlObject xmlObject() {
        return XBeansUtil.xmlElementToXmlObject(getTarget());
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2005 The Trustees of Indiana University.
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



