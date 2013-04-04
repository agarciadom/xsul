/**
 * CapmanAbstractImpl.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: CapmanAbstractImpl.java,v 1.5 2005/04/26 20:57:16 lifang Exp $
 */

package xsul.xpola.capman;

import edu.indiana.extreme.xsul.xpola.capman.xsd.GetAllCapabilityHandlesInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetAllCapabilityHandlesOutDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilitiesByOwnerInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilitiesByOwnerOutDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityByHandleInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityByHandleOutDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityHandlesByOwnerInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityHandlesByOwnerOutDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityHandlesByUserInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityHandlesByUserOutDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.GetCapabilityOutDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.RegisterCapabilityInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.RegisterCapabilityOutDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.RevokeCapabilitiesByOwnerInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.RevokeCapabilitiesByOwnerOutDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.RevokeCapabilityByHandleInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.RevokeCapabilityByHandleOutDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.UpdateCapabilityInDocument;
import edu.indiana.extreme.xsul.xpola.capman.xsd.UpdateCapabilityOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestByIdInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestByIdOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestsByIssuerInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestsByIssuerOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestsByReceiverInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.GetRequestsByReceiverOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.RegisterRequestInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.RegisterRequestOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.RemoveRequestByIdInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.RemoveRequestByIdOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.RemoveRequestsByIssuerInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.RemoveRequestsByIssuerOutDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.ResponseToRequestInDocument;
import edu.indiana.extreme.xsul.xpola.requestman.xsd.ResponseToRequestOutDocument;
import xsul.MLogger;


