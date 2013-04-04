/**
 * XpolaFactory.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: XpolaFactory.java,v 1.7 2005/07/14 19:23:35 lifang Exp $
 */

package xsul.xpola;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import xsul.MLogger;
import xsul.xpola.XpolaConstants;
import xsul.xpola.capman.CapabilityManager;
import xsul.xpola.capman.CapmanClient;
import xsul.xpola.capman.PersistentCapman;
import xsul.xpola.capman.RequestManager;
import xsul.xpola.db.conn.DBConnManager;
import xsul.xpola.groupman.GroupManager;
import xsul.xpola.groupman.GroupmanClient;
import xsul.xpola.groupman.PersistentGroupman;

public class XpolaFactory {
    static private final MLogger logger = MLogger.getLogger();
    static private CapabilityManager cm;
    static private GroupManager gm;
    static private RequestManager rm;
    static private int dbType = 0;
    static private String dbname;
    static private String username;
    static private String password;
    static private int dbcontext = xsul.xpola.XpolaConstants.PORTAL_CONTEXT;
    
    // to make sure init is called
    static private XpolaFactory _fac = new XpolaFactory();
    private XpolaFactory() {
        init();
    }
    
    /**
     * If there is no location paramerter specified, we can get the db context
     * from the configuration in the portlet's web.xml
     *
     * @return   a CapabilityManager instance
     *
     */
    static public CapabilityManager getCapman() {
        return getCapman(dbcontext);
    }
    
    static public GroupManager getGroupman() {
        return getGroupman(dbcontext);
    }
    
    static public RequestManager getRequestman() {
        return getRequestman(dbcontext);
    }
    
    static public CapabilityManager getCapman(String svcloc) {
//        if(cm == null) {
//            CapmanClient client = new CapmanClient(svcloc);
//            cm = client;
//            rm = client;
//        }
//        return cm;
        return new CapmanClient(svcloc);
    }
    
    static public GroupManager getGroupman(String svcloc) {
//        if(gm == null) {
//            GroupmanClient client = new GroupmanClient(svcloc);
//            gm = client;
//        }
//        return gm;
        return new GroupmanClient(svcloc);
    }
    
    static public RequestManager getRequestman(String svcloc) {
//        if(rm == null) {
//            getCapman(svcloc);
//        }
//        return rm;
        return new CapmanClient(svcloc);
    }
    
    static public CapabilityManager getCapman(int _dbType, String _dbname,
                                              String _username, String _passwd) {
        if(cm == null) {
            PersistentCapman capman =
                new PersistentCapman(_dbType, _dbname, _username, _passwd);
            cm = capman;
            rm = capman;
            logger.finest("got new capman");
        }
        return cm;
    }
    
    static public GroupManager getGroupman(int _dbType, String _dbname,
                                           String _username, String _passwd) {
        if(gm == null) {
            gm = new PersistentGroupman(_dbType, _dbname, _username, _passwd);
            logger.finest("got new groupman");
        }
        return gm;
    }
    
    static public RequestManager getRequestman(int _dbType, String _dbname,
                                               String _username, String _passwd) {
        if(rm == null) {
            getCapman(_dbType, _dbname, _username, _passwd);
        }
        return rm;
    }
    
    static public CapabilityManager getCapman(int location) {
        if(cm == null) {
            if(location == xsul.xpola.XpolaConstants.WEBAPP_CONTEXT) {
                PersistentCapman capman =
                    new PersistentCapman(dbType, dbname, username, password);
                cm = capman;
                rm = capman;
                logger.finest("got new capman");
            }
            else if(location == xsul.xpola.XpolaConstants.PORTAL_CONTEXT) {
                PersistentCapman capman =
                    new PersistentCapman(DBConnManager.DATASOURCE,
                                         XpolaConstants.PORTAL_DATASOURCE,
                                         null, null);
                cm = capman;
                rm = capman;
                logger.finest("got new capman");
            }
        }
        
        return cm;
    }
    
    static public RequestManager getRequestman(int location) {
        if(rm == null) {
            getCapman(location);
        }
        return rm;
    }
    
    static public GroupManager getGroupman(int location) {
        if(gm == null) {
            if(location == xsul.xpola.XpolaConstants.WEBAPP_CONTEXT) {
                gm = new PersistentGroupman(dbType, dbname, username, password);
                logger.finest("got group capman ");
            }
            else if(location == xsul.xpola.XpolaConstants.PORTAL_CONTEXT) {
                gm = new PersistentGroupman(DBConnManager.DATASOURCE,
                                            XpolaConstants.PORTAL_DATASOURCE,
                                            null, null);
                logger.finest("got group capman ");
            }
        }
        return gm;
    }
    
    private void init() {
        logger.entering();
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            
            // Look up environment entry
            String sdbcontext = (String)envCtx.lookup(XpolaConstants.DBCONTEXT);
            logger.finest("getting capman db from: " + sdbcontext);
            if(sdbcontext.equalsIgnoreCase("Portal")) {
                dbcontext = XpolaConstants.PORTAL_CONTEXT;
            }
            else if(sdbcontext.equalsIgnoreCase("Webapp")) {
                dbcontext = XpolaConstants.WEBAPP_CONTEXT;
                String dburl = (String)envCtx.lookup(XpolaConstants.DBURL);
                username = (String)envCtx.lookup(XpolaConstants.USERNAME);
                password = (String)envCtx.lookup(XpolaConstants.PASSWORD);
                dbType = DBConnManager.HSQL;
                if(dburl != null) {
                    if(dburl.indexOf("hsql") > 0) {
                        dbType = DBConnManager.HSQL;
                        dbname = dburl.substring("jdbc:hsqldb:".length());
                    }
                    else if(dburl.indexOf("derby") > 0) {
                        dbType = DBConnManager.DERBY;
                        dbname = dburl.substring("jdbc:derby:".length());
                    }
                }
                logger.finest("dbname: " + dbname);
            }
        } catch (NamingException e) {
            logger.warning("", e);
        }
        logger.exiting();
    }
    
}
