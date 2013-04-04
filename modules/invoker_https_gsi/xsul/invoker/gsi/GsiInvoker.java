/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: GsiInvoker.java,v 1.1 2006/04/18 19:13:01 aslom Exp $
 */


package xsul.invoker.gsi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.ptls.PureTLSContext;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import xsul.MLogger;
import xsul.XsulException;
import xsul.http_client.ClientSocketFactory;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientRequest;
import xsul.http_client.HttpClientResponse;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.http_common.HttpConstants;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.puretls_client_socket_factory.PuretlsClientSocketFactory;
import xsul.util.XsulUtil;

/**
 * @version $Revision: 1.1 $
 * @author Satoshi Shirasuna [mailto:sshirasu@cs.indiana.edu]
 * @author Alek
 */
public class GsiInvoker extends SoapHttpDynamicInfosetInvoker
{
    private MLogger logger = MLogger.getLogger();
    protected HttpClientConnectionManager connMgr;
    protected GlobusCredential globusCred = null;
    protected X509Certificate[] certs = null;
    
    public GsiInvoker(GSSCredential cred) throws XsulException
    {
        if (! (cred instanceof GlobusGSSCredentialImpl))
        {
            String error = "The credential is not a Globus credential. ";
            logger.severe(error);
            throw new XsulException(error);
        }
        
        globusCred = ( (GlobusGSSCredentialImpl) cred).getGlobusCredential();
        TrustedCertificates tc = TrustedCertificates.getDefaultTrustedCertificates();
        
        if (tc == null)
        {
            String error = "Trusted certificates is null. ";
            logger.severe(error);
            throw new XsulException(error);
        }
        
        certs = tc.getCertificates();
        PureTLSContext ctx = new PureTLSContext();
        try
        {
            ctx.setTrustedCertificates(certs);
            ctx.setCredential(globusCred);
        }
        catch (GeneralSecurityException e)
        {
            String error = "General security exception. " + e.getMessage();
            logger.severe(error, e);
            throw new XsulException(error);
        }
        
        ClientSocketFactory clientSocketFactory  = new PuretlsClientSocketFactory(ctx);
        connMgr = HttpClientReuseLastConnectionManager.newInstance(clientSocketFactory);
        setSecureConnectionManager(connMgr);
        setKeepAlive(true);
    }
    
    public String invokeHttpGet(String urlStr) throws XsulException
    {
        String respBody = invokeHttp(urlStr, "GET", null);
        return respBody;
    }
    
    public String invokeHttpPost(String urlStr, String[] names, String[] values) throws XsulException
    {
        String reqBody = "";
        for (int i = 0; i < names.length; i++)
        {
            if (i > 0)
            {
                reqBody += "&";
            }
            reqBody += names[i] + "=" + values[i]; //TODO: UrlEncode!!!!
        }
        
        String bodyResp = invokeHttp(urlStr, "POST", reqBody);
        return bodyResp;
    }
    
    private String invokeHttp(String urlStr, String method, String reqBody) throws XsulException
    {
        URL url = null;
        try
        {
            url = new URL(urlStr);
        }
        catch (MalformedURLException e)
        {
            String error = "Malformed URL. " + e.getMessage();
            logger.severe(error, e);
            throw new XsulException(error, e);
        }
        
        String host = url.getHost();
        int port = url.getPort();
        
        HttpClientRequest req;
        
        int timeout = 10 * 60 * 1000;
        req = connMgr.connect(host, port, timeout);
        req.setRequestLine(method, urlStr, "HTTP/1.0");
        req.ensureHeadersCapacity(2);
        req.setHeader("Keep-Alive", "300");
        req.setConnection("keep-alive");
        
        if ("POST".equals(method))
        {
            req.setContentType("application/x-www-form-urlencoded");
        }
        
        HttpClientResponse resp = req.sendHeaders();
        OutputStream out = req.getBodyOutputStream();
        try
        {
            if (reqBody != null)
            {
                OutputStreamWriter outWriter = new OutputStreamWriter(out);
                outWriter.write(reqBody);
                outWriter.close();
            }
            else
            {
                out.close();
            }
        }
        catch (IOException e)
        {
            String error = "I/O exception. " + e.getMessage();
            logger.severe(error, e);
            throw new XsulException(error, e);
        }
        
        resp.readStatusLine();
        resp.readHeaders();
        
        String respStatusCode = resp.getStatusCode();
        boolean successCode = respStatusCode.startsWith("20");
        if (!successCode)
        {
            String error = "The HTTP server returned the following error code. " + respStatusCode;
            logger.severe(error);
            throw new XsulException(error);
        }
        
        String body = null;
        InputStream in = resp.getBodyInputStream();
        byte[] streamAsByteArray = null;
        try
        {
            streamAsByteArray = xsul.util.XsulUtil.readInputStreamToByteArray(in);
        }
        catch (IOException e)
        {
            String error = "I/O exception. " + e.getMessage();
            logger.severe(error, e);
            throw new XsulException(error, e);
        }
        try
        {
            body = new String(streamAsByteArray, HttpConstants.ISO88591_CHARSET);
        }
        catch (UnsupportedEncodingException e)
        {
            String error = "Unsupported encoding exception. " + e.getMessage();
            logger.severe(error, e);
            throw new XsulException(error, e);
        }
        return body;
    }
    
    
    public XmlElement invokeSoap(String urlStr, XmlElement message)
    {
        //XsdTypeHandlerRegistry.getInstance(XBeansTypeHandlerRegistry.newInstance());
        SoapHttpDynamicInfosetInvoker invoker = new SoapHttpDynamicInfosetInvoker();
        invoker.setSecureConnectionManager(connMgr);
        invoker.setKeepAlive(true);
        invoker.setLocation(urlStr);
        logger.finer("Attempting to make a soap invocation on service at " + urlStr);
        XmlElement response = invoker.invokeMessage(message);
        logger.finest("Response message = "+XsulUtil.safeXmlToString(response));
        return response;
    }
    
    public XmlDocument invokeXml(XmlDocument request)
        throws DynamicInfosetInvokerException
    {
        XmlDocument response = super.invokeXml(request);
        return response;
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


