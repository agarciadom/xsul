/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: EchoService.java,v 1.3 2005/02/14 03:43:21 aslom Exp $
 */

package soap_bench;

import soap_bench.complex_types.ArrayOfSimpleEvents;
import soap_bench.complex_types.ArrayOfMeshInterfaceObject;

/**
 * Benchmark1PortType
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface EchoService {
    public void echoVoid() throws Exception;
    public byte[] echoBase64(byte[] input) throws Exception;
    public String[] echoStrings(String[] input) throws Exception;
    public int[] echoInts(int[] input) throws Exception;
    public double[] echoDoubles(double[] input) throws Exception;
    public ArrayOfMeshInterfaceObject echoMeshInterfaceObjects(ArrayOfMeshInterfaceObject input)throws Exception;
    public ArrayOfSimpleEvents echoSimpleEvents(ArrayOfSimpleEvents input)throws Exception;
}



