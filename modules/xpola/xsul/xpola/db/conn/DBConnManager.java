/**
 * ConnManager.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DBConnManager.java,v 1.5 2005/05/02 18:35:42 lifang Exp $
 */

package xsul.xpola.db.conn;

import java.sql.Connection;
import xsul.MLogger;

public class DBConnManager {
    private static final MLogger logger = MLogger.getLogger();
    
    public static final int ORACLE = 1;
    public static final int HSQL = 2;
    public static final int MYSQL = 3;
    public static final int POSTGRE = 4;
    public static final int DERBY = 5;
    public static final int DATASOURCE = 6;
    
    private static DBConnFactory dcf = null;
    private static DBConnManager connMgr = null;
    
    private DBConnManager() {
    }
    
    private DBConnManager(int dbType,
                          String db,
                          String user,
                          String password) throws Exception {
        if(dbType == HSQL) {
            dcf = HSqlDBConnFactory.getInstance();
            dcf.dbType = "HSQL";
        }
        else if(dbType == DERBY) {
            dcf = DerbyConnFactory.getInstance();
            dcf.dbType = "Derby";
        }
        else if(dbType == DATASOURCE) {
            dcf = DataSourceConnFactory.getInstance();
            dcf.dbType = "DataSource";
        }
        else {
            throw new Exception("database not supported yet.");
        }
        
        dcf.jdbcUrl+=db;
        dcf.user=user;
        dcf.password=password;
    }
    
    public static synchronized DBConnManager getInstance(){
        
        if(connMgr==null) {
            connMgr = new DBConnManager();
        }
        
        return connMgr;
    }
    
    public static synchronized DBConnManager getInstance(int dbType,
                                                         String db,
                                                         String user,
                                                         String password)
        throws Exception {
        
        if(connMgr==null) {
            connMgr = new DBConnManager(dbType, db, user, password);
        }
        
        return connMgr;
    }
    
    public void init() throws Exception {
        dcf.createTables();
    }
    
    public Connection getConnection() throws Exception {
        return dcf.getConnection();
    }
    
}

