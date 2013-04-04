/**
 * DBConnFactory.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DBConnFactory.java,v 1.5 2005/05/02 18:35:42 lifang Exp $
 */

package xsul.xpola.db.conn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import xsul.MLogger;

public abstract class DBConnFactory {
    
    private static final MLogger logger = MLogger.getLogger();
    
    protected String dbType = null;
    protected String user = null;
    protected String password = null;
    protected String driver = null;
    protected String jdbcUrl = null;
    
    public Connection getConnection() throws Exception{

        Connection conn;
        //Register a driver
        Class.forName(driver).newInstance();
        
        //Obtain a Connection object
        logger.finer("Connecting to " + dbType + " database " + jdbcUrl + "...");
        if(user == null || password == null) {
            conn = DriverManager.getConnection(jdbcUrl);
        }
        else {
            conn = DriverManager.getConnection(jdbcUrl, user, password);
        }
        logger.finer("Connection successful..");
        
        return conn;
    }
        
    abstract public void createTables() throws Exception;
}

