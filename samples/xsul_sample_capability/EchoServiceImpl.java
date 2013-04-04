/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoServiceImpl.java,v 1.1 2004/03/06 21:15:25 aslom Exp $
 */

package xsul_sample_capability;

//import org.gjt.xpp.XmlNode;
import java.rmi.RemoteException;

public class EchoServiceImpl implements EchoService {

    public String sayHello(String name) throws RemoteException {
        return "Hello, "+name;
    }

}



