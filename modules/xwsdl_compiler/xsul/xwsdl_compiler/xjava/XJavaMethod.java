/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XJavaMethod.java,v 1.2 2005/12/06 08:01:43 aslom Exp $
 */
package xsul.xwsdl_compiler.xjava;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;

/**
 *
 */
public class XJavaMethod {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    public final static String INDENT = "    ";
    private XJavaClass klazz;
    private String name;
    private String returnType;
    private boolean isInterface;
    private List argumentNames = new ArrayList();
    private List argumentTypes = new ArrayList();
    
    
    public XJavaMethod(XJavaClass klazz, String name) {
        this.klazz = klazz;
        this.name = name;
    }
    
    public void addArgument(String type, String name) {
        argumentTypes.add(type);
        argumentNames.add(name);
    }
    
    /**
     * Sets method return type
     *
     * @param    ReturnType          a  String
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    /**
     * Returns method return type
     *
     * @return    a  String
     */
    public String getReturnType() {
        return returnType;
    }
    
    public void generate(PrintStream out) {
        out.print(INDENT + "public "+returnType+" " + name + "(");
        //XmlElement
        for (int i = 0; i < argumentTypes.size(); i++) {
            String atype = (String) argumentTypes.get(i);
            String aname = (String) argumentNames.get(i);
            if(i > 0) out.print(", ");
            out.print(atype+" "+aname);
        }
        out.println(");");
    }
    
    
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

