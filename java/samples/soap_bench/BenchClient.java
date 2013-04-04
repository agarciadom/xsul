/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: BenchClient.java,v 1.20 2005/02/15 17:41:57 aslom Exp $
 */

package soap_bench;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import soap_bench.complex_types.ArrayOfMeshInterfaceObject;
import soap_bench.complex_types.ArrayOfSimpleEvents;
import soap_bench.complex_types.MeshInterfaceObject;
import soap_bench.complex_types.SimpleEvent;
import xsul.XmlConstants;
import xsul.common_type_handler.CommonTypeHandlerRegistry;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.soap11_util.Soap11Util;
import xsul.soap12_util.Soap12Util;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.type_handler.TypeHandlerException;
import java.io.FileWriter;

//improvements: for send*(size, offset)
//improvements: for echo/receive(*, offset) //add offset to all int/double parameters
//improvements: run benchmark for N seconds

public class BenchClient {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static String BANCHMARK_URI = "urn:Benchmark1";
    private final static String SMOKE_TEST = "smoke_test";
    private final static String PRINT_TRACE = "print_trace";
    private final static boolean dumpMessages  = System.getProperty("dumpMessages") != null;
    private final static boolean axisCppSpecial  = System.getProperty("AXISCPP") != null;
    private final static boolean glueSpecial  = System.getProperty("GLUE") != null;
    private final static boolean VERBOSE = true;
    
    
    //URL or port of service
    //total number of elements to send (default 10K)
    //[rse] means receive, send, or echo (a == all)
    //[bdisva] means base64, double, int, string, void (only applies to echo), a == all methods;
    //arraySize (optional for void) - default to 10
    //jaav -Dmachine.name=... -Dserver.name=... Client URL total {rsea}{bdisvmta} [arraySize]
    public static void main(String[] args) throws Exception
    {
        long benchmarkStart = System.currentTimeMillis();
        final String BENCHMARK_DRIVER_VERSION = "$Date: 2005/02/15 17:41:57 $";
        final String ID = "SoapBenchmark2 Driver Version 2.0.1 ("+BENCHMARK_DRIVER_VERSION+")";
        verbose("Starting "+ID+" at "+(new Date()));
        if(dumpMessages) verbose("will be dumping messages");
        // allow multiple URLs (each must start with http"
        List locationList = new ArrayList();
        //int port = Integer.parseInt(args[0]);
        int pos = 0;
        while(pos < args.length) {
            String s = args[pos];
            if(s.startsWith("http")) {
                locationList.add(s);
                //} else {
                //port = Integer.parseInt(s);
            } else {
                break;
            }
            ++pos;
        }
        if(locationList.isEmpty()) {
            int port = 34321;
            locationList.add("http://localhost:"+port);
        }
        
        final int elementsToSend = args.length > pos ? Integer.parseInt(args[pos]) : 10000;
        
        String testType = "aa";
        if(args.length > (pos + 1)) {
            testType = args[(pos + 1)];
        }
        
        String arrSizeToSend = "10";
        
        if(args.length > (pos + 2)) {
            arrSizeToSend = args[(pos + 2)];
        }
        //        System.out.println("invoking "+td.serverLocation+" for  elements="+td.elementsToSend+" "
        //                               +" direction="+td.direction+" method="+td.method+" arraySize="+td.arrSizeToSend);
        
        //new String[1];
        String[] locations = new String[locationList.size()];
        locationList.toArray(locations);
        
        for (int i = 0; i < locations.length; i++)
        {
            String location = locations[i];
            verbose("connecting to "+location);
            runTestsForSize(location, elementsToSend, testType, arrSizeToSend);
        }
        long benchmarkEnd = System.currentTimeMillis();
        double seconds = ((benchmarkEnd-benchmarkStart)/1000.0);
        System.err.println("Finished "+ID+" in "+seconds+" seconds at "+(new Date()));
    }
    
