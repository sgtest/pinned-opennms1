//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.poller.pollables.PollStatus;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a collection of nodes each with interfaces and services
 */
public class PollerNetwork extends PollerContainer {

    private Poller m_poller;

    /**
     * Map of 'PollableNode' objects keyed by nodeId
     */
    private Map m_pollableNodes;

    private List m_pollableServices;

    public PollerNetwork(Poller poller) {
        super(PollStatus.STATUS_UP);
        m_poller = poller;
        m_pollableNodes = Collections.synchronizedMap(new HashMap());
        m_pollableServices = Collections.synchronizedList(new ArrayList());
    }

    /**
     * @param pNode
     * @param poller
     */
    public void addNode(PollerNode pNode) {
        m_pollableNodes.put(new Integer(pNode.getNodeId()), pNode);
        Category log = ThreadCategory.getInstance(getClass());
        log.debug("PollableNetwork.addNode: adding pollable node with id: " + pNode.getNodeId() + " new size: " + m_pollableNodes.size());
    }

    /**
     * @param nodeId
     * @param poller
     * @return
     */
    public PollerNode findNode(int nodeId) {
        Integer key = new Integer(nodeId);
        if (m_pollableNodes.containsKey(key)) {
            return (PollerNode) m_pollableNodes.get(key);
        } else {
            return null;
        }
    }

    /**
     * @param nodeId
     * @param poller
     */
    public void removeNode(int nodeId) {
        m_pollableNodes.remove(new Integer(nodeId));
    }

    public void visit(PollerVisitor v) {
        Iterator j = m_pollableNodes.values().iterator();
        while (j.hasNext()) {
            PollerNode pNode = (PollerNode) j.next();
            pNode.visit(v);
        }

    }

    public Poller getPoller() {
        return m_poller;
    }

    class DumpVisitor extends PollerVisitorAdaptor {
        
        private Category m_log;

        public DumpVisitor(Category log) {
            m_log = log;
        }
        public void visitNode(PollerNode pNode) {
            m_log.debug(" nodeid=" + pNode.getNodeId() + " status=" + pNode.getStatus());
        }

        public void visitInterface(PollerInterface pIf) {
            m_log.debug("     interface=" + pIf.getAddress().getHostAddress() + " status=" + pIf.getStatus());
        }

        public void visitService(PollerService pSvc) {
            m_log.debug("         service=" + pSvc.getServiceName() + " status=" + pSvc.getStatus());
        }
    };

    /**
     * @param poller
     */
    public void dumpNetwork() {
        final Category log = ThreadCategory.getInstance(getClass());

        DumpVisitor dumper = new DumpVisitor(log);
        visit(dumper);

    }
    
    public void dumpNode(PollerNode node) {
        DumpVisitor dumper = new DumpVisitor(ThreadCategory.getInstance(getClass()));
        node.visit(dumper);
    }

    public PollerService createPollableService(int nodeId, String ipAddr, ServiceConfig svcConfig, PollStatus lastKnownStatus, Date svcLostDate) throws InterruptedException, UnknownHostException {
        Category log = ThreadCategory.getInstance();
        PollerService pSvc;
        PollerNode pNode = null;
        boolean ownLock = false;
        try {
            PollerInterface pInterface = null;
            boolean nodeCreated = false;
            boolean interfaceCreated = false;

            // Does the node already exist in the poller's
            // pollable node map?
            //
            pNode = findNode(nodeId);
            if (pNode == null) {
                // Nope...so we need to create it
                pNode = new PollerNode(nodeId, m_poller);
                nodeCreated = true;
            } 
            
            // Obtain node lock
            //
            ownLock = pNode.getNodeLock(NodeLocker.WAIT_FOREVER);
            

            // Does the interface exist in the pollable
            // node?
            //
            pInterface = pNode.findInterface(ipAddr);
            if (pInterface == null) {
                // Create the PollableInterface
                pInterface = new PollerInterface(pNode, InetAddress.getByName(ipAddr));
                interfaceCreated = true;
            }

            // Create a new PollableService representing
            // this node, interface,
            // service and package pairing
            //
            pSvc = new PollerService(pInterface, svcConfig, lastKnownStatus, svcLostDate);

            // Add the service to the PollableInterface
            // object
            //
            // WARNING: The PollableInterface stores
            // services in a map
            // keyed by service name, therefore, only the
            // LAST
            // PollableService aded to the interface for a
            // particular service will be represented in the
            // map. THIS IS BY DESIGN.
            //
            // NOTE: addService() calls recalculateStatus()
            // on the interface
            log.debug("createPollableService: adding pollable service to service list of interface: " + ipAddr);
            pInterface.addService(pSvc);

            if (interfaceCreated) {
                // Add the interface to the node
                //
                // NOTE: addInterface() calls
                // recalculateStatus() on the node
                if (log.isDebugEnabled())
                    log.debug("createPollableService: adding new pollable interface " + ipAddr + " to pollable node " + nodeId);
                pNode.addInterface(pInterface);
            } else {
                // Recalculate node status
                //
                pNode.recalculateStatus();
            }

            if (nodeCreated) {
                // Add the node to the node map
                //
                if (log.isDebugEnabled())
                    log.debug("createPollableService: adding new pollable node: " + nodeId);
                addNode(pNode);
            }

            // Add new service to the pollable services
            // list.
            //
            m_pollableServices.add(pSvc);

        } finally {
            if (ownLock) {
                try {
                    pNode.releaseNodeLock();
                } catch (InterruptedException iE) {
                    log.error("createPollableService: Failed to release node lock on nodeid " + pNode.getNodeId() + ", thread interrupted.");
                }
            }

        }
        return pSvc;
    }

    /**
     * @return
     */
    public List getPollableServices() {
        return m_pollableServices;
    }
    
    public Event createUpEvent(Date d) { return null; }
    public Event createDownEvent(Date d) { return null; }

}
