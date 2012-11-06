package org.kevoree.library.sky.libvirt

import org.kevoree.library.sky.api.KevoreeNodeRunner
import org.kevoree.ContainerRoot
import org.libvirt.{Connect, LibvirtException, Domain}
import nu.xom._
import java.util.UUID
import org.kevoree.framework.KevoreePropertyHelper
import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.slf4j.LoggerFactory

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


          //look for the amount of memory // TODO get the code in jails that help to use GB, etc
          val memoryOption = KevoreePropertyHelper.getStringPropertyForNode(iaasModel, nodeName, "RAM")
          val defaultMemory = iaasNode.getDictionary.get("default_RAM")
          var memory = "1024"
          if (defaultMemory != null) {
            memory = defaultMemory.toString
          }
          if (memoryOption.isDefined) {
            memory = memoryOption.get
          }
          val memoryElement: Element = doc.query("/domain/memory").get(0).asInstanceOf[Element]
          memoryElement.removeChildren()
          memoryElement.appendChild(memory)

          val currentMemoryElement: Element = doc.query("/domain/currentMemory").get(0).asInstanceOf[Element]
          currentMemoryElement.removeChildren()
          currentMemoryElement.appendChild(memory)

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

}
