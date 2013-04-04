/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //- -----100-columns-wide------>*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XBeansDocumentInvoker.java,v 1.5 2004/03/25 07:17:06 aslom Exp $
 */

package xsul.xbeans_document_invoker;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.XsulException;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.soap11_util.Soap11Util;
import xsul.ws_addressing.WsaEndpointReference;

/**
 * Sends a XmlObject document to remote service and returns the resulting document
 * todo: add support for XmlElement
 *
 * @version $Revision: 1.5 $
 * @author Yogesh L. Simmhan [mailto:ysimmhan@cs.indiana.edu]
 */
public class XBeansDocumentInvoker extends SoapHttpDynamicInfosetInvoker {

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    protected static HashMap invokerMap = new HashMap();

    public static XBeansDocumentInvoker getInvoker(String locationUrl) throws URISyntaxException{

        return getInvoker(new WsaEndpointReference(new URI(locationUrl)));
    }

    public static XBeansDocumentInvoker getInvoker(WsaEndpointReference serviceEpr){

        String locationUrl = serviceEpr.getAddress().toString();
        XBeansDocumentInvoker invoker = (XBeansDocumentInvoker)invokerMap.get(locationUrl);

        if(invoker == null){
            invoker = new XBeansDocumentInvoker(locationUrl);
            invokerMap.put(locationUrl, invoker);
        }

        return invoker;
    }

    protected XBeansDocumentInvoker(String locationUrl){

        super(locationUrl);
    }

    public XmlObject invoke(XmlObject param) throws XsulException {

        String param1Xml = param.xmlText();
        XmlElement requestEl = builder.parseFragmentFromReader(new StringReader(param1Xml));

        XmlElement responseEl = super.invokeMessage(requestEl);

        processSoapFaultIfPresent(responseEl);
        String responseXml = builder.serializeToString(responseEl);
        try {

            XmlObject response = XmlObject.Factory.parse(responseXml);
            return response;

        } catch (XmlException e) {
            throw new XsulException("error parsing response for the request: " + param +
                                        "\nreceived : " + responseXml);
        }

    }

    protected void processSoapFaultIfPresent(XmlElement response)
        throws XmlBuilderException, XsulException
    {
        if("Fault".equals(response.getName())) {
            //ALEK: Leaky Abstraciton: now can process both SOAP 1.1 and 1.2 ...
            if(XmlConstants.NS_URI_SOAP11.equals(response.getNamespaceName())
                   || XmlConstants.NS_URI_SOAP12.equals(response.getNamespaceName()) )
            {
                // TODO extract faultcode + faultstring + detail etc.
                //StringWriter sw = new StringWriter();
                String s = builder.serializeToString(response);
                throw new XsulException("remote exception .... "+s);
            }
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


