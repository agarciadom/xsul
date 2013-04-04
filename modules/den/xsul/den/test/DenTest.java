/**
 * DenTest.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.den.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Properties;
import xsul.dispatcher.routingtable.WS;

public class DenTest {
    static public void main(String[] args) {
        
        //-Dauthen=uid=fang,passwd=liang
        String auth = System.getProperty("authen");
        System.out.println(auth);
        
        Properties dataProps = new Properties();
        FileInputStream in = null;
        
        try {
            in = new FileInputStream("d:\\WUTemp\\sample.properties");
            dataProps.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Enumeration enumKey = dataProps.keys();
        while (enumKey.hasMoreElements()) {
            String pathVirtual = (String) enumKey.nextElement();
            String pathWS = dataProps.getProperty(pathVirtual);
            
            //            String proc = pathWS.replaceFirst(".*://", "");
            //            System.out.println("protocol: " + proc);
            
            String[] paths = pathWS.split(",");
            for(int i = 0; i < paths.length;i++) {
                System.out.println("paths " + i + ": " + paths[i]);
                String proc = paths[i].replaceFirst("://.*", "");
                String host = paths[i].replaceFirst(".*://", "").replaceFirst(":.*", "");
                String path = paths[i].replaceFirst(".*/", "");
                String port = paths[i].replaceFirst(".*:", "").replaceFirst("/.*", "");
                System.out.println("protocol: " + proc);
                System.out.println("host: " + host);
                System.out.println("path: " + path);
                System.out.println("port: " + port);
                
                System.out.println(pathVirtual + "->" + paths[i]
                                       + " added in the Routing Table");
            }
        }
    }
}

