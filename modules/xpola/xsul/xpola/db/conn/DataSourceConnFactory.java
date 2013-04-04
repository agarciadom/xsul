/**
 * DataSourceConnFactory.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: DataSourceConnFactory.java,v 1.5 2005/05/02 18:35:42 lifang Exp $
 */

package xsul.xpola.db.conn;

import java.sql.Connection;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import xsul.MLogger;

public class DataSourceConnFactory  extends DBConnFactory {
    
    private static final MLogger logger = MLogger.getLogger();
    private static DataSource ds;
    public void createTables() throws Exception {
        throw new Exception("data source is not responsible for initializating tables");
    }
    
    private DataSourceConnFactory() {
        jdbcUrl = "java:comp/env/";
    }
    
    private static DBConnFactory dbf = null;
    
    public static synchronized DBConnFactory getInstance(){
        //If not initialized, do it here; otherwise return the existing object.
        if(dbf == null)
            dbf = new DataSourceConnFactory();
        
        return dbf;
    }
    
    public Connection getConnection() throws Exception{
        logger.entering();
        if(ds == null) {
            logger.finest("getting new ds");
            InitialContext initCtx = new InitialContext();
            logger.finest("got new initCtx");
            // database = "java:comp/env/jdbc/WroxTC41"
            DataSource _ds = (DataSource)initCtx.lookup(jdbcUrl);
            logger.finest("found new ds");
            ds = _ds;
        }
        else {
            logger.finest("reuse ds");
        }
        Connection conn = ds.getConnection();
        logger.exiting();
        return conn;
    }
}


