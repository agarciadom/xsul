*> Set local classapth (run classpath.bat or source classpath.sh)

*> Start test server in a separate window
start java -Dlog xsul.wsif.XwsifTestServer 1234

*> Run client
java xsul.dii.XsulDynamicInvoker tests/xsul/wsif/XwsifTestService.wsdl executeInputOnly baza

java xsul.dii.XsulDynamicInvoker tests/xsul/wsif/XwsifTestService.wsdl executeRequestResponse baza

java xsul.dii.XsulDynamicInvoker tests/xsul/wsif/XwsifTestService.wsdl executeRequestResponseWsdlFault baza

for troubleshooting
java -Dlog xsul.dii.XsulDynamicInvoker tests/xsul/wsif/XwsifTestService.wsdl executeRequestResponse baza
java -Dlog xsul.dii.XsulDynamicInvoker tests/xsul/wsif/XwsifTestService.wsdl executeRequestResponseWsdlFault baza

