/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WsdlResolver.java,v 1.5 2006/06/10 18:31:00 aslom Exp $
 */

package xsul.wsdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;


/**
 * Manages set of providers that can be used to execute WSDL based operations.
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsdlResolver {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static WsdlResolver instance = new WsdlResolver();
    protected Map ns2Wsdl = new HashMap();
    protected Map location2Wsdl = new HashMap();
    
    WsdlResolver() {
    }
    
    public static WsdlResolver getInstance() throws WsdlException {
        return instance;
    }
    
    
    public WsdlDefinitions loadWsdl(URI url)
        throws WsdlException
    {
        return loadWsdl(null, url, false);
    }
    
    public WsdlDefinitions loadWsdl(URI base, URI url)
        throws WsdlException
    {
        return loadWsdl(base, url, false);
    }
    
    public WsdlDefinitions loadWsdl(URI base, URI location, boolean useCache)
        throws WsdlException
    {
        
        if(useCache) {
            Object hit = location2Wsdl.get(location);
            if(hit != null) {
                //return (WsdlDefinitions) hit;
                return cloneWsdl((WsdlDefinitions) hit);
            }
        }
        
        WsdlDefinitions def;
        URL url;
        try {
            url = base != null ? new URL(base.toURL(), location.toASCIIString()) : location.toURL();
            URLConnection conn = url.openConnection();
            XmlDocument doc = builder.parseInputStream(conn.getInputStream(),
                                                       conn.getContentEncoding());
            def = new WsdlDefinitions(doc.getDocumentElement());
        } catch(XmlBuilderException e) {
            throw new WsdlException("could not load WSDL from "+location, e);
        } catch (IOException e) {
            throw new WsdlException("could not load WSDL from "+location, e);
        }
        location2Wsdl.put(location, def);
        ns2Wsdl.put(def.getTargetNamespace(), def);
        return def;
    }
    
    
    public WsdlDefinitions loadWsdlFromPath(Class classloader, String wsdlLoc)
    {
        URI base = ((new File(".")).toURI());
        URI wsdlLoclUri;
        try {
            wsdlLoclUri = new URI(wsdlLoc);
        } catch (URISyntaxException e) {
            throw new XsulException(
                "location of WSDL must be correct URI, could not parse '"+wsdlLoc+"'", e);
        }
        WsdlDefinitions def;
        try {
            def = loadWsdl(base, wsdlLoclUri);
        } catch(WsdlException we) {
            Throwable  t = we.getCause();
            if(t instanceof FileNotFoundException) {
                // let try loading WSDL from CLASSPATH
                try {
                    String locationOnClassapth = wsdlLoc;
                    if(wsdlLoc.charAt(0) != '/') {
                        locationOnClassapth = '/' + locationOnClassapth;
                    }
                    URL wsdlOnClassPath = classloader.getResource(locationOnClassapth);
                    if(wsdlOnClassPath != null) {
                        URI uri = URI.create(wsdlOnClassPath.toString());
                        def = WsdlResolver.getInstance().loadWsdl(null, uri);
                    } else {
                        throw we;
                    }
                } catch (WsdlException e) {
                    throw we;
                } catch(IllegalArgumentException e) {
                    throw we;
                }
            } else {
                throw we;
            }
        }
        return def;
    }
    
    
    public WsdlDefinitions resolveImport(String namespace, URI base, URI optionalLocation)
        throws WsdlException
    {
        Object hit = ns2Wsdl.get(namespace);
        if(hit != null) {
            return cloneWsdl((WsdlDefinitions)hit);
        }
        if(optionalLocation != null) {
            return loadWsdl(base, optionalLocation, true);
        }
        throw new WsdlException("could not resolve import for namespace "+namespace
                                    +" with location "+optionalLocation);
    }
    
    private WsdlDefinitions cloneWsdl(WsdlDefinitions wsdl) {
        try {
            WsdlDefinitions cloned = (WsdlDefinitions)(wsdl.clone());
            return cloned ;
        } catch (CloneNotSupportedException e) {
            String xml = "";
            try {
                xml = builder.serializeToString(wsdl);
            } catch (Exception e2) {
                logger.severe("could not get XML for wsdl "+wsdl.getTargetNamespace());
            }
            throw new XsulException("coul dnot clone wsdl "+xml, e);
        }
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getClass().getName()).append("={");
        buf.append("List of namespaces=");
        
        for (Iterator i = ns2Wsdl.keySet().iterator(); i.hasNext();)
        {
            buf.append(i.next());
            if(i.hasNext()){
                buf.append(",");
            }
        }
        buf.append("}");
        return buf.toString();
    }
}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University.
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

