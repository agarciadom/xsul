/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoService.java,v 1.1 2004/03/06 21:15:25 aslom Exp $
 */

package xsul_sample_capability;

import java.rmi.RemoteException;

public interface EchoService extends java.rmi.Remote {
    
    public String sayHello(String path)
        throws RemoteException;
}



