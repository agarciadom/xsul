/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulWsdlCompilerWithXmlBeans.java,v 1.4 2005/03/01 23:21:31 aslom Exp $
 */
package xsul.xwsdl_compiler_xbeans;

import java.util.Iterator;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import xsul.wsdl.WsdlMessage;
import xsul.wsdl.WsdlMessagePart;
import xsul.wsdl.WsdlPortTypeInput;
import xsul.wsdl.WsdlPortTypeOperation;
import xsul.wsdl.WsdlPortTypeOutput;
import xsul.xwsdl_compiler.XsulWsdlCompiler;
import xsul.xwsdl_compiler.XsulWsdlCompilerException;
import xsul.xwsdl_compiler.xjava.XJavaClass;
import xsul.xwsdl_compiler.xjava.XJavaMethod;

/**
 * Extension of XSUL WSDL Compiler that uses XmlBeans SchemaTypeSystem
 * to do mapping of WSDL message:element to Java classes.
 */
public class XsulWsdlCompilerWithXmlBeans extends XsulWsdlCompiler {
    //JDK5 enum
    public final static String BINDING_XMLBEANS = "xmlbeans";   //default
    public final static String BINDING_XPP3 = "xpp3";
    
    private String binding = BINDING_XMLBEANS;
    private SchemaTypeSystem schemaTypeSystem;
    
    public XsulWsdlCompilerWithXmlBeans(SchemaTypeSystem schemaTypeSystem) {
        this.schemaTypeSystem = schemaTypeSystem;
    }
    
    protected XJavaMethod generateDocLiteralMethod(XJavaClass xclass, WsdlPortTypeOperation op,
                                                   WsdlMessagePart outputElementPart,
                                                   WsdlMessagePart inputElementPart) {
        String origOpName = op.getOperationName();
        String opName = op.getOperationName();
        if(Character.isUpperCase(opName.charAt(0))) { //TODO more sophisticate "java-ization"
            opName = Character.toLowerCase(opName.charAt(0)) + opName.substring(1);
        }
        XJavaMethod xm = xclass.addMethod(new XJavaMethod(xclass, opName));
        if ( binding == BINDING_XPP3 ) {
            return super.generateDocLiteralMethod(xclass, op, outputElementPart, inputElementPart);
        } else if ( binding == BINDING_XMLBEANS ) {
            { // map input
                QName qn = inputElementPart.getPartElement();
                SchemaType inputElement = schemaTypeSystem.findDocumentType(qn);
                if(inputElement ==  null) {
                    throw new XsulWsdlCompilerException("could not find input element "+qn+" for "+origOpName);
                }
                String inputJavaFQN = inputElement.getFullJavaName();
                //xclass.addImport( inputJavaFQN );
                xm.addArgument(inputJavaFQN, "input");
            }
            if ( outputElementPart != null ) { // map output *if* it exists in WSDL
                QName qn = outputElementPart.getPartElement();
                SchemaType outputElement = schemaTypeSystem.findDocumentType(qn);
                if(outputElement ==  null) {
                    throw new XsulWsdlCompilerException("could not find output element "+qn+" for "+origOpName);
                }
                String outputJavaFQN = outputElement.getFullJavaName();
                xm.setReturnType(outputJavaFQN);
            } else {
                xm.setReturnType("void");
            }
            return xm;
            
        } else {
            throw new IllegalStateException("unknown binding type " + binding);
        }
    }
    
    /**
     * Sets Binding
     *
     * @param    Binding             a  String
     */
    public void setBinding(String binding) {
        if(binding != BINDING_XMLBEANS || binding != BINDING_XPP3) {
        }
        this.binding = binding;
    }
    
    /**
     * Returns Binding
     *
     * @return    a  String
     */
    public String getBinding() {
        return binding;
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