public abstract class CapmanAbstractImpl
    implements CapmanPortType, CapabilityManager, RequestManager {
    private static final MLogger logger = MLogger.getLogger();
    
    public GetCapabilityOutDocument getCapability(
        GetCapabilityInDocument input) {
        String userdn = input.getGetCapabilityIn().getUserdn();
        String handle = input.getGetCapabilityIn().getHandle();
        GetCapabilityOutDocument gcdoc =
            GetCapabilityOutDocument.Factory.newInstance();
        try {
            gcdoc.addNewGetCapabilityOut().setAcap(
                getCapability(handle, userdn));
        } catch (Exception e) {
            logger.severe("failed to get cap by handle and userdn", e);
        }
        return gcdoc;
    }
    
    public GetCapabilityHandlesByOwnerOutDocument getCapabilityHandlesByOwner(
        GetCapabilityHandlesByOwnerInDocument input) {
        String ownerdn = input.getGetCapabilityHandlesByOwnerIn().getOwner();
        GetCapabilityHandlesByOwnerOutDocument gchod =
            GetCapabilityHandlesByOwnerOutDocument.Factory.newInstance();
        try {
            gchod.addNewGetCapabilityHandlesByOwnerOut()
                .addNewHandlers().setItemArray(getCapabilityHandlesByOwner(ownerdn));
        } catch (Exception e) {
            logger.severe("failed to get cap handles by owner", e);
        }
        return gchod;
    }
    
    public GetCapabilityHandlesByUserOutDocument getCapabilityHandlesByUser(
        GetCapabilityHandlesByUserInDocument input) {
        String userdn = input.getGetCapabilityHandlesByUserIn().getUser();
        GetCapabilityHandlesByUserOutDocument gcuod =
            GetCapabilityHandlesByUserOutDocument.Factory.newInstance();
        try {
            gcuod.addNewGetCapabilityHandlesByUserOut()
                .addNewHandlers().setItemArray(getCapabilityHandlesByUser(userdn));
        } catch (Exception e) {
            logger.severe("failed to get cap handles by user", e);
        }
        return gcuod;
    }
    
    public GetAllCapabilityHandlesOutDocument getAllCapabilityHandles(
        GetAllCapabilityHandlesInDocument input) {
        GetAllCapabilityHandlesOutDocument gahod =
            GetAllCapabilityHandlesOutDocument.Factory.newInstance();
        try {
            gahod.addNewGetAllCapabilityHandlesOut()
                .addNewHandlers().setItemArray(getAllCapabilityHandles());
        } catch (Exception e) {
            logger.severe("failed to get all cap handles", e);
        }
        return gahod;
    }
    
    public GetCapabilityByHandleOutDocument getCapabilityByHandle(
        GetCapabilityByHandleInDocument input) {
        String handle = input.getGetCapabilityByHandleIn().getHandle();
        GetCapabilityByHandleOutDocument gcod =
            GetCapabilityByHandleOutDocument.Factory.newInstance();
        try {
            gcod.addNewGetCapabilityByHandleOut()
                .setAcap(getCapabilityByHandle(handle));
        } catch (Exception e) {
            logger.severe("failed to get capability by handle", e);
        }
        return gcod;
    }
    
    public GetCapabilitiesByOwnerOutDocument getCapabilitiesByOwner(
        GetCapabilitiesByOwnerInDocument input) {
        String owner = input.getGetCapabilitiesByOwnerIn().getOwner();
        GetCapabilitiesByOwnerOutDocument gcod =
            GetCapabilitiesByOwnerOutDocument.Factory.newInstance();
        try {
            gcod.addNewGetCapabilitiesByOwnerOut().addNewCaps()
                .setItemArray(getCapabilitiesByOwner(owner));
        } catch (Exception e) {
            logger.severe("failed to get capabilities by owner", e);
        }
        return null;
    }
    
    
    public RegisterCapabilityOutDocument registerCapability(
        RegisterCapabilityInDocument input) {
        String capstr = input.getRegisterCapabilityIn().getAcap();
        try {
            registerCapability(capstr);
        } catch (Exception e) {
            logger.severe("failed to register a cap", e);
        }
        return RegisterCapabilityOutDocument.Factory.newInstance();
    }
    
    public RevokeCapabilityByHandleOutDocument revokeCapabilityByHandle(
        RevokeCapabilityByHandleInDocument input) {
        String handle = input.getRevokeCapabilityByHandleIn().getHandle();
        try {
            revokeCapabilityByHandle(handle);
        } catch (Exception e) {
            logger.severe("failed to revoke a cap", e);
        }
        return RevokeCapabilityByHandleOutDocument.Factory.newInstance();
    }
    
    public RevokeCapabilitiesByOwnerOutDocument revokeCapabilitiesByOwner(
        RevokeCapabilitiesByOwnerInDocument input) {
        String owner = input.getRevokeCapabilitiesByOwnerIn().getOwner();
        try {
            revokeCapabilitiesByOwner(owner);
        } catch (Exception e) {
            logger.severe("failed to revoke caps by owner", e);
        }
        return null;
    }
    
    public UpdateCapabilityOutDocument updateCapability(
        UpdateCapabilityInDocument input) {
        String capstr = input.getUpdateCapabilityIn().getAcap();
        try {
            updateCapability(capstr);
        } catch (Exception e) {
            logger.severe("failed to update a cap", e);
        }
        return UpdateCapabilityOutDocument.Factory.newInstance();
    }
    
    public GetRequestByIdOutDocument getRequestById(
        GetRequestByIdInDocument input) {
        String id = input.getGetRequestByIdIn().getId();
        GetRequestByIdOutDocument grod =
            GetRequestByIdOutDocument.Factory.newInstance();
        try {
            grod.addNewGetRequestByIdOut().setArequest(getRequestById(id));
        } catch (Exception e) {
            logger.severe("failed to get request by id " + id, e);
        }
        return grod;
    }
    
    public GetRequestsByIssuerOutDocument getRequestsByIssuer(
        GetRequestsByIssuerInDocument input) {
        String issuer = input.getGetRequestsByIssuerIn().getIssuer();
        GetRequestsByIssuerOutDocument grod =
            GetRequestsByIssuerOutDocument.Factory.newInstance();
        try {
            grod.addNewGetRequestsByIssuerOut().addNewRequests()
                .setItemArray(getRequestsByIssuer(issuer));
        } catch (Exception e) {
            logger.severe("failed to get requests by issuer " + issuer, e);
        }
        return grod;
    }
    
    public GetRequestsByReceiverOutDocument getRequestsByReceiver(
        GetRequestsByReceiverInDocument input) {
        String receiver = input.getGetRequestsByReceiverIn().getReceiver();
        GetRequestsByReceiverOutDocument grod =
            GetRequestsByReceiverOutDocument.Factory.newInstance();
        try {
            grod.addNewGetRequestsByReceiverOut().addNewRequests()
                .setItemArray(getRequestsByReceiver(receiver));
        } catch (Exception e) {
            logger.severe("failed to get requests by receiver " + receiver, e);
        }
        return grod;
    }
    
    public RegisterRequestOutDocument registerRequest(
        RegisterRequestInDocument input) {
        String req = input.getRegisterRequestIn().getArequest();
        try {
            registerRequest(req);
        } catch (Exception e) {
            logger.severe("failed to register a request", e);
        }
        return RegisterRequestOutDocument.Factory.newInstance();
    }
    
    public ResponseToRequestOutDocument responseToRequest(
        ResponseToRequestInDocument input) {
        String resp = input.getResponseToRequestIn().getAresponse();
        try {
            responseToRequest(resp);
        } catch (Exception e) {
            logger.severe("failed to register a response", e);
        }
        return ResponseToRequestOutDocument.Factory.newInstance();
    }
    
    public RemoveRequestByIdOutDocument removeRequestById(
        RemoveRequestByIdInDocument input) {
        String id = input.getRemoveRequestByIdIn().getId();
        try {
            removeRequestById(id);
        } catch (Exception e) {
            logger.severe("failed to remove a request by id " + id, e);
        }
        return RemoveRequestByIdOutDocument.Factory.newInstance();
    }
    
    public RemoveRequestsByIssuerOutDocument removeRequestsByIssuer(
        RemoveRequestsByIssuerInDocument input) {
        String issuer = input.getRemoveRequestsByIssuerIn().getIssuer();
        try {
            removeRequestsByIssuer(issuer);
        } catch (Exception e) {
            logger.severe("failed to remove a request by issuer " + issuer, e);
        }
        return RemoveRequestsByIssuerOutDocument.Factory.newInstance();
    }
    
}

