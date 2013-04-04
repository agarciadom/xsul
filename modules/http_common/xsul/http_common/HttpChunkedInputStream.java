/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: HttpChunkedInputStream.java,v 1.2 2004/03/02 09:23:32 aslom Exp $
 */

package xsul.http_common;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

//TODO add support for trailer

/**
 * Implementation of input stream that will read HTTP chunked encoding
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class HttpChunkedInputStream extends InputStream {
    private static final boolean DEBUG_TRACE = false;
    private static final boolean DEBUG = DEBUG_TRACE || false;
    protected byte[] lineBuf;
    protected InputStream source;
    protected int blockSize;
    protected boolean finished;
    protected boolean initialized;

    public HttpChunkedInputStream(InputStream source) {
        this.source = source;
        lineBuf = new byte[2048];
        //lineOff = 0;
        //lineLen = 0;
    }

    public int read() throws IOException {
        if(DEBUG_TRACE) trace("read()");
        if(finished) return -1;
        if(blockSize == 0) {
            processHeader();
        }
        if(blockSize > 0) {
            --blockSize;
            return source.read();
        } else {
            assert finished == true;
            return -1;
        }
    }

    public int read(byte[] b)
        throws IOException
    {
        if(DEBUG_TRACE) trace("read(b[]");
        return read(b, 0, b.length);
    }

    public int read(byte[] b,
                    int off,
                    int len)
        throws IOException
    {
        if(DEBUG_TRACE) trace("read(off="+off+",len="+len+",b="+b);
        if(b == null) throw new NullPointerException("byte array to read into can not be null");
        if(off < 0) throw new IndexOutOfBoundsException(
                "offset of byte array to read into can not be negative");
        if(off + len > b.length) throw new IndexOutOfBoundsException(
                "number of bytes to read for byte array is bigger than space in array");
        if(len == 0) return 0;
        if(finished) return -1;
        if(blockSize == 0) {
            processHeader();
        }
        if(blockSize > 0) {  // read at least one byte!
            if(len > blockSize) len = blockSize;
            int r =  source.read(b, off, len);
            if(r == -1) return -1;
            blockSize -= r;
            if(DEBUG_TRACE) trace("read() after reading r="+r+" blockSize="+blockSize
                                      +" received "+printable(new String(b, off, r)));
            return r;
        } else {
            assert finished == true;
            return -1;
        }
    }

    /**
     * This implementation of HTTP chunked encoding
     * is designed to be layered over exisitng input stream
     * therefore close just finishes reading (discarding received data)
     * leaving stream exactly just after last byte of chunked data.
     * Also closing previously closed stream has no effect.
     */
    public void close() throws IOException {
        if(DEBUG_TRACE) trace("close()");
        //source.close();
        if(!finished) {
            // skip to the end of the stream --- to ensure proper encapsulation
            while(true) {
                if(blockSize > 0) source.skip(blockSize);
                if(finished) { // order is *very* important
                    break;
                }
                processHeader();
            }
        }
    }

    // ultra unefficient - but this methid is rarely used ...
    public long skip(long n)
        throws IOException
    {
        if(DEBUG_TRACE) trace("skip(n="+n);
        int count = 0;
        while(n-- > 0) {
            int i = read();
            if(i == -1) {
                break;
            }
            ++count;
        }
        return count;
    }

    public int available()
        throws IOException
    {
        if(DEBUG_TRACE) trace("available()");
        int av = source.available();
        if(av > blockSize) av = blockSize;
        return av;
    }

    public void mark(int readlimit) {
        if(DEBUG_TRACE) trace("mark(readLimit="+readlimit);
    }

    public boolean markSupported() {
        if(DEBUG_TRACE) trace("markSupported()");
        return false;
    }

    public void reset()
        throws IOException
    {
        if(DEBUG_TRACE) trace("reset()");
        throw new IOException("reset not supported");
    }


    protected void processHeader() throws IOException
    {
        if(DEBUG_TRACE) trace("processHeader() blockSize="+blockSize
                                  +" finished="+finished);
        if(initialized) {
            int i = source.read();
            if(i != '\r') {
                throw new IOException(
                    "expected CR followed by LF not "+i+" ("+printable((char)i)+")");
            }
            i = source.read();
            if(i != '\n') {
                throw new IOException(
                    "expected LF after CR not "+i+" ("+printable((char)i)+")");
            }
        }
        initialized = true;
        // read line
        int lineEnd = 0;
        boolean seenCR = false;
        while(true) {
            int i = source.read();
            if(i == -1) {
                if(lineEnd == 0) { // nothing was read so far
                    throw new EOFException("stream has no data available");
                    //blockSize = 0;
                    //finished = true;
                    //return;
                }
                throw new IOException("unexpected end of input when reading header: "
                                          + printable(new String(lineBuf, 0, lineEnd)));
            }
            if(lineEnd >= lineBuf.length) {
                throw new IOException("too long header: "
                                          +printable(new String(lineBuf, 0, lineEnd)));
            }
            lineBuf[lineEnd++] = (byte)i;
            if(i == (int)'\n') {  //LF
                if(seenCR) {
                    break;
                }
            } else if(i == (byte)'\r') { //CR
                seenCR = true;
            } else {
                seenCR = false;
            }
        }
        processHeader(lineBuf, lineEnd);
        if(blockSize == 0) {
            finished = true;
            int i = source.read();
            if(i != '\r') {
                throw new IOException(
                    "final CRLF: expected CR followed by LF not "+i+" ("+printable((char)i)+")");
            }
            i = source.read();
            if(i != '\n') {
                throw new IOException(
                    "final CRLF: expected LF after CR not "+i+" ("+printable((char)i)+")");
            }

        }
    }

    protected void processHeader(byte[] line, int lineEnd) throws IOException
    {
        if(DEBUG_TRACE) trace("processHeader(lineEnd="+lineEnd
                                  +",line="+printable(new String(line, 0, lineEnd)));

        if(DEBUG) debug("processing header "+printable(new String(line, 0, lineEnd)));
        int pos = 0;
        //        int digit = line[pos] - (byte)'0';
        //        if(digit < 0 || digit > 9) throw new IOException(
        //                "expected digit for length but instead got "+(char)line[pos]
        //                    +" in "+printable(new String(lineBuf, 0, lineEnd)));
        //        int length = digit;
        //        ++pos;
        int length = 0;
        // retrieve length
        do {
            char ch = (char) line[pos];
            if(ch == '\r' || ch == '\n' || ch == ';') {
                break;
            }
            if(ch >= '0' && ch <= '9') {
                length = length * 16 + (ch - '0');
            } else if(ch >= 'a' && ch <= 'f') {
                length = length * 16 + (ch - ('a' - 10));
            } else if(ch >= 'A' && ch <= 'F') {
                length = length * 16 + (ch - ('A' - 10));
            } else {
                throw new IOException(
                    "expected next digit for length but instead got "+printable(""+ch)
                        +" in "+printable(new String(lineBuf, 0, lineEnd)));
            }
            ++pos;
        } while(pos < lineEnd);
        blockSize = length;
        if(DEBUG_TRACE) trace("processHeader() blockSize="+blockSize);
    }


    private final void debug(String msg) {
        String name = getClass().getName();
        int i = name.lastIndexOf('.');
        name = name.substring(i+1);
        System.err.println(name+":"+msg);
    }

    private final void trace(String msg) {
        String name = getClass().getName();
        int i = name.lastIndexOf('.');
        name = name.substring(i+1);
        System.err.println(name+"."+msg);
    }

    /** simple utility method -- good for debugging */
    public static final String printable(String s) {
        StringBuffer retval = new StringBuffer();
        char ch;
        for (int i = 0; i < s.length(); i++) {
            addPrintable(retval, s.charAt(i));
        }
        return retval.toString();
    }

    public static final String printable(char ch) {
        StringBuffer retval = new StringBuffer();
        addPrintable(retval, ch);
        return retval.toString();
    }

    private static void addPrintable(StringBuffer retval, char ch)
    {
        switch (ch)
        {
            case '\b':
                retval.append("\\b");
                break;
            case '\t':
                retval.append("\\t");
                break;
            case '\n':
                retval.append("\\n");
                break;
            case '\f':
                retval.append("\\f");
                break;
            case '\r':
                retval.append("\\r");
                break;
            case '\"':
                retval.append("\\\"");
                break;
            case '\'':
                retval.append("\\\'");
                break;
            case '\\':
                retval.append("\\\\");
                break;
            default:
                if (ch  < 0x20 || ch > 0x7e) {
                    String ss = "0000" + Integer.toString(ch, 16);
                    retval.append("\\u" + ss.substring(ss.length() - 4, ss.length()));
                } else {
                    retval.append(ch);
                }
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

