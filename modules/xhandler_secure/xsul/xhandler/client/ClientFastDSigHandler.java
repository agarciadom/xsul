/**
 * ClientFastDSigHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ClientFastDSigHandler.java,v 1.3 2005/06/24 03:55:53 lifang Exp $
 */

package xsul.xhandler.client;

import java.security.cert.X509Certificate;
import java.util.Iterator;
import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlElement;
import xsul.MLogger;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlUtil;
import xsul.xhandler.FastDSigHandler;
import xsul.xhandler.MCtxConstants;
import xsul.xhandler.XHandlerContext;

public class ClientFastDSigHandler extends FastDSigHandler {
    private final static MLogger logger = MLogger.getLogger();
    
    public ClientFastDSigHandler(String name,
                                 GlobusCredential cred,
                                 X509Certificate[] trustedCerts) {
        super(name, cred, trustedCerts);
    }
    
    public ClientFastDSigHandler(String name) {
        super(name);
    }
    
    public void init(XHandlerContext handlerConfig) {
        super.init(handlerConfig);
        boolean sigRequired = false;
        WsdlPort port = handlerConfig.getWsdlPort();
        for(Iterator i =
            port.elements(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL)
            .iterator();i.hasNext();) {
            XmlElement featureEl = (XmlElement) i.next();
            String uri = featureEl.getAttributeValue(null, WsdlUtil.URI_ATTR);
            if(MCtxConstants.FEATURE_FASTDSIG.equals(uri)) {
                sigRequired = true;
                break;
            }
        }
        setHandlerDisabled(!sigRequired);
        logger.finest("fast dsig handlerDisabled="+isHandlerDisabled());
    }
    
}


