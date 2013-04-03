package org.kevoree.framework

import java.util.Date
import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import org.kevoree.NodeLink
import org.kevoree.NodeNetwork
import org.kevoree.impl.DefaultKevoreeFactory
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 02/04/13
 * Time: 13:23
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public object KevoreePlatformHelper {
    val logger = LoggerFactory.getLogger(this.javaClass)!!
    val factory = DefaultKevoreeFactory()

    fun updateNodeLinkProp(actualModel: ContainerRoot, currentNodeName: String, targetNodeName: String, key: String, value: String, networkType: String, weight: Int): ContainerNode {

        var thisNodeFound: ContainerNode? = null
        /* SEARCH THE NODE NETWORK */
        var nodeNetwork: NodeNetwork? = null
        for (nn in actualModel.getNodeNetworks()) {
            if (nn.getInitBy()!!.getName() == currentNodeName && nn.getTarget()!!.getName() == targetNodeName) {
                nodeNetwork = nn
            }
        }

        if (nodeNetwork == null) {
            nodeNetwork = factory.createNodeNetwork()

            thisNodeFound = actualModel.findNodesByID(currentNodeName)
            if (thisNodeFound == null) {
                thisNodeFound = factory.createContainerNode()
                thisNodeFound!!.setName(currentNodeName)
                actualModel.addNodes(thisNodeFound!!)
            }

            var targetNode = actualModel.findNodesByID(targetNodeName)
            if (targetNode == null) {
                logger.debug("Unknown node " + targetNodeName + " add to model")
                targetNode = factory.createContainerNode()
                targetNode!!.setName(targetNodeName)
                actualModel.addNodes(targetNode!!)
            }
            nodeNetwork!!.setTarget(targetNode)
            nodeNetwork!!.setInitBy(thisNodeFound)
            actualModel.addNodeNetworks(nodeNetwork!!)
        }

        /* Found node link */
        var nodeLink: NodeLink? = null
        for (l in nodeNetwork!!.getLink()) {
            if (l.getNetworkType() == networkType) {
                nodeLink = l
                break
            }
        }
        if (nodeLink == null) {
            nodeLink = factory.createNodeLink()
            nodeLink!!.setNetworkType(networkType)
            nodeNetwork!!.addLink(nodeLink!!)
        }

        try {
            nodeLink!!.setEstimatedRate(weight)
        } catch (e: Exception) {
            logger.debug("Unexpected estimate rate", e)
        }

        /* Found Property and SET remote IP */
        var prop = nodeLink!!.findNetworkPropertiesByID(key)
        if (prop == null) {
            prop = factory.createNetworkProperty()
            prop!!.setName(key)
            nodeLink!!.addNetworkProperties(prop!!)
        }
        prop!!.setValue(value)
        prop!!.setLastCheck(Date().getTime().toString())
        logger.debug("New node link prop registered = " + targetNodeName + "," + key + "," + value)
        return thisNodeFound!!
    }
}