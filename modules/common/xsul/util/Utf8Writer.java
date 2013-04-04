/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Utf8Writer.java,v 1.2 2004/03/02 09:23:30 aslom Exp $
 */

package xsul.util;

import java.io.*;

/**
 * Very simpple UTF8 writer to byte output stream.
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class Utf8Writer extends Writer {
    private OutputStream sink;
    private byte[] b ;

    public Utf8Writer(OutputStream sink) {
        this(sink, 1024);
    }

    public Utf8Writer(OutputStream sink, int bufferSize) {
        this.sink = sink;
        b = new byte[bufferSize];
    }

    public void close()
        throws IOException
    {
        sink.close();
    }

    public void write(char[] cbuf,
                      int off,
                      int len)
        throws IOException
    {
        int posEnd = b.length - 3;
        int pos = 0;
        int end = off + len;
        while(off < end) {
            // USASCII writer -- if ever needed
            //      int n = len > b.length ? b.length : len;
            //      for(int i = 0; i < n; ++i) {
            //          b[i] = (byte)cbuf[pos+i];
            //      }
            //      sink.write(b, 0, n);
            //      len -= n;
            //      pos += n;
            int cc = cbuf[ off++ ];
            if ( cc <= 0x7F  &&  cc != 0 )
            {
                b[pos++] = (byte) cc;
            }
            else if ( cc > 0x07FF )
            {
                b[ pos++ ] = (byte) ( 0xE0 | (cc >> 12) );
                b[ pos++ ] = (byte) ( 0x80 | ((cc >> 6) & 0x3F) );
                b[ pos++ ] = (byte) ( 0x80 | (cc & 0x3F) );
            }
            else
            {
                b[ pos++ ] = (byte) ( 0xC0 | (cc >> 6) );
                b[ pos++ ] = (byte) ( 0x80 | (cc & 0x3F) );
            }
            if(pos > posEnd) {
                sink.write(b, 0, pos);
                pos = 0;
            }
        }
        sink.write(b, 0, pos);
    }

    public void flush()
        throws IOException
    {
        sink.flush();
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


