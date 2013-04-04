/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package xsul.xwsdl_compiler_xbeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.impl.tool.SchemaCompilerExtension;
import xsul.MLogger;
import xsul.XsulVersion;
import xsul.xwsdl_compiler.XsulWsdlCompiler;
import xsul.xwsdl_compiler.XsulWsdlCompilerMappings;

/**
 * This code is based on
 * <a href="http://marc.theaimsgroup.com/?l=xmlbeans-dev&m=109537770023934&w=2">
 * an example provided by Dave Remy to the mailing list</a>
 * and was modified by Aleksander Slominski to use XSUL::XWSDLC
 */
public class XwsdlCodeGeneratorExtension implements SchemaCompilerExtension {
    private final static MLogger logger = MLogger.getLogger();
    public static final String PARAM_WSDL_ = "wsdlFile";
    public static final String PARAM_MAPPING_ = "mappingFile";
    public static final String PARAM_WSDL_GEN_DIR = "wsdlgendir";
    
    public void schemaCompilerExtension(SchemaTypeSystem schemaTypeSystem, Map parms) {
        
        //final String pkgName = (String) parms.get(PARAM_WSDL_PKG);
        //if ( pkgName == null ) {
        //    throw new IllegalArgumentException("Missing required parameter " + PARAM_WSDL_PKG);
        //}
        
        final String genDir = (String) parms.get(PARAM_WSDL_GEN_DIR);
        if ( genDir == null ) {
            throw new IllegalArgumentException("Missing required parameter " + PARAM_WSDL_GEN_DIR);
        }
        
        XsulWsdlCompilerMappings mappings = loadMappings(parms);
        
        XsulWsdlCompiler xwsdlc = new XsulWsdlCompilerWithXmlBeans(schemaTypeSystem);
        //xwsdlc.setJavaPackage(pkgName);
        xwsdlc.useMappings(mappings);
        List generatedArtifacts = new ArrayList();
        int count = 1;
        while(true) {
            
            final String wsdlLoc = (String) parms.get(PARAM_WSDL_ + "" + count);
            if ( wsdlLoc == null ) {
                if(count == 1) {
                    throw new IllegalArgumentException("Internal problem: missing " + PARAM_WSDL_);
                } else {
                    break;
                }
            }
            ++count;
            
            try {
                xwsdlc.useWsdlFromLocation(wsdlLoc);
                //PrintStream out = System.out;
                xwsdlc.generate(generatedArtifacts);
            } catch (Exception e) {
                String msg = "Could not compile WSDL from " + wsdlLoc + " : " + e;
                logger.config(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
        xwsdlc.generateFiles(generatedArtifacts, genDir);
    }
    
    private XsulWsdlCompilerMappings loadMappings(Map parms) {
        XsulWsdlCompilerMappings mappings = new XsulWsdlCompilerMappings();
        int count = 1;
        while(true) {
            
            final String mappingLoc = (String) parms.get(PARAM_MAPPING_ + "" + count);
            if ( mappingLoc == null ) {
                break;
            }
            
            try {
                System.out.println("mapping"+count+"="+mappingLoc);
                mappings.processMappings(mappingLoc);
            } catch (Exception e) {
                //e.printStackTrace();
                throw new RuntimeException("Could not load mappings from " + mappingLoc + " : " + e, e);
            }
            ++count;
        }
        return mappings;
    }
    
    
    public String getExtensionName() {
        return "XWSDLC Extension (XSUL "+XsulVersion.IMPL_VERSION+")";
    }
    
}

