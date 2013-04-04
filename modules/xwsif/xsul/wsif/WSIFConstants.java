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
 * Simple class to store constants used by WSIF
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFConstants {
    
    /**
     * WSIF Property file name
     */
    public static final String WSIF_PROPERTIES = "wsif.properties";
    
    /**
     *  WSIF property for pluggable provider defaults
     */
    public static final String WSIF_PROP_PROVIDER_PFX1 =
        "wsif.provider.default.";
    
    /**
     *  WSIF property for pluggable provider defaults
     */
    public static final String WSIF_PROP_PROVIDER_PFX2 = "wsif.provider.uri.";
    
    /**
     *  WSIF property for asynchronous requests
     */
    public static final String WSIF_PROP_ASYNC_TIMEOUT =
        "wsif.asyncrequest.timeout";
    
    /**
     *  WSIF property for asynchronous requests
     */
    public static final String WSIF_PROP_ASYNC_USING_MDB =
        "wsif.async.listener.mdb";
    
    /**
     * WSIFDefaultCorrelationService timeout check delay
     * in milliseconds. Default is 5 seconds
     */
    public static final int CORRELATION_TIMEOUT_DELAY = 5000;
    
    /**
     *  WSIF properties for synchronous requests
     */
    public static final String WSIF_PROP_SYNC_TIMEOUT =
        "wsif.syncrequest.timeout";
    
    /**
     *  WSIF property for unreferenced attachments
     */
    public static final String WSIF_PROP_UNREFERENCED_ATTACHMENTS =
        "wsif.unreferencedattachments";
    
    /**
     *  WSIFCorelationService registered JNDI name
     */
    public static final String CORRELATION_SERVICE_NAMESPACE =
        "wsif/WSIFCorrelationService";
    
    /**
     *  WSIF context part name for HTTP basic authentication userid
     */
    public static final String CONTEXT_HTTP_USER =
        "org.apache.wsif.http.UserName";
    
    /**
     *  WSIF context part name for HTTP basic authentication userid
     */
    public static final String CONTEXT_HTTP_PSWD =
        "org.apache.wsif.http.Password";
    
    /**
     *  WSIF context part name for proxy userid
     */
    public static final String CONTEXT_HTTP_PROXY_USER =
        "org.apache.wsif.http.proxy.UserName";
    
    /**
     *  WSIF context part name for proxy password
     */
    public static final String CONTEXT_HTTP_PROXY_PSWD =
        "org.apache.wsif.http.proxy.Password";
    
    /**
     *  WSIF context part name for SOAP headers
     * @deprecated use CONTEXT_REQUEST_SOAP_HEADERS
     */
    public static final String CONTEXT_SOAP_HEADERS =
        "org.apache.wsif.soap.RequestHeaders";
    
    /**
     *  WSIF context part name for HTTP headers
     */
    public static final String CONTEXT_REQUEST_HTTP_HEADERS =
        "org.apache.wsif.http.RequestHeaders";
    
    /**
     *  WSIF context part name for HTTP headers
     */
    public static final String CONTEXT_RESPONSE_HTTP_HEADERS =
        "org.apache.wsif.http.ResponseHeaders";
    
    /**
     *  WSIF context part name for SOAP headers
     */
    public static final String CONTEXT_REQUEST_SOAP_HEADERS =
        "org.apache.wsif.soap.RequestHeaders";
    
    /**
     *  WSIF context part name for SOAP headers
     */
    public static final String CONTEXT_RESPONSE_SOAP_HEADERS =
        "org.apache.wsif.soap.ResponseHeaders";
    
    /**
     *  WSIF context part name prefix for JMSProperties
     */
    public static final String CONTEXT_JMS_PREFIX = "JMSProperty.";
    
    /**
     *  WSIF context part name for the AXIS operation style
     */
    public static final String CONTEXT_OPERATION_STYLE =
        "org.apache.wsif.axis.operationStyle";
    
    /**
     *  WSIF context value for AXIS document operation style
     */
    public static final String CONTEXT_OPERATION_STYLE_DOCUMENT =
        "document";
    
    /**
     *  WSIF context value for AXIS wrapped operation style
     */
    public static final String CONTEXT_OPERATION_STYLE_WRAPPED =
        "wrapped";
    
    /**
     *  WSIF context value for AXIS unwrapped operation style
     */
    public static final String CONTEXT_OPERATION_STYLE_UNWRAPPED =
        "unwrapped";
    
    /**
     *  WSIF context value for AXIS message operation style
     */
    public static final String CONTEXT_OPERATION_STYLE_MESSAGE =
        "message";
    
    /**
     *  WSIF context part name for the list of schema types
     */
    public static final String CONTEXT_SCHEMA_TYPES =
        "org.apache.wsif.schematypes";
    
    /**
     *
     */
    public static final String CONTEXT_REQUEST_UNREFERENCED_ATTACHMENT_PARTS =
        "org.apache.wsif.attachments.request.unreferenced_attachment_parts";
    
    /**
     *
     */
    public static final String CONTEXT_RESPONSE_UNREFERENCED_ATTACHMENT_PARTS =
        "org.apache.wsif.attachments.response.unreferenced_attachment_parts";
    
    /**
     *  SOAP faults WSIFMessage part name for the fault code
     */
    public static final String SOAP_FAULT_MSG_NAME =
        "org.apache.wsif.soap.fault";
    
    /**
     *  SOAP faults WSIFMessage part name for the fault code
     */
    public static final String SOAP_FAULT_CODE =
        "org.apache.wsif.soap.fault.code";
    
    /**
     *  SOAP faults WSIFMessage part name for the fault string
     */
    public static final String SOAP_FAULT_STRING =
        "org.apache.wsif.soap.fault.string";
    
    /**
     *  SOAP faults WSIFMessage part name for the fault actor
     */
    public static final String SOAP_FAULT_ACTOR =
        "org.apache.wsif.soap.fault.actor";
    
    /**
     *  SOAP faults WSIFMessage part name for the fault object
     */
    public static final String SOAP_FAULT_OBJECT =
        "org.apache.wsif.soap.fault.object";
    
    /**
     *  WSDLFactory property name
     */
    public static final String WSDLFACTORY_PROPERTY_NAME =
        "javax.wsdl.factory.WSDLFactory";
    
    /**
     *  WSIF implemetation of WSDLfactory
     */
    public static final String WSIF_WSDLFACTORY =
        "org.apache.wsif.wsdl.WSIFWSDLFactoryImpl";
    
    /**
     *  JMS provider JMS property containing the operation name
     */
    public static final String JMS_PROP_OPERATION_NAME =
        "WSDLOperation";
    
    /**
     *  JMS provider JMS property containing the input message name
     */
    public static final String JMS_PROP_INPUT_NAME =
        "WSDLInput";
    
    /**
     *  JMS provider JMS property containing the output message name
     */
    public static final String JMS_PROP_OUTPUT_NAME =
        "WSDLOutput";
    
    public static final String NS_URI_1999_SCHEMA_XSD =
        "http://www.w3.org/1999/XMLSchema";
    
    public static final String NS_URI_2000_SCHEMA_XSD =
        "http://www.w3.org/2000/10/XMLSchema";
    
    public static final String NS_URI_2001_SCHEMA_XSD =
        "http://www.w3.org/2001/XMLSchema";
    
    public static final String NS_URI_SOAP_ENC =
        "http://schemas.xmlsoap.org/soap/encoding/";
    
    public static final String NS_URI_LITERAL_XML =
        "http://xml.apache.org/xml-soap/literalxml";
    
    public static final String NS_URI_WSDL =
        "http://schemas.xmlsoap.org/wsdl/";
    
    public static final String NS_URI_APACHE_SOAP =
        "http://xml.apache.org/xml-soap";
    
    /**
     * Property name that specifies a WSIFMapper implementation class. This property can be
     * set as a System property or in the wsif,properties file.
     * @see org.apache.wsif.mapping.WSIFMapperFactory
     */
    public static final String WSIF_MAPPER_PROPERTY = "org.apache.wsif.mapper";
    
    /**
     * Property name that specifies a WSIFMappingConvention implementation class. This property can be
     * set as a System property or in the wsif,properties file.
     * @see org.apache.wsif.mapping.WSIFMappingConventionFactory
     */
    public static final String WSIF_MAPPINGCONVENTION_PROPERTY = "org.apache.wsif.mappingconvention";
    
    /**
     * Feature name for service caching. The value of this feature should be a <code>java.lang.Boolean</code>
     * object<br><br>
     * Setting this feature as <code>true</code> will cause the WSIFServiceFactory to store shallow
     * copies of any WSIFService instances it creates and to reuse these whenever possible rather than
     * creating new WSIFService instances. This can provide a performance improvement if you are creating
     * and using the same WSIFServices multiple times. If this feature is not set, service caching will be
     * off.
     */
    public static final String WSIF_FEATURE_SERVICE_CACHING = "org.apache.wsif.servicecaching";
    
    /**
     * Feature name for service cache size. The value of this feature should be a
     * <code>java.lang.Integer</code> object<br><br>
     * This feature is used in conjunction with the service caching feature. It sets the number of
     * instances of WSIFService to be cached by the WSIFServiceFactory. When this number is reached
     * the oldest entries in the cache will be purged.
     */
    public static final String WSIF_FEATURE_SERVICE_CACHE_SIZE = "org.apache.wsif.servicecachesize";
    
    /**
     * Feature name for automatic mapping of types. The value of this feature should be a
     * <code>java.lang.Boolean</code> object<br><br>
     * Setting this feature as <code>true</code> will cause instances of WSIFService created by this factory,
     * to attempt to automatically calculate mappings between xml names and Java class names for the
     * types in the wsdl. If this feature is not set, automatic mapping of type will be off.
     */
    public static final String WSIF_FEATURE_AUTO_MAP_TYPES = "org.apache.wsif.automaptypes";
    
    /**
     * Feature name for the name of a WSIFMapper class to be used. The value for this feature should be
     * a String giving the name of the WSIFMapper implementation class to be used by all WSIFServices
     * created by the factory. Setting this feature will override the use of the org.apache.wsif.mapper
     * property for WSIFServices created by the factory.
     */
    public static final String WSIF_FEATURE_MAPPER_CLASS = "org.apache.wsif.mapper";
    
    /**
     * Feature name for the name of a WSIFMappingConvention class to be used. The value for this feature
     * should be a String giving the name of the WSIFMappingConvention implementation class to be used by
     * all WSIFServices created by the factory. Setting this feature will override the use of the
     * org.apache.wsif.mappingconvention property for WSIFServices created by the factory.
     */
    public static final String WSIF_FEATURE_MAPPINGCONVENTION_CLASS = "org.apache.wsif.mappingconvention";
    
    /**
     * Feature name for a authenticating proxy username and password to be used when reading wsdl files and
     * parsing schemas. The value for this feature should be a <code>java.net.PasswordAuthentication</code>
     * object which encapsultes the username and password.
     */
    public static final String WSIF_FEATURE_PROXY_AUTHENTICATION = "org.apache.wsif.proxyauthentication";
}
