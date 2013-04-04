/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.globus.security.authentication.wssec;


import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEREncodableVector;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

import org.globus.gsi.CertUtil;
import org.globus.gsi.bc.BouncyCastleUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;


public class PKIPathSecurityToken extends BinarySecurityToken {
    public static final QName TYPE = new QName(WSConstants.WSSE_NS, "PKIPath");

    public PKIPathSecurityToken(Element elem) throws WSSecurityException {
        super(elem);

        if (!getValueType().equals(TYPE)) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY_TOKEN, "invalidValueType",
                new Object[] { TYPE, getValueType() }
            );
        }
    }

    // create new
    public PKIPathSecurityToken(Document doc) {
        super(doc);

        setValueType(TYPE);
    }

    public X509Certificate[] getX509Certificates(boolean reverse)
        throws GeneralSecurityException, IOException {
        byte[] data = getToken();

        if (data == null) {
            return null;
        }

        DERObject obj = BouncyCastleUtil.toDERObject(data);
        ASN1Sequence seq = ASN1Sequence.getInstance(obj);
        int size = seq.size();
        ByteArrayInputStream in;
        X509Certificate[] certs = new X509Certificate[size];

        for (int i = 0; i < size; i++) {
            obj = seq.getObjectAt(i).getDERObject();
            data = BouncyCastleUtil.toByteArray(obj);
            in = new ByteArrayInputStream(data);
            certs[(reverse) ? (size - 1 - i) : i] =
                CertUtil.loadCertificate(in);
        }

        return certs;
    }

    public void setX509Certificates(
        X509Certificate[] certs,
        boolean reverse
    ) throws CertificateEncodingException, IOException {
        if (certs == null) {
            throw new IllegalArgumentException("data == null");
        }

        DEREncodableVector vec = new DEREncodableVector();

        if (reverse) {
            for (int i = certs.length - 1; i >= 0; i--) {
                vec.add(BouncyCastleUtil.toDERObject(certs[i].getEncoded()));
            }
        } else {
            for (int i = 0; i < certs.length; i++) {
                vec.add(BouncyCastleUtil.toDERObject(certs[i].getEncoded()));
            }
        }

        DERSequence seq = new DERSequence(vec);
        setToken(BouncyCastleUtil.toByteArray(seq));
    }
}
