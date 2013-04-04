/**
 * EchoMultiClients.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul_sample_secconv;

import xsul.dsig.saml.authorization.CapabilityUtil;

public class EchoMultiClients
{
    /**
     * <p>Start this sample.</p>
     * <p>Usage:</p>
     * <code>EchoMultiClients numberOfClients WSaddress</code>
     *
     * @param args the arguments of this sample.
     */
    public static void main(String[] args)
    {
        int clientsNumber = Integer.parseInt(args[2]);
        String location = args[0];
        String sclocation = args[1];
        
        for (int i = 0 ; i < clientsNumber ; i++){
            Thread t = new Thread(new EchoRunnableClient(location,sclocation));
            t.setName("thread " + i);
            t.start();
        }
        
    }
}

