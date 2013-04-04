/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: InMemoryStorageImpl.java,v 1.10 2005/06/16 03:17:11 aslom Exp $
 */
package xsul.msg_box.storage.memory;



/**
 * Message Box storage backend.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import xsul.MLogger;
import xsul.XsulException;
import xsul.msg_box.storage.MsgBoxStorage;
import xsul.util.FastUUIDGen;

public class InMemoryStorageImpl implements MsgBoxStorage {
    private final static MLogger logger = MLogger.getLogger();
    private Map map = new HashMap();
    
    public String createMsgBox() throws XsulException {
        
        String uuid = FastUUIDGen.nextUUID();//generate uuid
        lookupQueue(uuid); //that will create empty queue
        logger.finest("created mailbox "+uuid);
        return uuid;
    }
    
    public void destroyMsgBox(String key) throws XsulException {
        logger.finest("destroying mailbox '"+key+"'");
        synchronized(map) {
            if(map.containsKey(key)) { //destruciton is idempotent
                map.remove(key);
                //} else {
                //    throw new IllegalArgumentException("could not find mailbox '"+key+"' to destroy");
            }
        }
    }
    
    public void putMessageIntoMsgBox(String key, String message) throws XsulException {
        if(logger.isFinestEnabled()) {
            logger.finest("storing in mailbox "+key+" message="+message);
        }
        LinkedList list = lookupQueue(key);
        if(list == null) {
            throw new IllegalArgumentException("no message box with key "+key);
        }
        synchronized(list) {
            list.addLast(message);
            list.notify();
        }
    }
    
    // take all strategy
    public List takeMessagesFromMsgBox(String key) throws XsulException {
        if(key == null) throw new IllegalArgumentException();
        LinkedList list;
        synchronized(map) {
            Object v = map.get(key);
            if(v == null) {
                throw new IllegalArgumentException("no message box with key "+key);
            }
            list = (LinkedList) v;
        }
        long endTime = System.currentTimeMillis() + 5000L; // wait max 5 seconds
        synchronized(list) {
            while(true) {
                if(list.size() > 0) {
                    LinkedList results = new LinkedList();
                    for (int i = 0; i < 10 && list.size() > 0; i++)
                    {
                        String message = (String) list.removeFirst();
                        results.addLast(message);
                        if(logger.isFinestEnabled()) {
                            logger.finest("taking messages from "+key+" message="+message);
                        }
                    }
                    if(logger.isFinestEnabled()) {
                        logger.finest("taken from "+key+" #messages="+list.size());
                    }
                    return results;
                }
                //anti-flood messarus - wait a bit for result and block client for that time
                long duration = endTime - System.currentTimeMillis();
                if(duration <= 0) {
                    break;
                }
                try {
                    list.wait(duration);
                } catch (InterruptedException e) {
                }
                
            }
        }
        return null;
    }
    
    
    private LinkedList lookupQueue(String key) {
        if(key == null) throw new IllegalArgumentException();
        synchronized(map) {
            Object v = map.get(key);
            if(v != null) {
                return (LinkedList) v;
            }
            
            LinkedList list = new LinkedList();
            map.put(key, list);
            
            return list;
        }
    }
    
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1) All redistributions of source code must retain the above
 *    copyright notice, the list of authors in the original source
 *    code, this list of conditions and the disclaimer listed in this
 *    license;
 *
 * 2) All redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the disclaimer
 *    listed in this license in the documentation and/or other
 *    materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include
 *    the following acknowledgement:
 *
 *      "This product includes software developed by the Indiana
 *      University Extreme! Lab.  For further information please visit
 *      http://www.extreme.indiana.edu/"
 *
 *    Alternatively, this acknowledgment may appear in the software
 *    itself, and wherever such third-party acknowledgments normally
 *    appear.
 *
 * 4) The name "Indiana University" or "Indiana University
 *    Extreme! Lab" shall not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission from Indiana University.  For written permission,
 *    please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana
 *    University" name nor may "Indiana University" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code
 * provided does not infringe the patent or any other intellectual
 * property rights of any other entity.  Indiana University disclaims any
 * liability to any recipient for claims brought by any other entity
 * based on infringement of intellectual property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH
 * NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA
 * UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT
 * SOFTWARE IS FREE OF INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR
 * OTHER PROPRIETARY RIGHTS.  INDIANA UNIVERSITY MAKES NO WARRANTIES THAT
 * SOFTWARE IS FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP
 * DOORS", "WORMS", OR OTHER HARMFUL CODE.  LICENSEE ASSUMES THE ENTIRE
 * RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS,
 * AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION GENERATED USING
 * SOFTWARE.
 */






