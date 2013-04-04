/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //- -----100-columns-wide------>*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XBeansUtil.java,v 1.7 2006/06/19 02:51:17 aslom Exp $
 */

package xsul.xbeans_util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.XsulException;

/**
 * Maps document QName to a method in a java target. Invokes target when message with
 * document qname arrives.
 *
 * @version $Revision: 1.7 $
 * @author Yogesh L. Simmhan [mailto:ysimmhan@cs.indiana.edu]
 */
public class XBeansUtil {
    
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private final static MLogger logger = MLogger.getLogger();
    
    public static XmlElement xmlObjectToXmlElement(XmlObject responseXmlObj) throws XmlBuilderException {
        if(responseXmlObj == null) {
            throw new XmlBuilderException("response message was not initialized and is null");
        }
        String responseXml = responseXmlObj.xmlText();
        if(logger.isFinestEnabled()) logger.finest("responseXml.xmlText()='"+responseXml+"'");
        try {
            XmlElement outgoingMsg  = builder.parseFragmentFromReader(new StringReader(responseXml));
            return outgoingMsg;
        } catch (XmlBuilderException e) {
            throw new XsulException("could not convert XmlObject to XML Element:\n"+responseXml, e);
        }
    }
    
    public static XmlObject xmlElementToXmlObject(XmlElement xmlEl) {
        String xmlAsString = builder.serializeToString(xmlEl);
        return xmlElementToXmlObject(xmlAsString);
    }
    
    public static XmlObject xmlElementToXmlObject(String xmlAsString) {
        try {
            XmlObject requestXmlObj = XmlObject.Factory.parse(xmlAsString);
            return requestXmlObj;
        } catch (XmlException e) {
            throw new XsulException("could not parse XML of incoming message:\n"+xmlAsString, e);
        }
    }
    
    public static XmlObject xmlElementToXmlObject(XmlPullParser pp) throws XmlBuilderException {
        try {
            // first convert stream to string
            XmlSerializer xs = builder.getFactory().newSerializer();
            System.out.println("serializer implementation class is "+xs.getClass());
            StringWriter w = new StringWriter();
            xs.setOutput(w);
            serializeSubTree(pp, xs);
            String xmlAsString = w.toString();
            // then convert it to XmlObject
            return xmlElementToXmlObject(xmlAsString);
        } catch (Exception e) {
            throw new XsulException("could nto convert XML input stream to XmlBeans XmlObject", e);
        }
    }
    
    
    public static void serializeSubTree(XmlPullParser pp, XmlSerializer ser) {
        try {
            pp.require(XmlPullParser.START_TAG, null, null);
            // first walk upward namespace stack and declare all namespace prefixes that are declared in parent
            Map declareNamespacePrefixes = new HashMap();
            for (int i = pp.getNamespaceCount(pp.getDepth() ) - 1; i >= 0; --i) {
                String prefix = pp.getNamespacePrefix(i);
                if(!declareNamespacePrefixes.containsKey(prefix)) {
                    // add to list
                    String ns = pp.getNamespaceUri(i);
                    declareNamespacePrefixes.put(prefix, prefix);
                    ser.setPrefix(pp.getNamespacePrefix (i), pp.getNamespaceUri (i));
                }
            }
            ser.startTag(pp.getNamespace (), pp.getName ());
            
            for (int i = 0; i < pp.getAttributeCount (); i++) {
                ser.attribute
                    (pp.getAttributeNamespace (i),
                     pp.getAttributeName (i),
                     pp.getAttributeValue (i));
            }
            
            
            int level = 1;
            while(level > 0) {
                int eventType = pp.next(); // NOTE: it is OK to do it as we already written startTag
                writeToken(eventType, pp, ser);
                if(eventType == XmlPullParser.END_TAG) {
                    --level;
                } else if(eventType == XmlPullParser.START_TAG) {
                    ++level;
                }
            }
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not convert XPP input stream subtree to Xmlbeans object", e);
        } catch (IOException e) {
            throw new XmlBuilderException("IO error when convert XPP input stream subtree to Xmlbeans object", e);
        }
    }
    
