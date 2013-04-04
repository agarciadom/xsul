/**
 * TestCapman.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: TestCapman.java,v 1.11 2006/04/03 05:13:52 lifang Exp $
 */

package xsul.xpola;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import junit.framework.TestSuite;
import org.globus.gsi.GlobusCredential;
import xsul.XsulTestCase;
import xsul.dsig.saml.authorization.CapConstants;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.xpola.capman.PersistentCapman;
import xsul.xpola.db.conn.DBConnManager;
import xsul.xpola.util.XpolaUtil;

public class TestCapman extends XsulTestCase {
    
    static private Random rand = new Random();
    static private String location = "hsql://rainier.extreme.indiana.edu:1888/xdb";
    static private String[] urls =
    {"http://localhost:2243/test",
            "http://k2:2244/test1",
            "http://rainier:1823/what",
            "http://brick:3293/adab",
            "http://whitney:2243/test",
            "http://huink:2243/test",
            "http://hunk:2243/test",
            "http://chunk:2243/test"};

    private String subject;

    public TestCapman(String name) {
        super(name);
    }
    
    public static void main (String[] args) {
	if(args.length > 0){
	    location = args[0];
	}
        junit.textui.TestRunner.run (new TestSuite(TestCapman.class));
    }
    
    private Capability createAcap(String svc_url, boolean postponed) {
        String wsdlfile = this.getClass().getResource("test.wsdl").toString();
        Vector ops = XpolaUtil.getWSDLOperations(wsdlfile);

        GlobusCredential cred = null;
        try {
            cred = CapabilityUtil.getGlobusCredential(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        subject = CapabilityUtil.canonicalizeSubject(cred.getSubject());
	Vector userlist = new Vector(1);
	userlist.add(subject);
        Capability cap
            = new Capability(subject,
                             svc_url,
                             userlist,
                             "http://portal.extreme.indiana.edu/20030301/",
                             XpolaUtil.approveAll(ops),
                             new Date(System.currentTimeMillis()),
                             new Date(System.currentTimeMillis() + CapConstants.DEFAULT_LIFETIME),
                             cred,
                             postponed);
        return cap;
    }
    
    public void testSignedCapman() throws Exception {
        
        int num = rand.nextInt(urls.length);
        String svc_url = urls[num];
        
        Capability cap = createAcap(svc_url, false);
        
        doit(cap, svc_url);
        
        svc_url = urls[(num+1)%8];
        
        cap = createAcap(svc_url, true);
        
        doit(cap, svc_url);
    }
    
    private void doit(Capability cap, String svc_url) throws Exception {
        assertNotNull(cap);
        
        PersistentCapman capman =
            new PersistentCapman(DBConnManager.HSQL, location, "sa", "");
        
        System.out.println("handle: " + svc_url);
        capman.registerCapability(cap.toString());
        //        System.out.println("cap: " + cap);
        //        System.out.println("getcap: " + capman.getCapabilityByHandle(svc_url));
        assertEquals(cap.toString(), capman.getCapabilityByHandle(svc_url));
        String _cap = capman.getCapability(svc_url, subject);
        Capability realcap = new Capability(_cap);
        assertTrue(realcap.isSigned());

        String[] handles = capman.getCapabilityHandlesByOwner(subject);
        assertTrue(isIncluded(handles, svc_url));
        
	//        handles = capman.getCapabilityHandlesByUser(subject);
	//	for (int i = 0; i < handles.length; i++) {
	//	    System.out.println(handles[i]);
	//	}
	//	System.out.println("svc url:" + svc_url);
	//        assertTrue(isIncluded(handles, svc_url));
        
        capman.revokeCapabilitiesByOwner(subject);
        handles = capman.getCapabilityHandlesByOwner(subject);
        assertTrue(handles == null || handles.length == 0);
        
        capman.registerCapability(cap.toString());
        assertEquals(cap.toString(), capman.getCapabilityByHandle(svc_url));
        
        capman.revokeCapabilityByHandle(svc_url);
        handles = capman.getCapabilityHandlesByUser(subject);
        if(handles != null) {
            assertTrue(!isIncluded(handles, svc_url));
        }
        
        capman.registerCapability(cap.toString());
        assertEquals(cap.toString(), capman.getCapabilityByHandle(svc_url));
        
        StringBuffer strbuf = new StringBuffer("");
        int id = rand.nextInt(10000);
        strbuf.append("<CapabilityRequest>\n");
        strbuf.append("<Id>" + id + "</Id>\n");
        strbuf.append("<Issuer>C=US,O=National Center for Supercomputing Applications,CN=Lucy Liu</Issuer>\n");
        strbuf.append("<Resource>"+svc_url+"</Resource>\n");
        strbuf.append("<Action>decode</Action>\n");
        strbuf.append("<Status>"+CapConstants.PENDING+"</Status>\n");
        strbuf.append("</CapabilityRequest>");
        
        capman.registerRequest(strbuf.toString());
        assertEquals(strbuf.toString(), capman.getRequestById(""+id));
        
        assertEquals(strbuf.toString(),
                     capman.getRequestsByIssuer("C=US,O=National Center for Supercomputing Applications,CN=Lucy Liu")[0]);
        
        capman.removeRequestsByIssuer("C=US,O=National Center for Supercomputing Applications,CN=Lucy Liu");
        assertEquals(0, capman.getRequestsByIssuer("C=US,O=National Center for Supercomputing Applications,CN=Lucy Liu").length);
        
        capman.revokeCapabilitiesByOwner(subject);
        capman.removeRequestsByIssuer("C=US,O=National Center for Supercomputing Applications,CN=Lucy Liu");
        capman.getRequestsByReceiver("C=US,O=National Center for Supercomputing Applications,CN=Lucy Liu");
    }
    
    private boolean isIncluded(String[] handles, String handle) {
        return Arrays.asList(handles).contains(handle);
    }
}

