/**
 * ProxyRenewer.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ProxyRenewer.java,v 1.8 2005/07/14 03:55:17 lifang Exp $
 */

package xsul.xpola.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import xsul.MLogger;

public class ProxyRenewer {
    private final static MLogger logger = MLogger.getLogger();
    
    private static Timer timer = new Timer(true);
    private long interval = 110*60*1000;
    private RenewalTask rt;
    
    public ProxyRenewer(String username, String password,
                        String myproxyloc, int port, float lifetime) {
        // renewal interval is just a little less than the lifetime.
        interval = new Float(lifetime*56*60*1000).intValue();
        rt = new RenewalTask(username, password, myproxyloc, port, lifetime);
	// delay a little bit, in case we are too fast
        timer.schedule(rt, 2000, interval);
    }
    
    public static void main(String[] args) {
        String username = args[0];
        String password = args[1];
        String myproxyloc = args[2];
        int port = Integer.parseInt(args[3]);
        float lifetime = Float.parseFloat(args[4]);
        new ProxyRenewer(username, password, myproxyloc, port, lifetime);
    }
}

class RenewalTask extends TimerTask {
    public final static int SECS_PER_HOUR = 60*60;
    private final static MLogger logger = MLogger.getLogger();
    private String proxyloc;
    private String username;
    private String password;
    private String hostname;
    private int port;
    private int lifetime = 2;
    
    public RenewalTask(String username, String password,
                       String myproxyloc, int port, float lifetime) {
        this.username = username;
        this.password = password;
        this.hostname = myproxyloc;
        this.port = port;
        this.lifetime = new Float(lifetime * SECS_PER_HOUR).intValue();
    }
    
    public void run()  {
        try {
            MyProxy myproxy = new MyProxy( hostname, port );
            GSSCredential proxy =
                myproxy.get(username, password, lifetime);
            GlobusCredential globusCred = null;
            if(proxy instanceof GlobusGSSCredentialImpl) {
                globusCred =
                    ((GlobusGSSCredentialImpl)proxy).getGlobusCredential();
		logger.finest("got proxy from myproxy for " + username
			      + " with " + lifetime + " lifetime.");
		InputStream is = Runtime.getRuntime().exec( "/bin/env").getInputStream();
		BufferedReader in
		    = new BufferedReader(new InputStreamReader(is));
		String str;
		while((str = in.readLine()) != null) {
		    if(str.startsWith("X509_USER_PROXY=")) {
			proxyloc = str.substring("X509_USER_PROXY=".length());
		    }
		}
		//                proxyloc = XpolaUtil.getSysEnv("X509_USER_PROXY");
                logger.finest("proxy location0: " + proxyloc);
                if(proxyloc == null) {
                    String uid = XpolaUtil.getSysUserid();
		    logger.finest("uid: " + uid);
                    proxyloc = "/tmp/x509up_u" + uid;
                }
		logger.finest("proxy location: " + proxyloc);
		File proxyfile = new File(proxyloc);
		if(proxyfile.exists() == false) {
		    String dirpath = proxyloc.substring(0, proxyloc.lastIndexOf('/')); 
		    File dir = new File(dirpath);
		    if(dir.exists() == false) {
			dir.mkdirs();
			logger.finest("new directory " + dirpath + " is created.");
		    }
		    proxyfile.createNewFile();
		    logger.finest("new proxy file " + proxyloc + " is created.");
		}
                FileOutputStream fout = new FileOutputStream(proxyfile);
                globusCred.save(fout);
                fout.close();
                Runtime.getRuntime().exec( "/bin/chmod 600 " + proxyloc );
		logger.finest("proxy file renewed");
            }
        } catch (MyProxyException e) {
	    logger.severe("failed to get proxy from myproxy server", e);
        } catch (IOException e) {
	    logger.severe("failed to save proxy into file", e);
        }
        
    }
}


