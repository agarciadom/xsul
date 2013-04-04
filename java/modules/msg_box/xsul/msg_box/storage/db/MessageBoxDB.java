package xsul.msg_box.storage.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import xsul.MLogger;
import xsul.msg_box.MessageBoxConstants;


/**
 * @author Chathura Herath (cherath@cs.indiana.edu)
 */

public class MessageBoxDB {

    private final static MLogger logger = MLogger.getLogger();

    private static JdbcStorage db = new JdbcStorage(MessageBoxConstants.MESSAGEBOX_DB_CONFIG_NAME, true);
    public static final String TABLE_NAME = "msgbox";
    
    
    
    public static final String SQL_INSERT_STATEMENT = "INSERT INTO "
        + TABLE_NAME + " (xml, msgboxid) " + "VALUES (?,?)";
    
    public static final String SQL_DELETE_ALL_STATEMENT = "DELETE FROM "+TABLE_NAME+" WHERE msgboxid='";
    
    public static final String SQL_DELETE_ONE_STATEMENT  = "DELETE FROM "+TABLE_NAME+" WHERE id ='";

    public static final String SQL_SELECT_STATEMENT1 = "SELECT * FROM "+TABLE_NAME+" WHERE msgboxid='";
    public static final String SQL_SELECT_STATEMENT2 = "' ORDER BY id";
    public MessageBoxDB(){
        
    }

    public void addMessage(String msgBoxID, String message) throws Exception{
        Connection connection = db.connect();
        PreparedStatement stmt = connection.prepareStatement(SQL_INSERT_STATEMENT);        
        byte[] buffer;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(output);
        out.writeObject(message);
        buffer = output.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        stmt.setBinaryStream(1, in, buffer.length);
        stmt.setString(2, msgBoxID);
        db.insert(stmt);
        stmt.close();        
        connection.commit();
        db.closeConnection(connection);
    }
    
    public void removeAllMessages(String key) throws SQLException{
        Connection connection = db.connect();
        PreparedStatement stmt = connection.prepareStatement(SQL_DELETE_ALL_STATEMENT+key+"'");  
        db.insert(stmt);
        stmt.close();
        connection.commit();
        db.closeConnection(connection);
    }
    
    public String removeOneMessage(String key) throws SQLException, IOException{
        Connection connection = db.connect();
        PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_STATEMENT1+key+SQL_SELECT_STATEMENT2);
        ResultSet resultSet = stmt.executeQuery();
        String obj = null;
        int id = -1;
        if(resultSet.next()){
            id = resultSet.getInt(1);
            InputStream in = resultSet.getAsciiStream(2);
            ObjectInputStream s = new ObjectInputStream(in);
            try {
                obj = (String)s.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }            
        }
        resultSet.close();
        stmt.close();
        stmt = connection.prepareStatement(SQL_DELETE_ONE_STATEMENT+id+"'");  
        db.insert(stmt);
        stmt.close();
        connection.commit();       
        db.closeConnection(connection);
        return obj;
    }
    

}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (C) 2004 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */

