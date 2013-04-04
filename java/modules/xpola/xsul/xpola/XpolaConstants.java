/**
 * Constants.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: XpolaConstants.java,v 1.5 2005/04/09 05:03:41 lifang Exp $
 */

package xsul.xpola;



public interface XpolaConstants {
    // capman in context
    public static final String STORAGE_LOCATION = "xportlets.capabilitymanager";
    public static final int SYSTEM_PROPERTIES = 0;
    public static final int PORTAL_CONTEXT = 1;
    public static final int WEBAPP_CONTEXT = 2;
    
    // For database
    // table caps and capusers begin --
    public static final String CAP_TABLE = "cap_table";
    public static final String CAP_ID = "capid";
    public static final String OWNER_DN = "ownerdn";
    public static final String HANDLE = "handle";
    public static final String NOTBEFORE = "notbefore";
    public static final String NOTAFTER  = "notafter";
    public static final String ASSERTIONS = "assertions";
    // table caps  -- end
    
    // table cap-user begin --
    public static final String CAP_USER_TABLE = "cap_user_table";
    // CAP_ID -- CAPID
    // GROUP_NAME
    // USER_DN
    // table cap-user -- end
    
    // table group table begin
    public static final String GROUP_TABLE = "group_table";
    public static final String GROUP_NAME = "groupname";
    public static final String DESCRIPTION = "description";
    
    // table group-users begin --
    public static final String GROUP_USER_TABLE = "group_user_table";
    // could be the user's DN, or a role name
    //    public static String USER_ID = "username";  // the role name
    public static String USER_DN = "userdn";    // the user's dn
    // table user -- end
    
    // table requests begin --
    public static final String REQUEST_TABLE = "request_table";
    public static final String REQUEST_ID = "reqid";
    public static final String REQUEST_RESOURCE = "resource";
    public static final String REQUEST_ACTIONS = "actions";
    public static final String REQUEST_FROM = "issuer";
    public static final String REQUEST_TO = "to_user";
    public static final String REQUEST_STATUS = "status";
    // table requests -- end
    
    // running mode for capman portlet
    public static final String PROVIDER = "Provider";
    public static final String USER = "User";
    
    // storage option
    public static final String STORAGE = "capman.storage";
    public static final String HSQL    = "hsql";
    public static final String MYSQL   = "mysql";
    public static final String HIBERNATE="hibernate";
    public static final String MEMORY  = "memory";
    
    // defined in web.xml
    public static final String DBCONTEXT = "DBCONTEXT";
    public static final String DBTYPE = "DBType";
    public static final String DBURL = "DBURL";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String PORTAL_DATASOURCE = "jdbc/capstorage";
    
    public static final String LEADNAMESPACE = "http://www.extreme.indiana.edu/lead";
}

