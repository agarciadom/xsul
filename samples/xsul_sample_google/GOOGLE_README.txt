



DynamicInvoker operationName
	-Dwsdl=... wsdl=... 
	-Dkey=... key= 
	
	
To quickly try the API, run
  java -cp googleapi.jar com.google.soap.search.GoogleAPIDemo <key> search Foo
Where <key> is your registration key and Foo is the item you wish to
search for. GoogleAPIDemo is a simple demonstration of how to use the
Java API included in googleapi.jar. For usage, run it with no arguments: 
  java -cp googleapi.jar com.google.soap.search.GoogleAPIDemo

GoogleAPIDemo is only a demonstration; Java programmers should look at
the source for GoogleAPIDemo and the included Javadoc for the
GoogleSearch class to learn more about how to use our Java library.

The library has our SOAP endpoint address built in. You may want to
override this endpoint, for instance to point it at a debugging proxy.
You can do this either by calling the appropriate method in
GoogleSearch or by setting the Java property "google.soapEndpointURL".
The default URL is http://api.google.com/search/beta2
	