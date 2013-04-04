/**
 * CapabilityQuery.java
 *
 * @author Liang Fang lifang@cs.indiana.edu
 */

package xsul.dsig.saml.authorization;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import org.opensaml.SAMLAction;
import org.opensaml.SAMLAuthorizationDecisionQuery;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLQuery;
import org.opensaml.SAMLSubject;

public class CapabilityQuery
{
    private SAMLQuery query;
    private String subject;
    private String resource;
    
    /**
     * Constructor
     *
     * @param    query               a  SAMLQuery
     */
    public CapabilityQuery(SAMLQuery query)
    {
        this.query = query;
    }
    
    public CapabilityQuery(String subject, String resource, String nspace, String[] actions)
        throws Exception
    {
        String[] confirmationMethods = {SAMLSubject.CONF_BEARER};
        SAMLSubject sub =
            new SAMLSubject(new SAMLNameIdentifier(
                                subject, CapConstants.CAP_NAMEQUALIFIER,
                                CapConstants.CAP_NAMEIDENTIFIER_FORMAT),
                            Arrays.asList(confirmationMethods), null, null);
        Vector samlactions = new Vector(actions.length);
        for(int i = 0;i < actions.length;i++)
            samlactions.add(new SAMLAction(nspace, actions[i]));
        
        query = new SAMLAuthorizationDecisionQuery(sub, resource, samlactions, null);
        this.subject = subject;
        this.resource = resource;
    }
    /**
     * Sets Query
     *
     * @param    Query               a  SAMLQuery
     */
    public void setQuery(SAMLQuery query)
    {
        this.query = query;
    }
    
    /**
     * Returns Query
     *
     * @return    a  SAMLQuery
     */
    public SAMLQuery getQuery()
    {
        return query;
    }
    
    public String getSubject()
    {
        if(query instanceof SAMLAuthorizationDecisionQuery)
        {
            return subject;
        }
        
        return null;
    }
    
    public String getResource()
    {
        if(query instanceof SAMLAuthorizationDecisionQuery)
        {
            return resource;
        }
        
        return null;
    }
    
    public Vector getActions()
    {
        if(query instanceof SAMLAuthorizationDecisionQuery)
        {
            SAMLAuthorizationDecisionQuery adq = (SAMLAuthorizationDecisionQuery)query;
            Iterator iter = adq.getActions();
            Vector av = new Vector(1);
            while(iter.hasNext())
            {
                SAMLAction act = (SAMLAction)iter.next();
                av.add(act.getData());
            }
            
            return av;
        }
        
        return null;
    }
}

