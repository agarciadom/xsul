/**
 * CapmanClient.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: CapmanClient.java,v 1.11 2005/04/26 20:57:16 lifang Exp $
 */

package xsul.xpola.capman;

import edu.indiana.extreme.xsul.xpola.capman.xsd.*;

import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestByIdInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestByIdOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestsByIssuerInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestsByIssuerOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestsByReceiverInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestsByReceiverOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.RegisterRequestInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.RemoveRequestByIdInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.RemoveRequestsByIssuerInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.ResponseToRequestInDocument;
import xsul.xwsif_runtime.XmlBeansWSIFRuntime;

public class CapmanClient implements CapabilityManager, RequestManager {
    
    private CapmanPortType capman;
    private RequestmanPortType reqman;
    
    public CapmanClient(String location) {
        String cwsdlLoc =
            CapabilityManager.class.getResource("capman.wsdl").toString();
        String rwsdlLoc =
            CapabilityManager.class.getResource("requestman.wsdl").toString();
        //        capman = (CapmanPortType)WSIFRuntime.newClient(cwsdlLoc, location)
        //            .generateDynamicStub(CapmanPortType.class);
        capman = (CapmanPortType)XmlBeansWSIFRuntime.newClient(cwsdlLoc, location)
            .generateDynamicStub(CapmanPortType.class);
        // fixme: maybe reqman can share the same client with capman?
        reqman = (RequestmanPortType)XmlBeansWSIFRuntime.newClient(rwsdlLoc, location)
            .generateDynamicStub(RequestmanPortType.class);
    }
    
    public String getCapability(String handle, String userdn) throws Exception {
        GetCapabilityInDocument inDoc =
            GetCapabilityInDocument.Factory.newInstance();
        GetCapabilityInDocument.GetCapabilityIn gcin =
            inDoc.addNewGetCapabilityIn();
        gcin.setHandle(handle);
        gcin.setUserdn(userdn);
        GetCapabilityOutDocument outDoc = capman.getCapability(inDoc);
        return outDoc.getGetCapabilityOut().getAcap();
    }
    
    public String getCapabilityByHandle(String handle) throws Exception {
        GetCapabilityByHandleInDocument inDoc =
            GetCapabilityByHandleInDocument.Factory.newInstance();
        inDoc.addNewGetCapabilityByHandleIn().setHandle(handle);
        GetCapabilityByHandleOutDocument outDoc =
            capman.getCapabilityByHandle(inDoc);
        return outDoc.getGetCapabilityByHandleOut().getAcap();
    }
    
    public void revokeCapabilitiesByOwner(String owner) throws Exception {
        RevokeCapabilitiesByOwnerInDocument inDoc =
            RevokeCapabilitiesByOwnerInDocument.Factory.newInstance();
        inDoc.addNewRevokeCapabilitiesByOwnerIn().setOwner(owner);
        capman.revokeCapabilitiesByOwner(inDoc);
    }
    
    public String[] getCapabilitiesByOwner(String owner) throws Exception {
        GetCapabilitiesByOwnerInDocument inDoc =
            GetCapabilitiesByOwnerInDocument.Factory.newInstance();
        inDoc.addNewGetCapabilitiesByOwnerIn().setOwner(owner);
        GetCapabilitiesByOwnerOutDocument outDoc =
            capman.getCapabilitiesByOwner(inDoc);
        return outDoc.getGetCapabilitiesByOwnerOut().getCaps().getItemArray();
    }
    
    public void registerCapability(String acap) throws Exception {
        RegisterCapabilityInDocument inDoc =
            RegisterCapabilityInDocument.Factory.newInstance();
        inDoc.addNewRegisterCapabilityIn().setAcap(acap);
        capman.registerCapability(inDoc);
    }
    
    public String[] getCapabilityHandlesByUser(String userdn) throws Exception {
        GetCapabilityHandlesByUserInDocument inDoc =
            GetCapabilityHandlesByUserInDocument.Factory.newInstance();
        inDoc.addNewGetCapabilityHandlesByUserIn().setUser(userdn);
        GetCapabilityHandlesByUserOutDocument outDoc =
            capman.getCapabilityHandlesByUser(inDoc);
        return outDoc.getGetCapabilityHandlesByUserOut()
            .getHandlers().getItemArray();
    }
    
