/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: ReceiveService.java,v 1.3 2005/02/14 03:43:21 aslom Exp $
 */

package soap_bench;

import soap_bench.complex_types.ArrayOfMeshInterfaceObject;
import soap_bench.complex_types.ArrayOfSimpleEvents;


/**
 * Benchmark2PortType
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public interface ReceiveService {
    public int receiveBase64(byte[] input) throws Exception;
    public int receiveStrings(String[] input) throws Exception;
    public int receiveInts(int[] input) throws Exception;
    public int receiveDoubles(double[] input) throws Exception;
    public int receiveMeshInterfaceObjects(ArrayOfMeshInterfaceObject input) throws Exception;
    public int receiveSimpleEvents(ArrayOfSimpleEvents input) throws Exception;
}



