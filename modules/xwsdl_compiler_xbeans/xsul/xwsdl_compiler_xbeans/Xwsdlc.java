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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.xmlbeans.impl.tool.Extension;
import org.apache.xmlbeans.impl.tool.XMLBean;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.xwsdl_compiler.XsulWsdlCompiler;
import xsul.xwsdl_compiler.XsulWsdlCompilerException;
import xsul.xwsdl_compiler.XsulWsdlCompilerMappings;

/**
 * Modeled after Ant's javac and zip tasks.
 *
 * Schema files to process, or directories of schema files, are set with the 'schema'
 * attribute, and can be filtered with 'includes' and 'excludes'.
 * Alternatively, one or more nested &lt;fileset&gt; elements can specify the
 * files and directories to be used to generate this XMLBean.
 * The include set can also define .java files that should be built as well.
 * See the FileSet documentation at http://jakarta.apache.org/ant/manual/index.html
 * for instructions on FileSets if you are unfamiliar with their usage.
 */

public class Xwsdlc extends XMLBean {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private File wsdlGenDir;
    //private String wsdlPkg;
    private String binding;
    
    private ArrayList   schemas = new ArrayList();
    
    private HashMap     _extRouter = new HashMap(5);
    
    private static final String WSDL = ".wsdl";
    private static final String XSDCONFIG = ".xsdconfig";
    private static final String WSDLCONFIG = ".wsdlconfig";
    
    
    public void execute() throws BuildException {
        
        //required
        if ( schemas.size() == 0
                && getSchema() == null
                && fileset.getDir(project) == null ) {
            String msg = "The 'schema' or 'dir' attribute or a nested fileset is required.";
            if ( isFailonerror() )
                throw new BuildException(msg);
            else {
                log(msg, Project.MSG_ERR);
                return;
            }
        }
        
        _extRouter.put(WSDL, new HashSet());
        _extRouter.put(XSDCONFIG, new HashSet());
        _extRouter.put(WSDLCONFIG, new HashSet());
        
        File theBasedir = getSchema();
        
        if ( getSchema() != null ) {
            if ( getSchema().isDirectory() ) {
                FileScanner scanner = getDirectoryScanner(getSchema());
                String[] paths = scanner.getIncludedFiles();
                processPaths(paths, scanner.getBasedir());
            } else {
                theBasedir = getSchema().getParentFile();
                processPaths(new String[] { getSchema().getName() }, theBasedir);
            }
        }
        
        if ( fileset.getDir(project) != null ) {
            schemas.add(fileset);
        }
        
        Iterator si = schemas.iterator();
        while ( si.hasNext() ) {
            FileSet fs = (FileSet) si.next();
            FileScanner scanner = fs.getDirectoryScanner(project);
            File basedir = scanner.getBasedir();
            String[] paths = scanner.getIncludedFiles();
            processPaths(paths, basedir);
        }
        
        Set wsdlList = (Set) _extRouter.get(WSDL);
        
        if ( wsdlList.size() == 0 ) {
            log("Could not find any wsdl files to process.", Project.MSG_WARN);
            return;
        }
        
        Set xsdConfigList = (Set) _extRouter.get(XSDCONFIG);
        Set wsdlConfigList = (Set) _extRouter.get(WSDLCONFIG);
        
        List mappingFiles = new ArrayList();
        if ( xsdConfigList.size() > 0 || wsdlConfigList.size() > 0) {
            int count = 0;
            File[] xcArray = (File[]) xsdConfigList.toArray(new File[xsdConfigList.size()]);
            for ( int i = 0; i < xcArray.length; i++ ) {
                String fileName = xcArray[ i ].toString();
                System.out.println("mapping"+(++count)+"=" + fileName);
                mappingFiles.add(fileName);
            }
            File[] wcArray = (File[]) wsdlConfigList.toArray(new File[wsdlConfigList.size()]);
            for ( int i = 0; i < wcArray.length; i++ ) {
                String fileName = wcArray[ i ].toString();
                System.out.println("mapping"+(++count)+"=" + fileName);
                mappingFiles.add(fileName);
            }
        }
        
        
        
        //if ( wsdlPkg == null ) {
        //    throw new BuildException(XwsdlCodeGeneratorExtension.PARAM_WSDL_PKG+" is required");
        //}
        
        if ( wsdlGenDir == null ) {
            throw new BuildException(XwsdlCodeGeneratorExtension.PARAM_WSDL_GEN_DIR+" is required");
        }
        
        long startMs = System.currentTimeMillis();
        if(getBinding() == null
               || XsulWsdlCompilerWithXmlBeans.BINDING_XMLBEANS.equals(getBinding()))
        {
            Extension e = createExtension();
            e.setClassName(XwsdlCodeGeneratorExtension.class);
            
            int size = wsdlList.size();
            File[] wsdlArray = (File[]) wsdlList.toArray(new File[size]);
            for ( int i = 0; i < wsdlArray.length; i++ ) {
                int count = ( i + 1 );
                File f = wsdlArray[i];
                System.out.println("wsdl"+count+"=" + f);
                Extension.Param param = e.createParam();
                param.setName(XwsdlCodeGeneratorExtension.PARAM_WSDL_ + "" + count);
                param.setValue(wsdlArray[i].toURI().toString());
            }
            
            for ( int i = 0; i < mappingFiles.size(); i++ ) {
                int count = ( i + 1 );
                String mappingfileName = (String) mappingFiles.get(i);
                Extension.Param param = e.createParam();
                param.setName(XwsdlCodeGeneratorExtension.PARAM_MAPPING_+ "" + count);
                param.setValue(mappingfileName);
            }
            
            //            Extension.Param paramWsdlPkg = e.createParam();
            //            paramWsdlPkg.setName(XwsdlCodeGeneratorExtension.PARAM_WSDL_PKG);
            //            paramWsdlPkg.setValue(wsdlPkg);
            
            Extension.Param paramWsdlGenDir = e.createParam();
            paramWsdlGenDir.setName(XwsdlCodeGeneratorExtension.PARAM_WSDL_GEN_DIR);
            paramWsdlGenDir.setValue(wsdlGenDir.getAbsolutePath());
            
            //System.out.println("Hello Mom");
            super.execute(); //default
            //System.out.println("Hello Dad");
        } else if(XsulWsdlCompilerWithXmlBeans.BINDING_XPP3.equals(getBinding())) {
            
            XsulWsdlCompilerMappings mappings = new XsulWsdlCompilerMappings();
            if(mappingFiles.size() > 0) {
                try {
                    mappings.processListOfMappings(mappingFiles);
                } catch (XsulWsdlCompilerException e) {
                    log("Loading of mappings failed: "+e, Project.MSG_WARN);
                }
            }
            
            XsulWsdlCompiler xwsdlc = new XsulWsdlCompiler();
            xwsdlc.useMappings(mappings);
            List generatedArtifacts = new ArrayList();
            //xwsdlc.setJavaPackage(wsdlPkg);
            
            int size = wsdlList.size();
            File[] wsdlArray = (File[]) wsdlList.toArray(new File[size]);
            int count = 0;
            for ( int i = 0; i < wsdlArray.length; i++ ) {
                File wsdlLoc = wsdlArray[ i ];
                String wsdlUrl = wsdlLoc.toURI().toString();
                System.out.println("directly processing wsdl"+(++count)+" "+wsdlUrl);
                xwsdlc.useWsdlFromLocation(wsdlUrl);
                xwsdlc.generate(generatedArtifacts);
            }
            xwsdlc.generateFiles(generatedArtifacts, wsdlGenDir.toString());
        } else {
            throw new BuildException("Unknown binding type "+getBinding());
        }
        long endMs = System.currentTimeMillis();
        double duration = (double)(endMs - startMs)/1000.0;
        System.out.println("Finished in "+duration+" seconds.");
    }
    
