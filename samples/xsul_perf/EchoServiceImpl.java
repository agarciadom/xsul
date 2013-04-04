/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoServiceImpl.java,v 1.2 2004/03/02 09:24:56 aslom Exp $
 */

package xsul_perf;
import xsul.MLogger;



/**
 * Very simple service implementation.
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */


public class EchoServiceImpl implements EchoService {
    private final static MLogger logger = MLogger.getLogger();
    
    private String serviceName;
    
    EchoServiceImpl(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String echo(String s) throws Exception
    {
        logger.fine("got call "+s);
        return s;
        //return "Hello to "+name+" from "+serviceName;
    }
    
}



