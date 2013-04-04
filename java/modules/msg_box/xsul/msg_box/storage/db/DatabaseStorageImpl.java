package xsul.msg_box.storage.db;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import xsul.MLogger;
import xsul.XsulException;
import xsul.msg_box.storage.MsgBoxStorage;
import xsul.util.FastUUIDGen;

/**
 * @author Chathura Herath (cherath@cs.indiana.edu)
 */

public class DatabaseStorageImpl implements MsgBoxStorage {

    private static MessageBoxDB messageBoxDB = new MessageBoxDB();

    private final static MLogger logger = MLogger.getLogger();

    public String createMsgBox() throws XsulException {
        String uuid = FastUUIDGen.nextUUID();// generate uuid
        logger.finest("created mailbox " + uuid);
        return uuid;
    }

    public void destroyMsgBox(String key) throws XsulException {
        try {
            messageBoxDB.removeAllMessages(key);
        } catch (SQLException e) {
            throw new XsulException(
                    "Could not destroy the messagebox with key " + key, e);
        }
        logger.finest("Messagebox with key " + key
                + " was destroyed successfully");

    }

    public List takeMessagesFromMsgBox(String key) throws XsulException {
        String message;
        try {
            message = messageBoxDB.removeOneMessage(key);
        } catch (SQLException e) {
            throw new XsulException("Error reading the message with the key "
                    + key, e);
        } catch (IOException e) {
            throw new XsulException("Error reading the message with the key "
                    + key, e);
        }
        LinkedList list = new LinkedList();
        if (message != null) {
            list.add(message);
        }
        logger.finest("Retrived the message [" + message + "] for the key "
                + key);
        return list;
    }

    public void putMessageIntoMsgBox(String key, String message)
            throws XsulException {
        try {
            messageBoxDB.addMessage(key, message);
        } catch (Exception e) {
            throw new XsulException("Error storing the message[" + message
                    + "] with key " + key, e);
        }
        logger.finest("Message ["+message+"] was inserted to the database with the key "+key);

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

