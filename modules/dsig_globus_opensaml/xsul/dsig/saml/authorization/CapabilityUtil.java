/**
 * Util.java
 *
 * @author    Liang Fang lifang@cs.indiana.edu
 * @created   December 17, 2003
 * $Id: CapabilityUtil.java,v 1.16 2006/02/03 01:58:58 lifang Exp $
 */

package xsul.dsig.saml.authorization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.XMLUtils;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.opensaml.SAMLAction;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAudienceRestrictionCondition;
import org.opensaml.SAMLAuthorizationDecisionStatement;
import org.opensaml.SAMLException;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLStatement;
import org.opensaml.SAMLSubject;
import org.w3c.dom.Node;
import xsul.MLogger;


public class CapabilityUtil {
    private static final MLogger logger = MLogger.getLogger();
    
    public static SAMLStatement[] getAllStatements(SAMLAssertion sa) {
        Vector ss = new Vector();
        Iterator isa = sa.getStatements();
        while(isa.hasNext()) {
            ss.add(isa.next());
        }
        
        return (SAMLStatement[])ss.toArray(new SAMLStatement[0]);
    }
    
    public static SAMLAuthorizationDecisionStatement[]
        getAuthorizationDecisionStatements(SAMLAssertion sa) {
        Vector ss = new Vector();
        Iterator isa = sa.getStatements();
        while(isa.hasNext()) {
            Object obj = isa.next();
            if(obj instanceof SAMLAuthorizationDecisionStatement)
                ss.add(obj);
        }
        
        return (SAMLAuthorizationDecisionStatement[])ss.toArray(
            new SAMLAuthorizationDecisionStatement[0]);
    }
    
    public static boolean actionMatch(Iterator iter, Vector v) {
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            Object o1 = e.nextElement();
            if(o1 instanceof String)
                logger.finest("o1 string: " + (String)o1);
            else
                continue;
            
            String name1 = (String)o1;
            while(iter.hasNext()) {
                Object o2 = iter.next();
                if(o2 instanceof SAMLAction) {
                    String name2 = ((SAMLAction)o2).getData();
                    logger.finest("o2 string: " + (String)o2);
                    if(name1.equalsIgnoreCase(name2))
                        return true;
                }
                else {
                    logger.finest("o2 class type: "+o2.getClass());
                }
            }
        }
        
