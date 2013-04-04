/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulWsdlCompiler.java,v 1.9 2006/04/18 18:03:49 aslom Exp $
 */
package xsul.xwsdl_compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.util.XsulUtil;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlException;
import xsul.wsdl.WsdlMessage;
import xsul.wsdl.WsdlMessagePart;
import xsul.wsdl.WsdlPortType;
import xsul.wsdl.WsdlPortTypeInput;
import xsul.wsdl.WsdlPortTypeOperation;
import xsul.wsdl.WsdlPortTypeOutput;
import xsul.wsdl.WsdlResolver;
import xsul.xwsdl_compiler.xjava.XJavaClass;
import xsul.xwsdl_compiler.xjava.XJavaMethod;

/**
 *
 */
public class XsulWsdlCompiler {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static String INDENT = "    ";
    private PrintStream out = System.out;
    //private String  pkg;
    private WsdlDefinitions def;
    
    private XsulWsdlCompilerMappings mappings;
    
    /**
     * Run compiler: [--validate] URL.wsdl [URL.xsdconfig] [URL.wsdlconfig]
     * DISTANT FUTURE: [--binding xmlbeans{|dom2|xpp3|rpc|...}] [--async] [--wsdlgendir dir]
     */
    public static void main(String[] args) throws XsulWsdlCompilerException
    {
        (new XsulWsdlCompiler()).runCompiler(args);
    }
    
    public void runCompiler(String[] args) throws XsulWsdlCompilerException {
        String PARAM_ASYNC = "--async";
        String PARAM_BINDING = "--binding";
        String PARAM_WSDLGENDIR = "--wsdlgendir";
        String PARAM_VALIDATE = "--validate";
        int argpos = 0;
        boolean validate = false;
        List listOfWsdlLocations = new ArrayList();
        List listOfXsdconfigs = new ArrayList();
        List listOfWsdlconfigs = new ArrayList();
        while(argpos < args.length) {
            String arg = args[argpos];
            if(arg.indexOf(PARAM_ASYNC) == 0) {
                throw new XsulWsdlCompilerException(PARAM_ASYNC+" not supported in this version");
            } else if(arg.indexOf(PARAM_BINDING) == 0) {
                throw new XsulWsdlCompilerException(PARAM_BINDING+" not supported in this version");
                //++argpos;
                //pkg = args[argpos];
            } else if(arg.indexOf(PARAM_WSDLGENDIR) == 0) {
                throw new XsulWsdlCompilerException(PARAM_WSDLGENDIR+" not supported in this version");
            } else if(arg.indexOf(PARAM_VALIDATE) == 0) {
                validate = true;
            } else if(arg.indexOf("--") == 0) {
                throw new XsulWsdlCompilerException("unknown parameter "+arg);
            } else {
                if(arg.endsWith(".xsdconfig")) {
                    listOfXsdconfigs.add(arg);
                } else if(arg.endsWith(".wsdlconfig")) {
                    listOfWsdlconfigs.add(arg);
                } else {
                    listOfWsdlLocations.add(arg);
                }
            }
            argpos++;
        }
        
        this.mappings = new XsulWsdlCompilerMappings();
        mappings.processListOfMappings(listOfXsdconfigs);
        mappings.processListOfMappings(listOfWsdlconfigs);
        
        if(listOfWsdlLocations.size() == 0) {
            throw new XsulWsdlCompilerException("at least one input argument with WSDL location is required");
        }
        for (Iterator iter = listOfWsdlLocations.iterator(); iter.hasNext();  )
        {
            String wsdlLoc = (String) iter.next();
            if(validate) {
                URI base = ((new File(".")).toURI());
                try {
                    this.def = WsdlResolver.getInstance().loadWsdl(base, new URI(wsdlLoc));
                } catch (WsdlException e) {
                    throw new XsulWsdlCompilerException("could not parse WSDL from " + wsdlLoc, e);
                } catch (URISyntaxException e) {
                    throw new XsulWsdlCompilerException("incorrect location for WSDL: " + wsdlLoc, e);
                }
                String s = XsulUtil.safeXmlToString(def);
                System.out.println("OK "+wsdlLoc);
            } else {
                useWsdlFromLocation(wsdlLoc);
                List generatedArtifacts = new ArrayList();
                generate(generatedArtifacts);
                for ( int i = 0; i < generatedArtifacts.size(); i++ ) {
                    XJavaClass xc = (XJavaClass) generatedArtifacts.get(i);
                    xc.generate(System.out);
                }
            }
        }
        
    }
    
