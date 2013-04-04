/**
 * EchoMultiClients.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul_sample_capability;

public class EchoMultiClients
{
    public static void main(String[] args)
    {
        String certloc = args[0];
        String keyloc = args[1];
        String cacloc = args[2];
        String svcurl = args[3];
        String svccap = args[4];
        int clientsNumber = Integer.parseInt(args[5]);
        
        for (int i = 0 ; i < clientsNumber ; i++){
            Thread t = new Thread(new EchoRunnableClient(certloc,
                                                         keyloc,
                                                         cacloc,
                                                         svcurl,
                                                         svccap));
            t.setName("thread " + i);
            t.start();
        }
        
    }
}

