/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * Business Machines, Inc., http://www.ibm.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package xsul.wsif.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import xsul.common_type_handler.CommonTypeHandlerRegistry;
import xsul.type_handler.TypeHandlerRegistry;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlException;
import xsul.wsdl.WsdlPort;
import xsul.wsdl.WsdlPortType;
import xsul.wsdl.WsdlResolver;
import xsul.wsdl.WsdlService;
import xsul.wsif.WSIFException;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.spi.WSIFProviderManager;

/**
 * An entry point to dynamic WSDL invocations.
 *
 * @author Alekander Slominski
 * @author Sanjiva Weerawarana
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFServiceImpl implements WSIFService {
    //    private static PrivateCompositeExtensionRegistry providersExtRegs =
    //        new PrivateCompositeExtensionRegistry();
    private WsdlService service;
    //private WsdlPortType portType;
    //    private Map availablePorts;
    //    private WSIFDynamicTypeMap typeMap = new WSIFDynamicTypeMap();
    private String preferredPort;
    //    private Map typeReg = null;
    private WsdlPort chosenPort;
    //    private WSIFMessage context;
    private WSIFProviderManager providersMgr = WSIFProviderManager.getInstance();
    private WsdlResolver wsdlResolver = WsdlResolver.getInstance();
    
    private TypeHandlerRegistry typeMapper = CommonTypeHandlerRegistry.getInstance(); //TODO
    
    //    protected ArrayList schemaTypes = new ArrayList();
    //    protected WSIFMappingConvention mapCon = null;
    //    protected WSIFMapper mapper = null;
    //    // Flag to indicate that we have produced mappings based on the types in the schema
    //    private boolean typeMapInitialised = false;
    //    // Flag to indicate that we have parsed the schema to find the types
    //    private boolean schemaTypesInitialised = false;
    //    // WSDLLocator needed to find imported xsd files if parsing the schema
    //    private WSDLLocator specialistLocator = null;
    //
    //    protected Map features = null;
    
    /**
     * Create a WSIF service instance from WSDL document URL.
     * <br> If serviceName or serviceNS is null,
     *   then WSDL document must have exactly one service in it.
     * <br> If portTypeName or portTypeNS is null,
     *   then WSDL document must have exactly one portType in it
     *   and all ports of the selected service must
     *    implement the same portType.
     * <br>NOTE:
     * The equivalent {@link org.apache.wsif.WSIFServiceFactory}.getService method
     * should be used to create a WSIFService.
     */
    /*package*/ WSIFServiceImpl(String wsdlLoc,
                                String serviceNS,
                                String serviceName,
                                String portTypeNS,
                                String portTypeName)
        throws WSIFException
    {
        
        WsdlDefinitions def;
        WsdlService service = null;
        WsdlPortType portType = null;
        try {
            def = wsdlResolver.loadWsdl(new URI(wsdlLoc));
            // select WSDL service if given name
            //service = def.selectService(serviceNS, serviceName);
            
            // select WSDL portType if given name
            //portType = def.selectPortType(portTypeNS, portTypeName);
            // "/wsdl:PortType[@name=...]
            
        } catch (URISyntaxException e) {
            throw new WSIFException("invalid locaiton to load WSDL from "+wsdlLoc, e);
        } catch (WsdlException e) {
            throw new WSIFException("coul dnot load wsdl from "+wsdlLoc, e);
        }
        //checkWSDL(def);
        
        
        init(def, service, portType);
    }
    
    
    /**
     * Create a WSIF service instance
     * <br>NOTE:
     * The equivalent {@link org.apache.wsif.WSIFServiceFactory}.getService method
     * should be used to create a WSIFService.
     */
    WSIFServiceImpl(WsdlDefinitions def,
                    WsdlService service,
                    WsdlPortType portType)
        throws WSIFException
    {
        init(def, service, portType);
    }
    
    private void init(WsdlDefinitions def, WsdlService serv, WsdlPortType portType)
        throws WSIFException
    {
        if (def == null) {
            throw new IllegalArgumentException("WSDL definition can not be null");
        }
        
        
        if (serv == null) {
            // select first service
            
            for(Iterator iter = def.getServices().iterator(); iter.hasNext(); ) {
                serv = (WsdlService) iter.next();
                break;
            }
            if(serv == null) {
                throw new WSIFException("WSDL must contain at least one service in "+def);
            }
        }
        this.service = serv;
        
        //select by default first port in service
        
        for(Iterator iter = serv.getPorts().iterator(); iter.hasNext(); ) {
            chosenPort = (WsdlPort) iter.next();
            break;
        }
        if(serv == null) {
            throw new WSIFException("WSDL service must contain at least one port in "+serv);
        }
        
        
        //        if (service == null) {
        //          Map services = WSIFUtils.getAllItems(def, "Service");
        //
        //          service =
        //              (Service) WSIFUtils.getNamedItem(services, null, "Service");
        //        }
        //
        //        if (portType == null) {
        //          // if all ports have the same portType --> use it
        //          Map ports = service.getPorts();
        //          if (ports.size() == 0) {
        //              throw new WSIFException(
        //                  "WSDL must contain at least one port in "
        //                      + service.getQName());
        //          }
        //
        //          for (Iterator i = ports.values().iterator(); i.hasNext();) {
        //              Port port = (Port) i.next();
        //              if (portType == null) {
        //                  portType = port.getBinding().getPortType();
        //              } else {
        //                  PortType pt = port.getBinding().getPortType();
        //                  if (!pt.getQName().equals(portType.getQName())) {
        //                      throw new WSIFException(
        //                          "when no port type was specified all ports "
        //                              + "must have the same port type in WSDL service "
        //                              + service.getQName());
        //                  }
        //              }
        //          }
        //          if (portType == null) {
        //              throw new IllegalArgumentException(
        //                  "WSDL more than one portType in service " + service);
        //
        //          }
        //        }
        //        this.def = def;
        //        this.service = service;
        //        this.portType = portType;
        //
        //        // checkPortTypeIsRPC(Definition def, PortType portType) has been replaced by
        //        // checkPortTypeInformation(Definition def, PortType portType) since "Input Only"
        //        // operations are supported.
        //        checkPortTypeInformation(def, portType);
        //
        //        // get all ports from service that has given portType
        //
        //        Map ports = service.getPorts();
        //        // check that service has at least one port ...
        //        if (ports.size() == 0) {
        //          throw new WSIFException(
        //              "WSDL must contain at least one port in " + service.getQName());
        //        }
        //
        //        availablePorts = new Hashtable();
        //        for (Iterator i = ports.values().iterator(); i.hasNext();) {
        //          Port port = (Port) i.next();
        //          Binding binding = port.getBinding();
        //          if (binding != null) {
        //              List bindingExList = binding.getExtensibilityElements();
        //              if (bindingExList.size() > 0) {
        //                  ExtensibilityElement bindingFirstEx =
        //                      (ExtensibilityElement) bindingExList.get(0);
        //                  String bindingNS =
        //                      bindingFirstEx.getElementType().getNamespaceURI();
        //                  String addressNS;
        //                  List addressExList = port.getExtensibilityElements();
        //                  if (addressExList.size() > 0) {
        //                      ExtensibilityElement addressFirstEx =
        //                          (ExtensibilityElement) addressExList.get(0);
        //                      addressNS =
        //                          addressFirstEx.getElementType().getNamespaceURI();
        //                  } else {
        //                      addressNS = bindingNS;
        //                  }
        //                  if (WSIFPluggableProviders
        //                          .isProviderAvailable(bindingNS, addressNS)) {
        //                      // check if port has the same port type
        //                      QName ptName = portType.getQName();
        //                      if (binding.getPortType().getQName().equals(ptName)) {
        //                          String portName = port.getName();
        //                          availablePorts.put(portName, port);
        //                      }
        //                  }
        //              }
        //          }
        //        }
        //
        //        // Set up the WSIFMapper and WSIFMappingConvention appropriately
        //        String mapperClass = null;
        //        String mappingConvClass = null;
        //        try {
        //          mapperClass =
        //              (String) features.get(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS);
        //        } catch (ClassCastException cce) {
        //          Trc.ignoredException(cce);
        //        }
        //        try {
        //          mappingConvClass =
        //              (String) features.get(
        //              WSIFConstants.WSIF_FEATURE_MAPPINGCONVENTION_CLASS);
        //        } catch (ClassCastException cce) {
        //          Trc.ignoredException(cce);
        //        }
        //
        //        overrideMapper(mapperClass);
        //        overrideMappingConvention(mappingConvClass);
        //
        //        // If automatic mapping of types is required, generate the list of SchemaType objects
        //        if (autoMapTypesOn()) {
        //          populateSchemaTypes(specialistLocator);
        //        }
    }
    
    /**
     * Create dynamic port instance from WSDL model defnition and port.
     */
    private WSIFPort createDynamicWSIFPort(WsdlPort port)
        throws WSIFException
    {
        //        checkWSDLForWSIF(def);
        //        List bindingExList = port.getBinding().getExtensibilityElements();
        //        ExtensibilityElement bindingFirstEx =
        //          (ExtensibilityElement) bindingExList.get(0);
        //        String bindingNS = bindingFirstEx.getElementType().getNamespaceURI();
        //        WSIFProvider provider = WSIFPluggableProviders.getProvider(bindingNS);
        //        if (provider == null) {
        //          throw new WSIFException(
        //              "could not find suitable provider for binding namespace '"
        //                  + bindingNS
        //                  + "'");
        //        }
        
        WSIFPort wsifPort = WSIFProviderManager.getInstance().createDynamicWSIFPort(port, typeMapper);
        //provider.createDynamicWSIFPort(def, service, port, typeMap);
        //wsifPort.setContext(getContext());
        return wsifPort;
    }
    
    public WSIFPort getPort() throws WSIFException {
        WSIFPort wp = null;
        if (preferredPort != null) {
            //                && availablePorts.get(preferredPort) != null)
            wp = getPort(this.preferredPort);
        } else {
            //            if (preferredPort != null) {
            //                MessageLogger.log("WSIF.0011I", preferredPort);
            //            }
            wp = getPort(null);
        }
        return wp;
        
    }
    
    /**
     * Return dynamic port instance selected by port name.
     */
    public WSIFPort getPort(String portName) throws WSIFException {
        WsdlPort port = null;
        
        if (portName == null) {
            // Get first available port
            for(Iterator iter = service.getPorts().iterator(); iter.hasNext(); ) {
                port = (WsdlPort) iter.next();
                break;
            }
            
            if (port == null) {
                throw new WSIFException("Unable to find an available port");
            }
        } else {
            StringBuffer listOfPortNames = new StringBuffer("{");
            for(Iterator iter = service.getPorts().iterator(); iter.hasNext(); ) {
                WsdlPort possiblePort = (WsdlPort) iter.next();
                if(listOfPortNames.length() > 1) {
                    listOfPortNames.append(",");
                }
                String possiblePortName = possiblePort.getPortName();
                listOfPortNames.append(possiblePortName);
                if(possiblePortName.equals(portName)) {
                    port = possiblePort;
                    break;
                }
            }
            listOfPortNames.append("}");
            if (port == null) {
                throw new WSIFException(
                    "Port '"+portName+ "' is not available and no alternative can be found "
                        +listOfPortNames);
            }
        }
        WSIFPort portInstance = createDynamicWSIFPort(port);
        if (portInstance == null) {
            throw new WSIFException(
                "Provider was unable to create WSIFPort for port "
                    + port.getName());
        }
        // Store the chosen port so that we can query which was is being used
        chosenPort = port;
        
        return portInstance;
    }
    
    //
    //    /**
    //      * Create a WSIF service instance from another instance. This constructor
    //      * is used by the caching mechanism in WSIFServiceFactoryImpl
    //      */
    //    WSIFServiceImpl(WSIFServiceImpl wsi) throws WSIFException {
    //        Trc.entry(this, wsi);
    //        copyInitializedService(wsi);
    //        if (Trc.ON)
    //            Trc.exit(deep());
    //    }
    
    //    /**
    //     * Copy the "read-only" parts of an initialized WSIFServiceImpl
    //     */
    //    private void copyInitializedService(WSIFServiceImpl svc) {
    //        this.def = svc.def;
    //        this.service = svc.service;
    //        this.portType = svc.portType;
    //        this.availablePorts = (Map) ((Hashtable) svc.availablePorts).clone();
    //        this.typeMap = svc.typeMap.copy();
    //        this.schemaTypesInitialised = svc.schemaTypesInitialised;
    //        this.typeMapInitialised = svc.typeMapInitialised;
    //        this.schemaTypes = (ArrayList) svc.schemaTypes.clone();
    //        this.mapCon = svc.mapCon;
    //        this.mapper = svc.mapper;
    //        if (svc.features instanceof Hashtable) {
    //            this.features = (Map) ((Hashtable) svc.features).clone();
    //        } else if (svc.features instanceof HashMap) {
    //            this.features = (Map) ((HashMap) svc.features).clone();
    //        } else {
    //            this.features = svc.features;
    //        }
    //    }
    
    /**
     * Set the preferred port
     * @param portName The name of the port to use
     */
    public void setPreferredPort(String portName) throws WSIFException {
        this.preferredPort = portName;
        //TODO validateif such port exist?
        
        //        if (portName == null) {
        //            throw new WSIFException("Preferred port name cannot be null");
        //        }
        //        WsdlPortType pt = getPortTypeFromPortName(portName);
        //        if (pt.getQName().equals(this.portType.getQName())) {
        //            this.preferredPort = portName;
        //        } else {
        //            throw new WSIFException(
        //                "Preferred port "
        //                    + portName
        //                    + "is not available for the port type "
        //                    + this.portType.getQName());
        //        }
    }
    
    //    /**
    //     * Create a PortType object from the name of a port
    //     * @param portName The name of the port
    //     * @return A PortType corresponding to the port type used by the
    //     * specified port
    //     */
    //    private WsdlPortType getPortTypeFromPortName(String portName)
    //        throws WSIFException
    //    {
    //        //        if (portName == null) {
    //        //            throw new WSIFException("Unable to find port type from a null port name");
    //        //        }
    //        //        WsdlPort port = service.getPort(portName);
    //        //        if (port == null) {
    //        //            throw new WSIFException(
    //        //                "Port '" + portName + "' cannot be found in the service");
    //        //        }
    //        //        WsdlBinding binding = port.getBinding();
    //        //        if (binding == null) {
    //        //            throw new WSIFException(
    //        //                "No binding found for port '" + portName + "'");
    //        //        }
    //        //        WsdlPortType pt = binding.getPortType();
    //        //        if (pt == null) {
    //        //            throw new WSIFException(
    //        //                "No port type found for binding '" + binding.getQName() + "'");
    //        //        }
    //        //        checkPortTypeInformation(def, pt);
    //        //        return pt;
    //
    //        return null; //TODO
    //    }
    
    //    /**
    //     * Get the names of the available ports
    //     * @return Iterator for list of available port names.
    //     */
    //    public Iterator getAvailablePortNames() throws WSIFException {
    //        Iterator it = null;
    //        if (availablePorts != null) {
    //          it = availablePorts.keySet().iterator();
    //        }
    //        return it;
    //    }
    
    
    //    /**
    //     * Add an association between XML and Java type.
    //     * @param xmlType The qualified xml name
    //     * @param javaType The Java class
    //     * @param force flag to indicate if mapping should override an existing one
    //     * for the same xmlType
    //     */
    //    private void mapType(QName xmlType, Class javaType, boolean force)
    //        throws WSIFException {
    //        Trc.entry(this, xmlType, javaType, new Boolean(force));
    //        typeMap.mapType(xmlType, javaType, force);
    //        Trc.exit();
    //    }
    //
    //    /**
    //     * Add association between XML and Java type.
    //     * @param xmlType The qualified xml name
    //     * @param javaType The Java class
    //     */
    //    public void mapType(QName xmlType, Class javaType) throws WSIFException {
    //        Trc.entry(this, xmlType, javaType);
    //        if (mapper != null) {
    //            mapper.overrideTypeMapping(xmlType, javaType.getName());
    //        }
    //        if (!schemaTypesInitialised) {
    //            // Map it directly now!
    //            typeMap.mapType(xmlType, javaType, true);
    //        }
    //        Trc.exit();
    //    }
    //
    //    /**
    //     * Add association between XML and Java type.
    //     * @param xmlType The qualified xml name
    //     * @param className The Java class name
    //     */
    //    public void mapType(QName xmlType, String className) throws WSIFException {
    //        Trc.entry(this, xmlType, className);
    //        if (mapper != null) {
    //            mapper.overrideTypeMapping(xmlType, className);
    //        }
    //        if (!schemaTypesInitialised) {
    //            // Map it directly now!
    //            Class clazz = null;
    //            try {
    //                clazz =
    //                    Class.forName(
    //                        className,
    //                        true,
    //                        Thread.currentThread().getContextClassLoader());
    //            } catch (ClassNotFoundException e) {
    //                // Ignore error - mapping will not be added
    //                Trc.ignoredException(e);
    //            }
    //            if (clazz != null) {
    //                typeMap.mapType(xmlType, clazz, true);
    //            }
    //        }
    //        Trc.exit();
    //    }
    //
    //    /**
    //     * Add an association between a namespace URI and and a Java package. Calling
    //     * this method will trigger the automatic mapping of types, regardless of what
    //     * was set at the WSIFServiceFactory level, since all the types in the schema(s)
    //     * need to be examined in order to use the information provided.
    //     * @param namespace The namespace URI
    //     * @param packageName The full package name
    //     */
    //    public void mapPackage(String namespace, String packageName)
    //        throws WSIFException {
    //        Trc.entry(namespace, packageName);
    //        // In order to use package mapping information, we need a list
    //        // of types to apply the mapping to, so let's make sure we have one!
    //        populateSchemaTypes(specialistLocator);
    //
    //        if (mapCon != null) {
    //            mapCon.overridePackageMapping(namespace, packageName);
    //        }
    //        Trc.exit();
    //    }
    //
    //    /**
    //     * @deprecated this method is replaced by the getProvider
    //     * method in the org.apache.util.WSIFPluggableProviders class
    //     */
    //    public static WSIFProvider getDynamicWSIFProvider(String namespaceURI) {
    //        Trc.entry(null, namespaceURI);
    //        WSIFProvider p = WSIFPluggableProviders.getProvider(namespaceURI);
    //        Trc.exit(p);
    //        return p;
    //    }
    //
    //    /**
    //     * @deprecated this method is replaced by the overrideDefaultProvider
    //     * method in the org.apache.util.WSIFPluggableProviders class
    //     */
    //    public static void setDynamicWSIFProvider(
    //        String providerNamespaceURI,
    //        WSIFProvider provider) {
    //        Trc.entry(null, providerNamespaceURI, provider);
    //
    //        WSIFPluggableProviders.overrideDefaultProvider(
    //            providerNamespaceURI,
    //            provider);
    //
    //        Trc.exit();
    //    }
    //
    //    /**
    //     * @deprecated this method is replaced by the setAutoLoadProviders
    //     * method in the org.apache.util.WSIFPluggableProviders class
    //     */
    //    public static void setAutoLoadProviders(boolean b) {
    //        Trc.entry(null, b);
    //        WSIFPluggableProviders.setAutoLoadProviders(b);
    //        Trc.exit();
    //    }
    //
    //    /**
    //     * Get the dynamic proxy that will implement the interface iface
    //     * for the port portName.
    //     */
    //    public Object getStub(String portName, Class iface) throws WSIFException {
    //        Trc.entry(this, portName, iface);
    //
    //        // Stub support has always included automatically mapping
    //        // types, albeit badly! So we need to make sure that we
    //        // do it now, regardless of what flag is set on the WSIFServiceFactory
    //        populateSchemaTypes(specialistLocator);
    //
    //        // if the port is not available, force the expection now rather
    //        // rather than go through the rest of this method
    //        WSIFPort wsifPort = getPort(portName);
    //
    //        // If we've got to this line then the port must be available
    //        PortType pt = getPortTypeFromPortName(portName);
    //
    //        // If the user has already created a proxy for this interface before
    //        // but is now asking for a proxy for the same interface but a different
    //        // portName, we should cache the proxy here and just call
    //        // clientProxy.setPort() instead.
    //        WSIFClientProxy clientProxy =
    //            WSIFClientProxy.newInstance(
    //                iface,
    //                def,
    //                service.getQName().getNamespaceURI(),
    //                service.getQName().getLocalPart(),
    //                portType.getQName().getNamespaceURI(),
    //                portType.getQName().getLocalPart(),
    //                typeMap);
    //
    //        clientProxy.setPort(wsifPort);
    //        Object proxy = clientProxy.getProxy();
    //
    //        // Tracing the proxy causes a hang!
    //        Trc.exit();
    //        return proxy;
    //    }
    //
    //    /**
    //     * Get the dynamic proxy that will implement the interface iface
    //     */
    //    public Object getStub(Class iface) throws WSIFException {
    //        Trc.entry(this, iface);
    //
    //        // Stub support has always included automatically mapping
    //        // types, albeit badly! So we need to make sure that we
    //        // do it now, regardless of what flag is set on the WSIFServiceFactory
    //        populateSchemaTypes(specialistLocator);
    //
    //        // if the port is not available, force the expection now rather
    //        // rather than go through the rest of this method
    //        WSIFPort wsifPort = getPort();
    //
    //        // Chosen port has been stored so use it to find portType
    //        String portName = chosenPort.getName();
    //        PortType pt = getPortTypeFromPortName(portName);
    //
    //        // If the user has already created a proxy for this interface before
    //        // but is now asking for a proxy for the same interface but a different
    //        // portName, we should cache the proxy here and just call
    //        // clientProxy.setPort() instead.
    //        WSIFClientProxy clientProxy =
    //            WSIFClientProxy.newInstance(
    //                iface,
    //                def,
    //                service.getQName().getNamespaceURI(),
    //                service.getQName().getLocalPart(),
    //                pt.getQName().getNamespaceURI(),
    //                pt.getQName().getLocalPart(),
    //                typeMap);
    //
    //        clientProxy.setPort(wsifPort);
    //        Object proxy = clientProxy.getProxy();
    //
    //        // Tracing the proxy causes a hang!
    //        Trc.exit();
    //        return proxy;
    //    }
    //
    //    /**
    //     * Add new WSDL model extension registry that is shared by all
    //     * dynamic WSIF providers.
    //     */
    //    public static void addExtensionRegistry(ExtensionRegistry reg) {
    //        Trc.entry(null, reg);
    //        providersExtRegs.addExtensionRegistry(reg);
    //        Trc.exit();
    //    }
    //
    //    /**
    //     * Return extension registry that contains ALL declared extensions.
    //     * This is special registry that does not allow to register serializers
    //     * but only to add new extension registreis through
    //     * addExtensionRegistry method.
    //     *
    //     * @see #addExtensionRegistry
    //     */
    //    public static ExtensionRegistry getCompositeExtensionRegistry() {
    //        Trc.entry(null);
    //        Trc.exit(providersExtRegs);
    //        return providersExtRegs;
    //    }
    
    //    private void init(WsdlDefinitions def, WsdlService service, WsdlPortType portType)
    //        throws WSIFException
    //    {
    //        if (def == null) {
    //            throw new IllegalArgumentException("WSDL definition can not be null");
    //        }
    //        checkWSDLForWSIF(def);
    //
    //        if (service == null) {
    //            Map services = WSIFUtils.getAllItems(def, "Service");
    //
    //            service =
    //                (Service) WSIFUtils.getNamedItem(services, null, "Service");
    //        }
    //
    //        if (portType == null) {
    //            // if all ports have the same portType --> use it
    //            Map ports = service.getPorts();
    //            if (ports.size() == 0) {
    //                throw new WSIFException(
    //                    "WSDL must contain at least one port in "
    //                        + service.getQName());
    //            }
    //
    //            for (Iterator i = ports.values().iterator(); i.hasNext();) {
    //                Port port = (Port) i.next();
    //                if (portType == null) {
    //                    portType = port.getBinding().getPortType();
    //                } else {
    //                    PortType pt = port.getBinding().getPortType();
    //                    if (!pt.getQName().equals(portType.getQName())) {
    //                        throw new WSIFException(
    //                            "when no port type was specified all ports "
    //                                + "must have the same port type in WSDL service "
    //                                + service.getQName());
    //                    }
    //                }
    //            }
    //            if (portType == null) {
    //                throw new IllegalArgumentException(
    //                    "WSDL more than one portType in service " + service);
    //
    //            }
    //        }
    //        this.def = def;
    //        this.service = service;
    //        this.portType = portType;
    //
    //        // checkPortTypeIsRPC(Definition def, PortType portType) has been replaced by
    //        // checkPortTypeInformation(Definition def, PortType portType) since "Input Only"
    //        // operations are supported.
    //        checkPortTypeInformation(def, portType);
    //
    //        // get all ports from service that has given portType
    //
    //        Map ports = service.getPorts();
    //        // check that service has at least one port ...
    //        if (ports.size() == 0) {
    //            throw new WSIFException(
    //                "WSDL must contain at least one port in " + service.getQName());
    //        }
    //
    //        availablePorts = new Hashtable();
    //        for (Iterator i = ports.values().iterator(); i.hasNext();) {
    //            Port port = (Port) i.next();
    //            Binding binding = port.getBinding();
    //            if (binding != null) {
    //                List bindingExList = binding.getExtensibilityElements();
    //                if (bindingExList.size() > 0) {
    //                    ExtensibilityElement bindingFirstEx =
    //                        (ExtensibilityElement) bindingExList.get(0);
    //                    String bindingNS =
    //                        bindingFirstEx.getElementType().getNamespaceURI();
    //                    String addressNS;
    //                    List addressExList = port.getExtensibilityElements();
    //                    if (addressExList.size() > 0) {
    //                        ExtensibilityElement addressFirstEx =
    //                            (ExtensibilityElement) addressExList.get(0);
    //                        addressNS =
    //                            addressFirstEx.getElementType().getNamespaceURI();
    //                    } else {
    //                        addressNS = bindingNS;
    //                    }
    //                    if (WSIFPluggableProviders
    //                            .isProviderAvailable(bindingNS, addressNS)) {
    //                        // check if port has the same port type
    //                        QName ptName = portType.getQName();
    //                        if (binding.getPortType().getQName().equals(ptName)) {
    //                            String portName = port.getName();
    //                            availablePorts.put(portName, port);
    //                        }
    //                    }
    //                }
    //            }
    //        }
    //
    //        // Set up the WSIFMapper and WSIFMappingConvention appropriately
    //        String mapperClass = null;
    //        String mappingConvClass = null;
    //        try {
    //            mapperClass =
    //                (String) features.get(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS);
    //        } catch (ClassCastException cce) {
    //            Trc.ignoredException(cce);
    //        }
    //        try {
    //            mappingConvClass =
    //                (String) features.get(
    //                WSIFConstants.WSIF_FEATURE_MAPPINGCONVENTION_CLASS);
    //        } catch (ClassCastException cce) {
    //            Trc.ignoredException(cce);
    //        }
    //
    //        overrideMapper(mapperClass);
    //        overrideMappingConvention(mappingConvClass);
    //
    //        // If automatic mapping of types is required, generate the list of SchemaType objects
    //        if (autoMapTypesOn()) {
    //            populateSchemaTypes(specialistLocator);
    //        }
    //    }
    //
    //    /**
    //     * Check PortType information is consistent. This method can be updated when
    //     * new operation types are supported.
    //     */
    //    private void checkPortTypeInformation(Definition def, PortType portType)
    //        throws WSIFException {
    //        List operationList = portType.getOperations();
    //
    //        // process each operation to create dynamic operation instance
    //        for (Iterator i = operationList.iterator(); i.hasNext();) {
    //            Operation op = (Operation) i.next();
    //            String name = op.getName();
    //            if (op.isUndefined()) {
    //                throw new WSIFException("operation " + name + " is undefined!");
    //            }
    //            OperationType opType = op.getStyle();
    //            if (opType == null) {
    //                throw new WSIFException("operation " + name + " has no type!");
    //            }
    //            if (opType.equals(OperationType.REQUEST_RESPONSE)) {
    //                Input input = op.getInput();
    //                Output output = op.getOutput();
    //                if (input == null) {
    //                    throw new WSIFException(
    //                        "missing input message for operation " + name);
    //                }
    //                if (output == null) {
    //                    throw new WSIFException(
    //                        "missing output message for operation " + name);
    //                }
    //            } else if (opType.equals(OperationType.ONE_WAY)) {
    //                Input input = op.getInput();
    //                if (input == null) {
    //                    throw new WSIFException(
    //                        "missing input message for operation " + name);
    //                }
    //            } else {
    //                // Log message
    //                MessageLogger.log(
    //                    "WSIF.0004E",
    //                    opType,
    //                    portType.getQName().getLocalPart());
    //
    //                // End message
    //                throw new WSIFException(
    //                    "operation type "
    //                        + opType
    //                        + " is not supported in port instance for "
    //                        + portType.getQName());
    //            }
    //        }
    //    }
    //
    //    private void checkWSDLForWSIF(Definition def) throws WSIFException {
    //        try {
    //            checkWSDL(def);
    //        } catch (WSDLException ex) {
    //            Trc.exception(ex);
    //            throw new WSIFException(
    //                "invalid WSDL defintion " + def.getQName(),
    //                ex);
    //        }
    //    }
    //
    //    /**
    //     * Check WSDL defintion to make sure it does not contain undefined
    //     * elements (typical case is referncing not defined portType).
    //     * <p><b>NOTE:</b> check is done only for curent document and not
    //     *  recursively for imported ones (they may be invalid but this
    //     *  port factory may not need them...).
    //     */
    //    private void checkWSDL(Definition def) throws WSDLException {
    //        for (Iterator i = def.getMessages().values().iterator();
    //                 i.hasNext();
    //            ) {
    //            Message v = (Message) i.next();
    //            if (v.isUndefined()) {
    //                throw new WSDLException(
    //                    WSDLException.INVALID_WSDL,
    //                    "referencing undefined message " + v);
    //            }
    //        }
    //        for (Iterator i = def.getPortTypes().values().iterator();
    //                 i.hasNext();
    //            ) {
    //            PortType v = (PortType) i.next();
    //            if (v.isUndefined()) {
    //                throw new WSDLException(
    //                    WSDLException.INVALID_WSDL,
    //                    "referencing undefined portType " + v);
    //            }
    //        }
    //        for (Iterator i = def.getBindings().values().iterator();
    //                 i.hasNext();
    //            ) {
    //            Binding v = (Binding) i.next();
    //            if (v.isUndefined()) {
    //                throw new WSDLException(
    //                    WSDLException.INVALID_WSDL,
    //                    "referencing undefined binding " + v);
    //            }
    //        }
    //    }
    
    //    /**
    //     * Get the Definition object representing the wsdl document
    //     * @return The Definition object
    //     */
    //    public WsdlDefinitions getDefinition() {
    //        return def;
    //    }
    
    //    /**
    //     * Gets the context information for this WSIFService.
    //     * @return context
    //     */
    //    public WSIFMessage getContext() throws WSIFException {
    //        Trc.entry(this);
    //        WSIFMessage contextCopy;
    //        if (this.context == null) {
    //            contextCopy = new WSIFDefaultMessage();
    //        } else {
    //            try {
    //                contextCopy = (WSIFMessage) this.context.clone();
    //            } catch (CloneNotSupportedException e) {
    //                throw new WSIFException(
    //                    "CloneNotSupportedException cloning context",
    //                    e);
    //            }
    //        }
    //        Trc.exit(contextCopy);
    //        return contextCopy;
    //    }
    //
    //    /**
    //     * Sets the context information for this WSIFService.
    //     * @param WSIFMessage the new context information
    //     */
    //    public void setContext(WSIFMessage context) {
    //        Trc.entry(this, context);
    //        if (context == null) {
    //            throw new IllegalArgumentException("context must not be null");
    //        }
    //        this.context = context;
    //        Trc.exit();
    //    }
    //
    //    /**
    //     * Override the WSIFMapper used by this WSIFService
    //     * @param mapperClassName The class name for the WSIFMapper implementation to be used by this WSIFService
    //     * @throws A WSIFException if the WSIFMapper cannot be found or set
    //     */
    //    protected void overrideMapper(String mapperClassName)
    //        throws WSIFException {
    //        if (mapperClassName != null) {
    //            mapper = WSIFMapperFactory.newMapper(mapperClassName);
    //        } else {
    //            mapper = WSIFMapperFactory.newMapper();
    //        }
    //    }
    //
    //    /**
    //     * Override the WSIFMappingConvention used by this WSIFService
    //     * @param mappingConvClassName The class name for the WSIFMappingConvention implementation
    //     * to be used by this WSIFService
    //     * @throws A WSIFException if the WSIFMappingConvention cannot be found or set
    //     */
    //    protected void overrideMappingConvention(String mappingConvClassName)
    //        throws WSIFException {
    //        if (mappingConvClassName != null) {
    //            mapCon =
    //                WSIFMappingConventionFactory.newMappingConvention(
    //                    mappingConvClassName);
    //        } else {
    //            mapCon = WSIFMappingConventionFactory.newMappingConvention();
    //        }
    //        mapper.setMappingConvention(mapCon);
    //    }
    //
    //    /**
    //     * Parse the schema(s) associated with the wsdl and find all the types/elements that are defined.
    //     * @param loc A WSDLLocator to use in finding imported xsd files
    //     * @throws A WSIFException if the parsing fails
    //     */
    //    protected void populateSchemaTypes(WSDLLocator loc) throws WSIFException {
    //        // Get a lock on the list of type. Nobody else should be able to alter it
    //        // while we are working in this method.
    //        synchronized (schemaTypes) {
    //            // Don't parse the schema if we've already done it!!
    //            if (schemaTypesInitialised)
    //                return;
    //            if (loc == null) {
    //                loc =
    //                    new WSIFWSDLLocatorImpl(
    //                        (String) null,
    //                        (String) null,
    //                        (ClassLoader) null);
    //            }
    //            Parser.getAllSchemaTypes(def, schemaTypes, loc);
    //
    //            // if we can, close the WSDLLocator
    //            if (loc instanceof ClosableLocator) {
    //                try {
    //                    ((ClosableLocator) loc).close();
    //                } catch (IOException ioe) {
    //                    // Ignore. Is this the correct thing to do??
    //                    Trc.ignoredException(ioe);
    //                }
    //            }
    //
    //            // Add a read-only version of the list of types to the context message
    //            // so that providers can use the information if needed
    //            WSIFMessage ctx = getContext();
    //            ctx.setObjectPart(
    //                WSIFConstants.CONTEXT_SCHEMA_TYPES,
    //                Collections.unmodifiableList(schemaTypes));
    //            setContext(ctx);
    //
    //            schemaTypesInitialised = true;
    //        }
    //    }
    //
    //    /**
    //     * Create and set all mappings from xml types to Java classes
    //     * @throws A WSIFException if the mapping process fails
    //     */
    //    protected void setupTypeMappings() throws WSIFException {
    //        // Get a lock on the list of type. Nobody else should be able to alter it
    //        // while we are working in this method.
    //        synchronized (schemaTypes) {
    //            // Don't calculate the mappings if we've already done it!!
    //            // This is a one off method. Perhaps in the future a way of updating
    //            // existing mappings could be added.
    //            if (typeMapInitialised)
    //                return;
    //
    //            // Without a mapper we can't do any mapping!!
    //            if (mapper == null) {
    //                Trc.event(
    //                    this,
    //                    "Automatic mapping of types did not take place "
    //                        + "because mapper was null");
    //                return;
    //            }
    //            SchemaType[] types = new SchemaType[schemaTypes.size()];
    //            schemaTypes.toArray(types);
    //
    //            Map mappings = mapper.getMappings(types);
    //            if (mappings != null) {
    //                Iterator it = mappings.keySet().iterator();
    //                while (it.hasNext()) {
    //                    QName xmlType = (QName) it.next();
    //                    String clsName = (String) mappings.get(xmlType);
    //                    if (clsName != null) {
    //                        Class clazz = null;
    //                        try {
    //                            clazz =
    //                                Class.forName(
    //                                    clsName,
    //                                    true,
    //                                    Thread
    //                                        .currentThread()
    //                                        .getContextClassLoader());
    //                        } catch (ClassNotFoundException e) {
    //                            // Ignore error - mapping will not be added
    //                            Trc.ignoredException(e);
    //                        }
    //                        // Create a new mapping but don't override an existing
    //                        // one for the same type
    //                        if (clazz != null) {
    //                            mapType(xmlType, clazz, false);
    //                        }
    //                    }
    //                }
    //            }
    //            typeMapInitialised = true;
    //        }
    //    }
    //
    //    private boolean autoMapTypesOn() {
    //        if (features == null) {
    //            return false;
    //        }
    //        Object on = features.get(WSIFConstants.WSIF_FEATURE_AUTO_MAP_TYPES);
    //        if (on != null && on instanceof Boolean) {
    //            if (((Boolean) on).booleanValue()) {
    //                return true;
    //            } else {
    //                return false;
    //            }
    //        } else {
    //            return false;
    //        }
    //    }
    //
    //    private PasswordAuthentication getProxyAuthentication() {
    //        if (features != null) {
    //            Object pa =
    //                features.get(WSIFConstants.WSIF_FEATURE_PROXY_AUTHENTICATION);
    //            if (pa != null && pa instanceof PasswordAuthentication) {
    //                return (PasswordAuthentication) pa;
    //            }
    //        }
    //        return null;
    //    }
    //
    //    public String deep() {
    //        String buff = "";
    //        try {
    //            buff = new String(this.toString());
    //            buff += "\nprovidersExtRegs:"
    //                + (providersExtRegs == null
    //                    ? "null"
    //                    : providersExtRegs.toString());
    //            buff += "\ndef:" + Trc.brief(def);
    //            buff += "\nservice:" + Trc.brief(service);
    //            buff += "\nportType:" + Trc.brief(portType);
    //            buff += "\navailablePorts:" + Trc.brief(availablePorts);
    //            buff += "\ntypeMap:"
    //                + (typeMap == null ? "null" : typeMap.toString());
    //            buff += "\ntypeMapInitialised:" + typeMapInitialised;
    //            buff += "\npreferredPort:"
    //                + (preferredPort == null ? "null" : preferredPort);
    //            buff += "\nchosenPort:" + Trc.brief(chosenPort);
    //            buff += "\ncontext:" + context;
    //        } catch (Exception e) {
    //            Trc.exceptionInTrace(e);
    //        }
    //        return buff;
    //    }
}
