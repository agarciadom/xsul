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

import java.io.Serializable;
import xsul.wsdl.WsdlPort;

/**
 * A WSIFPort represents the handle by which the operations
 * from the <portType> of the <port> of this WSIFPort can be
 * executed. This is an interface which must implemented by
 * specific implementations for the ports. That is, the actual
 * logic is dependent on the binding associated with this port.
 * An interface is used to enable dynamic implementation generation
 * using JDK1.3 dynamic proxy stuff.
 * <br />Note: this API is based on <a href="http://ws.apache.org/wsif/">Apache WSIF API</a>.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 * @author Paul Fremantle
 * @author Matthew J. Duftler
 * @author Sanjiva Weerawarana
 * @author Nirmal Mukhi
 */
public interface WSIFPort extends Serializable {
    
    public WsdlPort getWsdlServicePort() throws WSIFException;
    
    
    /**
     * Create a new WSIFOperation. There must be exactly one
     * operation in this port's portType with this name. For
     * overloaded operations see {@link #createOperation(String,String,String)}.
     *
     * @param operationName the name of an operation in this port's portType
     * @return the new WSIFOperation
     * @exception WSIFException if something goes wrong
     */
    public WSIFOperation createOperation(String operationName)
        throws WSIFException;
    //
    //    /**
    //     * Create a new WSIFOperation. There must be an
    //     * operation in this port's portType with this operation name,
    //     * input message name and output message name. The input message name
    //     * distinguishes overloaded operations.
    //     *
    //     * @param operationName the name of an operation in this port's portType
    //     * @param inputName the input message name
    //     * @param outputName the output message name
    //     * @return the new WSIFOperation
    //     * @exception WSIFException if something goes wrong
    //     */
    //    public WSIFOperation createOperation(String operationName,
    //                                         String inputName,
    //                                         String outputName)
    //        throws WSIFException;
    
    /**
     * Close this port; indicates that the user is done using it. This
     * is only essential for WSIFPorts that are being used in a stateful
     * or resource-shared manner. Responsible stubs will call this if
     * feasible at the right time.
     * @exception WSIFException if something goes wrong
     */
    public void close() throws WSIFException;
    
    //    /**
    //     * Tests if this port supports synchronous calls to operations.
    //     * @return <code>true</code> this port support synchronous calls
    //     *      <br><code>false</code> this port does not support synchronous calls
    //     */
    //    public boolean supportsSync();
    //
    //    /**
    //     * Tests if this port supports asynchronous calls to operations.
    //     * @return <code>true</code> this port support asynchronous calls
    //     *      <br><code>false</code> this port does not support asynchronous calls
    //     */
    //    public boolean supportsAsync();
    //
    //    /**
    //     * Gets the context information for this WSIFPort.
    //     * @return context
    //     */
    //    public WSIFMessage getContext() throws WSIFException ;
    //
    //    /**
    //     * Sets the context information for this WSIFPort.
    //     * @param WSIFMessage the new context information
    //     */
    //    public void setContext(WSIFMessage context);
    
}

