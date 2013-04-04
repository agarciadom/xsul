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
import xsul.wsdl.WsdlBindingOperation;

/**
 * A WSIFOperation is a handle on a particular operation of a portType
 * that can be used to invoke web service methods. This interface is
 * implemented by each provider. A WSIFOperation can be created using
 * {@link WSIFPort#createOperation(String)}.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public interface WSIFOperation extends Serializable {

    public WsdlBindingOperation getBindingOperation() throws  WSIFException;

    /**
     * Return true if the operation is request-response - it will return output or fault
     * and executeRequestResponseOperation() needs to be called. If false is returned
     * executeInputOnlyOperation() should be used instead.
     */
     public boolean isRequestResponseOperation() throws  WSIFException;
    
    /**
     * Execute a request-response operation. The signature allows for
     * input, output and fault messages. WSDL in fact allows one to
     * describe the set of possible faults an operation may result
     * in, however, only one fault can occur at any one time.
     *
     * @param op name of operation to execute
     * @param input input message to send to the operation
     * @param output an empty message which will be filled in if
     *        the operation invocation succeeds. If it does not
     *        succeed, the contents of this message are undefined.
     *        (This is a return value of this method.)
     * @param fault an empty message which will be filled in if
     *        the operation invocation fails. If it succeeds, the
     *        contents of this message are undefined. (This is a
     *        return value of this method.)
     *
     * @return true indicating that operation succeded and result is
     *         output message, if false the fault is int the fault message.
     *
     * @exception WSIFException if something goes wrong.
     */
    public boolean executeRequestResponseOperation(
        WSIFMessage input,
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException;

    /**
     * Execute an asynchronous request
     * @param input   input message to send to the operation
     *
     * @return the correlation ID or the request. The correlation ID
     *         is used to associate the request with the WSIFOperation.
     *
     * @exception WSIFException if something goes wrong.
     */
    //public WSIFCorrelationId executeRequestResponseAsync(WSIFMessage input)
    //    throws WSIFException;

    /**
     * Execute an asynchronous request
     * @param input   input message to send to the operation
     * @param handler   the response handler that will be notified
     *        when the asynchronous response becomes available.
     *
     * @return the correlation ID or the request. The correlation ID
     *         is used to associate the request with the WSIFOperation.
     *
     * @exception WSIFException if something goes wrong.
     */
    //public WSIFCorrelationId executeRequestResponseAsync(
    //    WSIFMessage input,
    //    WSIFResponseHandler handler)
    //    throws WSIFException;

    /**
     * fireAsyncResponse is called when a response has been received
     * for a previous executeRequestResponseAsync call.
     * @param response   an Object representing the response
     * @exception WSIFException if something goes wrong
     */
    //public void fireAsyncResponse(Object response) throws WSIFException;

    /**
     * Processes the response to an asynchronous request.
     * This is called for when the asynchronous operation was
     * initiated without a WSIFResponseHandler, that is, by calling
     * the executeRequestResponseAsync(WSIFMessage input) method.
     *
     * @param response   an Object representing the response.
     * @param output an empty message which will be filled in if
     *        the operation invocation succeeds. If it does not
     *        succeed, the contents of this message are undefined.
     *        (This is a return value of this method.)
     * @param fault an empty message which will be filled in if
     *        the operation invocation fails. If it succeeds, the
     *        contents of this message are undefined. (This is a
     *        return value of this method.)
     *
     * @return true or false indicating whether a fault message was
     *         generated or not. The truth value indicates whether
     *         the output or fault message has useful information.
     *
     * @exception WSIFException if something goes wrong
     *
     */
    //public boolean processAsyncResponse(
    //    Object response,
    //    WSIFMessage output,
    //    WSIFMessage fault)
    //    throws WSIFException;

    /**
     * Execute an input-only operation.
     *
     * @param input input message to send to the operation
     * @exception WSIFException if something goes wrong.
     */
    public void executeInputOnlyOperation(WSIFMessage input) throws WSIFException;

//    /**
//     * Allows the application programmer or stub to pass context
//     * information to the binding. The Port implementation may use
//     * this context - for example to update a SOAP header. There is
//     * no definition of how a Port may utilize the context.
//     * @param context context information
//     */
//    public void setContext(WSIFMessage context);
//
//    /**
//     * Gets the context information for this binding.
//     * @return context
//     */
//    public WSIFMessage getContext() throws WSIFException;

    /**
     * Create an input message that will be sent via this port.
     * It is responsibility of caller to set message name.
     * @return a new message
     */
    public WSIFMessage createInputMessage();

//    /**
//     * Create an input message that will be sent via this port.
//     * @param name for the new message
//     * @return a new message
//     */
//    public WSIFMessage createInputMessage(String name);

    /**
     * Create an output message that will be received into via this port.
     * It is responsibility of caller to set message name.
     * @return a new message
     */
    public WSIFMessage createOutputMessage();

//    /**
//     * Create an output message that will be received into via this port.
//     *
//     * @param name for the new message
//     * @return a new message
//     */
//    public WSIFMessage createOutputMessage(String name);

    /**
     * Create a fault message that may be received into via this port.
     * It is responsibility of caller to set message name.
     * @return a new message
     */
    public WSIFMessage createFaultMessage();

//    /**
//     * Create a fault message that may be received into via this port.
//     *
//     * @param name for the new message
//     * @return a new message
//     */
//    public WSIFMessage createFaultMessage(String name);

}
