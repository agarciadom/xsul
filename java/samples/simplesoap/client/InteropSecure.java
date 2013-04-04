/**
 * InteropSecure.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: InteropSecure.java,v 1.10 2005/04/10 04:28:18 lifang Exp $
 */

package simplesoap.client;

import simplesoap.contract.InteropTestRpc;
import simplesoap.service.InteropSecureService;
import simplesoap.service.InteropService;
import xsul.MLogger;
import xsul.xhandler.client.ClientSignatureHandler;
import xsul.xwsif_runtime.WSIFRuntime;

public class InteropSecure {
    private final static MLogger logger = MLogger.getLogger();
    public static final String LOCALWSDL =
        "samples/simplesoap/contract/InteropTest.wsdl";
    
    public static void main(String[] args) throws Exception {
        String wsdlLoc =
            (args.length > 0 && args[0].indexOf("?wsdl") > 0) ? args[0] : LOCALWSDL;
        boolean selfTesting = System.getProperty("start_server") != null;
        if(selfTesting) { // just for testing
            logger.finest("Doing self testing");
            InteropService.main(new String[]{"0", wsdlLoc});
            wsdlLoc = InteropSecureService.getServiceWsdlLocation();
        }
        logger.finest("Using WSDL "+wsdlLoc);
        int idx = args[0].indexOf("?wsdl");
        InteropTestRpc stub = null;
        if(idx == -1) {
            int count = Integer.parseInt(args[1]);
            int thnum = Integer.parseInt(args[2]);
            new Watcher(args[0], thnum, count);
            //          for (int i = 0 ; i < thnum; i++){
            //              InteropThread t = new InteropThread(args[0], count);
            //              t.setName("interop " + i);
            //              t.start();
            //          }
            //          double t1 = InteropThread.turnaround1/count;
            //          double t2 = InteropThread.turnaround2/count;
            //          System.out.println("count: " + InteropThread.count);
            //          System.out.println("N="+count*thnum+" avg invocation:"+t1+" [ms] ");
            //          System.out.println("N'="+(count-1)*thnum+" avg invocation:"+t2+" [ms]");
        } else {
            stub = (InteropTestRpc) WSIFRuntime.newClient(wsdlLoc)
                .addHandler(new ClientSignatureHandler("client signature"))
                .generateDynamicStub(InteropTestRpc.class);
            logger.finest("echoString response="+stub.echoString("Alice"));
        }
        if(selfTesting) {
            InteropSecureService.shutdownServer();
        }
    }
    
}

class Watcher extends Thread {
    private int count;
    private int thnum;
    public Watcher(String svcloc, int num1, int num2) {
        this.thnum = num1;
        this.count = num2;
        for (int i = 0 ; i < thnum; i++){
            InteropThread t = new InteropThread(svcloc, count);
            t.setName("interop " + i);
            t.start();
        }
        start();
    }
    public void run() {
        while(InteropThread.count != this.thnum) {
//            System.out.println("counting: " + InteropThread.count + " expecting " + thnum);
            try {
                sleep(1000);
            } catch(InterruptedException e) {
                System.err.println("Interrupted");
            }
        }
        double t1 = InteropThread.turnaround1/thnum;
        double t2 = InteropThread.turnaround2/thnum;
        System.out.println("count: " + InteropThread.count);
        System.out.println("N="+count*thnum+" avg invocation:"+t1+" [ms] ");
        System.out.println("N'="+(count-1)*thnum+" avg invocation:"+t2+" [ms]");
    }
}

class InteropThread extends Thread {
    
    private final static MLogger logger = MLogger.getLogger();
    private InteropTestRpc stub;
    private int num;
    static int count = 0;
    static double turnaround1;
    static double turnaround2;
    
    InteropThread(String svcLoc, int num) {
        this.num = num;
        stub = (InteropTestRpc) WSIFRuntime.newClient(InteropSecure.LOCALWSDL, svcLoc)
            .addHandler(new ClientSignatureHandler("client signature"))
            .generateDynamicStub(InteropTestRpc.class);
    }
    
    public void run() {
        String tname = getName();
        long start = System.currentTimeMillis();
        String response = stub.echoString("echo for thread " + tname);
        long after1 = System.currentTimeMillis();
        for (int i = 0; i < num-1; i++) {
            stub.echoString("echo for thread " + tname);
        }
        long end = System.currentTimeMillis();
        long total = end-start;
        long first = end-after1;
        double sec1 = total/1000.0;
        double sec2 = first/1000.0;
        double tafter1 = (after1-start)/1000.0;
        double invPerSecs = (double)num / sec1 ;
        double invPersec2 = (double)(num-1)/sec2;
        double avgInvTimeInMs = (double)total / (double)num;
        turnaround1 += avgInvTimeInMs;
        double avgInvTimeInMs2 = (double)first / (double)(num-1);
        turnaround2 += avgInvTimeInMs2;
        logger.info("first one cost: " + tafter1);
        logger.info("N="+num+" avg invocation:"+avgInvTimeInMs+" [ms] "+
                        //"total:"+seconds+" [s] "+
                        "throughput:"+invPerSecs+" [invocations/s]");
        logger.info("N-1="+(num-1)+" avg invocation:"+avgInvTimeInMs2+" [ms] "+
                        //"total:"+seconds+" [s] "+
                        "throughput:"+invPersec2+" [invocations/s]");
        logger.info("got back "+response);
        count++;
    }
    
}


