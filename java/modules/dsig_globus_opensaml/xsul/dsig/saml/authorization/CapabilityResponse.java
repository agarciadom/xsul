/**
 * CapabilityResponse.java
 *
 * @author Liang Fang lifang@cs.indiana.edu
 */

package xsul.dsig.saml.authorization;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLResponse;
import xsul.MLogger;

public class CapabilityResponse {
    private static final MLogger logger = MLogger.getLogger();
    
    private SAMLResponse response;
    private Capability cap;
    private String status;
    // request id
    private String inResponse;
    private String recipient;
    
    public CapabilityResponse(String str) {
        if(str.startsWith("<CapabilityResponse>")) {
            inResponse =
                str.substring(str.indexOf("<InResponseTo>")
                                  + "<InResponseTo>".length(),
                              str.indexOf("</InResponseTo>"));
            logger.finest("id: " + inResponse);
            this.recipient =
                str.substring(str.indexOf("<Recipient>")
                                  + "<Recipient>".length(),
                              str.indexOf("</Recipient>"));
            logger.finest("recipient: " + recipient);
            this.status =
                str.substring(str.indexOf("<Status>") + "<Status>".length(),
                              str.indexOf("</Status>"));
            int idx = str.indexOf("<Capability>");
            if(idx > 0) {
                String capstr =
                    str.substring(idx + "<Capability>".length(),
                                  str.indexOf("</Capability>"));
                this.cap = new Capability(capstr);
            }
        }
        else {
            logger.severe("the string does not begin with <CapabilityResponse>.");
        }
    }
    
    public CapabilityResponse(SAMLResponse response) {
        this.response = response;
        
        Iterator iter = response.getAssertions();
        Vector sav = new Vector(1);
        while(iter.hasNext()) {
            SAMLAssertion sa = (SAMLAssertion)iter.next();
            sav.add(sa);
        }
        
        try {
            this.cap = new Capability(sav);
        }
        catch (CapabilityException e) {e.printStackTrace();}
        // fixme:
        status = CapConstants.SUCCESS;
    }
    
    public CapabilityResponse(String inRep, String recipient,
                              Capability _cap, String status) throws Exception {
        this.status = status;
        this.inResponse = inRep;
        this.recipient = recipient;
        if(status.equals(CapConstants.SUCCESS)) {
            this.cap = _cap;
            response =
                new SAMLResponse(inRep, recipient,
                                 Arrays.asList(_cap.getAllAssertions()), null);
        }
    }
    
    public Capability getCapability() {
        return cap;
    }
    
    public String getInResponseTo() {
        return inResponse;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String toString() {
        StringBuffer strbuf = new StringBuffer("");
        strbuf.append("<CapabilityResponse>\n");
        strbuf.append("<InResponseTo>"+getInResponseTo()+"</InResponseTo>\n");
        strbuf.append("<Capability>"+cap.toString()+"</Capability>\n");
        strbuf.append("<Recipient>"+getRecipient()+"</Recipient>\n");
        strbuf.append("<Status>"+getStatus()+"</Status>\n");
        strbuf.append("</CapabilityResponse>");
        return strbuf.toString();
    }
}

