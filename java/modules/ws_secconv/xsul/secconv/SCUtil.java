/**
 * SCUtil.java
 *
 * @author Liang Fang
 * $Id: SCUtil.java,v 1.6 2005/01/26 18:52:02 lifang Exp $
 */

package xsul.secconv;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.cert.X509Certificate;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import org.apache.xml.security.utils.XMLUtils;
import org.globus.gsi.TrustedCertificates;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import xsul.MLogger;
import xsul.dsig.globus.security.authentication.wssec.ProxyPathValidator;
import xsul.dsig.globus.security.authentication.wssec.ProxyPathValidatorException;
import xsul.secconv.token.RequestedSecurityTokenType;
import xsul.secconv.token.SecurityContextTokenType;

public class SCUtil
{
    private static final MLogger logger = MLogger.getLogger();
    
    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    
    //  static public Id getValidId(int i) {
    //      if(i < 0)
    //          i *= -1;
//
    //      return new Id("_"+new Integer(i).toString());
    //  }
//
    static public String createContextId(int hashcode) {
        
        UUID id = new UUID(System.currentTimeMillis(), hashcode);
        String contextId = id.toString();
        logger.finest("init contextid: " + contextId);
        
        return contextId;
    }
    
    //  static public SecurityContextTokenType createSCTToken(String contextId)
    //      throws IllegalArgumentException, URI.MalformedURIException {
    //      SecurityContextTokenType sctType =
    //          new SecurityContextTokenType();
//
    //      // identified with resource key value == contextId
    //      sctType.setIdentifier(new URI(contextId));
    //      sctType.setId(SCUtil.getValidId(sctType.hashCode()));
    //      return sctType;
    //  }
//
    static public Element toElement(Document doc, Object obj, QName qname) {
        logger.finest("qanem: " + qname);
        Element elem = doc.createElementNS(qname.getNamespaceURI(), "wsse:"+qname.getLocalPart());
        //      elem.setNodeValue(obj.toString());
        return elem;
    }
    
    static public Element getFirstElement(Element element) {
        for (
            Node currentChild = element.getFirstChild();
            currentChild != null;
            currentChild = currentChild.getNextSibling()
        ) {
            if (currentChild instanceof Element) {
                return (Element) currentChild;
            }
        }
        
        return null;
    }
    
    public static Node getDirectChild(
        Node fNode,
        String localName,
        String namespace
    ) {
        if(logger.isFinestEnabled()){
            ByteArrayOutputStream bElm = new ByteArrayOutputStream();
            XMLUtils.outputDOM(fNode, bElm);
            logger.finest("fNode=\n"+bElm);
        }
        logger.finest("haschild: " + fNode.hasChildNodes());
        NodeList nl = fNode.getChildNodes();
        logger.finest("list length: "+nl.getLength());
        logger.finest("ln0: " + localName);
        logger.finest("ns0: " + namespace);
        for(int i = 0; i < nl.getLength(); i++) {
            Node currentChild = nl.item(i);
            
            if(currentChild == null)
                continue;
            
            if (
                namespace.equalsIgnoreCase(currentChild.getNamespaceURI()) &&
                localName.equalsIgnoreCase(currentChild.getLocalName())
            ) {
                if(logger.isFinestEnabled()){
                    ByteArrayOutputStream bElm = new ByteArrayOutputStream();
                    XMLUtils.outputDOM(currentChild, bElm);
                    logger.finest("node "+i+"=\n"+bElm);
                    logger.finest("ns: "+currentChild.getNamespaceURI());
                    logger.finest("ln: "+currentChild.getLocalName());
                }
                
                return currentChild;
            }
        }
        
        return null;
    }
    
    
    /**
     * Temp method for gettig password from a local file
     *
     * @param    contextId           a  String
     *
     * @return   a password char[]
     *
     * @exception   Exception
     *
     */
    public static char[] getPassword() throws Exception {
        String fname = ".passwd";
        
        BufferedReader in = new BufferedReader(new FileReader(fname));
        String pw = in.readLine();
        in.close();
        if(pw != null) {
            logger.finest("password: " + pw);
            return pw.toCharArray();
        }
        
        return null;
    }
    
    public static Key getSessionKey(String contextId) throws Exception {
        
        logger.finest("contextid: " + contextId);
        
        if(contextId == null)
            throw new Exception("contextid null");
        
        String res = System.getProperty("res");
        if (res == null)
            res = "fs";
        
        InputStream in = null;
        
        // store in memeory if shared the same jvm?
        if(res.equals("mem")) {
            String strkey = System.getProperty(contextId);
            byte[] kMaterial = decoder.decodeBuffer(strkey);
            Key key = new SecretKeySpec(kMaterial, "HmacMD5");
            return key;
        }
        
        // store in file system
        try {
            in = new FileInputStream(contextId+"."+"cle");
            
        } catch (FileNotFoundException e) {
            try {
                in = new FileInputStream(contextId+"."+"svr");
            } catch (FileNotFoundException fe) {
                throw fe;
            }
        }
        ObjectInputStream ois = new ObjectInputStream(in);
        byte[] kMaterial = (byte[]) ois.readObject();
        
        // fixme: algorithm other than HmacMD5
        Key key = new SecretKeySpec(kMaterial, "HmacMD5");
        logger.finest("skey: " + encoder.encode(key.getEncoded()));
        ois.close();
        in.close();

        return key;
        
    }
    
    public static void saveSessionKey(String contextId, Key key, boolean b)
        throws Exception {
        
        logger.finest("contextid: " + contextId);
        
        if(contextId == null)
            throw new Exception("contextid null");
        
        String res = System.getProperty("res");
        
        if (res == null)
            res = "fs";
        
        // context storedin memeory
        if(res.equals("mem")) {
            String strkey = encoder.encode(key.getEncoded());
            System.setProperty(contextId, strkey);
        }
        else {
            String ext = null;
            
            if(b) {
                ext = "svr";
            }
            else {
                ext = "cle";
            }
            
            String fname = contextId + "." + ext;
            
            byte[] enckey = key.getEncoded();
            
            try {
                FileOutputStream fos =
                    new FileOutputStream(fname);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(enckey);
                logger.finest("skey: " + encoder.encode(enckey));
                oos.close();
                fos.close();
            } catch (IOException e) {
                throw e;
            }
        }
    }
    
    public static RequestedSecurityTokenType createNewContextResource(String contextId)
        throws RemoteException {
        
        if(contextId == null) {
            contextId = Integer.toString((new String("jfa id").hashCode()));
        }
        
        logger.finest("context id: " + contextId);
        
        RequestedSecurityTokenType rstt =
            new RequestedSecurityTokenType();
        SecurityContextTokenType sctt =
            new SecurityContextTokenType();
        //        sctt.setId("_865986784");
        try {
            sctt.setIdentifier(new URI(contextId));
        } catch (URISyntaxException e) {}
        rstt.setSecurityContextToken(sctt);
        logger.finest("context " + contextId + " created successfully!");
        return rstt;
    }
    
    static public void pathValidation(X509Certificate cert,
                                      TrustedCertificates trustedCerts)
        throws Exception {
        ProxyPathValidator validator = new ProxyPathValidator();
        X509Certificate[] certs = {cert};
        try {
            validator.validate(certs, trustedCerts);
        }
        catch (ProxyPathValidatorException e) {
            throw new Exception("Path Validation failed", e);
        }
    }
    
    
}

