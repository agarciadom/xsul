/**
 * EchoServiceImpl.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul_sample_secconv;

import java.rmi.RemoteException;

public class EchoServiceImpl implements EchoService {

    public String sayHello(String name) throws RemoteException {
        return "Hello, "+name;
    }

}


