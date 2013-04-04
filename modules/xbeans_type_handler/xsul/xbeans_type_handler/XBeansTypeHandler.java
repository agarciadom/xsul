/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //- -----100-columns-wide------>*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XBeansTypeHandler.java,v 1.4 2004/04/25 20:44:20 aslom Exp $
 */

package xsul.xbeans_type_handler;

import java.io.StringReader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.type_handler.TypeHandler;
import xsul.type_handler.TypeHandlerException;

/**
 *
 *
 * @version $Revision: 1.4 $
 * @author Yogesh L. Simmhan [mailto:ysimmhan@cs.indiana.edu]
 */
public class XBeansTypeHandler implements TypeHandler {

    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    public Object xmlToJava(Object xmlParam) throws TypeHandlerException {

        if(!(xmlParam instanceof XmlElement)){
            throw new TypeHandlerException("Cannot convert object other than XmlElement " +
                                               "to XmlObject");
        }

        XmlElement xmlElt = (XmlElement)xmlParam;
        String xmlAsString = builder.serializeToString(xmlElt);
        try {

            //System.out.println("xml2java: " + xmlAsString);
            XmlObject xmlObj = XmlObject.Factory.parse(xmlAsString);
            return xmlObj;

        } catch (XmlException e) {
            throw new TypeHandlerException("Error parsing xml to XmlObject: " +
                                               xmlAsString, e);
        }
    }

    public Object javaToXml(Object javaParam) throws TypeHandlerException {

        if(!(javaParam instanceof XmlObject)){
            throw new TypeHandlerException("Cannot convert object other than XmlObject or subclass " +
                                               "to XmlElement");
        }

        XmlObject xmlObj = (XmlObject)javaParam;
        String xmlAsString = xmlObj.xmlText();
        //System.out.println("java2xml: " + xmlAsString);
        XmlElement el = builder.parseFragmentFromReader(new StringReader(xmlAsString));
        return el;
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