    private static void runTestsForSize(String location,
                                        final int elementsToSend,
                                        String testType,
                                        String arrSizeToSend)
        throws Exception
    {
        TestDescriptor td= new TestDescriptor(location, elementsToSend);
        int commaPos = -1;
        boolean finished = false;
        while(!finished) {
            td.setDirection(testType.charAt(0));
            td.setMethod(testType.charAt(1));
            int prevPos = commaPos;
            commaPos = arrSizeToSend.indexOf(",", prevPos+1);
            String size;
            if(commaPos > 0) {
                size = arrSizeToSend.substring(prevPos+1, commaPos);
            } else {
                size = arrSizeToSend.substring(prevPos+1);
                finished = true;
            }
            td.arrSizeToSend = Integer.parseInt(size);
            System.err.println("runnig test with size="+size+" "+(new Date()));
            final char direction = td.getDirection();
            if(direction == 'a') {
                td.setDirection('e');
                runTestsForDirection(td);
                td.setDirection('r');
                runTestsForDirection(td);
                td.setDirection('s');
                runTestsForDirection(td);
                td.setDirection('a'); //restore
            } else {
                runTestsForDirection(td);
            }
        }
    }
    
    public static void runTestsForDirection(TestDescriptor td)
        throws Exception
    {
        final char direction = td.direction;
        final char method = td.method;
        if(method == 'a') {
            if(direction == 'e') {
                //runTestsForDirection(direction, 'v', td);
                td.setMethod('v');
                runOneTest(td);
            }
            td.setMethod('b');
            runOneTest(td);
            td.setMethod('d');
            runOneTest(td);
            td.setMethod('i');
            runOneTest(td);
            td.setMethod('s');
            runOneTest(td);
            td.setMethod('m');
            runOneTest(td);
            td.setMethod('t');
            runOneTest(td);
            td.setMethod('a'); //restore
        } else {
            runOneTest(td);
        }
    }
    
