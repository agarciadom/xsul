/**
 * HttpClientAttacker.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul_sample_secconv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import xsul.MLogger;
import xsul.XsulVersion;
import xsul.http_client.HttpClientConnectionManager;
import xsul.http_client.HttpClientException;
import xsul.http_client.HttpClientRequest;
import xsul.http_client.HttpClientResponse;
import xsul.http_client.HttpClientReuseLastConnectionManager;
import xsul.util.Utf8Writer;

public class HttpClientAttacker implements Runnable {
    
    private final static MLogger logger = MLogger.getLogger();
    private final static HttpClientConnectionManager cx =
        HttpClientReuseLastConnectionManager.newInstance();
    
    private String host;
    private int port;
    private String file;
    private String svcname;
    private int num;
    static int count = 0;
    static double turnaround1;
    static double turnaround2;
    
    public HttpClientAttacker(String host, int port, String svcname,
                              String file, int num) {
        this.host = host;
        this.port = port;
        this.file = file;
        this.svcname = svcname;
        this.num = num;
    }
    
    public void run() {
        long start = System.currentTimeMillis();
        doit();
        long after1 = System.currentTimeMillis();
        for (int i = 0; i < num-1; i++) {
            doit();
        }
        long end = System.currentTimeMillis();
        long first = end-after1;
        long total = end-start;
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
                        "throughput:"+invPerSecs+" [invocations/s]");
        logger.info("N-1="+(num-1)+" avg invocation:"+avgInvTimeInMs2+" [ms] "+
                        "throughput:"+invPersec2+" [invocations/s]");
        count++;
    }
    
    private void doit() throws HttpClientException {
        HttpClientRequest req = cx.connect(host, port, 300000);
        req.setRequestLine("POST", "/"+svcname, "HTTP/1.0");
        req.ensureHeadersCapacity(2); //action + keep-alive
        req.setConnection("close");
        req.setContentType("text/xml; charset=utf-8");
        req.setUserAgent(XsulVersion.getUserAgent());
        HttpClientResponse resp = req.sendHeaders();
        OutputStream out = req.getBodyOutputStream();
        Writer utf8Writer = new Utf8Writer(out, 8*1024);
        try {
            BufferedReader fin = new BufferedReader( new FileReader(file));
            String s;
            StringBuffer sbuf = new StringBuffer();
            while((s = fin.readLine())!= null)
                sbuf.append(s+'\n');
            
            utf8Writer.write(sbuf.toString());
            utf8Writer.close();
        } catch (IOException e) {}
        resp.readStatusLine();
        String respStatusCode = resp.getStatusCode();
        resp.readHeaders();
        String contentType = resp.getContentType();
        int contentLength = resp.getContentLength();
        InputStream in = resp.getBodyInputStream();
        try {
            in.close();
        } catch (IOException e) {}
    }
    
    public static void main(String[] args)
        throws IOException, InterruptedException {
        String host = xsul.util.XsulUtil.getHostfromURL(args[0]);
        int port = xsul.util.XsulUtil.getPortnumfromURL(args[0]);
        String svcname = xsul.util.XsulUtil.getSvcnamefromURL(args[0]);
        int thnum = Integer.parseInt(args[2]);
        int num = Integer.parseInt(args[3]);
        new Watcher(thnum, num).start();
        for(int i = 0; i < thnum; i++) {
            Thread t = new Thread(new HttpClientAttacker(host, port, svcname,
                                                         args[1], num));
            t.setName("thread " + i);
            t.start();
        }
    }
    
}

class Watcher extends Thread {
    private int thnum;
    private int num;
    
    public Watcher(int tnum,int num) {
        this.thnum = tnum;
        this.num = num;
    }
    
    public void run() {
        while(HttpClientAttacker.count != thnum) {
            try {
                sleep(1000);
            } catch(InterruptedException e) {
                System.err.println("Interrupted");
            }
        }
        double t1 = HttpClientAttacker.turnaround1/thnum;
        double t2 = HttpClientAttacker.turnaround2/thnum;
        System.out.println("count: " + HttpClientAttacker.count);
        System.out.println("N="+num*thnum+" avg invocation:"+t1+" [ms] ");
        System.out.println("N'="+(num-1)*thnum+" avg invocation:"+t2+" [ms]");
    }
}
