/**
 * MakeCapability.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: MakeCapability.java,v 1.1 2006/04/03 03:39:05 lifang Exp $
 */

package xsul_sample_capability;

import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import org.globus.gsi.GlobusCredential;
import xsul.dsig.saml.authorization.CapConstants;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.xpola.capman.PersistentCapman;
import xsul.xpola.db.conn.DBConnManager;
import xsul.xpola.util.XpolaUtil;

public class MakeCapability {
    
    static private String dblocation = "hsql://rainier.extreme.indiana.edu:1888/xdb";
    static private String svclocation = "http://rainier.extreme.indiana.edu:5367/arps";
    
    public Capability createAcap(String svc_url) {
        String wsdlfile = this.getClass().getResource("echo.wsdl").toString();
        Vector ops = XpolaUtil.getWSDLOperations(wsdlfile);
        GlobusCredential cred = null;
        
        try {
            cred = CapabilityUtil.getGlobusCredential(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String subject = CapabilityUtil.canonicalizeSubject(cred.getSubject());
        String[] user_list = {
            "C=US,O=National Center for Supercomputing Applications,CN=Dennis Gannon",
                subject
        };
        Capability cap
            = new Capability(subject,
                             svc_url,
                             new Vector(Arrays.asList(user_list)),
                             "http://portal.extreme.indiana.edu/20030301/",
                             XpolaUtil.approveAll(ops),
                             new Date(System.currentTimeMillis()),
                             new Date(System.currentTimeMillis() + CapConstants.DEFAULT_LIFETIME),
                             cred,
                             false);
        return cap;
    }
    
    public static void main (String[] args) {
        
        if(args.length > 0) {
            svclocation = args[0];
            if(args.length > 1)
                dblocation = args[1];
        }
        
        PersistentCapman capman =
            new PersistentCapman(DBConnManager.HSQL, dblocation, "sa", "");
        
        try {
            capman.registerCapability(
				      new MakeCapability().createAcap(svclocation).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
