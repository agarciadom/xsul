/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XmlNamingOverHttp.java,v 1.4 2004/03/16 11:28:14 aslom Exp $
 */

package xsul.xml_naming_over_http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;

/**
 * This class provides very simple to use client for naming service that
 * works over HTTP to associate XML documents with URLs.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/16 11:28:14 $ (GMT)
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XmlNamingOverHttp implements XmlNaming {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    
    //System properties
    //http.proxyHost=
    //http.proxyPort=
    //https.proxyHost=
    //https.proxyPort=
    
    //TODO: move to unit test ...
    public static void main(String[] args)
    {
        //
        XmlNaming naming = new XmlNamingOverHttp();
        //String url = "http://www.xmethods.com/sd/2001/BabelFishService.wsdl";
         String url = "http://portal.extreme.indiana.edu:8117/babelFishXMethods";
        XmlElement el = naming.lookup(url);
        // let see what we got - could be WSLD or WSA EPR ...
        System.out.println("url="+url+" got="+builder.serializeToString(el));
        //xmlns="http://schemas.xmlsoap.org/wsdl/"
        System.out.println(""+el.getName());
        System.out.println(""+el.getNamespace());
    }
    public XmlElement lookup(String url)  throws XmlNamingException
    {
        
        URLConnection connection;
        //HttpURLConnection connection; HttpURLConnection.setFollowRedirects(false);
        //HttpsURLConnection
        
        // open get conenction to host:port get path
        try {
            URL urlImpl = new URL(url);
            connection = urlImpl.openConnection();
        } catch (MalformedURLException e){
            throw new XmlNamingException("could not parse url "+url, e);
        } catch (IOException e) {
            throw new XmlNamingException("exception when connecting to url "+url, e);
        }
        final int BUF_SIZE = 4*1024;
        StringBuffer sb = new StringBuffer(BUF_SIZE); //JDK15 use unsync SB
        Reader r = null;
        try {
             r = new InputStreamReader(connection.getInputStream());
            char[] buf = new char[BUF_SIZE];
            int count;
            while ((count = r.read(buf)) > 0) {
                sb.append(buf, 0, count);
                int LIMIT = 50 * BUF_SIZE;
                if(sb.length() > LIMIT) {
                    throw new XmlNamingException(
                        "XML document pointed at "+url+" exceeded "+LIMIT+" bytes size");
                }
            }
        } catch (IOException e) {
            throw new XmlNamingException("exception when reading content from url "+url, e);
        } finally {
            try { if(r != null) { r.close();} } catch (IOException e) {}
        }
        // convert result String to XML doc
        XmlDocument doc = builder.parseReader(new StringReader(sb.toString()));
        return doc.getDocumentElement();
    }
    
    public void bind(String url, XmlElement xml)  throws XmlNamingException
    {
        // do POST to URL with content /path&xml=URLEncode.encode(serialized XML to string)
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