    private static void writeToken (int eventType, XmlPullParser parser, XmlSerializer serializer) throws XmlPullParserException, IOException {
        switch (eventType) {
            case XmlPullParser.START_TAG:
                writeStartTag (parser, serializer);
                break;
                
            case XmlPullParser.END_TAG:
                serializer.endTag(parser.getNamespace (), parser.getName ());
                break;
                
            case XmlPullParser.IGNORABLE_WHITESPACE:
                //comment it to remove ignorable whtespaces from XML infoset
                String s = parser.getText ();
                serializer.ignorableWhitespace (s);
                break;
                
            case XmlPullParser.TEXT:
                serializer.text (parser.getText ());
                break;
                
            case XmlPullParser.ENTITY_REF:
                serializer.entityRef (parser.getName ());
                break;
                
            case XmlPullParser.CDSECT:
                serializer.cdsect( parser.getText () );
                break;
                
            case XmlPullParser.PROCESSING_INSTRUCTION:
                serializer.processingInstruction( parser.getText ());
                break;
                
            case XmlPullParser.COMMENT:
                serializer.comment (parser.getText ());
                break;
                
        }
    }
    
    private static void writeStartTag (XmlPullParser parser, XmlSerializer serializer) throws XmlPullParserException, IOException {
        //check for case when feature xml roundtrip is supported
        //if (parser.getFeature (FEATURE_XML_ROUNDTRIP)) {
        if (!parser.getFeature (XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES)) {
            for (int i = parser.getNamespaceCount (parser.getDepth ()-1);
                     i <= parser.getNamespaceCount (parser.getDepth ())-1; i++) {
                serializer.setPrefix
                    (parser.getNamespacePrefix (i),
                     parser.getNamespaceUri (i));
            }
        }
        serializer.startTag(parser.getNamespace (), parser.getName ());
        
        for (int i = 0; i < parser.getAttributeCount (); i++) {
            serializer.attribute
                (parser.getAttributeNamespace (i),
                 parser.getAttributeName (i),
                 parser.getAttributeValue (i));
        }
        //serializer.closeStartTag();
    }
    
    private final static String TEST_XML = "<x xmlns:e='1' xmlns:f='3'>"+
        "<a xmlns:e='2' xmlns='4'>2</a>"+
        "</x>";
    
    /**
     *
     */
    public static void main(String[] args) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlSerializer xs = factory.newSerializer();
        System.out.println("serializer implementation class is "+xs.getClass());
        StringWriter w = new StringWriter();
        xs.setOutput(w);
        XmlPullParser pp = factory.newPullParser();
        pp.setInput( new StringReader(TEST_XML) );
        pp.nextTag();
        pp.nextTag();
        serializeSubTree(pp, xs);
        System.out.println("subTree="+w);
    }
    
}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2004 The Trustees of Indiana University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1) All redistributions of source code must retain the above
 *    copyright notice, the list of authors in the original source
 *    code, this list of conditions and the disclaimer listed in this
 *    license;
 *
 * 2) All redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the disclaimer
 *    listed in this license in the documentation and/or other
 *    materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include
 *    the following acknowledgement:
 *
 *      "This product includes software developed by the Indiana
 *      University Extreme! Lab.  For further information please visit
 *      http://www.extreme.indiana.edu/"
 *
 *    Alternatively, this acknowledgment may appear in the software
 *    itself, and wherever such third-party acknowledgments normally
 *    appear.
 *
 * 4) The name "Indiana University" or "Indiana University
 *    Extreme! Lab" shall not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission from Indiana University.  For written permission,
 *    please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana
 *    University" name nor may "Indiana University" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code
 * provided does not infringe the patent or any other intellectual
 * property rights of any other entity.  Indiana University disclaims any
 * liability to any recipient for claims brought by any other entity
 * based on infringement of intellectual property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH
 * NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA
 * UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT
 * SOFTWARE IS FREE OF INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR
 * OTHER PROPRIETARY RIGHTS.  INDIANA UNIVERSITY MAKES NO WARRANTIES THAT
 * SOFTWARE IS FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP
 * DOORS", "WORMS", OR OTHER HARMFUL CODE.  LICENSEE ASSUMES THE ENTIRE
 * RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS,
 * AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION GENERATED USING
 * SOFTWARE.
 */



