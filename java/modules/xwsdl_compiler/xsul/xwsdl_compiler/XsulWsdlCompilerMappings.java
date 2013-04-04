/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulWsdlCompilerMappings.java,v 1.1 2005/02/17 21:42:33 aslom Exp $
 */
package xsul.xwsdl_compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import org.apache.tools.ant.BuildException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.wsdl.WsdlDefinitions;

/**
 *
 */
public class XsulWsdlCompilerMappings {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    // now we will read mapping - first from XmlBeans (.xsdconfig) and can be overriden by
    // file format description
    // http://wiki.apache.org/xmlbeans/XmlBeansV1Faq#configPackageName
    // http://e-docs.bea.com/workshop/docs81/doc/en/workshop/guide/howdoi/howGuideXMLBeansTypeNaming.html
    private final String XB_NS = "http://www.bea.com/2002/09/xbean/config";
    
    private Map pkgMap = new TreeMap();
    
    public XsulWsdlCompilerMappings() {}
    
    public String mapNamespaceToJavaPackage(String namespace) {
        if(namespace == null) throw new IllegalArgumentException();
        String mapped = (String) pkgMap.get(namespace);
        if(mapped != null) {
            logger.fine(namespace+" -> "+mapped);
            return mapped;
        } else {
            throw new XsulWsdlCompilerException("missing namespace mapping for '"+namespace+"'");
        }
    }
    
    public String mapQNameToJavaName(QName qname) {
        if(qname.getNamespaceURI() == null) throw new IllegalArgumentException();
        if(qname.getLocalPart() == null) throw new IllegalArgumentException();
        String n = "{"+qname.getNamespaceURI()+"}"+qname.getLocalPart();
        String mapped = (String) pkgMap.get(n);
        
        if(mapped == null) {
            mapped = qname.getLocalPart(); // hope for the best ...
        }
        logger.fine(qname+" -> "+mapped);
        return mapped;
    }
    
    public URLConnection connectToLocation(String location) {
        URI base = ((new File(".")).toURI());
        URL url;
        try {
            url = new URL(base.toURL(), location);
            URLConnection conn = url.openConnection();
            return conn;
        } catch (IOException e) {
            try {
                url = new URL("file:///"+location);
                URLConnection conn = url.openConnection();
                return conn;
            } catch (IOException e2) {
                String msg = "could not open '"+location+"'";
                logger.info(msg, e);
                throw new XsulWsdlCompilerException(msg, e);
            }
        }
    }
    
    public void processListOfMappings(List /*<String>*/ listOfLocations) {
        for (int i = 0; i < listOfLocations.size(); i++) {
            String location = (String) listOfLocations.get(i);
            processMappings(location);
        }
    }
    
    public void processMappings(String location) throws XsulWsdlCompilerException {
        try {
            XmlPullParser pp = builder.getFactory().newPullParser();
            //System.out.println("processing mapping "+location);
            URLConnection conn = connectToLocation(location);
            //pp.setInput(new FileInputStream(location), "UTF8"); // bad assumption, bad
            String encoding = conn.getContentEncoding();
            logger.info("loading mapping from "+location+" encoding="+encoding);
            pp.setInput(conn.getInputStream(), encoding);
            pp.next();
            pp.require(XmlPullParser.START_TAG, XB_NS, "config");
            while(pp.nextTag() == XmlPullParser.START_TAG) {
                String tagName =  pp.getName();
                if(tagName.equals("namespace")) {
                    processNamespaceMapping(pp);
                } else if(tagName.equals("qname")) {
                    processQNameMapping(pp);
                } else {
                    throw new BuildException("unrecognized element "+tagName + pp.getPositionDescription());
                }
            }
            pp.require(XmlPullParser.END_TAG, XB_NS, "config");
        } catch(Exception e) {
            String msg = "Could not process mappings from '"+location+"' : "+e;
            logger.info(msg, e);
            throw new XsulWsdlCompilerException(msg, e);
            
        }
    }
    
    private void processNamespaceMapping(XmlPullParser pp) throws XmlPullParserException, IOException {
        //String tagName =  pp.getName();
        pp.require(XmlPullParser.START_TAG, null, "namespace");
        String uri = pp.getAttributeValue(null, "uri");
        if(uri == null) {
            throw new BuildException(
                "uri attribute is required for 'package' element "+ pp.getPositionDescription());
        }
        while(pp.nextTag() == XmlPullParser.START_TAG) {
            String tagName = pp.getName();
            if(tagName.equals("package")) {
                String javaPkg = pp.nextText();
                pkgMap.put(uri, javaPkg);
            } else {
                throw new BuildException(
                    "only 'package' elements expected inside <namespace> but got "+tagName + pp.getPositionDescription());
            }
        }
        pp.require(XmlPullParser.END_TAG, null, "namespace");
    }
    
    
    private void processQNameMapping(XmlPullParser pp) throws XmlPullParserException, IOException {
        pp.require(XmlPullParser.START_TAG, null, "qname");
        String name = pp.getAttributeValue(null, "name");
        if(name == null) {
            throw new BuildException(
                "name attribute is required for 'qname' element "+ pp.getPositionDescription());
        }
        String javaname = pp.getAttributeValue(null, "javaname");
        if(javaname == null) {
            throw new BuildException(
                "javaname attribute is required for 'qname' element "+ pp.getPositionDescription());
        }
        int pos = name.indexOf(':');
        if(pos == -1) {
            throw new BuildException(
                "name "+name+" must have colon to be QName"+ pp.getPositionDescription());
        }
        String prefix = name.substring(0, pos);
        String localName = name.substring(pos+1);
        String uri = pp.getNamespace(prefix);
        pkgMap.put("{"+uri+"}"+localName, javaname);
        pp.nextTag();
        pp.require(XmlPullParser.END_TAG, null, "qname");
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 *
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 *
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 *
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 *
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */


