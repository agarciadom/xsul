/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: SimpleChain.java,v 1.5 2004/03/02 09:23:33 aslom Exp $
 */

package xsul.message_router;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.xmlpull.v1.builder.Iterable;
//import org.xmlpull.v1.builder.SimpleIterator;

/**
 * are called in chain until one node in chain returns true from process() method
 * or chain is finished.
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class SimpleChain implements MessageProcessingNode {
    private List nodes = new LinkedList();

    public SimpleChain() {
    }

    /**
     * Return true to indicate that processingis finished
     * (no more links in chains will be called).
     */
    public boolean process(MessageContext context) throws MessageProcessingException
    {
        MessageProcessingNode[] seq = null;
        synchronized(nodes) {
            seq = (MessageProcessingNode[]) nodes.toArray(new MessageProcessingNode[]{});
        }
        boolean result = false;
        for (int i = 0; i < seq.length; i++)
        {
            result = seq[i].process(context);
            if(result) {
                break;
            }
        }
        return result;
    }

    // CHAIN MANAGEMENT

    // adds to the end
    public void addEntry(MessageProcessingNode en)
        throws MessageProcessingException
    {
        nodes.add(en);
    }

    public void insertEntryBefore(MessageProcessingNode insertPosition, MessageProcessingNode en)
        throws MessageProcessingException
    {
        int ipos = nodes.indexOf(insertPosition);
        if(ipos == -1) {
            throw new MessageRouterException("coul dnot find insertion position");
        }
        synchronized(nodes) {
            nodes.add(ipos, en);
        }
    }

    public Iterable entries()
        throws MessageProcessingException
    {
        // return nodes or ReadOnlyCOllection(node); //in JDK 1.5
        final Iterator iter = nodes.iterator();
        return new Iterable() {

            public Iterator iterator() {
                return iter;
            }

        };
    }

    // ysimmhan: added method: remove from the chain
    public void removeEntry(MessageProcessingNode en)
        throws MessageProcessingException
    {
        nodes.remove(en);
    }

    public void removeAllEntries()
        throws MessageProcessingException
    {
        synchronized(nodes) {
            nodes.clear();
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





