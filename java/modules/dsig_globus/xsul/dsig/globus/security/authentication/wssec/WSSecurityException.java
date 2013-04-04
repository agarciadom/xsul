/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.globus.security.authentication.wssec;


import xsul.XsulException;


public class WSSecurityException extends XsulException {
    // this is a generic error
    public static final int FAILURE = 0;
    public static final int UNSUPPORTED_SECURITY_TOKEN = 1;
    public static final int UNSUPPORTED_ALGORITHM = 2;
    public static final int INVALID_SECURITY = 3;
    public static final int INVALID_SECURITY_TOKEN = 4;
    public static final int FAILED_AUTHENTICATION = 5;
    public static final int FAILED_CHECK = 6;
    public static final int SECURITY_TOKEN_UNAVAILABLE = 7;


    public WSSecurityException(
        int errorCode,
        String msgId,
        Object[] args,
        Throwable exception
    ) {
        super(getMessage(errorCode, msgId, args), exception);
    }

    public WSSecurityException(
        int errorCode,
        String msgId,
        Object[] args
    ) {
        super(getMessage(errorCode, msgId, args));
    }

    public WSSecurityException(
        int errorCode,
        String msgId
    ) {
        this(errorCode, msgId, null);
    }

    public WSSecurityException(int errorCode) {
        this(errorCode, null, null);
    }

    private static String getMessage(
        int errorCode,
        String msgId,
        Object[] args
    ) {
        return msgId;
    }
}