    public void useWsdlFromLocation(String wsdlLoc) {
        URI base = ((new File(".")).toURI());
        try {
            this.def = WsdlResolver.getInstance().loadWsdl(base, new URI(wsdlLoc));
        } catch (WsdlException e) {
            throw new XsulWsdlCompilerException("could not parse WSDL from " + wsdlLoc, e);
        } catch (URISyntaxException e) {
            throw new XsulWsdlCompilerException("incorrect location for WSDL: " + wsdlLoc, e);
        }
        setWsdlToUse(def);
    }
    
    public void generateFiles(List generatedArtifacts, final String genDir) {
        final char S = File.separatorChar;
        for(Iterator i = generatedArtifacts.iterator(); i.hasNext(); ) {
            XJavaClass xc = (XJavaClass) i.next();
            String interfaceFileName = xc.getName() + ".java";
            // convert package name ots into (back)slashes
            File interfaceFileDir;
            String pkg = xc.getJavaPackage();
            if(pkg != null) {
                String subDir = pkg.replace('.', S);
                interfaceFileDir = new File(genDir + S + subDir);
            } else {
                interfaceFileDir = new File(genDir);
            }
            if(!interfaceFileDir.exists()) {
                interfaceFileDir.mkdirs();
            }
            File interfaceFile = new File(interfaceFileDir, interfaceFileName);
            if ( interfaceFile.exists() ) {
                System.out.println("Deleting existing java interface file: " + interfaceFile.getPath());
                interfaceFile.delete();
            }
            
            try {
                System.out.println("Writing java interface to file: " + interfaceFile.getPath());
                FileOutputStream fos = new FileOutputStream(interfaceFile);
                PrintStream ps = new PrintStream(fos, true, "UTF-8");
                xc.generate(ps);
                ps.flush();
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to save to types file specified:" + interfaceFile.getPath());
            }
        }
    }
    
    //        //generate(System.out); // by default we will print to stdout
    //    public List generate(PrintStream out) throws XsulWsdlCompilerException {
    //JDK5 List<XJavaClass>
    public void generate(List generatedArtifacts) throws XsulWsdlCompilerException {
        // for now compile first portType ???
        boolean found = false;
        for(Iterator portTypesIter = def.getPortTypes().iterator(); portTypesIter.hasNext(); ) {
            WsdlPortType pt = (WsdlPortType) portTypesIter.next();
            XJavaClass xclass = generateClassForPortType(pt); //def.getTargetNamespace()
            
            for(Iterator opIter =  pt.getOperations().iterator(); opIter.hasNext(); ) {
                WsdlPortTypeOperation op = (WsdlPortTypeOperation) opIter.next();
                XJavaMethod xm = generateMethodForOperation(xclass, op);
            }
            found = true;
            generatedArtifacts.add(xclass);
            //xclass.generate(out);
        }
        if(!found) {
            throw new XsulWsdlCompilerException(
                "no port type found in WSDL "+def.getTargetNamespace());
        }
    }
    
    
    protected XJavaMethod generateMethodForOperation(XJavaClass xclass, WsdlPortTypeOperation op) {
        // check validation that WSDL operation is doc/literal kind (uses element= ...)
        String opName = op.getOperationName();
        if(Character.isUpperCase(opName.charAt(0))) { //TODO more sophisticate "java-ization"
            opName = Character.toLowerCase(opName.charAt(0)) + opName.substring(1);
        }
        //input message is required
        WsdlMessage inputMessage;
        {
            WsdlPortTypeInput opInput = op.getInput();
            if ( opInput == null ) {
                throw new XsulWsdlCompilerException(
                    "operation input is required, missing for operation " + opName);
            }
            inputMessage = op.getInput().lookupMessage();
        }
        // output message may be not present
        WsdlMessage outputMessage = null;
        {
            WsdlPortTypeOutput opOutput = op.getOutput();
            if ( opOutput != null ) {
                outputMessage = op.getOutput().lookupMessage();
            }
        }
        WsdlMessagePart inputElementPart = requiredDocLiteralElement(inputMessage);
        WsdlMessagePart outputElementPart = null;
        if ( outputMessage != null ) {
            outputElementPart = requiredDocLiteralElement(outputMessage);
        }
        
        // now we will map input and iputput messages to parameters depending on binding
        return generateDocLiteralMethod(xclass, op, outputElementPart, inputElementPart);
    }
    
