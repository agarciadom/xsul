/**
 * CapabilityRequest.java
 *
 * @author Liang Fang lifang@cs.indiana.edu
 */

package xsul.dsig.saml.authorization;

import java.io.ByteArrayInputStream;
import org.opensaml.SAMLException;
import org.opensaml.SAMLRequest;
import xsul.MLogger;

public class CapabilityRequest {
    private static final MLogger logger = MLogger.getLogger();
    
    private SAMLRequest request;
    private String issuer;
    private String resource;
    private String id;
    private CapabilityQuery query;
    private String status;
    
    public CapabilityRequest(String str) {
        if(str.startsWith("<CapabilityRequest>")) {
            this.id = str.substring(str.indexOf("<Id>")+"<Id>".length(),
                                    str.indexOf("</Id>"));
            logger.finest("id: " + id);
            this.issuer =
                str.substring(str.indexOf("<Issuer>")+"<Issuer>".length(),
                              str.indexOf("</Issuer>"));
            logger.finest("issuer: " + issuer);
            this.resource =
                str.substring(str.indexOf("<Resource>")+"<Resource>".length(),
                              str.indexOf("</Resource>"));
            this.status =
                str.substring(str.indexOf("<Status>")+"<Status>".length(),
                              str.indexOf("</Status>"));
            String substr =
                str.substring(str.indexOf("<Action>"),
                              str.lastIndexOf("</Action>")+"</Action>".length());
            String[] actions = substr.split("</Action>");
            for (int i = 0; i < actions.length; i++) {
                actions[i] = actions[i].substring("<Action>".length());
            }
            try {
                query = new CapabilityQuery(issuer, resource, resource, actions);
            } catch (Exception e) {}
        }
        else {
            logger.severe("the string does not begin with <CapabilityRequest>.");
        }
    }
    
    public CapabilityRequest(String reqstr, String from, String ePRinReq)
        throws CapabilityException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(reqstr.getBytes());
            this.request = new SAMLRequest(bais);
        }
        catch (SAMLException e) {
            e.printStackTrace();
            throw new CapabilityException(e.getMessage());
        }
        this.issuer = from;
        this.resource = ePRinReq;
        this.status = CapConstants.PENDING;
    }
    
    public CapabilityRequest(String reqstr, String from, String ePRinReq, String status)
        throws CapabilityException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(reqstr.getBytes());
            this.request = new SAMLRequest(bais);
        }
        catch (SAMLException e) {
            e.printStackTrace();
            throw new CapabilityException(e.getMessage());
        }
        this.issuer = from;
        this.resource = ePRinReq;
        this.status = status;
    }
    
    public CapabilityRequest(CapabilityQuery capquery)
        throws Exception {
        try {
            request = new SAMLRequest(capquery.getQuery());
            
            issuer = capquery.getSubject();
            resource = capquery.getResource();
            query = capquery;
        }
        catch (SAMLException e) {throw e;}
        this.status = CapConstants.PENDING;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatus() {
        return status;
    }
    
    public SAMLRequest getRequest() {
        return request;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public String getId() {
        return id;
    }
    
    public CapabilityQuery getQuery() {
        return query;
    }
    
    public String getResourceInRequest() {
        return resource;
    }
    
    public String[] getActions() {
        return (String[])query.getActions().toArray(new String[0]);
    }
    
    public String toString() {
        StringBuffer strbuf = new StringBuffer("");
        strbuf.append("<CapabilityRequest>\n");
        strbuf.append("<Id>"+getId()+"</Id>\n");
        strbuf.append("<Issuer>"+getIssuer()+"</Issuer>\n");
        strbuf.append("<Resource>"+getResourceInRequest()+"</Resource>\n");
        String[] actions = getActions();
        for (int i = 0; i < actions.length; i++) {
            strbuf.append("<Action>"+actions[i]+"</Action>\n");
        }
        strbuf.append("<Status>"+getStatus()+"</Status>\n");
        strbuf.append("</CapabilityRequest>");
        return strbuf.toString();
    }
}

