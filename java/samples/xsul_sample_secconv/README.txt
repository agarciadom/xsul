after executing classpath.sh/csh/bat in each window,

-- Echo client:

Now support 3 modes:

* To use proxy certificate:
java -Dscprotocol=globus -cp `./classpath.sh` xsul_sample_secconv.EchoClient --svc http://localhost:8765 --scsvc http://localhost:8989

* To use RSA key pairs stored in KeyStore:
java -Dscprotocol=ks -Dalias=alias -Dpassword=password -Dkspaswd=keystorepassword -cp `./classpath.sh` xsul_sample_secconv.EchoClient --svc http://localhost:8765 --scsvc http://localhost:8989

* To use password-based protocol:
java -Dscprotocol=autha -cp `./classpath.sh` xsul_sample_secconv.EchoClient --svc http://localhost:8765 --scsvc http://localhost:8989

By adding the option --number, you may specify the number of client requests to be made.

-- Ws-secconv server:
java -cp `./classpath.sh` xsul.secconv.service.SecurityRequestorServer 8989

The server will choose the protocol dynamically according to the client. 
However, to use RSA key pairs stored in KeyStore, you also need to put 
the following parameters to the command line, right after "java":
-Dalias=alias -Dpassword=password -Dkspaswd=keystorepassword

-- Echo server:
java -cp `./classpath.sh` xsul_sample_secconv.EchoServer --port 8765


