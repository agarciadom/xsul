/**
 * TestCapmanService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: TestCapmanService.java,v 1.11 2006/04/03 05:13:52 lifang Exp $
 */

package xsul.xpola;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.globus.gsi.GlobusCredential;
import xsul.MLogger;
import xsul.dsig.saml.authorization.CapConstants;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.xpola.capman.CapabilityManager;
import xsul.xpola.util.XpolaUtil;

public class TestCapmanService extends TestCase {
    private final static MLogger logger = MLogger.getLogger();
    
    static private String location = "http://localhost:6012/capman";
    static private String[] urls =
    {"http://localhost:2543/test",
            "http://k2:2224/test1",
            "http://rainier:1223/what",
            "http://brick:3291/adab",
            "http://whitney:2043/test",
            "http://huink:2246/test",
            "http://hunk:2273/test",
            "http://chunk:2241/test"};
    static private Random rand = new Random();
    static private String userdn = "C=US,O=National Center for Supercomputing Applications,CN=Liang Fang";
    static private String userdn2 = "C=US,O=National Center for Supercomputing Applications,CN=Gopi Kandaswamy";
    // with the assumption that the following group has the above two userdns registered
    static private String groupname = "extreme";
    private String svc_url;
    private GlobusCredential cred = null;
    
    public TestCapmanService(String name) {
        super(name);
        int num = rand.nextInt(urls.length);
        svc_url = urls[num];
    }
    
    public void testCapmanService() throws Exception {
        CapabilityManager capman = XpolaFactory.getCapman(location);
        testRegisterCapability(capman);
        logger.info("RegisterCapability done ");
        testGetCapabilityByHandle(capman);
        logger.info("GetCapabilityByHandle done ");
        testGetCapabilityHandlesByOwner(capman);
        logger.info("GetCapabilityHandlesByOwner done ");
        testGetCapability(capman);
        logger.info("GetCapability done ");
        //        testUpdateCapability(capman);
        //        testGetCapabilityHandlesByUser(capman);
        //        logger.info("GetCapabilityHandlesByUser done ");
        //        testGetAllCapabilityHandles(capman);
        //        logger.info("GetAllCapabilityHandles done ");
        //        testRevokeCapabilityByHandle(capman);
    }
    
    private void testRevokeCapabilityByHandle(CapabilityManager capman)
        throws Exception {
        capman.revokeCapabilityByHandle(svc_url);
        String capstr = capman.getCapabilityByHandle(svc_url);
        logger.info("cap after being revoked: " + capstr);
        assertNull(capstr);
    }
    
    private void testUpdateCapability(CapabilityManager capman)
        throws Exception {
        capman.updateCapability(createAcap(svc_url, false).toString());
    }
    
    private void testGetCapability(CapabilityManager capman)
        throws Exception {
        Capability cap =
            new Capability(capman.getCapability(svc_url, userdn));
        assertTrue(cap.getResource().equals(svc_url));
	cap.verify();
        cap = new Capability(capman.getCapability(svc_url, userdn2));
        assertTrue(cap.getResource().equals(svc_url));
	cap.verify();
    }
    
    private void testGetCapabilityHandlesByUser(CapabilityManager capman)
        throws Exception {
        String[] handles = capman.getCapabilityHandlesByUser(userdn);
        for (int i = 0; i < handles.length; i++) {
            logger.info("handle by user " + i + ": " + handles[i]);
        }
        assertTrue(Arrays.asList(handles).contains(svc_url));
    }
    
    private void testGetCapabilityHandlesByOwner(CapabilityManager capman)
        throws Exception {
        try {
            cred = CapabilityUtil.getGlobusCredential(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String subject = CapabilityUtil.canonicalizeSubject(cred.getSubject());
        String[] handles = capman.getCapabilityHandlesByOwner(subject);
        for (int i = 0; i < handles.length; i++) {
            logger.info("handle by owner " + i + ": " + handles[i]);
        }
        assertTrue(Arrays.asList(handles).contains(svc_url));
    }
    
    private void testGetCapabilitiesByOwner(CapabilityManager capman)
        throws Exception {
        String[] capstrs = capman.getCapabilitiesByOwner(CapabilityUtil.canonicalizeSubject(cred.getSubject()));
    }
    
    private void testGetCapabilityByHandle(CapabilityManager capman)
        throws Exception {
        Capability cap = new Capability(capman.getCapabilityByHandle(svc_url));
        assertTrue(cap.getResource().equals(svc_url));
    }
    
    private void testGetAllCapabilityHandles(CapabilityManager capman)
        throws Exception {
        String[] handles = capman.getAllCapabilityHandles();
        for (int i = 0; i < handles.length; i++) {
            logger.info("handle " + i + ": " + handles[i]);
        }
        assertTrue(Arrays.asList(handles).contains(svc_url));
    }
    
    private void testRegisterCapability(CapabilityManager capman)
        throws Exception {
        capman.registerCapability(createAcap(svc_url, false).toString());
        capman.registerCapability(createAcap(svc_url, true).toString());
    }
    
    public static void main(String[] args) throws Exception {
        if(args.length > 0) {
            location = args[0];
        }
        else {
            xsul.xpola.XpolaCenter.main(new String[] {"6012", "hsql://k2.extreme.indiana.edu:1888/xdb"});
        }
        junit.textui.TestRunner.run (new TestSuite(TestCapmanService.class));
        if(args.length <= 0)
            xsul.xpola.XpolaCenter.shutdownServer();
    }
    
    private Capability createAcap(String svc_url, boolean postponed) {
        String wsdlfile = this.getClass().getResource("test.wsdl").toString();
        String[] user_list = null;
        if(postponed) {
            user_list = new String [] {
                groupname // a groupname
            };
        }
        else {
            user_list = new String [] {
                userdn, // user DN
            };
        }
        Vector ops = XpolaUtil.getWSDLOperations(wsdlfile);
        
        try {
            cred = CapabilityUtil.getGlobusCredential();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String subject = CapabilityUtil.canonicalizeSubject(cred.getSubject());
        Capability cap
            = new Capability(subject,
                             svc_url,
                             new Vector(Arrays.asList(user_list)),
                             XpolaConstants.LEADNAMESPACE,
                             XpolaUtil.approveAll(ops),
                             new Date(System.currentTimeMillis()),
                             new Date(System.currentTimeMillis() + CapConstants.DEFAULT_LIFETIME),
                             cred,
                             false);
        return cap;
    }
    
}