        return false;
    }
    
    public static void printDOMNode(Node elem) {
        try {
            ByteArrayOutputStream serializedelem = new ByteArrayOutputStream();
            XMLUtils.outputDOM(elem, serializedelem);
            logger.finest(serializedelem.toString());
            serializedelem.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void capability2File(Capability cap, String fileloc) {
        try {
            PrintWriter out1 =
                new PrintWriter(new BufferedWriter(new FileWriter(fileloc)));
            
            out1.println(cap.toString());
            out1.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static boolean exist(Object obj, Collection c) {
        if(obj == null || c == null)
            return false;
        
        for(Iterator iter = c.iterator(); iter.hasNext();) {
            Object vobj = iter.next();
            if(obj.equals(vobj))
                return true;
        }
        
        return false;
    }
    
    public static boolean exist(String handle, Capability[] caps) {
        if(handle == null || caps == null)
            return false;
        
        for(int i = 0; i < caps.length;i++) {
            String epr = caps[i].getResource();
            if(handle.equals(epr))
                return true;
        }
        
        return false;
    }
    
    public static boolean exist(Object obj, Object[] objs) {
        if(objs == null || obj == null)
            return false;
        
        for(int i = 0;i < objs.length;i++) {
            if(objs[i].equals(obj))
                return true;
        }
        
        return false;
    }
    
    public static Capability locate(String handle, Vector caps) {
        if(handle == null || caps == null)
            return null;
        
        for(Enumeration e = caps.elements(); e.hasMoreElements();) {
            Capability cap = (Capability)e.nextElement();
            String epr = cap.getResource();
            if(handle.equals(epr))
                return cap;
        }
        
        return null;
    }
    
    public static Collection minus(Collection a, Collection b) {
        if(b == null || a == null)
            return a;
        
        HashSet hs = new HashSet(a);
        for(Iterator i1 = a.iterator();i1.hasNext();) {
            Object o1 = i1.next();
            Object[] objs = b.toArray();
            if(exist(o1, objs))
                hs.remove(o1);
        }
        
        return hs;
    }
    
    public static boolean compareSubjects(String sub1, String sub2) {
        if(sub1 == null || sub2 == null)
            return false;
        
        String mysub1 = canonicalizeSubject(sub1);
        String mysub2 = canonicalizeSubject(sub1);
        
        if(mysub1.equalsIgnoreCase(mysub2))
            return true;
        
        return false;
    }
    
    public static String canonicalizeSubject(String subject) {
        if (subject == null)
            return null;
        
        String mysubject = new String(subject.trim());
        
        if (mysubject.charAt(0) == '/') {
            // the form "/O=US/CN=Liang Fang"
            mysubject = mysubject.substring(1);
            mysubject = mysubject.replace('/', ',');
        }
        
        // regular expression ", " --> ","
        mysubject = mysubject.replaceAll(",\\s+", ",");
	mysubject = mysubject.replaceAll(",CN=proxy", "");
	mysubject = mysubject.replaceAll(",CN=[0-9]+","");
        
        return mysubject;
    }
    
    static public SAMLAssertion makeAssertion(String _nspace,
                                              String _actionname,
                                              String _decision,
                                              String _ePR,
                                              String _ownername,
                                              String _username,
                                              Date _notbefore,
                                              Date _notafter,
                                              GlobusCredential _cred)
        throws CloneNotSupportedException, SAMLException {
        // make AudienceRestrictionCondition
        SAMLAudienceRestrictionCondition sar =
            new SAMLAudienceRestrictionCondition(Collections.singleton(_ePR));
        Vector conditions = new Vector(1);
        conditions.add(sar.clone());
        
        // make AuthorizationDecisionStatement
        String[] confirmationMethods = {SAMLSubject.CONF_BEARER};
        SAMLSubject subject =
            new SAMLSubject(new SAMLNameIdentifier(
                                _username, CapConstants.CAP_NAMEQUALIFIER,
                                CapConstants.CAP_NAMEIDENTIFIER_FORMAT),
                            Arrays.asList(confirmationMethods), null, null);
        SAMLAuthorizationDecisionStatement sad =
            new SAMLAuthorizationDecisionStatement((SAMLSubject)subject.clone(),
                                                   _ePR,
                                                   _decision,
                                                   Collections.singleton(new SAMLAction(_nspace,_actionname)),
                                                   null);
        // make SAML assertion
        SAMLAssertion assertion =
            new SAMLAssertion(_ownername,
                              _notbefore,
                              _notafter,
                              conditions,
                              null,
                              Collections.singleton(sad));
        assertion.sign(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
                       _cred.getPrivateKey(),
                       Arrays.asList(_cred.getCertificateChain())
                      );
        assertion.verify();
        return assertion;
    }
    
    public static TrustedCertificates getTrustedCertificates() {
        return getTrustedCertificates(null);
    }
    
    public static TrustedCertificates getTrustedCertificates(String cacloc)
        throws CapabilityException {
        TrustedCertificates tc = TrustedCertificates.load(cacloc);
        if(tc == null) {
            tc = TrustedCertificates.getDefaultTrustedCertificates();
            if(tc == null) {
                throw new CapabilityException(" can not be started as trusted certificates were not found");
            }
        }
        return tc;
    }
    
    public static GlobusCredential getGlobusCredential() throws Exception {
        return getGlobusCredential(null, null);
    }
    
    public static GlobusCredential getGlobusCredential(String keyloc,
                                                       String certloc)
        throws Exception {
        GlobusCredential mycred;
        
        if(keyloc != null && certloc != null) {
            logger.finest("using certificate "+certloc+" and key "+keyloc);
            mycred = new GlobusCredential(certloc, keyloc);
        }
        else {
            logger.finest("using default CoG credential");
            mycred = GlobusCredential.getDefaultCredential();
        }
        return mycred;
    }
    
    public static String textfile2String(String fileloc) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(fileloc));
        StringBuffer sbuf = new StringBuffer();
        int n;
        while((n=r.read())!=-1) {
            sbuf.append((char)n);
        }
        return sbuf.toString();
    }
    
    /**
     * Method parse. Parses the list of arguments passed to main and returns values
     * from argument name-value pairs, or their default values if argument not found.
     *
     * @param    argList             Array of arguments passed to main method
     * @param    argName             Array of valid argument names
     * @param    defaults            Array of default values for argument names (required argName.length == defaults.length)
     * @param    usage               A usage sting to be printed with exception
     *
     * @return   an array of argument values that were found in the argument list, or from defaults
     *
     * @exception   IllegalArgumentException if error occurs while parsing arg list
     * @author Yogesh L. Simmhan [mailto:ysimmhan@cs.indiana.edu]
     */
    public static String[] parse(String argList[],
                                 String[] argNames,
                                 String[] defaultValues,
                                 String usage)
        throws IllegalArgumentException {
        
        String[] result = (String[])defaultValues.clone();
        boolean[] foundArg = new boolean[defaultValues.length];
        
        int argIndex = 0;
        while(argIndex < argList.length) {
            
            boolean found = false;
            for(int argId=0; argId < argNames.length; argId++) {
                
                if(argNames[argId].equalsIgnoreCase(argList[argIndex])) {
                    
                    // check duplicate
                    if(foundArg[argId]) {
                        throw new IllegalArgumentException("Duplicate argument found for : " + argNames[argId] +
                                                               "\nAlready passed as value for this arg: " +
                                                               result[argId] +
                                                               "\nUsage:\n" + usage);
                    }
                    
                    // get and check value
                    argIndex++;
                    if(argIndex >= argList.length) {
                        throw new IllegalArgumentException("Could not find value for param: " + argNames[argId] +
                                                               " (not using default=" + defaultValues[argId] + ")" +
                                                               "\nUsage:\n" + usage);
                    }
                    
                    // set value in result
                    result[argId] = argList[argIndex];
                    argIndex++;
                    found = true;
                    break;
                }
                
            }
            
            // check arg name valid
            if(!found) {
                throw new IllegalArgumentException("Not a valid argument: " + argList[argIndex] +
                                                       "\nUsage:\n" + usage);
            }
            
        }
        
        return result;
    }
    
    public static String[] parse(String argList[],
                                 String[] argNames,
                                 String[] defaultValues,
                                 boolean[] requiredArgs,
                                 String usage)
        throws IllegalArgumentException {
        
        if(requiredArgs != null) {
            for(int i = 0;i < requiredArgs.length;i++) {
                if(requiredArgs[i]) {
                    if(!exist(argNames[i], argList))
                        throw new IllegalArgumentException("The value of " + argNames[i] + "is required." +
                                                               "\nUsage:\n" + usage);
                }
            }
        }
        
        return parse(argList, argNames, defaultValues, usage);
    }
    
    public static Vector getPermitedOperations(Capability cap) {
        
        Map actionmap = cap.getActionswithdecisions();
        
        if(actionmap == null) {
            logger.finest("no actions");
            return null;
        }
        
        Vector opv = new Vector(actionmap.size());
        Set keys = actionmap.keySet();
        for(Iterator iter = keys.iterator(); iter.hasNext();) {
            String url = (String)iter.next();
            if(actionmap.get(url).equals(CapConstants.PERMIT)) {
                opv.add(url);
            }
        }
        
        return opv;
    }
}


