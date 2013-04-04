/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SoapUtil.java,v 1.4 2005/01/31 08:35:59 aslom Exp $
 */

package xsul.soap;

import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XsulException;

/**
 * Set of typical SOAP operations on XML Infoset regardless of SOAP version.
 *
 * @version $Revision: 1.4 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public abstract class SoapUtil {
    

    public static SoapUtil selectSoapFragrance(XmlContainer xmlContainer, SoapUtil[] soapFragrances)
        throws UnsupportedSoapVersion
    {
        if(xmlContainer == null) throw new IllegalArgumentException();
        if(xmlContainer instanceof XmlElement) {
            return selectSoapFragrance((XmlElement)xmlContainer, soapFragrances);
        } else if(xmlContainer instanceof XmlDocument) {
            return selectSoapFragrance(((XmlDocument)xmlContainer).getDocumentElement(), soapFragrances);
        } else {
            throw new UnsupportedSoapVersion("expected "+XmlElement.class+" or "+XmlDocument.class+
                                            "not "+xmlContainer.getClass()+" for "+xmlContainer);
        }
    }
    
    /**
     * Try to match XML message root element agains list of provided SOAP versions.
     * Return matched SoapUtil or throws UnsupportedSoapVersion if no match could be found.
     *
     * @param    documentRoot        a  XmlElement message root
     * @param    soapFragrances      a  SoapUtil[] list of utility classes for SOAP versions
     *
     * @return   a SoapUtil selected SOAP version
     *
     * @exception   UnsupportedSoapVersion
     *
     */
    public static SoapUtil selectSoapFragrance(XmlElement documentRoot, SoapUtil[] soapFragrances)
        throws UnsupportedSoapVersion
    {
        
        if(soapFragrances == null || soapFragrances.length == 0) {
            throw new IllegalArgumentException("at least one SOAP version is required");
        }
        SoapUtil soapFragrance = null;
        for (int i = 0; i < soapFragrances.length; i++)
        {
            if(soapFragrances[i].isSoapEnvelopeSupported(documentRoot)){
                soapFragrance = soapFragrances[i];
                break;
            }
        }
        
        if (soapFragrance == null)
        {
            StringBuffer versions = new StringBuffer("");
            for (int i = 0; i < soapFragrances.length; i++)
            {
                if(i > 0) {
                    if(i == soapFragrances.length - 1) {
                        versions.append(" and ");
                    } else {
                        versions.append(", ");
                    }
                }
                versions.append(soapFragrances[i].getSoapVersion());
            }
            throw new UnsupportedSoapVersion("unsupported version of SOAP in XML input {"
                                                 +documentRoot.getName()+"}"+documentRoot.getName()
                                                 +", supproted "+versions.toString());
        } else {
            return soapFragrance;
        }
    }
    
    public abstract XmlElement generateSoapFault(XmlNamespace faultCodeValueNs,
                                                 String faultCodeValueName,
                                                 String reasonTextEnglish,
                                                 Throwable ex)
        throws XsulException;
    
    public abstract XmlElement generateSoapClientFault(String reasonTextEnglish,
                                                       Throwable ex)
        throws XsulException;
    
    
    public abstract XmlElement generateSoapServerFault(String reasonTextEnglish,
                                                       Throwable ex)
        throws XsulException;
    
    public abstract String getSoapVersion();
    
    public abstract boolean isSoapEnvelopeSupported(XmlElement root);
        
    public abstract XmlElement requiredBodyContent(XmlDocument respDoc)
        throws XsulException;
    
    public abstract XmlElement requiredBodyContent(XmlElement envelope)
        throws XsulException;
    
    public abstract XmlDocument wrapBodyContent(XmlElement bodyContent)
        throws XsulException;
    
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