    private void processPaths(String[] paths, File baseDir) {
        for ( int i = 0; i < paths.length; i++ ) {
            int dot = paths[i].lastIndexOf('.');
            if ( dot > -1 ) {
                String path = paths[i];
                String possExt = path.substring(dot).toLowerCase();
                Set set = (Set) _extRouter.get(possExt);
                
                if ( set != null )
                    set.add(new File(baseDir, path));
            }
        }
    }
    
    public void addFileset(FileSet fileset) {
        super.addFileset(fileset);
        schemas.add(fileset);
    }
    
    public File getWsdlgendir() {
        return this.wsdlGenDir;
    }
    
    public void setWsdlgendir(File wsdlgendir) {
        this.wsdlGenDir = wsdlgendir;
    }
    
    //    /**
    //     * Sets WsdlPkg
    //     *
    //     * @param    WsdlPkg             a  String
    //     */
    //    public void setWsdlpkg(String wsdlPkg) {
    //        this.wsdlPkg = wsdlPkg;
    //    }
    //
    //    /**
    //     * Returns WsdlPkg
    //     *
    //     * @return    a  String
    //     */
    //    public String getWsdlpkg() {
    //        return wsdlPkg;
    //    }
    //
    
    public void setBinding(String binding) {
        this.binding = binding;
    }
    
    public String getBinding() {
        return binding;
    }
    
    
}

