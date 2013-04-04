/**
 * PkiClientNegotiator.java
 *
 * support using public key for key exchange
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.secconv.pki;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import org.bouncycastle.asn1.DERObject;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.bc.BouncyCastleUtil;
import xsul.MLogger;
import xsul.dsig.saml.authorization.CapabilityUtil;
import xsul.secconv.SCUtil;
import xsul.secconv.token.pki.ClientInitTokenType;

public class GlobusCredClientNegotiator extends RSAClientNegotiator {
    private static final MLogger logger = MLogger.getLogger();
    
    private GlobusCredential globusCred;
    private TrustedCertificates trustedCerts;
    
    public GlobusCredClientNegotiator(GlobusCredential globusCred,
                                      TrustedCertificates trustedCerts) {
        this.globusCred = globusCred;
        this.trustedCerts = trustedCerts;
    }
    
    public GlobusCredClientNegotiator() throws GlobusCredentialException {
        globusCred = GlobusCredential.getDefaultCredential();
        trustedCerts = CapabilityUtil.getTrustedCertificates(null);
    }
    
    public GlobusCredClientNegotiator(String scred)
        throws GlobusCredentialException {
        this.globusCred = new GlobusCredential(scred);
        this.trustedCerts = CapabilityUtil.getTrustedCertificates(null);
    }
    
    public GlobusCredClientNegotiator(GlobusCredential gcred) {
        this.globusCred = gcred;
        trustedCerts = CapabilityUtil.getTrustedCertificates(null);
    }
    
    public void setGlobusCred(GlobusCredential globusCred) {
        this.globusCred = globusCred;
    }
    
    public GlobusCredential getGlobusCred() {
        return globusCred;
    }
    
    protected void init() throws Exception {
        prikey = globusCred.getPrivateKey();
        X509Certificate certs[] = globusCred.getCertificateChain();
        pubkey = certs[0].getPublicKey();
        if(pubkey == null) {
            throw new Exception("public key null");
        }
    }
    
    protected byte[] decrypt(byte[] serverPubkey, byte[] encsecret)
        throws Exception {
        DERObject obj = BouncyCastleUtil.toDERObject(serverPubkey);
        byte[] pubkey = BouncyCastleUtil.toByteArray(obj);
        ByteArrayInputStream in = new ByteArrayInputStream(pubkey);
        X509Certificate cert = CertUtil.loadCertificate(in);
        
        SCUtil.pathValidation(cert, trustedCerts);
        logger.finest("path validated !!!");
        
        return decryptSecret(cert.getPublicKey(), encsecret);
    }
    
    protected void loadCertificate(ClientInitTokenType cit)
        throws Exception {
        cit.setPublicKey(globusCred.getCertificateChain()[0].getEncoded());
    }
    
}

