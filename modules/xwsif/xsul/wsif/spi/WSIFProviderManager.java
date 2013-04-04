/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: WSIFProviderManager.java,v 1.4 2006/04/18 19:22:59 aslom Exp $
 */

package xsul.wsif.spi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.wsdl.WsdlPort;
import xsul.wsif.WSIFException;
import xsul.wsif.WSIFPort;


/**
 * Manages set of providers that can be used to execute WSDL based operations.
 *
 * @version $Revision: 1.4 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WSIFProviderManager {
    
    private static WSIFProviderManager instance = new WSIFProviderManager();
    private List providers = new ArrayList(8);
    
    WSIFProviderManager() {
    }
    
    public static WSIFProviderManager getInstance() throws WSIFException {
        return instance;
    }
    
    public void addProvider(WSIFProvider provider) {
        if(provider == null) throw new IllegalArgumentException();
        if(!providers.contains(provider)) {
            //providers.add(provider);
            providers.add(0, provider);
        }
    }
    
    public WSIFPort createDynamicWSIFPort(WsdlPort port,
                                          TypeHandlerRegistry typeMap)
        throws WSIFException
    {
        List problems = new ArrayList(providers.size());
        for (int i = 0; i < providers.size(); i++)
        {
            try {
                WSIFProvider provider = (WSIFProvider) providers.get(i);
                WSIFPort dynPort = provider.createDynamicWSIFPort(port, typeMap);
                if(dynPort != null) {
                    return dynPort;
                }
            } catch(WSIFException e) {
                problems.add(e);
            }
        }
        // create nice description of problems
        StringBuffer b = new StringBuffer();
        for(int i = 0; i < problems.size(); i++) {
            WSIFException e = (WSIFException) problems.get(i);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            pw.close();
            String s = sw.toString();
            b.append(s);
        }
        String msg = "";
        if(b.length() > 0) {
            msg="("+b+")";
        }
        throw new WSIFException("no provider could be found for WSDL port "+port+msg);
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

