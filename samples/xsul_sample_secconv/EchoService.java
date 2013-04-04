/**
 * EchoService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul_sample_secconv;

import java.rmi.RemoteException;

public interface EchoService extends java.rmi.Remote {
    
    public String sayHello(String path)
        throws RemoteException;
}
