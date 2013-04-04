/**
 * PasswordInvoker.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: PasswordInvoker.java,v 1.2 2005/01/18 09:44:34 aslom Exp $
 */

package xsul.invoker.password;

import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import xsul.MLogger;
import xsul.invoker.DynamicInfosetInvokerException;
import xsul.invoker.soap_over_http.SoapHttpDynamicInfosetInvoker;
import xsul.passwd.PasswordEnforcer;

public class PasswordInvoker extends SoapHttpDynamicInfosetInvoker {
    
    private final static MLogger logger = MLogger.getLogger();

    public PasswordInvoker(String location) {
        super(location);
    }
    
    public XmlDocument invokeXml(XmlDocument request)
        throws DynamicInfosetInvokerException {
        
        String authen = System.getProperty("authen");
        int colon = authen.indexOf(':');
        String uid = authen.substring(0, colon);
        String passwd = authen.substring(colon+1);
        logger.finest("uid: " + uid + " passwd: " + passwd);
        
        PasswordEnforcer pe = new PasswordEnforcer(uid, passwd.getBytes());
        XmlElement message = request.getDocumentElement();
        pe.enforceSoapMessage(message);
        return super.invokeXml(request);
    }
    
}

