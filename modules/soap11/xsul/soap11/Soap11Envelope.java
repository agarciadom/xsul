/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Soap11Envelope.java,v 1.2 2004/03/16 11:28:12 aslom Exp $
 */

package xsul.soap11;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.DataValidation;
import xsul.DataValidationException;
import xsul.XmlConstants;

/**
 * SOAP 1.1 envelope element wrapper - adds handy methods to extract headers, validate etc.
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class Soap11Envelope extends XmlElementAdapter implements DataValidation {
    
    protected final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    public Soap11Envelope(XmlElement el) {
        super(el);
        validateData();
    }

    public Soap11Envelope(XmlDocument doc) {
        super(doc.getDocumentElement());
        validateData();
    }
    
    public Soap11Header getHeader() {
        return null; //TODO !!!!
    }
    
    public void validateData() throws DataValidationException {
        // check root element is on SOAP 1.1 namespace
        // TODO: check it has required S:Body ...
    }
    

}

