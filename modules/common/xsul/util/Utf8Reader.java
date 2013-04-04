/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Utf8Reader.java,v 1.3 2005/02/08 05:21:58 aslom Exp $
 */

package xsul.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UTFDataFormatException;

/**
 * Very simpple UTF8 reader from byte input stream.
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class Utf8Reader extends Reader {
    private InputStream source;
    private byte[] b = new byte[1024];
    
    public Utf8Reader(InputStream source) {
        this(source, 1024);
    }
    
    public Utf8Reader(InputStream source, int bufferSize) {
        this.source = source;
        b = new byte[bufferSize];
    }
    
    public int read(char[] cbuf,
                    int off,
                    int len)
        throws IOException
    {
        if(len > b.length) len = b.length;
        int bEnd = source.read(b, 0, len);  // try to fill buffer
        if(bEnd == -1) return -1;
        // assert bEnd > 0
        int bPos = 0;
        int start = off;
        int end = off + len;
        int bb, value;
        while(bPos < bEnd) {
            bb = b[ bPos++ ];
            
            if ( bb >= 0 ) {      // bytes 0x00-0x7F
                cbuf[ off++ ] = (char) bb;
                continue;
            }
            
            
            // bytes 0x80-0xFF.
            // can be 110xxxxx and 1110xxxx
            
            
            if ( (bb & 0xE0) == 0xC0 ) {
                // must have a least than one byte!
                byte b1;
                if(bPos < bEnd) {
                    b1 =  b[ bPos++ ];
                } else {
                    int i =  source.read();
                    if(i == -1) return -1;
                    b1 = (byte)i;
                }
                
                int temp1 = b1 & 0xFF;
                if ( (temp1 & 0xC0) != 0x80 ) {  // 10xxxxxx
                    throw new UTFDataFormatException(
                        "UTF8 encoding incorrect expected 10xxxxxx but got "+Integer.toHexString((int)b1)+")" );
                }
                temp1  &= 0x3F;
                
                
                // 110xxxxx 10xxxxxx
                value = bb & 0x1F;
                value = (value << 6) | temp1;
                cbuf[ off++ ] = (char) value;
            } else if  ( (bb & 0xF0) == 0xE0 ) {
                // must have at lesat two bytes left in input ...
                byte b1;
                byte b2;
                if(bPos < bEnd - 1) {
                    b1 = b[ bPos++ ];
                    b2 = b[ bPos++ ];
                } else if(bPos < bEnd) {
                    b1 = b[ bPos++ ];
                    int i =  source.read();
                    if(i == -1) return -1;
                    b2 = (byte)i;
                } else {
                    int i =  source.read();
                    if(i == -1) return -1;
                    b1 = (byte)i;
                    i =  source.read();
                    if(i == -1) return -1;
                    b2 = (byte)i;
                }
                
                int temp1 = b1 & 0xFF;
                if ( (temp1 & 0xC0) != 0x80 ) {  // 10xxxxxx
                    throw new UTFDataFormatException(
                        "UTF8 encoding incorrect expected 10xxxxxx but got "
                            +Integer.toHexString((int)bb)+" "+Integer.toHexString((int)b1)+")" );
                    
                }
                temp1  &= 0x3F;
                
                int temp2 = b1 & 0xFF;
                if ( (temp2 & 0xC0) != 0x80 ) {  // 10xxxxxx
                    throw new UTFDataFormatException( "UTF8 encoding" );
                }
                temp2  &= 0x3F;
                
                // 1110xxxx 10xxxxxx 10xxxxxx
                value = bb & 0x0F;
                value = (value << 6) | temp1;
                value = (value << 6) | temp2;
                cbuf[ off++ ] = (char) value;
            }
            else {
                throw new UTFDataFormatException( "UTF8 encoding" );
            }
        }
        return off - start;
    }
    
    public void close()
        throws IOException
    {
        source.close();
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

