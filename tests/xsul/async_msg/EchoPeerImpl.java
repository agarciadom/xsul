package xsul.async_msg;

import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.lead.LeadContextHeader;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;

public class EchoPeerImpl implements EchoPeer  {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    public XmlElement echoString(XmlElement inputMsg) {
        
        LeadContextHeader nh = (LeadContextHeader) StickySoapHeaderHandler.
            getHeaderAs(LeadContextHeader.class, LeadContextHeader.TYPE);
        //System.err.println("input "+builder.serializeToString(param0));
        XmlElement param0 = inputMsg.requiredElement(null, "param0");
        if(nh != null) {
            System.err.println("SERVICE got event sink : "+nh.getEventSink());
            String s = param0.requiredTextContent();
            System.err.println("SERVICE will send notification "+s+" to "+nh.getEventSink());
        }
    
        inputMsg.setParent(null);
        param0.setName("return");
        inputMsg.setName("echoStringResponse");
        return inputMsg;
    }
}

