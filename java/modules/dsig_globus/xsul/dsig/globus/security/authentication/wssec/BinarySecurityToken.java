/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.globus.security.authentication.wssec;


import javax.xml.namespace.QName;
import org.apache.xml.security.utils.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import xsul.dsig.apache.axis.uti.XMLUtils;


public class BinarySecurityToken {
    public static final QName TOKEN =
        new QName(WSConstants.WSSE_NS, "BinarySecurityToken");
    public static final QName BASE64_ENCODING =
        new QName(WSConstants.WSSE_NS, "Base64Binary");
    //protected static XMLUtils XMLUtil = new XMLUtils();
    protected Element element;

    // init from existing element
    public BinarySecurityToken(Element elem) throws WSSecurityException {
        this.element = elem;

        QName el =
            new QName(
                this.element.getNamespaceURI(), this.element.getLocalName()
            );

        if (!el.equals(TOKEN)) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY_TOKEN, "badTokenType",
                new Object[] { el }
            );
        }

        if (!getEncodingType().equals(BASE64_ENCODING)) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY_TOKEN, "badEncoding",
                new Object[] { getEncodingType() }
            );
        }
    }

    // create new
    public BinarySecurityToken(Document doc) {
        this.element =
            doc.createElementNS(
                WSConstants.WSSE_NS, "wsse:BinarySecurityToken"
            );

        WSSecurityUtil.setNamespace(
            this.element, WSConstants.WSSE_NS, WSConstants.WSSE_PREFIX
        );

        setEncodingType(BASE64_ENCODING);

        this.element.appendChild(doc.createTextNode(""));
    }

    public QName getValueType() {
        String value = this.element.getAttribute("ValueType");

        return XMLUtils.getQNameFromString(value, this.element);
    }

    protected void setValueType(QName type) {
        this.element.setAttributeNS(
            null, "ValueType", XMLUtils.getStringForQName(type, this.element)
        );
    }

    public QName getEncodingType() {
        String value = this.element.getAttribute("EncodingType");

        
        return XMLUtils.getQNameFromString(value, this.element);
    }

    protected void setEncodingType(QName encoding) {
        this.element.setAttributeNS(
            null, "EncodingType",
            XMLUtils.getStringForQName(encoding, this.element)
        );
    }

    public byte[] getToken() {
        Text node = getFirstNode();

        if (node == null) {
            return null;
        }

        try {
            return Base64.decode(node.getData());
        } catch (Exception e) {
            return null;
        }
    }

    protected void setToken(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }

        Text node = getFirstNode();
        node.setData(Base64.encode(data));
    }

    protected Text getFirstNode() {
        Node node = this.element.getFirstChild();

        return ((node != null) && node instanceof Text) ? (Text) node : null;
    }

    public Element getElement() {
        return this.element;
    }

    public String getID() {
        return this.element.getAttributeNS(WSConstants.WSU_NS, "Id");
    }

    public void setID(String id) {
        String prefix =
            WSSecurityUtil.setNamespace(
                this.element, WSConstants.WSU_NS, WSConstants.WSU_PREFIX
            );
        this.element.setAttributeNS(WSConstants.WSU_NS, prefix + ":Id", id);
    }

    public String toString() {
        return XMLUtils.ElementToString(this.element);
    }
}
