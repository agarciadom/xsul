/**
 * PasswordProcessor.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: PasswordProcessor.java,v 1.1 2005/01/11 02:00:44 lifang Exp $
 */

package xsul.processor.password;

import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import xsul.MLogger;
import xsul.passwd.PasswordVerifier;
import xsul.processor.MessageProcessor;
import xsul.processor.soap_over_http.SoapHttpDynamicInfosetProcessor;
import xsul.soap.SoapUtil;
import xsul.processor.DynamicInfosetProcessorException;

public class PasswordProcessor extends SoapHttpDynamicInfosetProcessor {
    
    private final static MLogger logger = MLogger.getLogger();
    
    private MessageProcessor service;
    
    public PasswordProcessor(MessageProcessor service,
                             int port) {
        super(port);
        this.service = service;
    }
    
    public XmlDocument processSoapEnvelope(XmlElement envelope, SoapUtil su) {
        
        // -Dauthen=uid:password
        String authen = System.getProperty("authen");
        int colon = authen.indexOf(':');
        String uid = authen.substring(0, colon);
        String passwd = authen.substring(colon+1);
        logger.finest("uid: " + uid + " passwd: " + passwd);
        PasswordVerifier pv = new PasswordVerifier(uid, passwd.getBytes());
        pv.verifySoapMessage(envelope);
        
        return super.processSoapEnvelope(envelope, su);
    }
    
    public XmlElement processMessage(XmlElement message)
        throws DynamicInfosetProcessorException {
        XmlElement resp = service.processMessage(message);
        return resp;
    }
        
}

