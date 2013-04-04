/* DO NOT MODIFY!!!! This file was generated automatically by xwsdlc (version 2.1.17_SE2) */
package xsul.wsif;
import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
public interface XwsifTestService {
    public final static QName XWSDLC_PORTTYPE_QNAME = new QName("http://example.org/xwsif", "XwsifTestService");
    public void executeInputOnly(XmlElement input);
    public XmlElement executeRequestResponseWsdlFault(XmlElement input);
    public XmlElement executeRequestResponse(XmlElement input);
}

