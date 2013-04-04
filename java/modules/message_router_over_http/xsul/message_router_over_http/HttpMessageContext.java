/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpMessageContext.java,v 1.4 2005/01/18 09:44:34 aslom Exp $
 */

package xsul.message_router_over_http;

import org.xmlpull.v1.builder.XmlElement;
import xsul.MLogger;
import xsul.http_server.HttpServerRequest;
import xsul.message_router.MessageContext;

/**
 *
 *
 * @version $Revision: 1.4 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class HttpMessageContext extends MessageContext {
    private static final MLogger logger = MLogger.getLogger();
    
    //public final static String HTTP_INCOMING_URI =  "HttpIncomingPath";
    public final static String HTTP =  "http";
    public final static String PATH =  "path";
    // consider: creating Map unitl it is necessary .... ????
    
    
    public HttpMessageContext(HttpServerRequest req) {
        super(DIR_INCOMING); //special container element
        setHttpRequestPath(req.getPath());
    }
    
    public HttpMessageContext(XmlElement target) {
        super(target);
    }
    
    // TODO: split into header, URI, Host ....
    //    public HttpServerRequest getServerRequest() {
    //        return null; //TODO!!!
    //    }
    
    
    public String getHttpRequestPath() {
        XmlElement container = element(XSUL_CTX_NS, INCOMING, true).element(XSUL_CTX_NS, HTTP, true);
        container = container.element(XSUL_CTX_NS, PATH);
        if(container.hasChildren()) {
            return container.requiredTextContent();
        } else {
            return null;
        }
    }
    
    public void setHttpRequestPath(String path) {
        XmlElement el = element(XSUL_CTX_NS, INCOMING, true).element(XSUL_CTX_NS, HTTP, true)
            .element(XSUL_CTX_NS, PATH, true);
        el.removeAllChildren();
        el.addChild(path);
    }
    
    //    public String getHttpRequestHeaderValue(String headerName) { //??????????
    //        return null; //TODO!!!
    //    }
    
    //    public Map getHttpRequestHeaders() { //??????????
    //        return null; //TODO!!!
    //    }

    public String getHttpSoapAction() {
        throw new IllegalStateException("not implemented"); //return null; //TODO!!
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





