after executing classpath.sh/csh/bat in each window,

For WS-SecureConversation:

-- Ws-secconv server:
java [-Dalias=alias -Dpassword=password -Dkspaswd=keystorepassword] -cp `./classpath.sh` xsul.secconv.service.SecurityRequestorServer 5656

The server will choose the protocol dynamically according to the client. 

However, to use RSA key pairs stored in KeyStore, you also need to put 
the following parameters to the command line, right after "java":
-Dalias=alias -Dpassword=yourpassword -Dkspaswd=keystorepassword

If you choose autha protocol, put the password parameter in the command line, right after "java":
-Dpassword=yourpassword

-- Interop server:
java -cp `./classpath.sh` simplesoap.service.InteropSecConvService 8989

-- Interop client:
Now support 3 modes:

* To use proxy certificate:
java -Dscprotocol=globus -cp `./classpath.sh` simplesoap.client.InteropSecConv http://localhost:8989/interop?wsdl http://localhost:5656

* To use RSA key pairs stored in KeyStore:
Sun has a keytool manual here: 
http://java.sun.com/j2se/1.3/docs/tooldocs/win32/keytool.html
java -Dscprotocol=ks -Dalias=alias -Dpassword=yourpassword -Dkspaswd=yourkeystorepassword -cp `./classpath.sh` simplesoap.client.InteropSecConv http://localhost:8989/interop?wsdl http://localhost:5656

* To use password-based protocol:
java -Dscprotocol=autha -Dpassword=yourpassword -cp `./classpath.sh` simplesoap.client.InteropSecConv http://localhost:8989/interop?wsdl http://localhost:5656


For Den:

-- Interop server without any security processing but application logic:
java simplesoap.service.InteropService 2346

-- Processor service with Den handler integrated. It is for general purpose since interop logic is to be omitted. The generalization work is to be done.
java simplesoap.service.InteropDenService 2345

-- dispatcher with table.properites and default.properties configured
java xsul.dispatcher.rpc.DispatcherRPC

-- client
java simplesoap.client.InteropSecure http://localhost/intop