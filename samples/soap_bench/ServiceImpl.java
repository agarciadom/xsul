/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: ServiceImpl.java,v 1.5 2005/02/14 04:15:41 aslom Exp $
 */

package soap_bench;
import soap_bench.complex_types.ArrayOfMeshInterfaceObject;
import soap_bench.complex_types.ArrayOfSimpleEvents;
import soap_bench.complex_types.MeshInterfaceObject;
import soap_bench.complex_types.SimpleEvent;
import xsul.MLogger;



/**
 * Very simple service implementation.
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */


public class ServiceImpl implements BenchService {
    
    
    
    private final static MLogger logger = MLogger.getLogger();
    
    ServiceImpl() {
    }
    
    public void echoVoid() throws Exception
    {
    }
    
    public byte[] echoBase64(byte[] input) throws Exception {
        return input;
    }
    
    public String[] echoStrings(String[] input) throws Exception {
        return input;
    }
    
    public int[] echoInts(int[] input) throws Exception {
        return input;
    }
    
    public double[] echoDoubles(double[] input) throws Exception {
        return input;
    }
    
    
    //    public ArrayOfSimpleEvents echoMeshInterfaceObjects(ArrayOfSimpleEvents input) throws Exception {
    //        return input;
    //    }
    public ArrayOfMeshInterfaceObject echoMeshInterfaceObjects(ArrayOfMeshInterfaceObject input) throws Exception {
        input.setParent(null); //remove from incoming SOAP:Body
        input.setName("output");
        return input;
    }
    
    public int receiveMeshInterfaceObjects(ArrayOfMeshInterfaceObject input) throws Exception
    {
        MeshInterfaceObject[] arr = input.asArray();
        return arr.length;
    }
    
    public ArrayOfMeshInterfaceObject sendMeshInterfaceObjects(int n) throws Exception {
        MeshInterfaceObject[] output = new MeshInterfaceObject[n];
        for (int i = 0; i < n; i++) {
            output[i] = new MeshInterfaceObject(i, i, Math.sqrt(i));
        }
        return new ArrayOfMeshInterfaceObject("output", output);
    }
    
    public ArrayOfSimpleEvents echoSimpleEvents(ArrayOfSimpleEvents input) throws Exception {
        input.setParent(null); //remove from incoming SOAP:Body
        input.setName("output");
        return input;
    }
    
    public int receiveSimpleEvents(ArrayOfSimpleEvents input) throws Exception {
        SimpleEvent[] arr = input.asArray();
        return arr.length;
    }
    
    public ArrayOfSimpleEvents sendSimpleEvents(int n) throws Exception {
        SimpleEvent[] output = new SimpleEvent[n];
        for (int i = 0; i < n; i++) {
            //output[i] = new SimpleEvent(i+1, i+12345678.0, "event no. "+(i+1));
            output[i] = new SimpleEvent(i, Math.sqrt(i), "Message #"+i);
        }
        return new ArrayOfSimpleEvents("output", output);
    }
    
    
    public int receiveBase64(byte[] input) throws Exception {
        return input.length;
    }
    
    public int receiveStrings(String[] input) throws Exception {
        return input.length;
    }
    
    public int receiveInts(int[] input) throws Exception {
        return input.length;
    }
    
    public int receiveDoubles(double[] input) throws Exception {
        return input.length;
    }
    
    public byte[] sendBase64(int n) throws Exception {
        byte[] output = new byte[n];
        for (int i = 0; i < n; i++)
        {
            output[i] = (byte) i;
        }
        return output;
    }
    
    public String[] sendStrings(int n) throws Exception {
        String[] output = new String[n];
        for (int i = 0; i < n; i++)
        {
            output[i] = "s"+i;
        }
        return output;
    }
    
    public int[] sendInts(int n) throws Exception {
        int[] output = new int[n];
        for (int i = 0; i < n; i++)
        {
            output[i] = (int) i;
        }
        return output;
    }
    
    public double[] sendDoubles(int n) throws Exception {
        double[] output = new double[n];
        for (int i = 0; i < n; i++)
        {
            output[i] = (double) i;
        }
        return output;
    }
    
}