    protected XJavaMethod generateDocLiteralMethod(XJavaClass xclass, WsdlPortTypeOperation op,
                                                   WsdlMessagePart outputElementPart,
                                                   WsdlMessagePart inputElementPart) {
        XJavaMethod xm = xclass.addMethod(new XJavaMethod(xclass, op.getOperationName()));
        //TODO: generate comment that has QName of expected XML element
        xclass.addImport("org.xmlpull.v1.builder.XmlElement");
        xm.addArgument("XmlElement", "input");
        if ( outputElementPart != null ) {
            xm.setReturnType("XmlElement");
        } else {
            xm.setReturnType("void");
        }
        return xm;
    }
    
    protected XJavaClass generateClassForPortType(WsdlPortType pt) {
        String ns = pt.getDefinitions().getTargetNamespace();
        String pkg = mappings.mapNamespaceToJavaPackage(ns);
        QName portTypeQname = new QName(ns,pt.getPortTypeName());
        String javaClassName =  mappings.mapQNameToJavaName(portTypeQname);
        XJavaClass xclass = new XJavaClass(pkg, javaClassName, portTypeQname, true);
        return xclass;
    }
    
    private WsdlMessagePart requiredDocLiteralElement(WsdlMessage message) throws XsulWsdlCompilerException {
        Iterator i = message.getParts().iterator();
        WsdlMessagePart part;
        if ( i.hasNext() ) {
            part = (WsdlMessagePart) i.next();
            if ( part.getPartType() != null ) {
                throw new XsulWsdlCompilerException(
                    "Part type is not supported for message " + message.getMessageName());
            }
            if ( part.getPartElement() == null ) {
                throw new XsulWsdlCompilerException(
                    "Part element is required in " + message.getMessageName());
            }
        } else {
            throw new XsulWsdlCompilerException(
                "At least one part is required in message " + message.getMessageName());
        }
        if ( i.hasNext() ) {
            throw new XsulWsdlCompilerException("one part os required in message " + message.getName());
        }
        return part;
    }
    
    public void useMappings(XsulWsdlCompilerMappings mappings) {
        this.mappings = mappings;
    }
    
    //    /**
    //     * Sets name of generate java package
    //     *
    //     * @param    Pkg                 a  String
    //     */
    //    public void setJavaPackage(String pkg) {
    //        this.pkg = pkg;
    //    }
    //
    //    /**
    //     * Returns Pkg
    //     *
    //     * @return    a  String
    //     */
    //    public String getJavaPackage() {
    //        return pkg;
    //    }
    
    /**
     * Sets Def
     *
     * @param    Def                 a  WsdlDefinitions
     */
    public void setWsdlToUse(WsdlDefinitions def) {
        this.def = def;
    }
    
    /**
     * Returns Def
     *
     * @return    a  WsdlDefinitions
     */
    public WsdlDefinitions getWsdlToUse() {
        return def;
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


