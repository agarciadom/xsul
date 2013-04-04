/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/* Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 * This software is open source. See the bottom of this file for the licence.
 * $Id: XsulTestCase.java,v 1.3 2006/04/30 06:48:15 aslom Exp $ */

package xsul;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.util.XsulUtil;

/**
 * Some common utilities to help XSUL unit tests.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XsulTestCase extends TestCase {
    
    public XsulTestCase(String name) {
        super(name);
    }
    
    
    public static void assertXmlElements(XmlElement left, XmlElement right) {
        try {
            compareXml(left, right);
        } catch(AssertionError err) {
            String lx = XsulUtil.safeXmlToString(left);
            String rx = XsulUtil.safeXmlToString(right);
            throw new ComparisonFailure("XML elements differ: "+err.getMessage(), lx, rx);
        }
        // nice error message
    }
    
    public static void compareXml(XmlElement left, XmlElement right) {
        //AssertionFailedError(message);
        compareXmlWithoutChildren(left, right);
        Iterator li = left.children();
        Iterator ri = right.children();
        compareElementIterators("children", li, XsulUtil.safeXmlToString(left), ri, XsulUtil.safeXmlToString(right));
    }
    
    protected static void compareXmlWithoutChildren(XmlElement left, XmlElement right) {
        compareElementNames(left, right);
        compareAttributes(left, right);
        compareNamespaceDeclarations(left, right);
    }
    
    protected static void compareAttributes(XmlElement left, XmlElement right) {
        Iterator li = left.attributes();
        Iterator ri = right.attributes();
        compareWithoutOrder("attributes", li, e2s(left), ri, e2s(right));
    }
    
    protected static void compareNamespaceDeclarations(XmlElement left, XmlElement right) {
        Iterator li = left.namespaces();
        Iterator ri = right.namespaces();
        compareWithoutOrder("namespace declarations", li, e2s(left), ri, e2s(right));
        
    }
    
    protected static void compareElementNames(XmlElement left, XmlElement right) {
        {
            String ln = left.getName();
            String rn = right.getName();
            if(!ln.equals(rn)) {
                throw new ComparisonFailure("element name differs (left:'"+ln+"' right:'"+rn+"')", ln, rn);
            }
        }
        {
            XmlNamespace lns = left.getNamespace();
            XmlNamespace rns = right.getNamespace();
            if(!lns.equals(rns)) {
                throw new ComparisonFailure(
                    "element namespace differs (left:'"+lns+"' right:'"+rns+"')", lns.toString(), rns.toString());
            }
        }
    }
    
    private static List iteratorToList(Iterator li) {
        ArrayList list = new ArrayList();
        while(li.hasNext()) {
            list.add(li.next());
        }
        return list;
    }
    
    private static void compareWithoutOrder(String typeNameS, Iterator li, String leftContext, Iterator ri, String rightContext) {
        List ll = iteratorToList(li);
        List rl = iteratorToList(ri);
        if(ll.size() != rl.size()) {
            throw new ComparisonFailure(
                "different number of "+typeNameS+" in elements (left:"+ll.size()+" right:"+rl.size()+")", leftContext, rightContext);
        }
        for (int i = 0; i < ll.size(); i++) { //OK assuming lists have no duplicates of elements (for equals)
            Object o = ll.get(i);
            if(!rl.contains(o)) {
                throw new ComparisonFailure("one of "+typeNameS+" ('"+o+"') is missing in right element", leftContext, rightContext);
            }
        }
    }
    
    private static void compareElementIterators(String typeNameS, Iterator li, String leftContext, Iterator ri, String rightContext) {
        while(li.hasNext()) {
            if(!ri.hasNext()) {
                throw new ComparisonFailure("right element has not enough "+typeNameS, leftContext, rightContext);
            }
            Object lo =  li.next();
            Object ro =  ri.next();
            if(lo == null || ro == null) {
                assertSame(lo, ro);
            } else if(lo instanceof XmlElement || ro instanceof XmlElement) {
                compareXml((XmlElement)lo, (XmlElement)ro);
            } else if(!lo.equals(ro)) {
                throw new ComparisonFailure(
                    "elements have different "+typeNameS+" (left='"+lo+"' right='"+ro+"')", leftContext, rightContext);
            }
        }
        if(ri.hasNext()) {
            throw new ComparisonFailure("right element has too many "+typeNameS, leftContext, rightContext);
        }
    }
    
    
    public static String e2s(XmlElement el) {
        XmlSerializer ser = null;
        StringWriter writer = new StringWriter();
        try {
            ser = XmlConstants.BUILDER.getFactory().newSerializer();
            ser.setOutput(writer);
        } catch (Exception e) {
            throw new XmlBuilderException("could not serialize node to writer", e);
        }
        XmlConstants.BUILDER.serializeStartTag(el, ser);
        try {
            ser.flush();
        } catch (IOException e) {
            throw new XmlBuilderException("could not flush output", e);
        }
        String s = writer.toString();
        return s;
    }
    
    
    protected String printable(char ch) {
        if(ch == '\n') {
            return "\\n";
        } else if(ch == '\r') {
            return "\\r";
        } else if(ch == '\t') {
            return "\\t";
        } if(ch > 127 || ch < 32) {
            StringBuffer buf = new StringBuffer("\\u");
            String hex = Integer.toHexString((int)ch);
            for (int i = 0; i < 4-hex.length(); i++)
            {
                buf.append('0');
            }
            buf.append(hex);
            return buf.toString();
        }
        return ""+ch;
    }
    
    protected String printable(String s) {
        if(s == null) return null;
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < s.length(); ++i) {
            buf.append(printable(s.charAt(i)));
        }
        s = buf.toString();
        return s;
    }
    
}


