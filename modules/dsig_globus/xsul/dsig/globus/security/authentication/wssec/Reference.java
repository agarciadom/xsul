/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.globus.security.authentication.wssec;




import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xsul.dsig.apache.axis.uti.XMLUtils;


public class Reference {
    public static final QName TOKEN =
        new QName(WSConstants.WSSE_NS, "Reference");
    protected Element element;

    // init from existing element
    public Reference(Element elem) throws WSSecurityException {
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
    public Reference(Document doc) {
        this.element =
            doc.createElementNS(WSConstants.WSSE_NS, "wsse:Reference");
    }

    public Element getElement() {
        return this.element;
    }

    public String getURI() {
        return this.element.getAttribute("URI");
    }

    public void setURI(String uri) {
        this.element.setAttribute("URI", uri);
    }

    public String toString() {
        return XMLUtils.ElementToString(this.element);
    }
}
