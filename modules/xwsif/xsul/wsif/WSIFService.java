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



/**
 * A WSIFService is a factory via which WSIFPorts
 * are retrieved. This follows the J2EE design pattern of accessing
 * resources (WSIFPorts, in this case) via a factory which
 * is retrieved from the context in which the application is running.
 * When WSIF is hosted in an app server, the container can manage
 * service invocation details by providing a factory implementation
 * that follows the app servers wishes and guidelines.
 *
 * The factory is assumed to be for a specific portType; i.e.,
 * the factory knows how to factor WSIFPorts for a given portType.
 * As such the getPort() methods do not take portType arguments.
 *
 * <br />Note: this API is based on <a href="http://ws.apache.org/wsif/">Apache WSIF API</a>.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 * @author Paul Fremantle
 * @author Michael Beisiegel
 * @author Sanjiva Weerawarana
 */
public interface WSIFService {
    /**
     * Returns an appropriate WSIFPort for the portType that this factory
     * supports. If the service had multiple ports, which one is returned
     * depends on the specific factory - the factory implementation may
     * use whatever heuristic it feels like to select an "appropriate" one.
     *
     * @return the new WSIFPort
     * @exception WSIFException if a suitable port cannot be located.
     */
    public WSIFPort getPort() throws WSIFException;

    /**
     * Returns a WSIFPort for the indicated port.
     *
     * @param portName name of the port (local part of the name).
     * @return the new WSIFPort
     * @exception WSIFException if the named port is not known or available
     */
    public WSIFPort getPort(String portName) throws WSIFException;

//    /**
//     * Get the dynamic proxy that will implement an interface for a port
//     *
//     * @param portName the name of the port
//     * @param iface the interface that the stub will implement
//     * @return a stub (a dynamic proxy)
//     * @exception WSIFException if something goes wrong
//     */
//    public Object getStub(String portName, Class iface) throws WSIFException;
//
//    /**
//     * Get the dynamic proxy that will implement an interface for a port
//     * This method will attempt to use the preferred port if set otherwise
//     * it will use the first available port.
//     *
//     * @param portName the name of the port
//     * @return a stub (a dynamic proxy)
//     * @exception WSIFException if something goes wrong
//     */
//    public Object getStub(Class iface) throws WSIFException;
//
//    /**
//     * Inform WSIF that a particular XML type (referred to in the WSDL)
//     * actually maps to a particular Java class. Use this method when there
//     * is no schema definition for this type, or when the mapping is
//     * sufficiently complicated that WSIF does not understand the schema
//     * definition. Calling this method overrides whatever schema is present
//     * in the WSDL for this type.
//     * @param xmlType the fully qualified XML type name
//     * @param javaType the java class that this type maps to
//     * @exception WSIFException if something goes wrong
//     */
//    public void mapType(QName xmlType, Class javaType) throws WSIFException;
//
//    /**
//     * Add an association between a namespace URI and and a Java package.
//     * This enables WSIF to map schema definitions to real java classes.
//     * @param namespace The namespace URI
//     * @param packageName The full package name
//     * @exception WSIFException if something goes wrong
//     */
//    public void mapPackage(String namespace, String packageName)
//        throws WSIFException;

    /**
     * Set the preferred port
     * @param portName The name of the port to use
     * @exception WSIFException if something goes wrong
     */
    public void setPreferredPort(String portName) throws WSIFException;

    /**
     * Get the names of the available ports
     * @return Iterator for list of available port names.
     * @exception WSIFException if something goes wrong
     */
    //public Iterator getAvailablePortNames() throws WSIFException;

    /**
     * Get the Definition object representing the wsdl document
     * @return The Definition object
     */
    //public WsdlDefinitions getDefinition();

//    /**
//     * Gets the context information for this WSIFService.
//     * @return context
//     */
//    public WSIFMessage getContext() throws WSIFException ;
//
//    /**
//     * Sets the context information for this WSIFService.
//     * @param WSIFMessage the new context information
//     */
//    public void setContext(WSIFMessage context);

}
