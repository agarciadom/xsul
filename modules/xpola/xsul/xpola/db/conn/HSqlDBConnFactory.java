/**
 * HSqlDBConnFactory.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: HSqlDBConnFactory.java,v 1.7 2005/05/02 18:35:42 lifang Exp $
 */

package xsul.xpola.db.conn;
import xsul.xpola.*;

import java.sql.Connection;
import java.sql.Statement;
import xsul.MLogger;
import java.sql.SQLException;

public class HSqlDBConnFactory extends DBConnFactory {
    
    private static final MLogger logger = MLogger.getLogger();
    private static DBConnFactory dbf = null;
    
    private HSqlDBConnFactory() {
        jdbcUrl = "jdbc:hsqldb:";
        driver = "org.hsqldb.jdbcDriver";
    }
    
    public static synchronized DBConnFactory getInstance(){
        //If not initialized, do it here; otherwise return the existing object.
        if(dbf == null)
            dbf = new HSqlDBConnFactory();
        
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
        String sql0 = "DROP TABLE " + XpolaConstants.CAP_USER_TABLE + ";";
        try {
            statement.execute(sql0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql0 = "DROP TABLE " + XpolaConstants.GROUP_USER_TABLE + ";";
        try {
            statement.execute(sql0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql0 = "DROP TABLE " + XpolaConstants.GROUP_TABLE + ";";
        try {
            statement.execute(sql0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql0 = "DROP TABLE " + XpolaConstants.REQUEST_TABLE + ";";
        try {
            statement.execute(sql0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql0 = "DROP TABLE " + XpolaConstants.CAP_TABLE + ";";
        try {
            statement.execute(sql0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        String sql1 = "CREATE TABLE " + XpolaConstants.CAP_TABLE + " (" +
            XpolaConstants.CAP_ID + " VARCHAR(20) NOT NULL PRIMARY KEY, " +
            XpolaConstants.OWNER_DN + " VARCHAR(40), " +
            XpolaConstants.HANDLE + " VARCHAR(40) NOT NULL, " +
            XpolaConstants.NOTBEFORE + " VARCHAR(20) NOT NULL, " +
            XpolaConstants.NOTAFTER + " VARCHAR(20) NOT NULL, " +
            XpolaConstants.ASSERTIONS + " LONGVARCHAR NOT NULL" +
            ");";
        logger.finest("sql1: " + sql1);
        statement.execute(sql1);
        
        String sql2 = "CREATE TABLE " + XpolaConstants.CAP_USER_TABLE + " (" +
            XpolaConstants.CAP_ID + " VARCHAR(20) NOT NULL, " +
            XpolaConstants.GROUP_NAME + " VARCHAR(40), " +
            XpolaConstants.USER_DN  + " VARCHAR(40), " +
            "FOREIGN KEY("+XpolaConstants.CAP_ID +
            ") references "+XpolaConstants.CAP_TABLE +
            "(" + XpolaConstants.CAP_ID + ") ON DELETE CASCADE" +
            ");";
        logger.finest("sql2: " + sql2);
        statement.execute(sql2);
        
        String sql3 =
            "CREATE TABLE " + XpolaConstants.GROUP_TABLE + " (" +
            XpolaConstants.GROUP_NAME + " VARCHAR(40) NOT NULL PRIMARY KEY, " +
            XpolaConstants.DESCRIPTION + " VARCHAR(200) " +
            ");";
        logger.finest("sql3: " + sql3);
        statement.execute(sql3);

        String sql4 =
            "CREATE TABLE " + XpolaConstants.GROUP_USER_TABLE + " (" +
            XpolaConstants.GROUP_NAME + " VARCHAR(40) NOT NULL, " +
            XpolaConstants.USER_DN + " VARCHAR(40), " +
            "FOREIGN KEY(" + XpolaConstants.GROUP_NAME +
            ") REFERENCES " + XpolaConstants.GROUP_TABLE +
            "(" + XpolaConstants.GROUP_NAME + ") ON DELETE CASCADE" +
            ");";
        logger.finest("sql4: " + sql4);
        statement.execute(sql4);
        
        String sql5 =
            "CREATE TABLE " + XpolaConstants.REQUEST_TABLE + " (" +
            XpolaConstants.REQUEST_ID + " VARCHAR(20) NOT NULL PRIMARY KEY, " +
            XpolaConstants.REQUEST_FROM + " VARCHAR(40) NOT NULL, " +
            XpolaConstants.REQUEST_RESOURCE + " VARCHAR(40) NOT NULL, " +
            XpolaConstants.REQUEST_ACTIONS + " VARCHAR(100) NOT NULL, " +
            XpolaConstants.REQUEST_STATUS + " VARCHAR(10)" +
//            ", FOREIGN KEY(" + Constants.REQUEST_RESOURCE +
//            ") references " + Constants.CAP_TABLE +
//            "(" + Constants.EPR + ") ON DELETE CASCADE" +
            ");";
        logger.finest("sql5: " + sql5);
        statement.execute(sql5);
        
        statement.close();
        conn.close();
        logger.exiting();
    }
}

