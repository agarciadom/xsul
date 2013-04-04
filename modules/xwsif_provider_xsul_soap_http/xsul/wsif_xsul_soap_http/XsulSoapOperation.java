/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002-2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XsulSoapOperation.java,v 1.11 2006/04/21 20:05:32 aslom Exp $
 */

package xsul.wsif_xsul_soap_http;

import java.net.URI;
import java.util.Iterator;
import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import xsul.XmlConstants;
import xsul.invoker.soap.SoapDynamicInfosetInvoker;
import xsul.ws_addressing.WsAddressing;
import xsul.ws_addressing.WsaMessageInformationHeaders;
import xsul.ws_addressing_xwsdl.WsaWsdlUtil;
import xsul.wsdl.WsdlBindingOperation;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlException;
import xsul.wsdl.WsdlMessage;
import xsul.wsdl.WsdlPortTypeInput;
import xsul.wsdl.WsdlPortTypeOperation;
import xsul.wsif.WSIFException;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.impl.WSIFMessageElement;

/**
 * Handle invocations of one of WSDL operations
 *
 * @version $Revision: 1.11 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XsulSoapOperation implements WSIFOperation {
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    private XsulSoapPort endpoint;
    private WsdlBindingOperation bindingOp;
    private WsdlPortTypeOperation op;
    private WsdlMessage opInputMessage;
    private WsdlMessage opOutputMessage;
    private String opName;
    private XmlNamespace soapNs;
    private String soapAction;
    private String inputSoapBodyNamespace;
    private XmlNamespace inputNs;
    private String inputSoapBodyUse;
    private String inputSoapBodyEncodingStyle;
    private URI opInputWsaActionUri;
    private boolean rpcStyle;
    
    private URI location;
    
    XsulSoapOperation(XsulSoapPort wsifPort,
                      String bindingStyle,
                      WsdlBindingOperation bindingOp,
                      XmlNamespace soapNs)
        throws WSIFException
    {
        this.endpoint = wsifPort;
        this.rpcStyle = bindingStyle.equals("rpc");
        this.bindingOp = bindingOp;
        this.soapNs = soapNs;
        opName = bindingOp.getAttributeValue(null, "name");
        this.op = bindingOp.lookupOperation();
        this.opInputMessage = op.getInput().lookupMessage();
        if(opInputMessage == null) {
            throw new WSIFException("input message is required in operation "+opName);
        }
        {
            WsdlPortTypeInput opInput = op.getInput();
            //wsa:Action="http://soapinterop.org/WSDLInteropTestDocLit/echoString"
            String opInputWsaAction = opInput.getAttributeValue(
                WsAddressing.getDefaultNs().getNamespaceName(), WsaMessageInformationHeaders.ACTION_EL);
            // use default
            if(opInputWsaAction == null) {
                opInputWsaAction = WsaWsdlUtil.generateDefaultWsaAction(op.getPortType(), opName);
            }
            opInputWsaActionUri = URI.create(opInputWsaAction);
        }
        this.opOutputMessage = null;
        if(op.getOutput() != null) {
            this.opOutputMessage = op.getOutput().lookupMessage();
        }
        // extract soap:operation@soapAction
        XmlElement soapOperation = bindingOp.requiredElement(soapNs, "operation");
        soapAction = soapOperation.getAttributeValue(null, "soapAction");
        // extract input/soap:body
        XmlElement input = bindingOp.requiredElement(WsdlDefinitions.TYPE, "input");
        XmlElement inputSoapBody = input.requiredElement(soapNs, "body");
        //   namespace
        inputSoapBodyUse = inputSoapBody.getAttributeValue(null, "use");
        if(inputSoapBodyUse == null) {
            throw new WsdlException("attribute use is required on soap:body in "+input);
        }
        //   check use="encoded" encodingStyle="..."
        if( inputSoapBodyUse.equals("encoded") ) {
        }
        inputSoapBodyEncodingStyle = inputSoapBody.getAttributeValue(null, "encodingStyle");
        inputSoapBodyNamespace = inputSoapBody.getAttributeValue(null, "namespace");
        if(inputSoapBodyNamespace != null) {
            inputNs = builder.newNamespace(inputSoapBodyNamespace);
        } else {
            inputNs = builder.newNamespace(""); //TODO: is it correct????????
        }
        
        //XmlElement output = bindingOp.requiredElement(WsdlDefinitions.TYPE, "output");
        XmlElement output = bindingOp.element(WsdlDefinitions.TYPE, "output");
        if(output != null) {
            XmlElement outputSoapBody = output.requiredElement(soapNs, "body");
        }
        
        //TODO: do the same for list of faults ...
        this.location = URI.create(endpoint.getLocation());
    }
    
    public WsdlBindingOperation getBindingOperation() throws  WSIFException
    {
        return bindingOp;
    }
    
    public WSIFMessage createInputMessage() {
        // set schema for element based on WsdlMessage ...
        //TODO:  new WSIFMessageElement(op.getInput());
        return new WSIFMessageElement(opInputMessage);
    }
    
    
    public boolean isRequestResponseOperation() {
        return opOutputMessage != null;
    }
    
    public WSIFMessage createOutputMessage() {
        //TODO: check that no request-only op
        if(opOutputMessage != null) {
            return new WSIFMessageElement(opOutputMessage);
        } else {
            //throw new WSIFException("no output message available for operation "+op.getOperationName());
            return null;
        }
    }
    
    public WSIFMessage createFaultMessage() {
        return new WSIFMessageElement("fault");
    }
    
    
    // WSIFMessae executeRequestResponseOperation(WSIFMessage input) throws WSifFault, WSIFException
    
    public boolean executeRequestResponseOperation(WSIFMessage input,
                                                   WSIFMessage output,
                                                   WSIFMessage fault)
        throws WSIFException
    {
        // send input message name to follow WSDL or extract literal part ...
        XmlElement inputEl = (XmlElement)input;
        if(rpcStyle) {
            inputEl.setName(opName);
            inputEl.setNamespace(inputNs);
        } else {
            //extract first message part and this is inputEl
        }
        // add encoding style if necessary
        if(inputSoapBodyEncodingStyle != null) {
            // only for SOAP 1.1 ...
            if(soapNs.equals(Provider.WSDL_SOAP_NS)) {
                XmlAttribute encodingStyle = inputEl.attribute(XmlConstants.SOAP11_NS, "encodingStyle");
                if(encodingStyle != null && ! inputSoapBodyEncodingStyle.equals( encodingStyle.getValue() ) ) {
                    inputEl.removeAttribute(encodingStyle);
                }
                inputEl.addAttribute(XmlConstants.SOAP11_NS, "encodingStyle", inputSoapBodyEncodingStyle);
            }
        }
        //use endpoint invoker to send message
        SoapDynamicInfosetInvoker invoker = endpoint.getInvoker();
        invoker.setSoapAction(soapAction);
        //XmlElement response = invoker.invokeMessage(inputEl);
        XmlDocument doc = invoker.wrapAsSoapDocument(inputEl);
        WsaMessageInformationHeaders wsah = new WsaMessageInformationHeaders(doc);
        wsah.setTo(location);
        wsah.setAction(opInputWsaActionUri);
        
        XmlDocument respDoc = invoker.invokeXml(doc);
        
        XmlElement response = null;
        if(respDoc != null) { //handle one way messaging!
            response = invoker.extractBodyContent(respDoc);
        }
        
        if(response == null) {
            if(output != null) {
                throw new WSIFException("operation "+opName+" response was missing (one way?)");
            } else {
                return true;
            }
        }
        
        if(output == null) {
            throw new WSIFException("no output to store result of operation "+opName+"");
        }
        
        // transfer response to output or fault WSIF message
        if("Fault".equals(response.getName())) {
            //ALEK: Leaky Abstraciton: now can process both SOAP 1.1 and 1.2 ...
            XmlElement detail = null;
            //            String faultcode = "";
            //            String faultstring = "";
            if(XmlConstants.NS_URI_SOAP11.equals(response.getNamespaceName()) ) {
                detail = response.element(null, "detail");
                //                XmlElement fcEl = response.element(XmlConstants.SOAP11_NS, "faultcode");
                //                if(fcEl != null) {
                //                    faultcode = fcEl.requiredTextContent();
                //                }
                //                XmlElement fsEl = response.element(XmlConstants.SOAP11_NS, "faulstring");
                //                if(fsEl != null) {
                //                    faultstring = fsEl.requiredTextContent();
                //                }
            } else if(XmlConstants.NS_URI_SOAP12.equals(response.getNamespaceName()) ) {
                detail = response.element(XmlConstants.SOAP12_NS, "Detail");
                //                XmlElement codeEl = response.element(XmlConstants.SOAP12_NS, "Code");
                //                if(codeEl != null) {
                //                    XmlElement valueEl = response.element(XmlConstants.SOAP12_NS, "Value");
                //                    if(valueEl != null) {
                //                        faultstring = valueEl.requiredTextContent();
                //                    }
                //                }
                //                XmlElement reasonEl = response.element(XmlConstants.SOAP12_NS, "Reason");
                //                if(reasonEl != null) {
                //                    XmlElement textEl = response.element(XmlConstants.SOAP12_NS, "Text");
                //                    if(textEl != null) {
                //                        faultstring = textEl.requiredTextContent();
                //                    }
                //                }
                
            }
            // TODO extract faultcode + faultstring + detail etc. and put where????
            XmlElement faultEl = (XmlElement) fault;
            faultEl.removeAllChildren();
            if(detail != null) {
                faultEl.setNamespace(detail.getNamespace());
                faultEl.setName(detail.getName());
                for(Iterator i = detail.children(); i.hasNext(); ) {
                    Object child = i.next();
                    faultEl.addChild(child); //this WILL NOT set parent even if child is XmlElement!
                }
            } else {
                //faultEl.addElement(null, "fault").addChild(faultcode+":"+faultstring);
                for(Iterator i = response.children(); i.hasNext(); ) {
                    Object child = i.next();
                    faultEl.addChild(child); //this WILL NOT set parent even if child is XmlElement!
                }
                
            }
            return false;
        } else {
            XmlElement outputEl = (XmlElement) output;
            outputEl.removeAllChildren();
            for(Iterator i = response.children(); i.hasNext(); ) {
                Object child = i.next();
                outputEl.addChild(child); //this WILL NOT set parent even if child is XmlElement!
            }
            return true;
            
        }
    }
    
    public void executeInputOnlyOperation(WSIFMessage input) throws WSIFException
    {
        // TODO
        //throw new WSIFException("not implemented");
        executeRequestResponseOperation(input, null, null);
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


