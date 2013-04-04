/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XJavaClass.java,v 1.6 2005/12/06 08:10:02 aslom Exp $
 */
package xsul.xwsdl_compiler.xjava;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulVersion;

/**
 *
 */
public class XJavaClass {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private String pkg;
    private boolean isInterface;
    private Map importsMap = new TreeMap();
    private List methods = new ArrayList();
    
    private QName portTypeQname;
    private String name;
    
    private static final boolean addMeta = true;
    
    public XJavaClass(String pkg, String name, QName portTypeQname, boolean isInterface) {
        this.pkg = pkg;
        this.name = name;
        this.portTypeQname = portTypeQname;
        this.isInterface = isInterface;
    }
    
    public void setPortTypeQname(QName portTypeQname) {
        this.portTypeQname = portTypeQname;
    }
    
    public QName getPortTypeQname() {
        return portTypeQname;
    }
    
    
    /**
     * Sets Pkg
     *
     * @param    Pkg                 a  String
     */
    public void setJavaPackage(String pkg) {
        this.pkg = pkg;
    }
    
    /**
     * Returns Pkg
     *
     * @return    a  String
     */
    public String getJavaPackage() {
        return pkg;
    }
    
    /**
     * Sets Name
     *
     * @param    Name                a  String
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns Name
     *
     * @return    a  String
     */
    public String getName() {
        return name;
    }
    
    public XJavaMethod addMethod(XJavaMethod xm) {
        //XJavaMethod xm = new XJavaMethod(name);
        methods.add(xm);
        return xm;
    }
    
    public void addImport(String type) {
        if (!importsMap.containsKey(type)) {
            importsMap.put(type, "token");
        }
    }
    
    public void generate() {
        generate(System.out);
    }
    
    public void generate(PrintStream out) {
        writeInterfaceStart(out);
        
        //for each method -- write method
        for (int i = 0; i < methods.size(); i++) {
            XJavaMethod xm = (XJavaMethod) methods.get(i);
            xm.generate(out);
        }
        writeInterfaceEnd(out);
        out.flush();
    }
    
    private void writeInterfaceStart(PrintStream out) {
        String version = XsulVersion.getImplementationVersion();
        out.println("/* DO NOT MODIFY!!!! This file was generated automatically by xwsdlc (version " + version + ") */");
        if (pkg != null) {
            out.println("package " + pkg + ";"); //convertNamespaceToJavaPkg(targetNamespace));
        }
        writeImports(out);
        out.println("public " + (isInterface ? "interface" : "class") + " " + name + " {");
        if(addMeta) {
            String WSDL_NS = portTypeQname.getNamespaceURI(); //TODO escape especially "
            String PORT_TYPE_NAME = portTypeQname.getLocalPart(); //TODO escape especially "
            out.println(XJavaMethod.INDENT+"public final static QName XWSDLC_PORTTYPE_QNAME "+
                            "= new QName(\""+WSDL_NS+"\", \""+PORT_TYPE_NAME+"\");");
            
        }
    }
    
    //write all imports
    private void writeImports(PrintStream out) {
        if(addMeta) {
            out.println("import javax.xml.namespace.QName;");
        }
        for (Iterator i = importsMap.keySet().iterator(); i.hasNext();) {
            String im = (String) i.next();
            out.println("import " + im + ";");
        }
    }
    
    private void writeInterfaceEnd(PrintStream out) {
        out.println("}");
        out.println("");
    }
    
    //    private String convertNamespaceToJavaPkg(String targetNamespace) {
    //        return targetNamespace;
    //    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2005 The Trustees of Indiana University. All rights
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