    public String[] getAllCapabilityHandles() throws Exception {
        GetAllCapabilityHandlesInDocument inDoc =
            GetAllCapabilityHandlesInDocument.Factory.newInstance();
        inDoc.addNewGetAllCapabilityHandlesIn();
        GetAllCapabilityHandlesOutDocument outDoc =
            capman.getAllCapabilityHandles(inDoc);
        return outDoc.getGetAllCapabilityHandlesOut().getHandlers().getItemArray();
    }
    
    public String[] getCapabilityHandlesByOwner(String ownerdn) throws Exception {
        GetCapabilityHandlesByOwnerInDocument inDoc =
            GetCapabilityHandlesByOwnerInDocument.Factory.newInstance();
        inDoc.addNewGetCapabilityHandlesByOwnerIn().setOwner(ownerdn);
        GetCapabilityHandlesByOwnerOutDocument outDoc =
            capman.getCapabilityHandlesByOwner(inDoc);
        return outDoc.getGetCapabilityHandlesByOwnerOut().getHandlers().getItemArray();
    }
    
    public void updateCapability(String acap) throws Exception {
        UpdateCapabilityInDocument inDoc =
            UpdateCapabilityInDocument.Factory.newInstance();
        inDoc.addNewUpdateCapabilityIn().setAcap(acap);
        capman.updateCapability(inDoc);
    }
    
    public void revokeCapabilityByHandle(String handle) throws Exception {
        RevokeCapabilityByHandleInDocument inDoc =
            RevokeCapabilityByHandleInDocument.Factory.newInstance();
        inDoc.addNewRevokeCapabilityByHandleIn().setHandle(handle);
        capman.revokeCapabilityByHandle(inDoc);
    }
    
    public void removeRequestById(String id) throws Exception {
        RemoveRequestByIdInDocument inDoc =
            RemoveRequestByIdInDocument.Factory.newInstance();
        inDoc.addNewRemoveRequestByIdIn().setId(id);
        reqman.removeRequestById(inDoc);
    }
    
    public void registerRequest(String request) throws Exception {
        RegisterRequestInDocument inDoc =
            RegisterRequestInDocument.Factory.newInstance();
        inDoc.addNewRegisterRequestIn().setArequest(request);
        reqman.registerRequest(inDoc);
    }
    
    public String[] getRequestsByIssuer(String issuer) throws Exception {
        GetRequestsByIssuerInDocument inDoc =
            GetRequestsByIssuerInDocument.Factory.newInstance();
        inDoc.addNewGetRequestsByIssuerIn().setIssuer(issuer);
        GetRequestsByIssuerOutDocument outDoc = reqman.getRequestsByIssuer(inDoc);
        return outDoc.getGetRequestsByIssuerOut().getRequests().getItemArray();
    }
    
    public void responseToRequest(String response) throws Exception {
        ResponseToRequestInDocument inDoc =
            ResponseToRequestInDocument.Factory.newInstance();
        inDoc.addNewResponseToRequestIn().setAresponse(response);
        reqman.responseToRequest(inDoc);
    }
    
    public void removeRequestsByIssuer(String issuer) throws Exception {
        RemoveRequestsByIssuerInDocument inDoc =
            RemoveRequestsByIssuerInDocument.Factory.newInstance();
        inDoc.addNewRemoveRequestsByIssuerIn().setIssuer(issuer);
        reqman.removeRequestsByIssuer(inDoc);
    }
    
    public String getRequestById(String id) throws Exception {
        GetRequestByIdInDocument inDoc =
            GetRequestByIdInDocument.Factory.newInstance();
        inDoc.addNewGetRequestByIdIn().setId(id);
        GetRequestByIdOutDocument outDoc = reqman.getRequestById(inDoc);
        return outDoc.getGetRequestByIdOut().getArequest();
    }
    
    public String[] getRequestsByReceiver(String receiver) throws Exception {
        GetRequestsByReceiverInDocument inDoc =
            GetRequestsByReceiverInDocument.Factory.newInstance();
        inDoc.addNewGetRequestsByReceiverIn().setReceiver(receiver);
        GetRequestsByReceiverOutDocument outDoc = reqman.getRequestsByReceiver(inDoc);
        return outDoc.getGetRequestsByReceiverOut().getRequests().getItemArray();
    }
    
}

