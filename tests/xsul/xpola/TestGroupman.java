/**
 * TestGroupman.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: TestGroupman.java,v 1.6 2005/06/16 18:29:27 lifang Exp $
 */

package xsul.xpola;

import java.sql.Connection;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import xsul.xpola.db.conn.DBConnManager;
import xsul.xpola.groupman.PersistentGroupman;

public class TestGroupman extends TestCase {
    
    static private DBConnManager dcm = null;
    static private String location = "hsql://k2.extreme.indiana.edu:1888/xdb";
    
    private void init() throws Exception {
        dcm = DBConnManager.getInstance(DBConnManager.HSQL, location, "sa", "");
        dcm.init();
    }
    
    public void testGroupman() throws Exception {
        init();
        PersistentGroupman pgman =
            new PersistentGroupman(DBConnManager.HSQL, location, "sa", "");
        pgman.deleteGroups(new String[] {"first", "second", "third", "fourth"});
        pgman.addUsersToGroup(new String[] {"zhang1", "wang1", "li1"}, "first",
            new String[]{"first test group"});
//        pgman.addUsersToGroup(new String[] {"chang1", "wong1", "li1"}, "second");
//        pgman.addUsersToGroup(new String[] {"zhang1", "wang1", "lee1"}, "third");
        pgman.addUsersToGroup(new String[] {"zhang1", "wang1", "lee1"}, "fourth",
            new String[]{"fourth test group"});
        String[] grups = pgman.listGroups(new String[] {"group"});
        assertTrue(isIncluded(grups, "first"));
        assertTrue(isIncluded(grups, "fourth"));
//        for (int i = 0; i < grups.length; i++) {
//            System.out.println(grups[i]);
//        }
        String[] users = pgman.listUsersOfGroup("first", false);
        assertTrue(isIncluded(users, "zhang1"));
//        for (int i = 0; i < users.length; i++) {
//            System.out.println(users[i]);
//        }
        pgman.deleteGroups(new String[] {"fourth"});
        grups = pgman.listGroups(new String[] {"group"});
        assertFalse(isIncluded(grups, "fourth"));
    }
    
    public static void main(String[] args) throws Exception {
        
        junit.textui.TestRunner.run (new TestSuite(TestGroupman.class));
    }

    private boolean isIncluded(String[] handles, String handle) {
        for (int i = 0; i < handles.length; i++) {
            if(handle.equals(handles[i]))
                return true;
        }
        
        return false;
    }
}

