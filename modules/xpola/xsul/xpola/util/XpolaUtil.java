/**
 * XpolaUtils.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: XpolaUtil.java,v 1.11 2005/07/13 23:36:45 lifang Exp $
 */

package xsul.xpola.util;

import com.ibm.wsdl.PortImpl;
import com.ibm.wsdl.ServiceImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import xsul.MLogger;
import xsul.dsig.saml.authorization.CapConstants;
import xsul.dsig.saml.authorization.Capability;
import xsul.xpola.XpolaConstants;

public class XpolaUtil {
    
    private static final MLogger logger = MLogger.getLogger();
    
    static public Map approveAll(Collection actions) {
        Hashtable hact = new Hashtable();
        for(Iterator iter = actions.iterator(); iter.hasNext();) {
            QName act = (QName)iter.next();
            hact.put(act.getLocalPart(), CapConstants.PERMIT);
        }
        
        return hact;
    }
    
    static public Vector getUserlist(String userlistfile)
        throws Exception {
        Vector v = new Vector(11);
        // ysimmhan: fixed
        BufferedReader r = new BufferedReader(new FileReader(userlistfile));
        String user;
        while((user=r.readLine())!=null) {
            //            v.add(user.trim().replaceAll("[ ]+", ""));
            v.add(user.trim());
        }
        return v;
    }
    
    static public Vector getEndpointOps(String registryURL,
                                        String name)
        throws Exception {
        // LEADNAMESPACE by default
        QName qname = new QName(XpolaConstants.LEADNAMESPACE, name);
        return getEndpointOps(registryURL, qname);
    }
    
    static public Vector getEndpointOps(String registryURL,
                                        QName qname)
        throws Exception {
        if(registryURL == null) {
            registryURL = CapConstants.REGISTRY_URL;
        }
        String searchString = registryURL + "?xpath="
            + "/ogsi:entry/ogsi:memberServiceLocator/ogsi:reference/wsdl:definitions"
            + "[@name='" + qname.getLocalPart() + "'and@targetNamespace='"
            + qname.getNamespaceURI() + "']&ns_wsdl=http://schemas.xmlsoap.org/wsdl/&format=xml";
        
        return getWSDLOperations(searchString);
    }
    
    static public Vector getWSDLOperations(String wsdlloc) {
        StringBuffer str = new StringBuffer();
        
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            
            reader.setFeature("javax.wsdl.verbose", true);
            reader.setFeature("javax.wsdl.importDocuments", true);
            
            Definition def = reader.readWSDL(null, wsdlloc);
            Map svcmap = def.getServices();
            Collection svcs = svcmap.values();
            Iterator svcIter = svcs.iterator();
            QName serviceName = null;
            if ( svcIter.hasNext() ) {
                ServiceImpl svc = (ServiceImpl)svcIter.next();
                serviceName = svc.getQName();
            }
            Map ptypes = def.getPortTypes();
            logger.finest("port types:");
            
            Set qset = ptypes.keySet();
            Iterator iter = qset.iterator();
            Vector opv = new Vector(1);
            while(iter.hasNext()) {
                Object obj = null;
                obj = iter.next();
                logger.finest(obj.getClass().toString());
                PortType ptype = (PortType)ptypes.get(obj);
                List list = ptype.getOperations();
                Iterator iter2 = list.iterator();
                while(iter2.hasNext()) {
                    obj = iter2.next();
                    logger.finest(obj.getClass().toString());
                    Operation op = (Operation)obj;
                    String name = op.getName();
                    
                    str.append("operation name: " + serviceName+"."+name + "\n");
                    QName qn =
                        new QName(XpolaUtil.qName2URI(serviceName).toString(),
                                  name);
                    opv.add(qn);
                }
            }
            
            logger.finest(str.toString());
            return opv;
        }
        catch(Exception e) {
            logger.severe("", e);
        }
        
