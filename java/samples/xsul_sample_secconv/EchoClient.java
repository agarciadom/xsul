/**
 * EchoClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul_sample_secconv;

import java.lang.reflect.Proxy;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.invoker.secconv.SecurityRequestorInvoker;
import xsul.soaprpc_client.SoapRpcInvocationHandler;
import xsul.xsd_type_handler.XsdTypeHandlerRegistry;

public class EchoClient
{
    public static final String USAGE =
        "usage: java "+EchoClient.class.getName()+" \n" +
        "\t[--svc service location]\n" +
        "\t[--scsvc secconv service location]\n" +
        "\t[--passwd your password]\n"+
        "\t[--# number of threads]\n"+
        "";
    
    private static final int SVC = 0;
    private static final int SCSVC = 1;
    private static final int PSWD = 2;
    private static final int NUM = 3;
    
    private static boolean QUIET = false;
    private final static String PREFIX = "ECHO Client: ";
    
    static String[] parseArgs(String[] args) throws IllegalArgumentException
    {
        String[] argNames = new String[4];
        argNames[SVC] = "--svc";
        argNames[SCSVC] = "--scsvc";
        argNames[PSWD] = "--passwd";
        argNames[NUM] = "--#";
        
        // set defaults
        String[] defaultValues = new String[4];
        defaultValues[SVC] = "http://localhost:8765";
        defaultValues[SCSVC] = "http://localhost:8080";
        defaultValues[PSWD] = "";
        defaultValues[NUM] = "1";
        
        // set required
        boolean[] requiredArgs = new boolean[4];
        requiredArgs[SVC] = false;
        requiredArgs[SCSVC] = false;
        requiredArgs[PSWD] = false;
        requiredArgs[NUM] = false;
        
        return CapabilityUtil.parse(args, argNames, defaultValues,
                                    requiredArgs, USAGE);
    }
    
    public static void main(String[] args)
    {
        String[] myArgs = parseArgs(args);
        String location = myArgs[SVC];
        String scloc = myArgs[SCSVC];
        char[] passwd = myArgs[PSWD].toCharArray();
        int count = Integer.parseInt(myArgs[NUM]);
        
        try
        {
            SecurityRequestorInvoker srinvoker =
                new SecurityRequestorInvoker(location, scloc);
            //            srinvoker.setLocation("http://localhost:"+port);
            
            SoapRpcInvocationHandler handler = new SoapRpcInvocationHandler(
                srinvoker, XsdTypeHandlerRegistry.getInstance());
            
            EchoService ref = (EchoService) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(), //XDirectoryService.class.getClassLoader(),
                new Class[] { EchoService.class },
                handler);
            
            int N = 3;
            long start = System.currentTimeMillis();
            String result = ref.sayHello("/hello");
            long after1 = System.currentTimeMillis();
            for(int i = 1; i < N; i++) {
                ref.sayHello("/hello");
            }
            long end = System.currentTimeMillis();
            long total = end-start;
            long total2 = end-after1;
            double sec1 = (total/1000.0);
            double sec2 = total2/1000.0;
            double tafter1 = (after1-start)/1000.0;
            double invPerSecs = (double)N / sec1 ;
            double invPs2 = (double)(N-1)/sec2;
            double avgInvTimeInMs = (double)total / (double)N;
            double avgInvTimeInMs2 = (double)total2 / (double)(N-1);
            System.out.println("first one cost: " + tafter1);
            System.out.println("N="+N+" avg invocation:"+avgInvTimeInMs+" [ms] "+
                                   //"total:"+seconds+" [s] "+
                                   "throughput:"+invPerSecs+" [invocations/s]");
            System.out.println("N-1="+(N-1)+" avg invocation:"+avgInvTimeInMs2+" [ms] "+
                                   //"total:"+seconds+" [s] "+
                                   "throughput:"+invPs2+" [invocations/s]");
            
            System.out.println("got back "+result);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}

