/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SendService.java,v 1.3 2005/02/14 03:43:21 aslom Exp $
 */

package soap_bench;

import soap_bench.complex_types.ArrayOfMeshInterfaceObject;
import soap_bench.complex_types.ArrayOfSimpleEvents;

/**
 * Benchmark1PortType
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface SendService {
    public byte[] sendBase64(int n) throws Exception;
    public String[] sendStrings(int n) throws Exception;
    public int[] sendInts(int n) throws Exception;
    public double[] sendDoubles(int n) throws Exception;
    public ArrayOfMeshInterfaceObject sendMeshInterfaceObjects(int n) throws Exception;
    public ArrayOfSimpleEvents sendSimpleEvents(int n) throws Exception;
}



