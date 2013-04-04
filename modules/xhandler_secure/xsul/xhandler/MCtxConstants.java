/**
 * MCtxConstants.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: MCtxConstants.java,v 1.6 2005/06/24 03:55:53 lifang Exp $
 */

package xsul.xhandler;

import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;
import xsul.dsig.globus.security.authentication.wssec.WSConstants;
import xsul.dsig.saml.authorization.CapConstants;

public interface MCtxConstants {
    
    final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    final static public XmlNamespace NS = builder.newNamespace("http://extreme/security");
    final static public XmlNamespace WSSEC_NS = builder.newNamespace(WSConstants.WSSE_NS);
    final static public XmlNamespace SIG_NS = builder.newNamespace(WSConstants.SIG_NS);
    
    final static public String SIGNED = "signature-signed";
    final static public String CAPENFORCED = "capability-enforced";
    
    final static public String SIGCHECKED = "signature-checked";
    final static public String CAPCHECKED = "capability-checked";
    
    final static public String NOSIGNING = "no-signing";
    final static public String NOCAPABILITY = "no-capability";
    
    final static public String NOSIGCHECK = "no-signing-check";
    final static public String NOCAPABILITYCHECK = "no-capability-check";
    
    final static public String PRINCIPAL = "principal";

    final static public String CLIENTCONTEXTID = "client-context-id";
    final static public String SERVERCONTEXTID = "server-context-id";

    public final static String FEATURE_PASSWORD = "http://www.extreme.indiana.edu/xgws/xsul/2005-password";
    public final static String FEATURE_SIGNATURE = "http://www.extreme.indiana.edu/xgws/xsul/2005-signature";
    public final static String FEATURE_FASTDSIG = "http://www.extreme.indiana.edu/xgws/xsul/2005-fastdsig";
    public final static String FEATURE_CAPABILITY = "http://www.extreme.indiana.edu/xgws/xsul/2005-capability";
    public final static String FEATURE_SECCONV = "http://www.extreme.indiana.edu/xgws/xsul/2005-secconv";
}

