/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulUtil.java,v 1.1 2006/04/18 18:09:34 aslom Exp $
 */

package xsul.util;


/**
 * Utility methods shared by all clases in package.
 *
 * @version $Revision: 1.1 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlComment;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.XmlSerializable;
import xsul.DataValidationException;
import xsul.MLogger;
import xsul.XmlConstants;

public class XsulUtil {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public static XmlDocument getDocumentOutOfElement(XmlElement outgoingMessage) {
        XmlDocument outgoingDoc = null;
        XmlContainer container = outgoingMessage.getRoot();
        if(container != null && container == outgoingMessage) {
            XmlContainer parent = outgoingMessage.getParent();
            if(parent instanceof XmlElement) {
                container = ((XmlElement)parent).getRoot();
            } else {
                container = parent;
            }
        }
        if(container instanceof XmlDocument) {
            outgoingDoc = (XmlDocument) container;
        } else {
            throw new XmlBuilderException("element expected to have XML document owner");
        }
        return outgoingDoc;
    }
    
    /**
     * Will serialize XML and call toString() on unrecongized objects
     */
    public static String safeXmlToString(List xmlContainers) { //JDK15 List<>
        StringBuffer sb = new StringBuffer();
        for (Iterator i = xmlContainers.iterator(); i.hasNext(); ) {
            Object child = i.next();
            //          if(child instanceof XmlContainer) {
            //            XmlContainer xc = (XmlContainer) child;
            //            String xml = safeXmlToString(xc);
            //          }
            String xml = safeSerializeXmlItem(child);
            sb.append(xml);
        }
        return sb.toString();
    }
    
    public static String safeXmlToString(XmlContainer xc) {
        return safeSerializeXmlItem(xc);
    }
    
    public static String safeXmlToString(XmlElement el) {
        return safeSerializeXmlItem(el);
    }
    
    public static String safeSerializeXmlItem(Object item) {
        StringWriter sw = new StringWriter();
        XmlSerializer ser = preapareSerializer(sw);
        try {
            safeSerializeXmlItem(item, ser);
        } catch (IOException e) {
            throw new XmlBuilderException("could not serialize output", e);
        }
        try {
            ser.flush();
        } catch (IOException e) {
            throw new XmlBuilderException("could not flush output", e);
        }
        return sw.toString();
    }
    
    private static XmlSerializer preapareSerializer(StringWriter sw)
        throws XmlBuilderException {
        XmlSerializer ser;
        try {
            ser = builder.getFactory().newSerializer();
            ser.setOutput(sw);
        } catch (Exception e) {
            throw new XmlBuilderException("could not serialize node to writer", e);
        }
        return ser;
    }
    
    public static void safeSerializeXmlElement(XmlElement el, XmlSerializer ser) throws IOException {
        builder.serializeStartTag(el, ser);
        //now do children recurson - bit only if they are recognized safe to serialize
        if(el.hasChildren()) {
            Iterator iter = el.children();
            while (iter.hasNext()) {
                Object child = iter.next();
                safeSerializeXmlItem(child, ser);
            }
        }
        builder.serializeEndTag(el, ser);
    }
    
    /**
     * It is safe serialization in sense that non XML serializable content is represnted as toString()
     * and no exception is thrown (so one can even print Thread to XML).
     * <br />NOTE: use this and related methods for debuggin purposes only!!!
     * <br />NOTE: this method is not suitable to generate correct XML
     * (output from toString() may be hard or impossible to deserialize !!!!)
     */
    public static void safeSerializeXmlItem(Object child, XmlSerializer ser)
        throws IllegalArgumentException, IllegalStateException, IOException, XmlBuilderException {
        if(child instanceof XmlSerializable) {
            //((XmlSerializable)child).serialize(ser);
            try {
                ((XmlSerializable)child).serialize(ser);
            } catch (IOException e) {
                throw new XmlBuilderException("could not serialize item "+child+": "+e, e);
            }
            
        } else if(child instanceof XmlElement) {
            safeSerializeXmlElement((XmlElement)child, ser);
        } else if(child instanceof XmlDocument) {
            // ignore prolog
            XmlElement el = ((XmlDocument) child).getDocumentElement();
            safeSerializeXmlElement(el, ser);
        } else if(child instanceof String) {
            ser.text(child.toString());
        } else if(child instanceof XmlComment) {
            ser.comment(((XmlComment)child).getContent());
        } else {
            //throw new IllegalArgumentException("could not serialize "+child.getClass());
            ser.text(child != null ? child.toString() : "null"); //TODO revisit
        }
    }
    
    public static String validateNcName(String name) {
        int pos2 = name.indexOf(':');
        if(pos2 != -1) {
            throw new DataValidationException(
                "expected non colon name but got '"+name+"'");
        }
        return name;
    }
    
    public static QName getQNameContent(XmlElement el) throws DataValidationException {
        if(el == null) throw new IllegalArgumentException();
        String t = el.requiredTextContent();
        return toQName(el, t);
    }
    
    public static QName toQName(XmlElement context, String qnameValue) throws DataValidationException {
        
        int pos = qnameValue.indexOf(':');
        String prefix;
        String localName;
        XmlNamespace n;
        if(pos != -1) {
            prefix = qnameValue.substring(0, pos);
            localName = qnameValue.substring(pos+1);
            int pos2 = localName.indexOf(':');
            if(pos2 != -1) {
                throw new DataValidationException(
                    "expected element "+context
                        +" to have qname 'prefix:local' with exactly one colon and not '"
                        +qnameValue+"'");
            }
        } else {
            prefix = "";
            localName = qnameValue;
        }
        n = context.lookupNamespaceByPrefix(prefix);
        if(n != null) {
            return new QName(n.getNamespaceName(), localName, prefix);
        } else {
            throw new DataValidationException(
                "could not find namespace for prefix '"+prefix+"' in "+qnameValue+" (context:"+context+")");
        }
    }
    
    public static String getContentTypeCharset(String contentType) {
        return getContentTypeCharset(contentType, null);
    }
    
    public static String getContentTypeCharset(String contentType,
                                               String defaultCharset) {
        String charset = null;
        if(contentType != null) {
            int ndx = contentType.indexOf("charset=");
            logger.finest("ndx="+ndx
                              +" from contentType="+contentType);
            if(ndx != -1) {
                ndx += "charset=".length();
                char c = contentType.charAt(ndx);
                if(c == '\'' || c == '\"') {
                    int ndx2 = contentType.indexOf(c, ndx+1);
                    if(ndx2 == -1) {
                        //throw new RemoteException(
                        //  "content type header '"+contentType+"'"
                        //    +" has malformed charset");
                        logger.warning("could not get charset"
                                           +" from contentType="+contentType);
                        return null;
                    }
                    charset = contentType.substring(ndx+1, ndx2);
                } else {
                    charset = contentType.substring(ndx);
                }
                return charset;
            }
        }
        return defaultCharset;
    }
    
    
    public static final byte[] readInputStreamToByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8*1024];
        int ret = 0;
        while(  (ret = in.read(buf)) > 0) {
            baos.write(buf, 0, ret);
        }
        buf = baos.toByteArray();
        return buf;
    }
    
    
    public static final void removeIgnorableSpace(XmlElement e) {
        //we remove all the empty strings if the element has xmlelement children
        boolean ignoreWS = false;
        ArrayList toDelete = new ArrayList();
        
        Iterator children = e.children();
        while (children.hasNext()) {
            Object child = children.next();
            if (child instanceof XmlElement) {
                ignoreWS = true;
            } else if (child instanceof String) {
                if (isWhiteSpace((String) child)) {
                    toDelete.add(child);
                }
            }
        }
        
        
        if (ignoreWS) {
            for (int i = 0; i < toDelete.size(); i++) {
                e.removeChild(toDelete.get(i));
            }
        }
        
        children = e.children();
        while (children.hasNext()) {
            Object child = children.next();
            if (child instanceof XmlElement) {
                removeIgnorableSpace((XmlElement) child);
            }
        }
    }
    
    
    
    public static final boolean isWhiteSpace(String txt) {
        for (int i = 0; i < txt.length(); i++) {
            if ( (txt.charAt(i) != ' ') &&
                    (txt.charAt(i) != '\n') &&
                    (txt.charAt(i) != '\t') &&
                    (txt.charAt(i) != '\r')) {
                return false;
            }
        }
        
        return true;
    }
    
    public static String escapeForHtml(String text) {
        if(text == null) {
            return "";
        }
        StringBuffer escapedText = new StringBuffer(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if(ch == '<') {
                escapedText.append("&lt;");
            } else if(ch == '>') {
                escapedText.append("&gt;");
            } else if(ch == '&') {
                escapedText.append("&amp;");
                //} else if(ch == '"') {
                //    escapedText.append("&quot;");
                //} else if(ch == '\'') {
                //    escapedText.append("&apos;");
            } else {
                escapedText.append(ch);
            }
        }
        return escapedText.toString();
    }
    
    public static final String escapeXml(String s) {
        return escapeXml(s, true, true);
    }
    
    public static final String escapeXml(String s, boolean escapeNewLine, boolean escapeTabs) {
        StringBuffer buf = new StringBuffer(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if(ch == '&') {
                buf.append("&amp;");
            } else if(ch == '<') {
                buf.append("&lt;");
            } else if(ch == '>') {
                buf.append("&gt;");
            } else if(ch == '\r') {
                buf.append("&#13;");
            } else if(ch == '\t' && escapeTabs) {
                buf.append("&#9;");
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }
    
    
    public static final String printable(String s) {
        return printable(s, true);
    }
    
    public static final String printable(String s, boolean escapeNewLine) {
        return printable(s, escapeNewLine, true);
    }
    
    public static final String printable(String s, boolean escapeNewLine, boolean escapeTabs) {
        if(s == null) return "null";
        StringBuffer retval = new StringBuffer(s.length() + 16);
        //retval.append("'");
        char ch;
        for (int i = 0; i < s.length(); i++) {
            addPrintable(retval, s.charAt(i), escapeNewLine, escapeTabs);
        }
        //retval.append("'");
        return retval.toString();
    }
    
    public static final String printable(char ch) {
        StringBuffer retval = new StringBuffer();
        addPrintable(retval, ch, true, true);
        return retval.toString();
    }
    
    private static void addPrintable(StringBuffer retval, char ch, boolean escapeNewLine, boolean escapeTabs) {
        switch (ch) {
            case '\b':
                retval.append("\\b");
                break;
            case '\t':
                //retval.append("\\t");
                if(escapeTabs) {
                    retval.append("\\t");
                } else {
                    retval.append(ch);
                }
                break;
            case '\n':
                if(escapeNewLine) {
                    retval.append("\\n");
                } else {
                    retval.append(ch);
                }
                break;
            case '\f':
                retval.append("\\f");
                break;
            case '\r':
                retval.append("\\r");
                break;
            case '\"':
                retval.append("\\\"");
                break;
                //case '\'':
                //    retval.append("\\\'");
                //    break;
            case '\\':
                retval.append("\\\\");
                break;
            default:
                if (ch  < 0x20 || ch > 0x7e) {
                    String ss = "0000" + Integer.toString(ch, 16);
                    retval.append("\\u" + ss.substring(ss.length() - 4, ss.length()));
                } else {
                    retval.append(ch);
                }
        }
    }
    
    /**
     * Method copyInput2Output copies an InputStream to an OutputStream.
     * Notice that jakarta commons IO has IOUtils.copy with the same function.
     * However we save one more jar file here ;)
     *
     * @param    in                  an InputStream
     * @param    out                 an OutputStream
     *
     * @exception   IOException
     *
     */
    public static void copyInput2Output(InputStream in, OutputStream out)
        throws IOException {
        logger.entering(new Object[] { in, out });
        byte[] buffer = new byte[1024];
        int lastRead;
        do {
            lastRead = in.read(buffer);
            logger.finest("got " + lastRead + " bytes");
            if (lastRead != -1) {
                out.write(buffer, 0, lastRead);
            } else {
                break;
            }
        } while (lastRead != -1);
        in.close();
        out.close();
        logger.exiting();
    }
    
    public static String getHostfromURL(String url) {
        int i = url.indexOf("//");
        if(i == -1)
            return null;
        i += 2;
        int j = url.indexOf(':', i);
        if(j == -1) {
            j = url.indexOf('/', i);
        }
        return url.substring(i, j);
    }
    
    public static int getPortnumfromURL(String url) {
        int j = url.lastIndexOf(':');
        if(j == -1) {
            if(url.startsWith("http"))
                return 80;
            else if(url.startsWith("ftp"))
                return 21;
            else if(url.startsWith("gsiftp"))
                return 2811;
            else if(url.startsWith("gridftp"))
                return 2811;
            else
                return -1;
        }
        
        int k = url.indexOf('/', j);
        if(k == -1)
            k = url.length();
        
        return Integer.parseInt(url.substring(j+1, k));
    }
    
    public static String getSvcnamefromURL(String url) {
        int i = url.indexOf("//");
        if(i == -1)
            return null;
        i += 2;
        int j = url.indexOf('/', i);
        if(j == -1) {
            return null;
        }
        int k = url.indexOf('/', j+1);
        if(k == -1) {
            k = url.length();
        }
        return url.substring(j+1, k);
    }
}

