/**
 * TestRenewer.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: TestRenewer.java,v 1.1 2005/06/24 04:30:07 lifang Exp $
 */

package xsul.xpola;



import xsul.xpola.util.ProxyRenewer;

public class TestRenewer extends Thread {
    
    public TestRenewer() {
    }
    
    public void run() {
        while (true) {
            try {
                sleep(10000);
            } catch(InterruptedException e) {
                System.err.println("Interrupted");
            }
        }
    }
    
    
    public static void main (String[] args) {
        ProxyRenewer pr =
            new ProxyRenewer("lifang", "f22liang",
                             "rainier.extreme.indiana.edu",
                             7512, 2);
        new TestRenewer().start();
    }
    
}

