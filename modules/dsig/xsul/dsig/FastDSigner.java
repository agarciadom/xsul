/**
 * SoapSignature.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: FastDSigner.java,v 1.3 2005/06/30 20:29:15 lifang Exp $
 */

package xsul.dsig;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import xsul.MLogger;

public class FastDSigner {
    private final static MLogger logger = MLogger.getLogger();
    
    private Signature sig = null;
    private String algorithm;
    
    public FastDSigner(String algo) {
        algorithm = algo;
        try {
            sig = Signature.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            logger.severe("no such algorithm: " + algo, e);
            sig = null;
        }
    }
    
    public byte[] sign(PrivateKey priKey, byte[] tobesigned)
        throws Exception {
        sig.initSign(priKey);
        sig.update(tobesigned);
        return sig.sign();
    }
    
    public boolean verify(Certificate cert, String text, byte[] tobeverified)
        throws Exception {
        return verify(cert, text.getBytes(), tobeverified);
    }

    public boolean verify(Certificate cert, byte[] btext, byte[] tobeverified)
        throws Exception {
        sig.initVerify(cert);
        sig.update(btext);
        return sig.verify(tobeverified);
    }
}

