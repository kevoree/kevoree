package org.kevoree.library.sky.libvirt

import org.kevoree.library.sky.api.KevoreeNodeRunner
import org.kevoree.ContainerRoot
import org.libvirt.{Network, Connect, LibvirtException, Domain}
import nu.xom._
import java.util.UUID
import org.kevoree.framework.{Constants, KevoreePropertyHelper}
import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.slf4j.LoggerFactory
import java.io.IOException
import org.kevoree.library.sky.api.helper.KloudNetworkHelper
import org.kevoree.library.sky.api.property.PropertyConversionHelper

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 06/11/12
 * Time: 18:38
 *
 * @author Erwan Daubert
 * @version 1.0
 */
abstract class LibVirtKevoreeNodeRunner (nodeName: String, iaasNode: AbstractHostNode, conn: Connect) extends KevoreeNodeRunner(nodeName) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private var nodeDomain: Domain = null


  def cloneDisk (distPath: String, newDiskPath: String, copy_mode: String): Boolean

  def startNode (iaasModel: ContainerRoot, childBootStrapModel: ContainerRoot) = {
    logger.debug("Starting " + nodeName)
    // look at the already defined domain to see if the domain is already defined but not started
    try {
      val domain: Domain = conn.domainLookupByName(nodeName)
      if (domain.isActive == 0) {
        domain.create()
        true
      } else if (domain.isActive == 1) {
        logger.debug("The VM '{}' is already defined and is running ", nodeName)
        true
      } else {
        logger.error("A VM is already defined with the same name ({}) and is running or not available (libvirt isActive code = {})", nodeName, domain.isActive)
        false
      }
    } catch {
      case e: LibvirtException => {
        try {
          val domain = conn.domainLookupByName("debian_base")

          val parser: Builder = new Builder
          val doc: Document = parser.build(domain.getXMLDesc(0), null)

          val nameElement: Element = doc.query("/domain/name").get(0).asInstanceOf[Element]
          nameElement.removeChildren()
          nameElement.appendChild(nodeName)

          val uuidElement: Element = doc.query("/domain/uuid").get(0).asInstanceOf[Element]
          uuidElement.removeChildren()
          uuidElement.appendChild(UUID.randomUUID.toString)


          //look for the amount of memory
          val memoryOption = KevoreePropertyHelper.getStringPropertyForNode(iaasModel, nodeName, "RAM")
          val defaultMemory = iaasNode.getDictionary.get("default_RAM")
          var memory = "1024000"
          if (defaultMemory != null) {
            memory = PropertyConversionHelper.getRAM(defaultMemory.toString).toString
          }
          if (memoryOption.isDefined) {
            memory = PropertyConversionHelper.getRAM(memoryOption.get).toString
          }
          val memoryElement: Element = doc.query("/domain/memory").get(0).asInstanceOf[Element]
          memoryElement.removeChildren()
          memoryElement.appendChild(memory)
          memoryElement.removeAttribute(memoryElement.getAttribute("unit"))
          val unit = new Attribute("unit", "b")
          memoryElement.addAttribute(unit)

          val currentMemoryElement: Element = doc.query("/domain/currentMemory").get(0).asInstanceOf[Element]
          currentMemoryElement.removeChildren()
          currentMemoryElement.appendChild(memory)
          currentMemoryElement.removeAttribute(currentMemoryElement.getAttribute("unit"))
          val unit2 = new Attribute("unit", "b")
          currentMemoryElement.addAttribute(unit2)

          // look for the number of CPU
          val cpuOption = KevoreePropertyHelper.getStringPropertyForNode(iaasModel, nodeName, "CPU_CORE")
          val defaultnbCPU = iaasNode.getDictionary.get("default_CPU_CORE")
          var nbCPU = "1"
          if (defaultnbCPU != null) {
            nbCPU = defaultnbCPU.toString
          }
          if (cpuOption.isDefined) {
            nbCPU = cpuOption.get
          }
          val vCPUElement: Element = doc.query("/domain/vcpu").get(0).asInstanceOf[Element]
          vCPUElement.removeChildren()
          vCPUElement.appendChild(nbCPU)

          // look at the network interface to remove the mac address element allowing the libvirt to dynamically generate a new one
          val interfaceElements: Nodes = doc.query("/domain/devices/interface")
          val nbInterfaces = interfaceElements.size()
          for (i <- 0 to nbInterfaces - 1) {
            val macElement: Element = interfaceElements.get(i).asInstanceOf[Element].query("./mac").get(0).asInstanceOf[Element]
            interfaceElements.get(i).asInstanceOf[Element].removeChild(macElement)
          }


          // TODO look for the type of the architecture
          /*val arch = "i686"
                      val typeElement: Element = doc.query("/domain/os/type").get(0).asInstanceOf[Element]
                      typeElement.removeAttribute(typeElement.getAttribute("arch"))
                      val archAttribute: Attribute = new Attribute("arch", arch)
                      typeElement.addAttribute(archAttribute)*/

          // look for the hard drive disk, check if it is already in use and clone it if needed
          val diskOption = KevoreePropertyHelper.getStringPropertyForNode(iaasModel, nodeName, "DISK")
          val defaultDisk = iaasNode.getDictionary.get("default_DISK")
          var disk = ""
          if (defaultDisk != null) {
            disk = defaultDisk.toString
          }
          if (diskOption.isDefined) {
            disk = diskOption.get
          }
          val sourceElement: Element = doc.query("/domain/devices/disk/source").get(0).asInstanceOf[Element]
          // clone the disk if needed
          val copyModeOption = KevoreePropertyHelper.getStringPropertyForNode(iaasModel, nodeName, "COPY_MODE")
          val defaultCopyMode = iaasNode.getDictionary.get("default_COPY_MODE")
          var copyMode = ""
          if (defaultCopyMode != null) {
            copyMode = defaultCopyMode.toString
          }
          if (copyModeOption.isDefined) {
            copyMode = copyModeOption.get
          }
          var pursue = true
          if (copyMode != "as_is") {
            val newDiskPath = disk.substring(0, disk.lastIndexOf(".")) + nodeName + disk.substring(disk.lastIndexOf("."))
            pursue = cloneDisk(disk, newDiskPath, copyMode)
            disk = newDiskPath
          }

          if (pursue) {
            sourceElement.removeAttribute(sourceElement.getAttribute("file"))
            val fileAttribute: Attribute = new Attribute("file", disk)
            sourceElement.addAttribute(fileAttribute)

            nodeDomain = conn.domainDefineXML(doc.toXML)
            createIP(conn, domain, iaasModel)
            nodeDomain.create()
            true
          } else {
            logger.error("Unable to start the VM because the disk cannot be defined correctly")
            false
          }

        } catch {
          case e: Throwable => logger.error("Unable to create or start the VM {}", nodeName, e); false
        }
      }
    }
  }

  def stopNode () = {
    if (nodeDomain != null && nodeDomain.isActive == 1) {
      try {
        nodeDomain.destroy()
        true
      } catch {
        case e: LibvirtException => logger.error("Unable to stop the VM {}", nodeName, e); false
      }
    } else {
      true
    }
  }

  private def createIP (conn: Connect, domain: Domain, model: ContainerRoot) {
    try {
      val parser: Builder = new Builder
      val domainDoc: Document = parser.build(domain.getXMLDesc(0), null)
      val net: Network = conn.networkLookupByName("default")
      val doc: Document = parser.build(net.getXMLDesc(0), null)

      val ipOption = KevoreePropertyHelper.getStringNetworkProperty(model, domain.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)

      if (ipOption.isDefined && ipOption.get != "127.0.0.1") {
        val ip = ipOption.get
        val networks: Nodes = doc.query("/network")
        for (i <- 0 to networks.size - 1) {
          val name: Element = networks.get(i).query("./name").get(0).asInstanceOf[Element]
          if (name.getValue == "default") {
            val dhcp: Element = networks.get(i).query("./ip/dhcp").get(0).asInstanceOf[Element]
            val host: Element = new Element("host")
            val ipAttribute: Attribute = new Attribute("ip", ip)
            val nameAttribute: Attribute = new Attribute("name", domain.getName)
            val mac: Element = domainDoc.query("/domain/devices/interface/mac").get(0).asInstanceOf[Element]
            val address: Attribute = mac.getAttribute("address")
            val macAttribute: Attribute = new Attribute("mac", address.getValue)
            host.addAttribute(ipAttribute)
            host.addAttribute(nameAttribute)
            host.addAttribute(macAttribute)
            dhcp.appendChild(host)
          }
        }
        net.destroy()
        val network: Network = conn.networkDefineXML(doc.toXML)
        network.create()
      }
    } catch {
      case e: Throwable => {
        logger.error("Unable to reconfigure network for VM {}", domain.getName, e)
        throw e
      }
    }

  }

}
