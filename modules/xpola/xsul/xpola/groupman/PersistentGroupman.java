/**
 * PersistentGroupman.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: PersistentGroupman.java,v 1.8 2005/04/28 02:49:52 lifang Exp $
 */

package xsul.xpola.groupman;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import xsul.MLogger;
import xsul.xpola.XpolaConstants;
import xsul.xpola.db.conn.DBConnManager;

public class PersistentGroupman extends GroupmanAbstractImpl {
    
    private static final MLogger logger = MLogger.getLogger();
    
    static private DBConnManager cm = null;
    
    public PersistentGroupman(int dbType, String db,
                              String user, String password) {
        try {
            cm = DBConnManager.getInstance(dbType, db, user, password);
        } catch (Exception e) {
            logger.severe("db connection failed", e);
            cm = null;
        }
    }
    
    public String[] listUsersOfGroup(String gname, boolean recursive)
        throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(gname == null) {
            logger.finest("group name null");
            return null;
        }
        
        String sql1 = "SELECT * FROM " + XpolaConstants.GROUP_USER_TABLE +
            " WHERE " + XpolaConstants.GROUP_NAME + "='" + gname + "';";
        logger.finest("sql: " + sql1);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql1);
        Vector users = new Vector(11);
        while(rs.next()) {
            String user = rs.getString(XpolaConstants.USER_DN);
            users.add(user);
        }
        statement.close();
        conn.close();
        return (String[])users.toArray(new String[0]);
    }
    
    public void addGroup(String gname, String[] metadata) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(gname == null) {
            logger.finest("group name null");
            return;
        }
        
        String desc = "";
        if(metadata != null && !metadata[0].equals("")) {
            desc = metadata[0];
        }
        String sqlscript = "INSERT INTO " + XpolaConstants.GROUP_TABLE
            + " VALUES ('" + gname + "', '" + desc + "');";
        logger.finest("sql: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sqlscript);
        statement.close();
        conn.close();
    }
    
    public void deleteGroups(String[] gnames) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(gnames == null) {
            logger.finest("group names null");
            return;
        }
        
        StringBuffer sqlscript = new StringBuffer("");
        String fixedhead = "DELETE FROM " + XpolaConstants.GROUP_TABLE
            + " WHERE " + XpolaConstants.GROUP_NAME + "='";
        for (int i = 0; i < gnames.length; i++) {
            sqlscript.append(fixedhead);
            sqlscript.append(gnames[i]+"';\n");
        }
        logger.finest("sql: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sqlscript.toString());
        statement.close();
        conn.close();
    }
    
    public void addUsersToGroup(String[] unames, String gname, String[] metadata)
        throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(unames == null) {
            logger.finest("user names null");
            return;
        }
        
        if(gname == null) {
            logger.finest("group name null");
            return;
        }
        String desc = "";
        if(metadata != null && !metadata[0].equals("")) {
            desc = metadata[0];
        }
        StringBuffer sqlscript =
            new StringBuffer("INSERT INTO " + XpolaConstants.GROUP_TABLE
                                 + " VALUES ('" + gname + "', '" + desc + "');\n");
        String fixedhead = "INSERT INTO " + XpolaConstants.GROUP_USER_TABLE
            + " VALUES ('" + gname + "', '";
        for (int i = 0; i < unames.length; i++) {
            sqlscript.append(fixedhead);
            sqlscript.append(unames[i]+"');\n");
        }
        logger.finest("sql: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sqlscript.toString());
        statement.close();
        conn.close();
    }
    
    public String[] listGroups(String[] metadata) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        String sqlscript = "SELECT " + XpolaConstants.GROUP_NAME + " FROM "
            + XpolaConstants.GROUP_TABLE;
        if(metadata != null && metadata[0] != null) {
            sqlscript += " WHERE " + XpolaConstants.DESCRIPTION + " LIKE '%" +
                metadata[0] + "%';";
        }
        logger.finest("sql: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlscript);
        Vector groups = new Vector(11);
        while(rs.next()) {
            String user = rs.getString(XpolaConstants.GROUP_NAME);
            groups.add(user);
        }
        statement.close();
        conn.close();
        return (String[])groups.toArray(new String[0]);
    }
    
    public void removeUsersFromGroup(String[] unames, String gname)
        throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(unames == null) {
            logger.finest("user names null");
            return;
        }
        
        if(gname == null) {
            logger.finest("group name null");
            return;
        }
        String fixedhead = "DELETE FROM " + XpolaConstants.GROUP_USER_TABLE
            + " WHERE " + XpolaConstants.GROUP_NAME + "='" + gname + "' AND ";
        StringBuffer sqlscript = new StringBuffer(fixedhead);
        int i = 0;
        for (i = 0; i < unames.length - 1; i++) {
            sqlscript.append(XpolaConstants.USER_DN+"='"+unames[i]+"' OR ");
        }
        sqlscript.append(XpolaConstants.USER_DN+"='"+unames[i] + "';");
        logger.finest("sql: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sqlscript.toString());
        statement.close();
        conn.close();
    }
    
}

