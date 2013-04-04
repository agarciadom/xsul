/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: PasswordInvokerHandler.java,v 1.3 2005/01/31 08:36:00 aslom Exp $
 */
package simplesoap.client.client_security_handler;


import java.util.Iterator;
import org.xmlpull.v1.builder.XmlElement;
import simplesoap.contract.CommonConstants;
import xsul.MLogger;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.message_router.MessageContext;
import xsul.passwd.PasswordEnforcer;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlUtil;
import xsul.xhandler.BaseHandler;
import xsul.xhandler.XHandlerContext;


/**
 * If target servicerequires password and password was provided by user as WS-UserName token.
 *
 * $Id: PasswordInvokerHandler.java,v 1.3 2005/01/31 08:36:00 aslom Exp $
 */
public class PasswordInvokerHandler extends BaseHandler {
    public final static String FEATURE_PASSWORD = CommonConstants.FEATURE_PASSWORD;
    private final static MLogger logger = MLogger.getLogger();
    private boolean handlerDisabled = true;
    private String uid;
    private String passwd;
    
    public PasswordInvokerHandler(String name) {
        super(name);
    }
    
    public void init(XHandlerContext handlerConfig) {
        super.init(handlerConfig);
        boolean authenRequired = false;
        
        WsdlPort port = handlerConfig.getWsdlPort();
        for(Iterator i = port.elements(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL).iterator();i.hasNext();)
        {
            XmlElement featureEl = (XmlElement) i.next();
            String uri = featureEl.getAttributeValue(null, WsdlUtil.URI_ATTR);
            if(FEATURE_PASSWORD.equals(uri)) {
                authenRequired = true;
            }
        }
        if(authenRequired) {
            String authen = System.getProperty("authen");
            if(authen == null) {
                throw new PasswordConfigurationRequiredException("missing -Dauthen=...");
            }
            int colon = authen.indexOf(':');
            uid = authen.substring(0, colon);
            passwd = authen.substring(colon+1);
            logger.finest("using uid: " + uid + " passwd: " + passwd);
            handlerDisabled = false;
        }
        logger.finest("handlerDisabled="+handlerDisabled);
    }
    
    
    
    
    public boolean processOutgoingXml(XmlElement message, MessageContext context)
        throws DynamicInfosetInvokerException
    {
        if(handlerDisabled) return false; //do nothing
        
        PasswordEnforcer pe = new PasswordEnforcer(uid, passwd.getBytes());
        pe.enforceSoapMessage(message);
        //return super.invokeXml(request);
        return false;
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

