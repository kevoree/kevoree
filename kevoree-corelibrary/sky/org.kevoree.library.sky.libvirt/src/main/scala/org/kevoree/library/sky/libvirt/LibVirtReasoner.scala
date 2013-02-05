package org.kevoree.library.sky.libvirt

import org.libvirt.{Network, Domain}
import org.kevoree.api.service.core.script.KevScriptEngine
import nu.xom.{Element, Builder, Document}
import org.slf4j.{LoggerFactory, Logger}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/11/12
 * Time: 23:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object LibVirtReasoner {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def createNode(domain: Domain, kengine: KevScriptEngine, iaasNode: LibVirtNode) {
    if (domain.isActive == 1) {
      val parser: Builder = new Builder
      val doc: Document = parser.build(domain.getXMLDesc(0), null)
      val nodeName = doc.query("/domain/name").get(0).getValue
      val ram = doc.query("/domain/memory").get(0).getValue
      val cpuCore = doc.query("/domain/vcpu").get(0).getValue
      val disk = doc.query("/domain/devices/disk/source").get(0).asInstanceOf[Element].getAttributeValue("file")
      kengine addVariable("nodeName", nodeName)
      kengine addVariable("ram", ram)
      kengine addVariable("cpuCore", cpuCore)
      kengine addVariable("disk", disk)
      kengine addVariable("parentNodeName", iaasNode.getName)
      kengine append "addNode {nodeName} : PLibVirtNode { RAM = '{ram}', CPU_CORE = '{cpuCore}', DISK = '{disk}' }"
      kengine append "addChild {nodeName}@{parentNodeName}"
      logger.debug("try to add a VM as node with name {}", nodeName)
    }
  }

  def updateNetwork(network: Network, kengine: KevScriptEngine, iaasNode: LibVirtNode) {
    logger.debug("Try to update the network configuration according to the network properties of libvirt")
    val parser: Builder = new Builder
    val doc: Document = parser.build(network.getXMLDesc(0), null)
    val hosts = doc.query("/network/ip/dhcp/host")
    for (i <- 0 to hosts.size() - 1) {
      kengine addVariable("nodeName", hosts.get(i).asInstanceOf[Element].getAttributeValue("name"))
      kengine addVariable("ip", hosts.get(i).asInstanceOf[Element].getAttributeValue("ip"))
      kengine append "network {nodeName} => {nodeName} { 'KEVOREE.remote.node.ip' = '{ip}' }"
      logger.debug("try to add IP '{}'for node '{}'", Array[AnyRef](hosts.get(i).asInstanceOf[Element].getAttributeValue("ip"), hosts.get(i).asInstanceOf[Element].getAttributeValue("name")))
    }
  }

}
