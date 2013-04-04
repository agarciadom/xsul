/**
 * ServerFastDSigHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ServerFastDSigHandler.java,v 1.3 2005/06/24 03:55:53 lifang Exp $
 */

package xsul.xhandler.server;

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

public class ServerFastDSigHandler extends FastDSigHandler {
    private final static MLogger logger = MLogger.getLogger();
    
    public ServerFastDSigHandler(String name,
                                 GlobusCredential cred,
                                 X509Certificate[] trustedCerts) {
        super(name, cred, trustedCerts);
    }
    
    public ServerFastDSigHandler(String name) {
        super(name);
    }
    
    public void init(XHandlerContext handlerConfig) {
        super.init(handlerConfig);
        // something for wsdl
        if(handlerConfig == null)
            return;
        
        WsdlPort port = handlerConfig.getWsdlPort();
        XmlElement featureEl =
            port.element(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL);
        if(featureEl == null) {
            featureEl =
                port.addElement(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL);
        }
        else {
            for(Iterator i =
                port.elements(WsdlUtil.WSDL_SOAP12_NS, WsdlUtil.FEATURE_EL)
                .iterator();i.hasNext();) {
                XmlElement featureEl2 = (XmlElement) i.next();
                String uri =
                    featureEl2.getAttributeValue(null, WsdlUtil.URI_ATTR);
                if(MCtxConstants.FEATURE_FASTDSIG.equals(uri)) {
                    logger.config("fast signaure attr existed");
                    return;
                }
            }
        }
        featureEl.addAttribute(WsdlUtil.URI_ATTR,
                               MCtxConstants.FEATURE_FASTDSIG);
        featureEl.addAttribute(WsdlUtil.REQUIRED_ATTR, "true");
    }
    
}

