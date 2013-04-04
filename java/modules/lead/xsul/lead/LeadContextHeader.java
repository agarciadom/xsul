/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/* Copyright (c) 2002-2005 Extreme! Lab, Indiana University. All rights reserved.
 * This software is open source. See the bottom of this file for the licence.
 * $Id: LeadContextHeader.java,v 1.10 2006/06/10 16:07:17 aslom Exp $ */
package xsul.lead;

import javax.xml.namespace.QName;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;
import xsul.MLogger;
import xsul.XmlConstants;
import xsul.ws_addressing.WsaEndpointReference;

/**
 * Represents SOAP Header with context information for LEAD workflows/services.
 */
public class LeadContextHeader extends XmlElementAdapter {
    private final static MLogger logger = MLogger.getLogger();
    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
    
    public final static QName TYPE = new QName(
        "http://lead.extreme.indiana.edu/namespaces/2005/10/lead-context-header", "context");
    public final static XmlNamespace NS = builder.newNamespace("lh", TYPE.getNamespaceURI());
    
    public final static String EXPERIMENT_ID = "experiment-id";
    public final static String WORKFLOW_INSTANCE_ID = "workflow-instance-id";
    public final static String NODE_ID = "workflow-node-id";
    public final static String TIME_STEP = "workflow-time-step";
    public final static String SERVICE_INSTANCE_ID = "service-instance-id";
    //public final static String PARENT_SERVICE_INSTANCE_ID = "parent-service-instance-id";
    //public final static String SERVICE_TYPE_ID = "service-type-id";
    public final static String GFAC_URL = "gfac-url";
    public final static String RESOURCE_CATALOG_URL = "resource-catalog-url";
    //public final static String PROPERTIES_FILE_URL = "properties-file-url";
    public final static String EVENT_SINK_EPR = "event-sink-epr";
    public final static String USER_DN = "user-dn";
    
    
    public LeadContextHeader(String experimentId, String userDn) {
        super(builder.newFragment(NS, TYPE.getLocalPart()));
        setExperimentId(experimentId);
        setUserDn(userDn);
    }
    
    public LeadContextHeader(XmlElement target) {
        super(target);
    }
    
    protected void setStringAppend(String name, String value) {
        XmlElement el = element(NS, name, true);
        el.replaceChildrenWithText(value);
    }

    protected void setStringSecondEl(String name, String value) {
        XmlElement el = element(NS, name);
        if(el == null) {
            // make sure element are inserted after experiment Id
            el = newElement(NS, name);
            addChild(1, el);
            el.setParent(this);
        }
        el.replaceChildrenWithText(value);
    }

    protected String getString(String name) {
        XmlElement el = element(NS, name);
        if(el == null) {
            return null;
        }
        return el.requiredTextContent();
    }
    
    public void setExperimentId(String experimentId) { setStringAppend(EXPERIMENT_ID, experimentId); }
    public String getExperimentId() { return getString(EXPERIMENT_ID); }
    public void setWorkflowId(String workflowId) { setStringSecondEl(WORKFLOW_INSTANCE_ID, workflowId); }
    public String getWorkflowId() { return getString(WORKFLOW_INSTANCE_ID); }
    public void setNodeId(String nodeId) { setStringSecondEl(NODE_ID, nodeId); }
    public String getNodeId() { return getString(NODE_ID); }
    public void setTimeStep(String timeStep) { setStringSecondEl(TIME_STEP, timeStep); }
    public String getTimeStep() { return getString(TIME_STEP); }
    public void setServiceId(String serviceId) { setStringSecondEl(SERVICE_INSTANCE_ID, serviceId); }
    public String getServiceId() { return getString(SERVICE_INSTANCE_ID); }
    public void setGfacUrl(String url) { setStringSecondEl(GFAC_URL, url); }
    public String getGfacUrl() { return getString(GFAC_URL); }
//    public void setPropertiesFileUrl(String url) { setStringSecondEl(PROPERTIES_FILE_URL, url); }
//    public String getPropertiesFileUrl() { return getString(PROPERTIES_FILE_URL); }
    public void setResourceCatalogUrl(String url) { setStringSecondEl(RESOURCE_CATALOG_URL, url); }
    public String getResourceCatalogUrl() { return getString(RESOURCE_CATALOG_URL); }
    public void setUserDn(String userDn) { setStringSecondEl(USER_DN, userDn); }
    public String getUserDn() { return getString(USER_DN); }

    
    public void setEventSink(WsaEndpointReference epr) {
        //epr.setNamespace(builder.newNamespace(XmlElement.NO_NAMESPACE)); //CHECKME
        epr.setNamespace(NS);
        epr.setName(EVENT_SINK_EPR);
        XmlElement el = element(NS, EVENT_SINK_EPR);
        if(el != null) {
            replaceChild(epr, el);
        } else {
            addChild(1, epr); //addChild(epr);
        }
    }
    
    public WsaEndpointReference getEventSink() {
        XmlElement el = element(NS, EVENT_SINK_EPR);
        if(el == null) {
            return null;
        }
        if(el instanceof WsaEndpointReference) {
            return (WsaEndpointReference) el;
        } else {
            return new WsaEndpointReference(el);
        }
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2005 The Trustees of Indiana University.
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


