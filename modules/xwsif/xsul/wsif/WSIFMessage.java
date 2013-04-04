/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
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
/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "WSIF" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package xsul.wsif;

import org.xmlpull.v1.builder.Iterable;

//import java.util.Map;

//import xsul.wsdl.WsdlMessage;

/**
 * A WSIFMessage is a an interface representing a WSDL Message.
 * <p>
 * In WSDL, a Message describes the abstract type of the input
 * or output to an operation. This is the corresponding WSIF
 * class which represents in memory the actual input or output
 * of an operation.
 * <p>
 * A WSIFMessage is a container for a set of named parts. The
 * WSIFMessage interface separates the actual representation of
 * the data from the abstract type defined by WSDL.
 * <p>
 * WSIFMessage implementations are free to represent the
 * actual part data in any way that is convenient to them.
 * This could be a simple HashMap as in the WSIFDefaultMessage
 * implementation, or it could be something more complex, such
 * as a stream or tree representation.
 * <p>
 * In addition to the containing parts, a WSIFMessage also has
 * a message name. This can be used to distinguish between messages.
 * <p>
 * WSIFMessages are cloneable and serializable. If the parts set are
 * not cloneable, the implementation should try to clone them using
 * serialization. If the parts are not serializable either, then a
 * CloneNotSupportedException will be thrown if cloning is attempted.
 * <p>
 * A WSIFMessage should be not created by directly instantiating
 * a WSIFMessage, but should be created by calling one of the
 * {@link WSIFOperation#createInputMessage()}, {@link WSIFOperation#createOutputMessage()},
 * or {@link WSIFOperation#createFaultMessage()} methods.
 * <p>
 * An instance of a WSIFMessage should only be used for the purpose
 * it was created for, for example, a WSIFMessage created by the
 * {@link WSIFOperation#createInputMessage(String)} should not be used as an
 * output message. A WSIFMessage should only be used once, it should
 * not be reused in any subsequent WSIFOperation requests.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 * @author Paul Fremantle
 * @author Matthew J. Duftler
 * @author Sanjiva Weerawarana
 * @author Nirmal Mukhi
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>

 */
public interface WSIFMessage extends java.io.Serializable, Cloneable {
    /**
     * Get the name of this message.
     */
    public String getName();

    /**
     * Set the name of this message.
     */
    public void setName(String name);

    /**
     * Return list of part names.
     * <p><b>NOTE:</b> part names are unordered.
     */
    public Iterable partNames();

    /**
     * Create an iterator of the parts in this message.
     * Supercedes void getParts(Map).
     */
    public Iterable parts();

//    /**
//     * This message parts will be replaced by sourceParts.
//     * @parameter sourceParts must be Map that has as key Strings with
//     *   part names and values must be instances of WSIFPart.
//     */
//    public void setParts(Map sourceParts);

//    /**
//     * Get the underlying WSDL model for this message.
//     * @return javax.wsdl.Message
//     */
//    public WsdlMessage getMessageDefinition();
//
//    /**
//     * Set the underlying WSDL model for this message.
//     * @param msgDefinition
//     */
//    public void setMessageDefinition(WsdlMessage msgDef);

//    public String getRepresentationStyle();
//    public void setRepresentationStyle(String rStyle);

    public Object getObjectPart(String name) throws WSIFException;
    public Object getObjectPart(String name, Class sourceClass)
        throws WSIFException;
    public void setObjectPart(String name, Object part) throws WSIFException;

    public char getCharPart(String name) throws WSIFException;
    public byte getBytePart(String name) throws WSIFException;
    public short getShortPart(String name) throws WSIFException;
    public int getIntPart(String name) throws WSIFException;
    public long getLongPart(String name) throws WSIFException;
    public float getFloatPart(String name) throws WSIFException;
    public double getDoublePart(String name) throws WSIFException;
    public boolean getBooleanPart(String name) throws WSIFException;

    public void setCharPart(String name, char charPart);
    public void setBytePart(String name, byte bytePart);
    public void setShortPart(String name, short shortPart);
    public void setIntPart(String name, int intPart);
    public void setLongPart(String name, long longPart);
    public void setFloatPart(String name, float floatPart);
    public void setDoublePart(String name, double doublePart);
    public void setBooleanPart(String name, boolean booleanPart);

    public Object clone() throws CloneNotSupportedException;
}
