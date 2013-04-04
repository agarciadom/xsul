/**
 * ProxyPathValidatorException.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: ProxyPathValidatorException.java,v 1.1 2005/01/26 18:52:56 lifang Exp $
 */

package xsul.dsig.globus.security.authentication.wssec;

import java.security.cert.X509Certificate;

public class ProxyPathValidatorException extends Exception {
    public static final int FAILURE = -1;

    // proxy constraints violation
    public static final int PROXY_VIOLATION = 1;

    // unsupported critical extensions
    public static final int UNSUPPORTED_EXTENSION = 2;

    // proxy or CA path length exceeded
    public static final int PATH_LENGTH_EXCEEDED = 3;

    // unknown CA
    public static final int UNKNOWN_CA = 4;

    // unknown proxy policy
    public static final int UNKNOWN_POLICY = 5;

    private X509Certificate cert;

    private int errorCode = FAILURE;
    
    public ProxyPathValidatorException(int errorCode) {
    this(errorCode, null);
    }

    public ProxyPathValidatorException(int errorCode,
                       Throwable root) {
    this(errorCode, "", root);
    }

    public ProxyPathValidatorException(int errorCode,
                       String msg,
                       Throwable root) {
    super(msg, root);
    this.errorCode = errorCode;
    }

    public ProxyPathValidatorException(int errorCode,
                       X509Certificate cert,
                       String msg) {
    super(msg, null);
    this.errorCode = errorCode;
    this.cert = cert;
    }
    
    public int getErrorCode() {
    return this.errorCode;
    }
    

    /**
     * Returns the certificate that was being validated when
     * the exception was thrown.
     *
     * @return the <code>Certificate</code> that was being validated when
     * the exception was thrown (or <code>null</code> if not specified)
     */
    public X509Certificate getCertificate() {
    return this.cert;
    }
        
}

