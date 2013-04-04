/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.globus.security.authentication.wssec;



import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import xsul.dsig.apache.axis.uti.XMLUtils;


public class SecurityTokenReference {
    public static final QName TOKEN =
        new QName(WSConstants.WSSE_NS, "SecurityTokenReference");
    protected Element element;

    // init from existing element
    public SecurityTokenReference(Element elem) throws WSSecurityException {
        this.element = elem;

        QName el =
            new QName(
                this.element.getNamespaceURI(), this.element.getLocalName()
            );

        if (!el.equals(TOKEN)) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, "badElement",
                new Object[] { TOKEN, el }
            );
        }
    }

    // create new
    public SecurityTokenReference(Document doc) {
        this.element =
            doc.createElementNS(
                WSConstants.WSSE_NS, "wsse:SecurityTokenReference"
            );
    }

    public void setReference(Reference ref) {
        Element elem = getFirstElement();

        if (elem != null) {
            this.element.replaceChild(ref.getElement(), elem);
        } else {
            this.element.appendChild(ref.getElement());
        }
    }

    public Reference getReference() throws WSSecurityException {
        Element elem = getFirstElement();

        return (elem == null) ? null : new Reference(elem);
    }

    private Element getFirstElement() {
        for (
            Node currentChild = this.element.getFirstChild();
                currentChild != null;
                currentChild = currentChild.getNextSibling()
        ) {
            if (currentChild instanceof Element) {
                return (Element) currentChild;
            }
        }

        return null;
    }

    public Element getElement() {
        return this.element;
    }

    public String getID() {
        return this.element.getAttributeNS(WSConstants.WSU_NS, "Id");
    }

    public void setID(String id) {
        this.element.setAttributeNS(WSConstants.WSU_NS, "wsu:Id", id);
    }

    public String toString() {
        return XMLUtils.ElementToString(this.element);
    }
}