        return null;
    }
    
    static public Vector getHandlesfromCapabilities(Vector caps) {
        if (caps == null || caps.size() == 0)
            return null;
        
        if(caps.elementAt(0) instanceof Capability) {
            return getHandlesfromCapabilities((Capability[])caps.toArray(
                                                  new Capability[0]));
        }
        else if(caps.elementAt(0) instanceof String) {
            Vector handles = getHandlesfromCapabilities((String[])caps.toArray(
                                                            new String[0]));
            return handles;
        }
        else {
            logger.finest("weird class: " + caps.elementAt(0).getClass());
        }
        
        return null;
        
    }
    
    static private Vector getHandlesfromCapabilities(String[] caps) {
        Vector handles = new Vector(caps.length);
        for(int i = 0;i < caps.length;i++) {
            String resource =
                caps[i].substring(caps[i].indexOf("<Resource>")
                                      + "<Resource>".length(),
                                  caps[i].indexOf("</Resource>"));
            handles.add(resource);
        }
        return handles;
    }
    
    static public Vector getHandlesfromCapabilities(Capability[] caps) {
        Vector handles = new Vector(caps.length);
        for(int i = 0;i < caps.length;i++) {
            handles.add(caps[i].getResource());
        }
        
        return handles;
    }
    
    static public Vector strings2Capabilities(Vector capv) {
        for (int i = 0; i < capv.size(); i++) {
            if(capv.elementAt(i) instanceof String) {
                Capability cap = new Capability((String)capv.elementAt(i));
                capv.setElementAt(cap, i);
            }
        }
        return capv;
    }
    
    static public URI qName2URI(QName qname) throws URISyntaxException {
        String qstr = qname.toString();
        return qNameString2URI(qstr);
    }
    
    static public URI qNameString2URI(String qstr)
        throws URISyntaxException {
        int idx1 = qstr.indexOf('{');
        int idx2 = qstr.indexOf('}');
        if(idx1 == 0 && idx2 > 0) {
            qstr = qstr.substring(1).replaceFirst("}", "/");
        }
        return new URI(qstr);
    }
    
    static public QName string2QName(String qstr) {
        int idx1 = qstr.indexOf('{');
        int idx2 = qstr.indexOf('}');
        if(idx1 == 0 && idx2 > 0) {
            String nspace = qstr.substring(1).replaceFirst("}.*", "");
            String local = qstr.replaceFirst(".*}", "");
            return new QName(nspace, local);
        }
        return null;
    }
    
    static public QName uRI2QName(URI uri) {
        String uristr = uri.toASCIIString();
        int idx = uristr.lastIndexOf('/');
        return new QName(uristr.substring(0, idx),
                         uristr.substring(idx+1));
    }
    
    static public Hashtable searchRegistryByServiceName(String registryURL,
                                                        String namespace,
                                                        String name)
        throws Exception {
        if(namespace == null || namespace.equals("")) {
            namespace = XpolaConstants.LEADNAMESPACE;
        }
        namespace.replaceAll("\"", "");
        String searchString = "?xpath="
            + "/ogsi:entry/ogsi:memberServiceLocator/ogsi:reference/wsdl:definitions"
            + "[contains(@name,'" + name + "')and@targetNamespace='"
            + namespace+"']&ns_wsdl=http://schemas.xmlsoap.org/wsdl/&format=xml";
        return searchRegistryByServiceName(registryURL, searchString);
    }
    
    static public Hashtable searchRegistryByServiceName(String registryURL,
                                                        String searchString)
        throws Exception {
        
        Hashtable table = new Hashtable();
        
        if(registryURL == null) {
            registryURL = CapConstants.REGISTRY_URL;
        }
        
        try {
            int index = 0;
            while(true) {
                String newSearchString = registryURL + searchString +
                    "&index=" + index;
                WSDLFactory fac = WSDLFactory.newInstance();
                WSDLReader reader = fac.newWSDLReader();
                
                reader.setFeature("javax.wsdl.verbose", true);
                reader.setFeature("javax.wsdl.importDocuments", true);
                Definition def = reader.readWSDL(null, newSearchString);
                
                Map svcmap = def.getServices();
                Collection svcs = svcmap.values();
                for (Iterator svcIter = svcs.iterator(); svcIter.hasNext();) {
                    ServiceImpl svc = (ServiceImpl)svcIter.next();
                    QName serviceName = svc.getQName();
                    //                    String serviceName = svc.getQName().getLocalPart();
                    Map portmap = svc.getPorts();
                    Collection ports = portmap.values();
                    
                    for (Iterator portIter = ports.iterator(); portIter.hasNext();) {
                        PortImpl port = (PortImpl)portIter.next();
                        List plist = port.getExtensibilityElements();
                        Object[] objs = plist.toArray();
                        String uri = null;
                        
                        for(int i = 0; i < objs.length; ++i) {
                            // FIXME: Only returns the first known URI
                            if(objs[i] instanceof SOAPAddressImpl) {
                                SOAPAddressImpl soapAddress =
                                    (SOAPAddressImpl) objs[i];
                                uri = soapAddress.getLocationURI();
                                break;
                            }
                        }
                        table.put(uri, serviceName);
                        index++;
                    }
                }
            }
        }
        catch(WSDLException e) {
            logger.finer("Ignore exception : " + e);
        }
        catch(Exception e) {
            logger.severe("Exception : " + e);
        }
        
        if(table.isEmpty()) {
            logger.finer("No matching Service names found for " + searchString);
            throw new Exception("No matching Service names found for " + searchString);
        }
        
        return table;
    }
    
    static public Properties getSysEnv() {
        Properties jvmEnv = System.getProperties();
        Properties envVars = new java.util.Properties();
        try {
            if ( jvmEnv.getProperty( "os.name" ).equalsIgnoreCase( "SunOS"  ) ) {
                envVars.load( Runtime.getRuntime().exec( "/bin/env" )
                                 .getInputStream() );
            }
            else if ( jvmEnv.getProperty( "os.name" ).equalsIgnoreCase( "WinNT" ) ) {
                envVars.load( Runtime.getRuntime().exec( "set" )
                                 .getInputStream() );
            }
        }
        catch ( Throwable t ) {
            logger.severe("failed to get system env", t);
        }
        return envVars;
    }
    
    static public String getSysEnv(String env) {
        Properties envVars = getSysEnv();
        String x509 = envVars.getProperty(env);
        if(x509 == null) {
            logger.finest("cannot find env parameter " + env);
            if(logger.isFinestEnabled()) {
                for(Enumeration en = envVars.keys(); en.hasMoreElements();) {
                    String key = (String) en.nextElement();
                    logger.finest("key: " + key + "=" + envVars.getProperty(key));
                }
            }
        }
        return x509;
    }
    
    static public String getSysId() {
        Properties jvmEnv = System.getProperties();
        try {
            InputStream is = null;
            if ( jvmEnv.getProperty( "os.name" ).equalsIgnoreCase( "SunOS"  ) ) {
                is = Runtime.getRuntime().exec( "/bin/id" ).getInputStream();
            }
            else if ( jvmEnv.getProperty( "os.name" ).equalsIgnoreCase( "Linux" ) ) {
                is = Runtime.getRuntime().exec( "/usr/bin/id" ).getInputStream();
            }
            int c;
            StringBuffer sbuf = new StringBuffer();
            while((c = is.read()) != -1) {
                sbuf.append((char)c);
            }
            is.close();
            return sbuf.toString();
        } catch (IOException e) {e.printStackTrace();}
        return null;
    }
    
    static public String getSysUserid() {
        String userinfo = getSysId();
        int idx = userinfo.indexOf('(');
        return userinfo.substring(4, idx);
    }

    static public String getSysUsername() {
        String userinfo = getSysId();
        int idx1 = userinfo.indexOf('(');
        int idx2 = userinfo.indexOf(')');
        return userinfo.substring(idx1+1, idx2);
    }
}