    public static void runOneTest(TestDescriptor td)
        throws Exception
    {
        final char direction = td.direction;
        final char method = td.method;
        //int arrSize = method == 'v' ? 1 : td.arrSizeToSend;
        final int arrSize = td.arrSizeToSend;
        int N = td.elementsToSend / arrSize; // + 1;
        if(N == 0) {
            N = 1;
        }
        final boolean smokeTest  = System.getProperty(SMOKE_TEST) != null;
        if(smokeTest) N = 3;
        
        int totalInv = N * td.arrSizeToSend;
        
        byte[] barr = null;
        byte[] ba = null;
        if(method == 'b') {
            ba = new byte[td.arrSizeToSend];
            barr = new byte[totalInv];
            for (int i = 0; i < barr.length; i++) {
                barr[ i ] = (byte)i;
            }
        }
        
        
        double[] darr = null;
        double[] da = null;
        if(method == 'd') {
            da = new double[td.arrSizeToSend];
            darr = new double[totalInv];
            for (int i = 0; i < darr.length; i++) {
                darr[ i ] = i;
            }
        }
        
        int[] iarr = null;
        int[] ia = null;
        if(method == 'i') {
            ia = new int[td.arrSizeToSend];
            iarr = new int[totalInv];
            for (int i = 0; i < iarr.length; i++) {
                iarr[ i ] =  i;
            }
        }
        
        
        MeshInterfaceObject[] marr = null;
        MeshInterfaceObject[] ma = null;
        if(method == 'm') {
            ma = new MeshInterfaceObject[td.arrSizeToSend];
            marr = new MeshInterfaceObject[totalInv];
            for (int i = 0; i < marr.length; i++) {
                marr[ i ] = new MeshInterfaceObject(i, i, Math.sqrt(i));
            }
        }
        
        String[] sarr = null;
        String[] sa = null;
        if(method == 's') {
            sa = new String[td.arrSizeToSend];
            sarr = new String[totalInv];
            for (int i = 0; i < sarr.length; i++) {
                sarr[ i ] = "s" + i;
            }
        }
        
        SimpleEvent[] tarr = null;
        SimpleEvent[] ta = null;
        if(method == 't') {
            ta = new SimpleEvent[td.arrSizeToSend];
            tarr = new SimpleEvent[totalInv];
            for (int i = 0; i < tarr.length; i++) {
                //tarr[ i ] = new SimpleEvent(i+1, i+12345678.0, "event no. "+(i+1));
                tarr[ i ] = new SimpleEvent(i,Math.sqrt(i), "Message #"+i);
            }
        }
        
        //System.out.println("connecting to "+location);
        SoapHttpDynamicInfosetInvoker invoker = new SoapHttpDynamicInfosetInvoker() {
            
            public XmlDocument wrapAsSoapDocument(XmlElement message)
                throws DynamicInfosetInvokerException
            {
                
                XmlDocument envelopeDoc = super.wrapAsSoapDocument(message);
                if(glueSpecial) {
                    XmlElement envelope = envelopeDoc.getDocumentElement();
                    XmlNamespace envelopeNs = envelope.getNamespace();
                    XmlElement body = envelope.requiredElement(envelopeNs, "Body");
                    //S:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"
                    body.addAttribute(envelopeNs,
                                      "encodingStyle",
                                      "http://schemas.xmlsoap.org/soap/encoding/");
                }
                if(dumpMessages) {
                    dumpMessageToFile(envelopeDoc);
                }
                return envelopeDoc;
            }

            private void dumpMessageToFile(XmlDocument envelopeDoc) throws XmlBuilderException {
                String name = "dump-"+method2s(direction, method)+"-"+arrSize+"-0.xml";
                String xmlMessage = builder.serializeToString(envelopeDoc);
                String print = xmlMessage;
                if(print.length() > 1000) {
                    String s1 = xmlMessage.substring(0, 500);
                    String s2 = xmlMessage.substring(print.length() - 500, print.length());
                    print = s1+"\n ... "+s2; //less is more
                }
                System.out.println("DUMP="+name+"\n"+print);
                File f = new File(name);
                if(f.exists()) {
                    f.delete();
                }
                try {
                    FileWriter fw = new FileWriter(f);
                    fw.write(xmlMessage);
                    fw.close();
                } catch (IOException e) {
                    fail("ERROR: failed to dump message into file "+name);
                }
            }
            
            
        };
        //new Soap11HttpDynamicInfosetInvoker(HttpClientReuseLastConnectionManager.newInstance());
        //new Soap11HttpDynamicInfosetInvoker(HttpClientConnectionManager.newInstance());
        
        
        boolean keeapAlive = false;
        //System.out.println("keepAlive "+(keeapAlive ? "enabled" : "disabled" ));
        invoker.setKeepAlive(keeapAlive);
        
        if(axisCppSpecial) {
            invoker.setSoapFragrance(Soap12Util.getInstance()); //to use SOAP 1.2
        } else {
            invoker.setSoapFragrance(Soap11Util.getInstance()); //to use SOAP 1.1
        }
        invoker.setLocation(td.serverLocation);
        
        
        SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
            invoker, CommonTypeHandlerRegistry.getInstance())
        {
            
            protected XmlElement invokeRemoteEndpoint(XmlElement message){
                XmlElement result = super.invokeRemoteEndpoint(message);
                //if(dumpMessages) {
                //    return null; // no invocation
                //} else {
                return result;
                //}
            }
            
            protected String getArgumentName(int i, Method m, Object[] params) {
                if(i == 0) {
                    if(direction == 's') {
                        return "size";
                    } else {
                        return "input";
                    }
                } else {
                    return super.getArgumentName(i, m, params);
                }
            }
            
            protected XmlElement prepareArgumentToSend(int i, Method method, Object[] params)
                throws TypeHandlerException
            {
                XmlElement arg = super.prepareArgumentToSend(i, method, params);
                if(axisCppSpecial || glueSpecial) {
                    Object o = params[i];
                    if(o != null && o.getClass().isArray() &&
                           o.getClass().getComponentType().equals(Byte.TYPE))
                    {
                        //TODO: this is fishy ...
                        String value = XmlConstants.XS_NS.getPrefix()+":base64Binary";
                        arg.addAttribute(XmlConstants.XSI_NS, "type", value);
                    }
                }
                return arg;
            }
            
            
        };
        handler.setTargetServiceNamespace(builder.newNamespace(BANCHMARK_URI));
        //        if(glueSpecial) { //set to default (preferred?) namesapce
        //            handler.setTargetServiceNamespace(builder.newNamespace("x"));
        //        }
        BenchService ref = (BenchService) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { BenchService.class },
            handler);
        
        // This is workaround for AXIS-C++ as SOAPAction is REQUIRED to do message dispatch
        // even though WSDL had declared soapAction="" ...
        final String AXISCPP_PREFIX = "Benchmark1#";
        System.err.println("invoking "+N+(smokeTest ? " (SMOKE TEST)" : "")
                               +" times "+method2s(direction, method)
                               +" arraysSize="+td.arrSizeToSend
                               +" "+(new Date()));
        //boolean validate = true;
        long start = System.currentTimeMillis();
        for (int count = 0; count < N ; count++) {
            int off = count * arrSize;
            //String arg = "echo"+i;
            if(method == 'v') {
                if(direction == 'e') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    ref.echoVoid();
                } else if(direction == 'r' || direction == 's') {
                    throw new RuntimeException("usupported direction "+direction+" for void method");
                } else {
                    throw new RuntimeException("unrecongized direction "+direction);
                }
            } else if(axisCppSpecial && direction == 's') {
                System.err.println("WARNING: skipping test "+method2s(direction, method)+" as AXIS-C++ does not suport it");
                start = -1;
                break;
            } else if(method == 'b') {
                System.arraycopy(barr, off, ba, 0, ba.length);
                byte[] uba = null;
                int ulen = -1;
                if(direction == 'e') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    uba = ref.echoBase64(ba);
                } else if(direction == 'r') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    ulen = ref.receiveBase64(ba);
                    if(ulen != ba.length) fail(method2s(direction, method)+" returned wrong size");
                } else if(direction == 's') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    uba = ref.sendBase64(arrSize);
                } else {
                    throw new RuntimeException("unrecongized direction "+direction);
                }
                if((count == 0 ||  count == N-1) && (direction=='e' || direction == 's')) {
                    // bruta force
                    if(direction=='s') off = 0;
                    if(uba == null) fail(method2s(direction, method)+" byte array response was null");
                    if(uba.length != ba.length) {
                        fail(method2s(direction, method)+" byte array had wrong size "+uba.length
                                 +" (expected "+ba.length+")");
                    }
                    for (int i = 0; i < ba.length; i++) {
                        if(uba[i] != barr[i+off]) {
                            fail("byte array response had wrong content");
                        }
                    }
                }
            } else if(method == 'd') {
                System.arraycopy(darr, off, da, 0, da.length);
                double[] uda = null;
                int dlen = -1;
                if(direction == 'e') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    uda = ref.echoDoubles(da);
                } else if(direction == 'r') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    dlen = ref.receiveDoubles(da);
                    if(dlen != da.length) fail("receive double array returned wrong size");
                } else if(direction == 's') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    uda = ref.sendDoubles(arrSize);
                } else {
                    throw new RuntimeException("unrecongized direction "+direction);
                }
                if((count == 0 ||  count == N-1) && (direction=='e' || direction == 's')) {
                    // bruta force verification
                    if(direction=='s') off = 0;
                    if(uda == null) fail(method2s(direction, method)+" double array response was null");
                    if(uda.length != da.length) {
                        fail(method2s(direction, method)+" double array had wrong size "+uda.length
                                 +" (expected "+da.length+")");
                    }
                    for (int i = 0; i < uda.length; i++) {
                        if(uda[i] != darr[i+off]) {
                            fail(method2s(direction, method)+" double array response had wrong content");
                        }
                    }
                }
            } else if(method == 'i') {
                System.arraycopy(iarr, off, ia, 0, ia.length);
                int[] uia = null;
                int ulen = -1;
                if(direction == 'e') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    uia = ref.echoInts(ia);
                } else if(direction == 'r') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    ulen = ref.receiveInts(ia);
                    if(ulen != ia.length) {
                        fail(method2s(direction, method)+" receive byte array returned wrong size");
                    }
                } else if(direction == 's') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    uia = ref.sendInts(arrSize);
                } else {
                    throw new RuntimeException("unrecongized direction "+direction);
                }
                if((count == 0 ||  count == N-1) && (direction=='e' || direction == 's')) {
                    // bruta force verification
                    if(direction=='s') off = 0;
                    if(uia == null) fail(method2s(direction, method)+" int array response was null");
                    if(uia.length != ia.length) {
                        fail(method2s(direction, method)+" int array had wrong size "+uia.length
                                 +" (expected "+ia.length+")");
                    }
                    for (int i = 0; i < uia.length; i++) {
                        if(uia[i] != iarr[i+off]) {
                            fail(method2s(direction, method)+" int array response had wrong content");
                        }
                    }
                }
            } else if(method == 'm') {
                System.arraycopy(marr, off, ma, 0, ma.length);
                MeshInterfaceObject[] uma = null;
                int mlen = -1;
                if(direction == 'e') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    ArrayOfMeshInterfaceObject input = new ArrayOfMeshInterfaceObject("input", ma);
                    ArrayOfMeshInterfaceObject result = ref.echoMeshInterfaceObjects(input);
                    uma =  result.asArray();
                } else if(direction == 'r') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    ArrayOfMeshInterfaceObject input = new ArrayOfMeshInterfaceObject("input", ma);
                    mlen = ref.receiveMeshInterfaceObjects(input);
                    if(mlen != ma.length) fail(method2s(direction, method)+" receive MIO array returned wrong size");
                } else if(direction == 's') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    ArrayOfMeshInterfaceObject result = ref.sendMeshInterfaceObjects(arrSize);
                    uma = result.asArray();
                } else {
                    throw new RuntimeException("unrecongized direction "+direction);
                }
                if(start > 0 && (count == 0 ||  count == N-1) && (direction=='e' || direction == 's')) {
                    // bruta force verification
                    if(direction=='s') off = 0;
                    if(uma == null) fail(method2s(direction, method)+" MIO array response was null");
                    if(uma.length != ma.length) {
                        fail(method2s(direction, method)+" MIO array had wrong size "+uma.length
                                 +" (expected "+ma.length+")");
                    }
                    for (int i = 0; i < uma.length; i++) {
                        MeshInterfaceObject m1 = uma[i];
                        MeshInterfaceObject m2 = marr[i+off];
                        if(!m1.equals(m2)) {
                            fail(method2s(direction, method)+" MIO array response"
                                     +" had wrong content (m1="+m1+" m2="+m2+" i="+i+")");
                        }
                    }
                }
            } else if(method == 's') {
                System.arraycopy(sarr, off, sa, 0, sa.length);
                String[] usa = null;
                int slen = -1;
                if(direction == 'e') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    usa = ref.echoStrings(sa);
                } else if(direction == 'r') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    slen = ref.receiveStrings(sa);
                    if(slen != sa.length) fail(method2s(direction, method)+" receive string array returned wrong size");
                } else if(direction == 's') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    usa = ref.sendStrings(arrSize);
                } else {
                    throw new RuntimeException("unrecongized direction "+direction);
                }
                if(start > 0 && (count == 0 ||  count == N-1) && (direction=='e' || direction == 's')) {
                    // bruta force verification
                    if(direction=='s') off = 0;
                    if(usa == null) fail(method2s(direction, method)+" string array response was null");
                    if(usa.length != sa.length) {
                        fail(method2s(direction, method)+" string array had wrong size "+usa.length
                                 +" (expected "+sa.length+")");
                    }
                    for (int i = 0; i < usa.length; i++) {
                        String s1 = usa[i];
                        String s2 = sarr[i+off];
                        if(!s1.equals(s2)) {
                            fail(method2s(direction, method)+" string array response"
                                     +" had wrong content (s1="+s1+" s2="+s2+" i="+i+")");
                        }
                    }
                }
            } else if(method == 't') {
                System.arraycopy(tarr, off, ta, 0, ta.length);
                SimpleEvent[] uta = null;
                int tlen = -1;
                if(direction == 'e') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    ArrayOfSimpleEvents input = new ArrayOfSimpleEvents("input", ta);
                    ArrayOfSimpleEvents result = ref.echoSimpleEvents(input);
                    uta =  result.asArray();
                } else if(direction == 'r') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    ArrayOfSimpleEvents input = new ArrayOfSimpleEvents("input", ta);
                    tlen = ref.receiveSimpleEvents(input);
                    if(tlen != ta.length) fail(method2s(direction, method)+" receive SE array returned wrong size");
                } else if(direction == 's') {
                    if(axisCppSpecial) invoker.setSoapAction(AXISCPP_PREFIX+method2s(direction, method));
                    ArrayOfSimpleEvents result = ref.sendSimpleEvents(arrSize);
                    uta = result.asArray();
                } else {
                    throw new RuntimeException("unrecongized direction "+direction);
                }
                if(start > 0 && (count == 0 ||  count == N-1) && (direction=='e' || direction == 's')) {
                    // bruta force verification
                    if(direction=='s') off = 0;
                    if(uta == null) fail(method2s(direction, method)+" SE array response was null");
                    if(uta.length != ta.length) {
                        fail(method2s(direction, method)+" SE array had wrong size "+uta.length
                                 +" (expected "+ta.length+")");
                    }
                    for (int i = 0; i < uta.length; i++) {
                        SimpleEvent t1 = uta[i];
                        SimpleEvent t2 = tarr[i+off];
                        if(!t1.equals(t2)) {
                            fail(method2s(direction, method)+" SE array response"
                                     +" had wrong content (t1="+t1+" t2="+t2+" i="+i+")");
                        }
                    }
                }
            } else {
                throw new RuntimeException("unrecongized method "+method);
            }
            
            if(dumpMessages) break;
            
            //            if(start > 0 && smokeTest) {
            //                String resp = builder.serializeToString(handler.getLastResponse());
            //                System.err.println(method2s(direction, method)+" response=\n"+resp+"---\n");
            //            }
            //System.out.println(arg+" -> "+result);
            //if(!arg.equals(result)) {
            //    throw new RuntimeException("service sent wrong answer");
            //}
        }
        if(start > 0) {
            long end = System.currentTimeMillis();
            long total = end-start;
            double seconds = (total/1000.0);
            double invPerSecs = (double)N / seconds ;
            double avgInvTimeInMs = (double)total / (double)N;
            System.err.println("N="+N+" avg invocation:"+avgInvTimeInMs+" [ms]"+
                                   //"total:"+seconds+" [s] "+
                                   " throughput:"+invPerSecs+" [invocations/second]"+
                                   " arraysSize="+arrSize+
                                   " direction="+direction+
                                   " method="+method
                                   +" "+(new Date())
                              );
            td.printResult(avgInvTimeInMs/1000.0, invPerSecs);
        }
    }
    
    private static void verbose(String msg) {
        System.err.println("SB2> "+msg);
    }
    
    private static void fail(String msg) {
        String s = "FATAL ERROR: service is not following benchmark requirement: "+msg;
        System.err.println(s);
        throw new RuntimeException(s);
        //System.exit(-1);
    }
    
    private static String method2s(char direction, char method) {
        StringBuffer sb = new StringBuffer(20);
        if(direction == 'e') {
            sb.append("echo");
        } else if(direction == 's') {
            sb.append("send");
        } else if(direction == 'r') {
            sb.append("receive");
        }
        if(method == 'v') {
            sb.append("Void");
        } else if(method == 'b') {
            sb.append("Base64");
        } else if(method == 'd') {
            sb.append("Doubles");
        } else if(method == 'i') {
            sb.append("Ints");
        } else if(method == 's') {
            sb.append("Strings");
        } else if(method == 'm') {
            sb.append("MeshInterfaceObjects");
        } else if(method == 't') {
            sb.append("SimpleEvents");
        }
        return sb.toString();
    }
    
    private final static class TestDescriptor {
        private java.text.DecimalFormat df = new java.text.DecimalFormat("##0.000000000");
        private java.text.DecimalFormat df2 = new java.text.DecimalFormat("##0.0000");
        private String testSetup;
        private String clientName = "XSUL1";
        private String serverName = null;
        private String serverLocation;
        private int arrSizeToSend;
        private int elementsToSend;
        
        private char direction;
        private char method;
        
        TestDescriptor(//String serverName,
            String location,
            //final char direction,
            //final char method,
            int elementsToSend)
            //int arrSizeToSend)
        {
            this.testSetup = System.getProperty("test.setup");
            if(this.testSetup == null) {
                this.testSetup = System.getProperty("machine.name", "UNKNOWN_SETUP");
            }
            final String SERVER_NAME = "server.name";
            this.serverName = System.getProperty(SERVER_NAME);
            if(serverName == null) {
                throw new RuntimeException(SERVER_NAME+" must be specified as system property");
            }
            //this.serverName = serverName;
            this.serverLocation = location;
            //this.direction =  direction;
            //this.method = method;
            this.elementsToSend = elementsToSend;
            //this.arrSizeToSend = arrSizeToSend;
        }
        
        public void setDirection(char direction) {
            this.direction = direction;
        }
        
        public char getDirection() {
            return direction;
        }
        
        public void setMethod(char method) {
            this.method = method;
        }
        
        public char getMethod() {
            return method;
        }
        
        public void printResult(double timeSecs, double throughput) throws IOException {
            PrintWriter results = new PrintWriter(System.out, true);
            results.print(  testSetup + '\t'
                              + clientName + '\t'
                              + serverName + '\t'
                              + method2s(direction, method) + '\t'
                              + arrSizeToSend + '\t'
                              + df.format(timeSecs) + '\t'
                              + df2.format(throughput)
                              + "\r\n");
            results.flush();
        }
        
    }
    
    
}




