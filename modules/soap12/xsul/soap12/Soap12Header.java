/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Soap12Header.java,v 1.2 2004/03/16 11:28:13 aslom Exp $
 */

package xsul.soap12;

import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.XmlConstants;

/**
 * SOAP 1.1 Header eii wrapper - adds handy methods.
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class Soap12Header extends XmlElementAdapter {
    protected final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public Soap12Header(XmlElementAdapter target) {
        super(target);
    }
    
    
}

