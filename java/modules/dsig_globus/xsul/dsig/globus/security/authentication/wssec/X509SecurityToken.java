/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.globus.security.authentication.wssec;


import org.globus.gsi.CertUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.io.ByteArrayInputStream;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;


public class X509SecurityToken extends BinarySecurityToken {
    public static final QName TYPE = new QName(WSConstants.WSSE_NS, "X509v3");

    public X509SecurityToken(Element elem) throws WSSecurityException {
        super(elem);

        if (!getValueType().equals(TYPE)) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY_TOKEN, "invalidValueType",
                new Object[] { TYPE, getValueType() }
            );
        }
    }

    // create new
    public X509SecurityToken(Document doc) {
        super(doc);

        setValueType(TYPE);
    }

    public X509Certificate getX509Certificate() throws GeneralSecurityException {
        byte[] data = getToken();

        if (data == null) {
            return null;
        }

        ByteArrayInputStream in = new ByteArrayInputStream(data);

        return CertUtil.loadCertificate(in);
    }

    public void setX509Certificate(X509Certificate cert)
        throws CertificateEncodingException {
        if (cert == null) {
            throw new IllegalArgumentException("data == null");
        }

        setToken(cert.getEncoded());
    }
}
