/**
 * WorkloadHandler.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: WorkloadHandler.java,v 1.1 2005/04/01 19:20:17 lifang Exp $
 */

package xsul.xhandler;

import org.globus.gsi.GlobusCredential;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.dsig.globus.GlobusCredSOAPEnvelopeSigner;
import xsul.message_router.MessageContext;
import xsul.xhandler.BaseHandler;
import xsul.XsulException;
import org.globus.gsi.GlobusCredentialException;

public class WorkloadHandler extends BaseHandler {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public final static String SIGN_ENVELOPE = "signEnvelope";
    
    private String job = SIGN_ENVELOPE;
    private int incomingWorkload = 1;
    private int outgoingWorkload = 1;
    
    public WorkloadHandler(String name) {
        super(name);
    }
    
    public WorkloadHandler(String name, String job,
                           int incomingWorkload, int ougoingWorkload) {
        super(name);
        this.job = job;
        this.incomingWorkload = incomingWorkload;
        this.outgoingWorkload = ougoingWorkload;
    }
    
    public void setJob(String job) {
        this.job = job;
    }
    
    public String getJob() {
        return job;
    }
    
    public void setIncomingWorkload(int incomingWorkload) {
        this.incomingWorkload = incomingWorkload;
    }
    
    public int getIncomingWorkload() {
        return incomingWorkload;
    }
    
    public void setOutgoingWorkload(int ougoingWorkload) {
        this.outgoingWorkload = ougoingWorkload;
    }
    
    public int getOutgoingWorkload() {
        return outgoingWorkload;
    }
    
    public boolean processOutgoingXml(XmlElement soapEnvelope,
                                      MessageContext context) {
        // do something purely wasting of time
        for (int i = 0; i < outgoingWorkload; i++) {
            if(job.equals(SIGN_ENVELOPE)) {
                signEnvelope(soapEnvelope);
            }
        }
        
        return false;
    }
    
    public boolean processIncomingXml(XmlElement soapEnvelope,
                                      MessageContext context) {
        // do something purely wasting of time
        for (int i = 0; i < incomingWorkload; i++) {
            if(job.equals(SIGN_ENVELOPE)) {
                signEnvelope(soapEnvelope);
            }
        }
        
        return false;
    }
    
    private void signEnvelope(XmlElement soapEnvelope) {
        try {
            GlobusCredential credential = GlobusCredential.getDefaultCredential();
            XmlDocument doc = (XmlDocument) soapEnvelope.getParent();
            if(doc == null)
                logger.finest("doc is null!!!!!");
//          XmlDocument signedDoc =
                GlobusCredSOAPEnvelopeSigner.getInstance(credential)
                .signSoapMessage(doc);
        } catch (Exception e) {
            logger.warning("something wrong", e);
        }
    }
}

