/**
 * CapmanClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: CapmanClient.java,v 1.3 2005/05/04 06:29:58 lifang Exp $
 */

package xsul.xpola.util;

import java.util.Date;
import java.util.Vector;
import org.globus.gsi.GlobusCredential;
import xsul.MLogger;
import xsul.XsulVersion;
import xsul.dsig.saml.authorization.CapConstants;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.xpola.XpolaConstants;
import xsul.xpola.XpolaFactory;
import xsul.xpola.capman.CapabilityManager;
import xsul.xpola.util.XpolaUtil;

public class CapmanClient {
    private static final MLogger logger = MLogger.getLogger();
    
    public static final String USAGE =
        "usage: java "+CapmanClient.class.getName()+" \n" +
        "[-url service url]\n"+
        "[-csurl capman service url]\n"+
        "[-wsdl wsdl_file] "+
        "[-userlist file_with_user_DN_list]\n" +
        "[-cmd command]";
    
    private static final int SERVICE_URL = 0;
    private static final int WSDL_LOCATION = 1;
    private static final int USER_LIST = 2;
    private static final int CSURL = 3;
    private static final int CAP = 4;
    private static final int CMD = 5;
    
    static String[] parseArgs(String[] args) throws IllegalArgumentException {
        String[] argNames = new String[8];
        argNames[WSDL_LOCATION] = "-wsdl";
        argNames[SERVICE_URL] = "-url";
        argNames[USER_LIST] = "-userlist";
        argNames[CAP] = "-cap";
        argNames[CMD] = "-cmd";
        argNames[CSURL] = "-csurl";
        
        // set defaults
        String[] defaultValues = new String[8];
        defaultValues[WSDL_LOCATION] = null;
        defaultValues[SERVICE_URL] = null;
        defaultValues[USER_LIST] = null;
        defaultValues[CAP] = "default.cap";
        defaultValues[CMD] = "register";
        defaultValues[CSURL] = "http://localhost:3756";
        
        // set required
        boolean[] requiredArgs = new boolean[8];
        requiredArgs[WSDL_LOCATION] = true;
        requiredArgs[SERVICE_URL] = true;
        requiredArgs[USER_LIST] = true;
        requiredArgs[CAP] = false;
        requiredArgs[CMD] = false;
        requiredArgs[CSURL] = false;
        
        return CapabilityUtil.parse(args, argNames, defaultValues,
                                    requiredArgs, USAGE);
    }
    
    public static void main(String[] args) throws Exception {
        XsulVersion.exitIfRequiredVersionMissing("2.0.9");
        String[] myArgs = parseArgs(args);
        String wsdlfile = myArgs[WSDL_LOCATION];
        String svc_url = myArgs[SERVICE_URL];
        String cs_url = myArgs[CSURL];
        String user_list = myArgs[USER_LIST];
        String capfile = myArgs[CAP];
        String cmd = myArgs[CMD];
        
        CapabilityManager cpstub = XpolaFactory.getCapman(cs_url);
        
        if(cmd.equals("register")) {
            GlobusCredential cred =
                GlobusCredential.getDefaultCredential();
            
            Vector ops = XpolaUtil.getWSDLOperations(wsdlfile);
            Vector users = XpolaUtil.getUserlist(user_list);
            
            String subject = CapabilityUtil.canonicalizeSubject(cred.getSubject());
            Capability cap = new Capability(subject,
                                            svc_url,
                                            users,
                                            XpolaConstants.LEADNAMESPACE,
                                            XpolaUtil.approveAll(ops),
                                            new Date(System.currentTimeMillis()),
                                            new Date(System.currentTimeMillis()
                                                         + 240*CapConstants.DEFAULT_LIFETIME),
                                            cred,
                                            false);
            cpstub.registerCapability(cap.toString());
        }
        else if(cmd.equals("get")) {
            String capstr = cpstub.getCapabilityByHandle(svc_url);
            logger.finest(capstr.toString());
            Capability cap = new Capability(capstr);
            System.out.println(capstr);
            CapabilityUtil.capability2File(cap, capfile);
        }
    }
}

