Provide very simple to use WSDL 1.1 API

For WSDL4J users: this API makes heavier use of XPath and 
gves direct access to underlying XML Infoset - no need to create
specialized ExtensibilityHandlers!!!

As of classes here is rough mapping:
javax.wsdl.Definition -> WsdlDefinitions (not "s" at the end!!!)
	WsdlDefinition is an entry point o get everything else including raw XML!!!]
javax.wsdl.* -> xsul.wsdl.Wsdl* (for example javax.wsdl.PortType -> xsul.wsdl.WsdlPortType)