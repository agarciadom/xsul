/**
 * PersistentCapman.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: PersistentCapman.java,v 1.13 2005/05/08 20:20:04 lifang Exp $
 */

package xsul.xpola.capman;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import xsul.MLogger;
import xsul.dsig.saml.authorization.CapConstants;
import xsul.dsig.saml.authorization.Capability;
import xsul.dsig.saml.authorization.CapabilityRequest;
import xsul.dsig.saml.authorization.CapabilityResponse;
import xsul.xpola.XpolaConstants;
import xsul.xpola.db.conn.DBConnManager;

public class PersistentCapman extends CapmanAbstractImpl {
    
    private static final MLogger logger = MLogger.getLogger();
    private static SimpleDateFormat formatter =
        new SimpleDateFormat(CapConstants.DATEFORMAT);
    
    static private DBConnManager cm = null;
    
    public PersistentCapman(int dbType, String db,
                            String user, String password) {
        try {
            cm = DBConnManager.getInstance(dbType, db, user, password);
        } catch (Exception e) {
            logger.severe("db connection failed", e);
            cm = null;
        }
    }
    
    public void registerCapability(String acap) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(acap == null || acap.equals("")) {
            throw new Exception("capability null");
        }
        logger.entering();
        Capability cap = new Capability(acap);
        registerCapability(cap, acap);
        logger.exiting();
    }
    
    private void registerCapability(Capability cap, String acap)
        throws Exception {
        logger.entering();
        Date notbefore = cap.getNotbefore();
        Date notafter = cap.getNotafter();
        
        // merge the old cap tokens into the new one
        String oldcapstr = getCapabilityByHandle(cap.getResource());
        acap += oldcapstr;
        
        String sqlscript = "DELETE FROM " + XpolaConstants.CAP_TABLE +
            " WHERE " + XpolaConstants.HANDLE + "='" +
            cap.getResource() + "';\n" +
            // fixme: remove other capabilities with the same handles;
            // while what is supposed to be is to remove the records in
            // cap_user_table with duplicated userdn or groupname
            // this will work for the time being, but definitely need to be
            // fixed later.
            "INSERT INTO " + XpolaConstants.CAP_TABLE + " VALUES ('" +
            cap.getId() + "', '" +
            cap.getOwner() + "', '" +
            cap.getResource() + "', \n'" +
            formatter.format(notbefore) + "', '" +
            formatter.format(notafter) + "', \n'" +
            acap + "'" +
            ");\n";
        logger.finest("sql script for inserting a cap: " + sqlscript);
        Iterator iter = cap.getUsers().iterator();
        while(iter.hasNext()) {
            String name = (String)iter.next();
            // fixme: stupid way to judge whether it is a DN or groupname
            // may have some better/explicit way later ...
            if(name.indexOf("CN=") >= 0 ) {
                logger.finest("username: " + name);
                sqlscript += "INSERT INTO " + XpolaConstants.CAP_USER_TABLE
                    + " VALUES ('" + cap.getId() + "', '', '" + name + "');\n";
            }
            else {
                logger.finest("groupname: " + name);
                sqlscript += "INSERT INTO " + XpolaConstants.CAP_USER_TABLE
                    + " VALUES ('" + cap.getId() + "', '" + name + "', '');\n";
            }
        }
        logger.finest("sql script for inserting a cap: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sqlscript);
        statement.close();
        conn.close();
        logger.exiting();
    }
    
    public String getCapability(String handle, String userdn) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(handle == null || handle.equals("")) {
            throw new Exception("handle null");
        }
        
        if(userdn == null || userdn.equals("")) {
            throw new Exception("userdn null");
        }
        String capstr = "";
        logger.entering();
        String sql1 = "SELECT * FROM " + XpolaConstants.CAP_TABLE +
            " WHERE " + XpolaConstants.HANDLE + "='" + handle + "';";
        logger.finest("sql1: " + sql1);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs1 = statement.executeQuery(sql1);
        while(rs1.next()) {
            String id = rs1.getString(XpolaConstants.CAP_ID);
            String owner = rs1.getString(XpolaConstants.OWNER_DN);
            String notbefore = rs1.getString(XpolaConstants.NOTBEFORE);
            String notafter = rs1.getString(XpolaConstants.NOTAFTER);
            String assertions = rs1.getString(XpolaConstants.ASSERTIONS) + "\n";
            
            String sql2 = "SELECT * FROM " + XpolaConstants.CAP_USER_TABLE +
                " WHERE " + XpolaConstants.CAP_ID + "='" + id + "';";
            logger.finest("sql2: " + sql2);
            ResultSet rs2 = statement.executeQuery(sql2);
            Vector users = new Vector(11);
            boolean found = false;
            while(rs2.next()) {
                String grup = rs2.getString(XpolaConstants.GROUP_NAME);
                if(grup != null && !grup.equals("")) {
                    // look for users in group_user_table
                    String sql3 = "SELECT * FROM " + XpolaConstants.GROUP_USER_TABLE+
                        " WHERE " + XpolaConstants.GROUP_NAME + "='" + grup +"';";
                    logger.finest("sql3: " + sql3);
                    ResultSet rs3 = statement.executeQuery(sql3);
                    while(rs3.next()) {
                        String user = rs3.getString(XpolaConstants.USER_DN);
                        if(user.equals(userdn)) {
                            found = true;
                            break;
                        }
                    }
                }
                else {
                    String user = rs2.getString(XpolaConstants.USER_DN);
                    if(user.equals(userdn)) {
                        found = true;
                    }
                }
                if(found == true) {
                    users.add(userdn);
                    // dont care about other users at all
                    break;
                }
            }
            
            Capability thecap = new Capability();
            if(assertions != null)
                thecap = new Capability(assertions);
            thecap.setId(id);
            thecap.setResource(handle);
            thecap.setOwner(owner);
            thecap.setUsers(users);
            try {
                thecap.setNotbefore(formatter.parse(notbefore));
                thecap.setNotafter(formatter.parse(notafter));
            }
            catch (ParseException e) {}
            try {
                thecap.sign(null);
            }
            catch (Exception e) {
                // it's all right if it has been signed.
                logger.severe("failed to sign the cap", e);
            }
            capstr += thecap.toString();
        }
        statement.close();
        conn.close();
        logger.exiting();
        return capstr;
    }
    
    public String getCapabilityByHandle(String handle) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(handle == null || handle.equals("")) {
            throw new Exception("handle null");
        }
        
        String capstr = "";
        logger.entering();
        String sql1 = "SELECT * FROM " + XpolaConstants.CAP_TABLE +
            " WHERE " + XpolaConstants.HANDLE + "='" + handle + "';";
        logger.finest("sql1: " + sql1);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs1 = statement.executeQuery(sql1);
        while(rs1.next()) {
            String id = rs1.getString(XpolaConstants.CAP_ID);
            String owner = rs1.getString(XpolaConstants.OWNER_DN);
            String notbefore = rs1.getString(XpolaConstants.NOTBEFORE);
            String notafter = rs1.getString(XpolaConstants.NOTAFTER);
            String assertions = rs1.getString(XpolaConstants.ASSERTIONS) + "\n";
            
            String sql2 = "SELECT * FROM " + XpolaConstants.CAP_USER_TABLE +
                " WHERE " + XpolaConstants.CAP_ID + "='" + id + "';";
            logger.finest("sql2: " + sql2);
            ResultSet rs2 = statement.executeQuery(sql2);
            Vector users = new Vector(11);
            while(rs2.next()) {
                String grup = rs2.getString(XpolaConstants.GROUP_NAME);
                if(grup != null && !grup.equals("")) {
                    // look for users in group_user_table
                    String sql3 = "SELECT * FROM " + XpolaConstants.GROUP_USER_TABLE+
                        " WHERE " + XpolaConstants.GROUP_NAME + "='" + grup +"';";
                    logger.finest("sql3: " + sql3);
                    ResultSet rs3 = statement.executeQuery(sql3);
                    while(rs3.next()) {
                        String user = rs3.getString(XpolaConstants.USER_DN);
                        users.add(user);
                    }
                }
                else {
                    String user = rs2.getString(XpolaConstants.USER_DN);
                    users.add(user);
                }
            }
            
            Capability thecap = new Capability();
            if(assertions != null)
                thecap = new Capability(assertions);
            thecap.setId(id);
            thecap.setResource(handle);
            thecap.setOwner(owner);
            thecap.setUsers(users);
            try {
                thecap.setNotbefore(formatter.parse(notbefore));
                thecap.setNotafter(formatter.parse(notafter));
            }
            catch (ParseException e) {}
            capstr += thecap.toString();
        }
        statement.close();
        conn.close();
        logger.exiting();
        return capstr;
    }
    
    public String[] getCapabilitiesByOwner(String owner) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(owner == null || owner.equals("")) {
            throw new Exception("owner null");
        }
        
        logger.entering();
        logger.finest("got statement");
        String sql1 = "SELECT * FROM " + XpolaConstants.CAP_TABLE +
            " WHERE " + XpolaConstants.OWNER_DN + "='" + owner + "';";
        logger.finest("sql1: " + sql1);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs1 = statement.executeQuery(sql1);
        Vector capv = new Vector(11);
        while(rs1.next()) {
            String id = rs1.getString(XpolaConstants.CAP_ID);
            String handle = rs1.getString(XpolaConstants.HANDLE);
            String notbefore = rs1.getString(XpolaConstants.NOTBEFORE);
            String notafter = rs1.getString(XpolaConstants.NOTAFTER);
            String assertions = rs1.getString(XpolaConstants.ASSERTIONS) + "\n";
            
            String sql2 = "SELECT * FROM " + XpolaConstants.CAP_USER_TABLE +
                " WHERE " + XpolaConstants.CAP_ID + "='"+ id +"';";
            logger.finest("sql2: " + sql2);
            ResultSet rs2 = statement.executeQuery(sql2);
            Vector users = new Vector(11);
            while(rs2.next()) {
                String grup = rs2.getString(XpolaConstants.GROUP_NAME);
                if(grup != null && !grup.equals("")) {
                    // look for users in group_user_table
                    String sql3 = "SELECT * FROM " + XpolaConstants.GROUP_USER_TABLE+
                        " WHERE " + XpolaConstants.GROUP_NAME + "='" + grup +"';";
                    logger.finest("sql3: " + sql3);
                    ResultSet rs3 = statement.executeQuery(sql3);
                    while(rs3.next()) {
                        String user = rs3.getString(XpolaConstants.USER_DN);
                        users.add(user);
                    }
                }
                else {
                    String user = rs2.getString(XpolaConstants.USER_DN);
                    users.add(user);
                }
            }
            
            Capability thecap = new Capability();
            if(assertions != null)
                thecap = new Capability(assertions);
            thecap.setId(id);
            thecap.setResource(handle);
            thecap.setOwner(owner);
            thecap.setUsers(users);
            try {
                thecap.setNotbefore(formatter.parse(notbefore));
                thecap.setNotafter(formatter.parse(notafter));
            }
            catch (ParseException e) {}
            
            capv.add(thecap.toString());
        }
        statement.close();
        conn.close();
        logger.exiting();
        return (String[])capv.toArray(new String[0]);
    }
    
    public void revokeCapabilityByHandle(String handle) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(handle == null || handle.equals("")) {
            throw new Exception("epr null");
        }
        
        logger.entering();
        // cascatingly delete the entries in cap_user_table
        // so don't worry about it
        String sqlscript = "DELETE FROM " + XpolaConstants.CAP_TABLE + " WHERE " +
            XpolaConstants.HANDLE + "='"+ handle +"';";
        logger.finest("sql script for deleting a cap: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sqlscript);
        statement.close();
        conn.close();
        logger.exiting();
    }
    
    public void revokeCapabilitiesByOwner(String owner) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(owner == null) {
            throw new Exception("owner null");
        }
        
        logger.entering();
        // cascatingly delete the entries in cap_user_table
        // so don't worry about it
        String sqlscript = "DELETE FROM " + XpolaConstants.CAP_TABLE + " WHERE " +
            XpolaConstants.OWNER_DN + "='"+ owner +"';";
        logger.finest("sql script for deleting caps by owner: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sqlscript);
        statement.close();
        conn.close();
        logger.exiting();
    }
    
    public void updateCapability(String acap) throws Exception {
        Capability cap = new Capability(acap);
        revokeCapabilityByHandle(cap.getResource());
        registerCapability(cap, acap);
    }
    
    public String[] getAllCapabilityHandles() throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        logger.entering();
        String sqlscript = "SELECT DISTINCT " + XpolaConstants.HANDLE + " FROM " +
            XpolaConstants.CAP_TABLE + "; ";
        logger.finest("sql script for getting all cap handles: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlscript);
        Vector handlev = new Vector(11);
        while(rs.next()) {
            handlev.add(rs.getString(XpolaConstants.HANDLE));
        }
        statement.close();
        conn.close();
        logger.exiting();
        return (String[])handlev.toArray(new String[0]);
    }
    
    public String[] getCapabilityHandlesByOwner(String ownerdn)
        throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(ownerdn == null) {
            throw new Exception("ownerdn null");
        }
        
        logger.entering();
        String sqlscript = "SELECT DISTINCT " + XpolaConstants.HANDLE + " FROM " +
            XpolaConstants.CAP_TABLE + " WHERE " + XpolaConstants.OWNER_DN + "='"
            + ownerdn + "';";
        logger.finest("sql script f getting owner's cap handles: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlscript);
        Vector handlev = new Vector(11);
        while(rs.next()) {
            handlev.add(rs.getString(XpolaConstants.HANDLE));
        }
        statement.close();
        conn.close();
        logger.exiting();
        return (String[])handlev.toArray(new String[0]);
    }
    
    public String[] getCapabilityHandlesByUser(String userdn)
        throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(userdn == null) {
            throw new Exception("userdn null");
        }
        
        logger.entering();
        String sqlscript = "SELECT DISTINCT " + XpolaConstants.CAP_ID + " FROM " +
            XpolaConstants.CAP_USER_TABLE + " WHERE " + XpolaConstants.USER_DN + "='"
            + userdn + "';";
        logger.finest("sql script f getting user's cap handles: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlscript);
        Vector handlev = new Vector(11);
        while(rs.next()) {
            String id = rs.getString(XpolaConstants.CAP_ID);
            String sqlscript2 = "SELECT "+ XpolaConstants.HANDLE + " FROM "
                + XpolaConstants.CAP_TABLE + " WHERE " + XpolaConstants.CAP_ID
                + "='" + id + "';";
            ResultSet rs2 = statement.executeQuery(sqlscript2);
            if(rs2.next()) {
                handlev.add(rs2.getString(XpolaConstants.HANDLE));
            }
        }
        statement.close();
        conn.close();
        logger.exiting();
        return (String[])handlev.toArray(new String[0]);
    }
    
    public void registerRequest(String request) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(request == null) {
            throw new Exception("request null");
        }
        
        logger.entering();
        CapabilityRequest creq = new CapabilityRequest(request);
        String[] actions = creq.getActions();
        StringBuffer actionstr = new StringBuffer("");
        for (int i = 0; i < actions.length; i++) {
            actionstr.append(actions[i] + ";");
        }
        String sqlscript =
            "INSERT INTO " + XpolaConstants.REQUEST_TABLE + " VALUES ('" +
            creq.getId() + "', '" +
            creq.getIssuer() + "', '" +
            creq.getResourceInRequest() + "', '" +
            actionstr + "', '" +
            creq.getStatus() + "'" +
            ");\n";
        logger.finest("sql script for inserting a cap: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sqlscript);
        statement.close();
        conn.close();
        logger.exiting();
    }
    
    public void responseToRequest(String response) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(response == null) {
            throw new Exception("response null");
        }
        
        logger.entering();
        CapabilityResponse cresp = new CapabilityResponse(response);
        String status = cresp.getStatus();
        if(status.equals(CapConstants.SUCCESS)) {
            Capability cap = cresp.getCapability();
            if(cap != null) {
                String capstr =
                    response.substring(response.indexOf("<Capability>")
                                           + "<Capability>".length(),
                                       response.indexOf("</Capability>"));
                registerCapability(cap, capstr);
            }
            else {
                logger.warning("cannot fiind capability string");
            }
        }
        String id = cresp.getInResponseTo();
        String sqlscript = "UPDATE " + XpolaConstants.REQUEST_TABLE + " SET " +
            XpolaConstants.REQUEST_STATUS + "='"+status+"' WHERE " +
            XpolaConstants.REQUEST_ID + "='" + id + "'";
        logger.finest("sql script for responding to a request: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sqlscript);
        statement.close();
        conn.close();
        logger.entering();
    }
    
    public String getRequestById(String id) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(id == null) {
            throw new Exception("id null");
        }
        
        String reqstr = null;
        logger.entering();
        String sqlscript = "SELECT * FROM " + XpolaConstants.REQUEST_TABLE +
            " WHERE " + XpolaConstants.REQUEST_ID + "='" + id + "';";
        logger.finest("sql script for getting a request by id: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlscript);
        if(rs.next()) {
            String status = rs.getString(XpolaConstants.REQUEST_STATUS);
            String actionstr = rs.getString(XpolaConstants.REQUEST_ACTIONS);
            String from = rs.getString(XpolaConstants.REQUEST_FROM);
            String resource = rs.getString(XpolaConstants.REQUEST_RESOURCE);
            StringBuffer strbuf = new StringBuffer("");
            strbuf.append("<CapabilityRequest>\n");
            strbuf.append("<Id>"+id+"</Id>\n");
            strbuf.append("<Issuer>"+from+"</Issuer>\n");
            strbuf.append("<Resource>"+resource+"</Resource>\n");
            String[] actions = actionstr.split(";");
            for (int i = 0; i < actions.length; i++) {
                strbuf.append("<Action>"+actions[i]+"</Action>\n");
            }
            strbuf.append("<Status>"+status+"</Status>\n");
            strbuf.append("</CapabilityRequest>");
            logger.finest("caprequest: " + strbuf);
            reqstr = strbuf.toString();
        }
        statement.close();
        conn.close();
        logger.exiting();
        return reqstr;
    }
    
    public String[] getRequestsByIssuer(String issuer) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(issuer == null) {
            throw new Exception("issuer null");
        }
        
        logger.entering();
        String sqlscript = "SELECT * FROM " + XpolaConstants.REQUEST_TABLE +
            " WHERE " + XpolaConstants.REQUEST_FROM + "='" + issuer + "';";
        logger.finest("sqlscript for getting requests by issuer: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlscript);
        Vector requestv = new Vector(11);
        while(rs.next()) {
            String status = rs.getString(XpolaConstants.REQUEST_STATUS);
            String actionstr = rs.getString(XpolaConstants.REQUEST_ACTIONS);
            String id = rs.getString(XpolaConstants.REQUEST_ID);
            String resource = rs.getString(XpolaConstants.REQUEST_RESOURCE);
            StringBuffer reqbuf = new StringBuffer("");
            reqbuf.append("<CapabilityRequest>\n");
            reqbuf.append("<Id>"+id+"</Id>\n");
            reqbuf.append("<Issuer>"+issuer+"</Issuer>\n");
            reqbuf.append("<Resource>"+resource+"</Resource>\n");
            String[] actions = actionstr.split(";");
            for (int i = 0; i < actions.length; i++) {
                reqbuf.append("<Action>"+actions[i]+"</Action>\n");
            }
            reqbuf.append("<Status>"+status+"</Status>\n");
            reqbuf.append("</CapabilityRequest>");
            logger.finest("caprequest: " + reqbuf);
            requestv.add(reqbuf.toString());
        }
        statement.close();
        conn.close();
        logger.exiting();
        return (String[])requestv.toArray(new String[0]);
    }
    
    public String[] getRequestsByReceiver(String receiver) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(receiver == null) {
            throw new Exception("receiver null");
        }
        
        logger.entering();
        String sqlscript = "SELECT DISTINCT " + XpolaConstants.HANDLE + " FROM "
            + XpolaConstants.CAP_TABLE + " WHERE " + XpolaConstants.OWNER_DN + "='"
            + receiver + "';";
        logger.finest("script for getting requests by receiver: " + sqlscript);
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sqlscript);
        logger.finest("executed ...");
        Vector requestv = new Vector(11);
        while(rs.next()) {
            String handle = rs.getString(XpolaConstants.HANDLE);
            String sql2 = "SELECT * FROM " + XpolaConstants.REQUEST_TABLE +
                " WHERE " + XpolaConstants.REQUEST_RESOURCE +
                "='" + handle + "';";
            logger.finest("script2: " + sql2);
            ResultSet rs2 = statement.executeQuery(sql2);
            while(rs2.next()) {
                String status = rs.getString(XpolaConstants.REQUEST_STATUS);
                String astr = rs.getString(XpolaConstants.REQUEST_ACTIONS);
                String id = rs.getString(XpolaConstants.REQUEST_ID);
                String issuer = rs.getString(XpolaConstants.REQUEST_FROM);
                StringBuffer reqbuf = new StringBuffer("");
                reqbuf.append("<CapabilityRequest>\n");
                reqbuf.append("<Id>"+id+"</Id>\n");
                reqbuf.append("<Issuer>"+issuer+"</Issuer>\n");
                reqbuf.append("<Resource>"+handle+"</Resource>\n");
                String[] actions = astr.split(";");
                for (int i = 0; i < actions.length; i++) {
                    reqbuf.append("<Action>"+actions[i]+"</Action>\n");
                }
                reqbuf.append("<Status>"+status+"</Status>\n");
                reqbuf.append("</CapabilityRequest>");
                logger.finest("caprequest: " + reqbuf);
                requestv.add(reqbuf.toString());
            }
        }
        statement.close();
        conn.close();
        logger.exiting();
        return (String[])requestv.toArray(new String[0]);
    }
    
    public void removeRequestById(String id) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(id == null) {
            throw new Exception("id null");
        }
        
        logger.entering();
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        String sqlscript = "DELETE FROM " + XpolaConstants.REQUEST_TABLE
            + " WHERE " + XpolaConstants.REQUEST_ID + "='"+ id +"';";
        logger.finest("sql script for deleting a req by id: " + sqlscript);
        statement.execute(sqlscript);
        statement.close();
        conn.close();
        logger.exiting();
    }
    
    public void removeRequestsByIssuer(String issuer) throws Exception {
        if(cm == null) {
            throw new Exception("dababase not initialized");
        }
        
        if(issuer == null) {
            throw new Exception("issuer null");
        }
        
        logger.entering();
        Connection conn = cm.getConnection();
        Statement statement = conn.createStatement();
        String sqlscript = "DELETE FROM " + XpolaConstants.REQUEST_TABLE
            + " WHERE " + XpolaConstants.REQUEST_FROM + "='"+ issuer +"';";
        logger.finest("sql script for deleting a req by issuer: " + sqlscript);
        statement.execute(sqlscript);
        statement.close();
        conn.close();
        logger.exiting();
    }
    
}

