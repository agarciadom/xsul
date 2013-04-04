/**
 * DerbyConnFactory.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DerbyConnFactory.java,v 1.5 2005/05/02 18:35:42 lifang Exp $
 */

package xsul.xpola.db.conn;
import xsul.xpola.*;

import java.sql.Connection;
import java.sql.Statement;
import xsul.MLogger;

public class DerbyConnFactory extends DBConnFactory {
    
    private static final MLogger logger = MLogger.getLogger();
    private static DBConnFactory dbf = null;
    
    private DerbyConnFactory() {
        jdbcUrl = "jdbc:derby:";
        driver = "org.apache.derby.jdbc.EmbeddedDriver";
    }
    
    public static synchronized DBConnFactory getInstance(){
        //If not initialized, do it here; otherwise return the existing object.
        if(dbf == null)
            dbf = new DerbyConnFactory();
        
        return dbf;
    }
    
    public Connection getConnection() throws Exception{
        //Call the base class method to provide the connection
        return super.getConnection();
    }
    
    public void createTables() throws Exception {
        logger.entering();
        
        Connection conn = getConnection();
        if(conn == null) {
            throw new Exception("connection null");
        }
        
        Statement statement = conn.createStatement();

        String sql1 = "CREATE TABLE " + XpolaConstants.CAP_TABLE + " (" +
            XpolaConstants.CAP_ID + " VARCHAR(20) NOT NULL PRIMARY KEY, " +
            XpolaConstants.OWNER_DN + " VARCHAR(40), " +
            XpolaConstants.HANDLE + " VARCHAR(40) NOT NULL, " +
            XpolaConstants.NOTBEFORE + " VARCHAR(20) NOT NULL, " +
            XpolaConstants.NOTAFTER + " VARCHAR(20) NOT NULL, " +
            XpolaConstants.ASSERTIONS + " LONG VARCHAR NOT NULL" +
            ")";
        logger.finest("sql1: " + sql1);
        statement.execute(sql1);
        
        String sql2 = "CREATE TABLE " + XpolaConstants.CAP_USER_TABLE + " (" +
            XpolaConstants.CAP_ID + " VARCHAR(20) NOT NULL, " +
            XpolaConstants.GROUP_NAME + " VARCHAR(40), " +
            XpolaConstants.USER_DN  + " VARCHAR(40), " +
            "FOREIGN KEY("+XpolaConstants.CAP_ID +
            ") references "+XpolaConstants.CAP_TABLE +
            "(" + XpolaConstants.CAP_ID+") ON DELETE CASCADE" +
            ")";
        logger.finest("sql2: " + sql2);
        statement.execute(sql2);
        
        String sql3 = "CREATE TABLE " + XpolaConstants.GROUP_USER_TABLE + " (" +
            XpolaConstants.GROUP_NAME + " VARCHAR(40) NOT NULL, " +
            XpolaConstants.USER_DN + " VARCHAR(40) " +
            ")";
        logger.finest("sql3: " + sql3);
        statement.execute(sql3);
        
        String sql4 = "CREATE TABLE " + XpolaConstants.REQUEST_TABLE + " (" +
            XpolaConstants.REQUEST_ID + " INT NOT NULL PRIMARY KEY, " +
            XpolaConstants.REQUEST_FROM + " VARCHAR(40) NOT NULL, " +
            XpolaConstants.REQUEST_RESOURCE + " VARCHAR(40) NOT NULL, " +
            XpolaConstants.HANDLE + " VARCHAR(40) NOT NULL, " +
            XpolaConstants.REQUEST_STATUS + " VARCHAR(10)" +
            // TODO: check how the foreign key works here for epr
//          ", FOREIGN KEY("+Constants.EPR_NAME +
//          ") references "+Constants.CAPTABLE_NAME +
//          "(" + Constants.EPR_NAME+") ON DELETE CASCADE" +
            ")";
        logger.finest("sql4: " + sql4);
        statement.execute(sql4);
        
        statement.close();
        conn.close();
        logger.exiting();
    }
    
}

