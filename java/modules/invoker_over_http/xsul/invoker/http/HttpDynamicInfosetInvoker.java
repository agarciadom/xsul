/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*//*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpDynamicInfosetInvoker.java,v 1.22 2006/04/21 20:05:32 aslom Exp $
 */

package xsul.invoker.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.parser_pool.XmlPullParserPool;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulVersion;
import xsul.http_client.ClientSocketFactory;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientException;
import xsul.http_client.HttpClientRequest;
import xsul.http_client.HttpClientResponse;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.http_common.HttpConstants;
import xsul.invoker.DynamicInfosetInvoker;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.util.Utf8Reader;
import xsul.util.Utf8Writer;
import xsul.util.XsulUtil;

/**
 * This class allows to send XML to HTTP endpoint.
 * Simply set endpoint location and execute invoke*().
 *
 * @version $Revision: 1.22 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class HttpDynamicInfosetInvoker implements DynamicInfosetInvoker {
    private static final MLogger logger = MLogger.getLogger();
    //private static final MLogger xmlOutputTrace = MLogger.getLogger("trace.xsul.xml.invoker.out");
    //private static final MLogger xmlInputTrace = MLogger.getLogger("trace.xsul.xml.invoker.in");
    private static final XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private static final XmlPullParserPool pool = new XmlPullParserPool(builder.getFactory());
    private static int DEFAULT_TIMEOUT = 4 * 60 * 1000;
    public final static String HTTP_TRANSPORT_CLIENT_TIMEOUT_MS_PROPERTY =
        "http_transport.client.timeout.ms";
    private static boolean DEFAULT_KA = true;
    public final static String HTTP_TRANSPORT_CLIENT_DISABLE_KEEPALIVE_PROPERTY =
        "http_transport.client.disable.ka";
    
    
    private HttpClientConnectionManager connMgr;
    private HttpClientConnectionManager secureConnMgr;
    private String soapAcion = "";
    private String location;
    private String host;
    private int port;
    private String requestUri;
    private boolean keepAlive = DEFAULT_KA;
    private int timeout = DEFAULT_TIMEOUT;
    
    
    static {
        String disableKaVal = null;
        try {
            disableKaVal = (String) AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return System.getProperty(
                                HTTP_TRANSPORT_CLIENT_DISABLE_KEEPALIVE_PROPERTY);
                        }
                    });
        } catch(AccessControlException ace) {
            logger.severe(
                "could not read system property "+HTTP_TRANSPORT_CLIENT_DISABLE_KEEPALIVE_PROPERTY, ace);
        }
        if(disableKaVal != null) {
            DEFAULT_KA = false;
            logger.config("by default Keep-Alive will be disabled ("
                              +HTTP_TRANSPORT_CLIENT_DISABLE_KEEPALIVE_PROPERTY+")");
        }
        
        String timeoutVal = null;
        try {
            timeoutVal = (String) AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return System.getProperty(HTTP_TRANSPORT_CLIENT_TIMEOUT_MS_PROPERTY);
                        }
                    });
        } catch(AccessControlException ace) {
            logger.severe(
                "could not read system property "+HTTP_TRANSPORT_CLIENT_TIMEOUT_MS_PROPERTY, ace);
        }
        if(timeoutVal != null) {
            try {
                DEFAULT_TIMEOUT = Integer.parseInt(timeoutVal);
                logger.config(HTTP_TRANSPORT_CLIENT_TIMEOUT_MS_PROPERTY+"="+DEFAULT_TIMEOUT);
            } catch(Exception e) {
                logger.severe(
                    "user specified -D"+HTTP_TRANSPORT_CLIENT_TIMEOUT_MS_PROPERTY
                        +" is not integer value", e);
            }
        }
        
    }
    public HttpDynamicInfosetInvoker() {
        connMgr = HttpClientReuseLastConnectionManager.newInstance();
        init();
    }
    
    public HttpDynamicInfosetInvoker(ClientSocketFactory socketFactory) {
        //connMgr = HttpClientConnectionManager.newInstance(socketFactory);
        connMgr = HttpClientReuseLastConnectionManager.newInstance(socketFactory);
        init();
    }
    
    public HttpDynamicInfosetInvoker(HttpClientConnectionManager connMgr) {
        this.connMgr = connMgr;
        init();
    }
    
    public HttpDynamicInfosetInvoker(String locationUrl)
        throws DynamicInfosetInvokerException
    {
        this();
        setLocation(locationUrl);
    }
    
    private void init() {
        keepAlive = DEFAULT_KA;
        timeout = DEFAULT_TIMEOUT;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String url) {
        this.location = url;
        if(url.startsWith("http://")) {
        } else if(url.startsWith("https://")){
        } else {
            throw new IllegalArgumentException(
                "unsupported URL type (supported are http:// and https://): "+url);
        }
        URL u = null;
        try {
            u = new URL(url);
            host = u.getHost();
            port = u.getPort();
            if(port == -1) {
                if(url.startsWith("https://")) {
                    port = 443;
                } else {
                    port = 80;
                }
            }
            requestUri = u.getPath();
            if(requestUri == null || requestUri.length() == 0) {
                requestUri = "/";
            }
        } catch (MalformedURLException e) {
            throw new DynamicInfosetInvokerException("could not parse location "+url, e);
        }
        
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setSoapAction(String soapAction) {
        this.soapAcion = soapAction;
    }
    
    public void setKeepAlive(boolean enable) {
        this.keepAlive = enable;
    }
    
    public HttpClientConnectionManager getConnectionManager() {
        return connMgr;
    }
    
    public HttpClientConnectionManager getSecureConnectionManager() {
        return this.secureConnMgr;
    }
    
    public void setSecureConnectionManager(HttpClientConnectionManager secureConnMgr) {
        this.secureConnMgr = secureConnMgr;
    }
    
    protected XmlDocument buildResponseDocument(XmlPullParser pp) {
        return builder.parse(pp);
    }
    
    protected void serializeInvocationDocument(XmlDocument input, Writer writer) {
        //builder.serializeToWriter(input, utf8Writer);
        XmlSerializer ser = null;
        try {
            ser = builder.getFactory().newSerializer();
            ser.setOutput(writer);
            final String SERIALIZER_ATTVALUE_USE_APOSTROPHE =
                "http://xmlpull.org/v1/doc/features.html#serializer-attvalue-use-apostrophe";
            ser.setFeature(SERIALIZER_ATTVALUE_USE_APOSTROPHE, true);
        } catch (Exception e) {
            throw new XmlBuilderException("could not serialize node to writer", e);
        }
        serializeInvocationDocument(input, ser);
        try {
            ser.flush();
        } catch (IOException e) {
            throw new XmlBuilderException("could not flush output", e);
        }
    }
    
    protected void serializeInvocationDocument(XmlDocument input, XmlSerializer ser) {
        builder.serialize(input, ser);
    }
    
    /**
     * Invoke Web Service by sending XML document and returning back XML document.
     */
    public XmlDocument invokeXml(XmlDocument input) throws DynamicInfosetInvokerException
    {
        if(host == null) {
            throw new DynamicInfosetInvokerException("setLocation() must be called before invoking");
        }
        try {
            // open connection
            boolean secure = location.startsWith("https://");
            logger.finest("host="+host+" port="+port+" secure="+secure);
            
            timeout = DEFAULT_TIMEOUT;
            HttpClientRequest req;
            if(secure) {
                if(secureConnMgr == null) {
                    throw new DynamicInfosetInvokerException(
                        "secure connection manager must be set to allow https:// connection to "+location);
                }
                req = secureConnMgr.connect(host, port, timeout);
            } else {
                req = connMgr.connect(host, port, timeout);
            }
            //write first line
            req.setRequestLine("POST", requestUri, "HTTP/1.0");
            
            //write custom headers
            
            // Content-Length http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13
            // Host http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.23
            
            
            
            //RFC 2068 HTTP 1.0 KA compatibility http://www.freesoft.org/CIE/RFC/2068/248.htm
            // Connection http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.10
            // 19.6.1.1 Changes to Simplify Multi-homed Web Servers and Conserve IP
            // 19.6.2 Compatibility with HTTP/1.0 Persistent Connections
            //   http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.6.1
            req.ensureHeadersCapacity(2); //action + keep-alive
            if(keepAlive) {
                //      Keep-Alive: 300
                //      Connection: keep-alive
                req.setHeader("Keep-Alive", "300");
                req.setConnection("keep-alive");
            } else {
                req.setConnection("close");
            }
            req.setContentType("text/xml; charset=utf-8");
            //http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43
            req.setUserAgent(XsulVersion.getUserAgent());
            if(soapAcion != null) {
                req.setHeader("SOAPAction", "\""+soapAcion+"\"");
            }
            HttpClientResponse resp = req.sendHeaders();
            
            //write body using UTF8 Writer sooutput is UTF8 encoded
            input.setCharacterEncodingScheme("utf-8"); //force toprintXML epilog indicating use of UTF8
            OutputStream out = req.getBodyOutputStream();
            Writer utf8Writer = new Utf8Writer(out, 8*1024);
            //Writer writer = new BufferedWriter(new Utf8Writer(out, 8*1024), );
            
            serializeInvocationDocument(input, utf8Writer);
            
            //UNCOMMENT FOR EXTRA TRACING !!!
            //      if(false == xmlOutTracer.isFinestEnabled()) {
            //                //TODO use own XmlSerializer to make it faster (if set by user) as can be reused!!!
            //                //builder.serializeToWriter(input, utf8Writer);
            //                serializeInvocationDocument(input, utf8Writer);
            //            } else {
            //                StringWriter sw = new StringWriter();
            //                //builder.serializeToWriter(input, sw);
            //                serializeInvocationDocument(input, sw);
            //                sw.close();
            //                String s = sw.toString();
            //                xmlOutTracer.finest("soap message in request: (str len="+s.length()+") ---\n"+s+"---");
            //                utf8Writer.write(s);
            //            }
            
            utf8Writer.close();
            //req.close();
            
            
            // read staus typical HTTP/1.0 200 OK
            // read Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
            // http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1
            resp.readStatusLine();
            
            //try {
            // read headers
            resp.readHeaders();
            //} catch(Exception e) {
            //}
            String contentType = resp.getContentType();
            int contentLength = resp.getContentLength();
            
            boolean contentLooksXml = (contentType != null && contentType.indexOf("xml") != -1);
            String respStatusCode = resp.getStatusCode();
            //boolean successCode = "200".equals(respStatusCode))
            //    || "202".equals(respStatusCode);
            // 20x codes: http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5
            boolean successCode = respStatusCode.startsWith("20");
            if(contentLength == -1 && !"200".equals(respStatusCode)) {
                contentLength = 0;
            }
            if(!successCode || (contentLength != 0 && !contentLooksXml)) {
                //try read possibly HTML output as it give more error background!
                String headers = "";
                String body = "";
                try {
                    //resp.readHeaders();
                    
                    // read body -- Reader
                    InputStream in = resp.getBodyInputStream();
                    byte[] streamAsByteArray = XsulUtil.readInputStreamToByteArray(in);
                    body = "\n"+new String(streamAsByteArray, HttpConstants.ISO88591_CHARSET);
                    if("500".equals(resp.getStatusCode())
                           && streamAsByteArray.length > 0
                           && contentLooksXml)
                    {
                        // SOAP fasults may be returned as HTTP 500 code so try to handle it
                        // in the worst case we just throw exception and fall down to have transport exception
                        InputStream bais = new ByteArrayInputStream( streamAsByteArray );
                        Reader reader = new Utf8Reader(bais, 1024);
                        XmlDocument output = builder.parseReader(reader);
                        return output;
                    }
                    
                    
                } catch(Exception e) {
                    //ignore as we already had exception when HTTP status is not OK...
                }
                throw new DynamicInfosetInvokerException(
                    "HTTP server error "+resp.getStatusCode()+" "+resp.getReasonPhrase()+" body=\""
                        +XsulUtil.printable(headers+body, false, false)+"\"");
                //                  throw new DynamicInfosetInvokerException(
                //                      "invocation failed - did not get XML response"
                //                          +" HTTP error "+resp.getStatusCode()+" "+resp.getReasonPhrase()+" with content "+
                //                          Util.printable(new String(streamAsByteArray, HttpConstants.ISO88591_CHARSET), false));
                //resp.getHttpVersion();
            }
            
            
            // read body -- Reader
            InputStream in = null;
            
            if(contentLength != 0) {
                in = resp.getBodyInputStream();
            }
            
            //UNCOMMENT FOR EXTRA TRACING !!!
            //            if(xmlInTracer.isFinestEnabled()) {
            //                if(contentLength != 0) {
            //                    try {
            //                        //NOTE: reading whole input into byte array is ONLY good for debugging!!!!
            //                        byte[] streamAsByteArray = Util.readInputStreamToByteArray(in);
            //
            //                        //TODO add escaping of output when characters <32 (excpt \r\n\t) and  >128...
            //                        xmlInTracer.finest("received HTTP message body=---\n"+new String(streamAsByteArray)+"---");
            //
            //                        in.close();
            //
            //                        //NOTE: replaced original stream with already read octets
            //                        in = new ByteArrayInputStream(streamAsByteArray);
            //
            //                    } catch(IOException ioe) {
            //                        throw new DynamicInfosetInvokerException("could not read response:"+ ioe, ioe);
            //                    }
            //                } else {
            //                    xmlInTracer.finest("received HTTP message without body");
            //                }
            //            }
            
            XmlDocument output = null;
            if(contentLength != 0) {
                
                // retrieve encoding
                String enc = "UTF8"; //TODO parse and retrieve from contentType
                Reader reader;
                if("utf8".equals(enc.toLowerCase()) ) {
                    reader = new Utf8Reader(in, 8*1024);
                } else {
                    throw new DynamicInfosetInvokerException("unsupported encoding "+enc);
                }
                
                //output = builder.parseReader(reader);
                
                XmlPullParser pp;
                try {
                    pp = pool.getPullParserFromPool();
                } catch(XmlPullParserException e) {
                    throw new HttpClientException("could not get XML pull parser from the pool", e);
                }
                try {
                    pp.setInput(reader);
                } catch(XmlPullParserException e) {
                    throw new HttpClientException("could not set parser input", e);
                }
                try {
                    //output = builder.parse(pp);
                    output = buildResponseDocument(pp);
                } finally {
                    pool.returnPullParserToPool(pp);
                }
                
                
                reader.close();
            } else {
                resp.getBodyInputStream().close();
            }

            return output;
            
        } catch(HttpClientException ex) {
            if(ex.getDetail() != null) {
                throw new DynamicInfosetInvokerException("HTTP related exception: "+ex.getMessage(), ex.getDetail());
            } else {
                throw new DynamicInfosetInvokerException("HTTP related exception", ex);
            }
        } catch(IOException ex) {
            throw new DynamicInfosetInvokerException("Invoker had IO exception:"+ex, ex);
        }
        
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





