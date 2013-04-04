/**
 * TestGroupmanService.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: TestGroupmanService.java,v 1.5 2005/05/02 18:11:36 lifang Exp $
 */

package xsul.xpola;

import java.util.Arrays;
import java.util.Vector;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import xsul.xpola.groupman.GroupManager;
import xsul.xpola.util.XpolaUtil;

public class TestGroupmanService extends TestCase {
    
    static private String location = "http://localhost:6012/groupman";
    static private String gname1 = "testgroup1";
    static private String gname2 = "testgroup2";
    static private String[] metadata = new String[]{"some desc"};
    static private String[] users = {
        "C=US,O=National Center for Supercomputing Applications,CN=Liang Fang",
            "C=US,O=Indiana University,OU=Computer Science,CN=Liang Fang"};
    public TestGroupmanService(String name) {
        super(name);
    }
    
    public void testGroupmanService() throws Exception {
        GroupManager gman = XpolaFactory.getGroupman(location);
        testAddGroup(gman);
        testAddUsersToGroup(gman);
        testListGroups(gman);
        testListUsersOfGroup(gman);
        testRemoveUsersFromGroup(gman);
        testDeleteGroups(gman);
    }
    
    public void testAddGroup(GroupManager gman) throws Exception{
        gman.addGroup(gname1, metadata);
    }
    
    public void testAddUsersToGroup(GroupManager gman) throws Exception{
        gman.addUsersToGroup(users, gname2, metadata);
    }
    
    public void testListGroups(GroupManager gman) throws Exception{
        String[] gnames = gman.listGroups(metadata);
        assertTrue(Arrays.asList(gnames).contains(gname1));
        assertTrue(Arrays.asList(gnames).contains(gname2));
    }
    
    public void testListUsersOfGroup(GroupManager gman) throws Exception{
        String[] _users = gman.listUsersOfGroup(gname2, false);
        assertTrue(Arrays.asList(_users).contains(users[0]));
        assertTrue(Arrays.asList(_users).contains(users[1]));
    }
    
    public void testRemoveUsersFromGroup(GroupManager gman) throws Exception{
        gman.removeUsersFromGroup(new String[]{users[0]}, gname2);
        String[] _users = gman.listUsersOfGroup(gname2, false);
        assertFalse(Arrays.asList(_users).contains(users[0]));
        assertTrue(Arrays.asList(_users).contains(users[1]));
    }
    
    public void testDeleteGroups(GroupManager gman) throws Exception{
        gman.deleteGroups(new String[]{gname1, gname2});
        String[] gnames = gman.listGroups(metadata);
        assertFalse(Arrays.asList(gnames).contains(gname1));
        assertFalse(Arrays.asList(gnames).contains(gname2));
    }
    
    public static void main(String[] args) throws Exception {
	if(args.length > 0) {
	    location = args[0];
	} else {
	    xsul.xpola.XpolaCenter.main(new String[] {"6012", "hsql://k2.extreme.indiana.edu:1888/xdb"});
	}
        junit.textui.TestRunner.run (new TestSuite(TestGroupmanService.class));
	if(args.length <= 0)
	    xsul.xpola.XpolaCenter.shutdownServer();
    }
    
}

