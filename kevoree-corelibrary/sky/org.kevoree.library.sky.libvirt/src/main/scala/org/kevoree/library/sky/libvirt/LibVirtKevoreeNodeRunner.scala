package org.kevoree.library.sky.libvirt

import org.kevoree.library.sky.api.KevoreeNodeRunner
import org.kevoree.ContainerRoot
import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.libvirt.{LibvirtException, Domain, Connect}
import java.util.UUID
import org.slf4j.LoggerFactory
import java.io.IOException
import nu.xom._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/10/12
 * Time: 18:27
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class LibVirtKevoreeNodeRunner (nodeName: String, iaasNode: AbstractHostNode) extends KevoreeNodeRunner(nodeName) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private var nodeDomain : Domain = null

  def startNode (iaasModel: ContainerRoot, childBootStrapModel: ContainerRoot) = {
    try {
      val conn = new Connect("qemu:///system", false)

      val domain: Domain = conn.domainLookupByName("debian_base")
      // TODO check if the domain exist before to continue

      val parser: Builder = new Builder
      val doc: Document = parser.build(domain.getXMLDesc(0), null)

      val nameElement: Element = doc.query("/domain/name").get(0).asInstanceOf[Element]
      nameElement.removeChildren()
      nameElement.appendChild(nodeName)

      val uuidElement: Element = doc.query("/domain/uuid").get(0).asInstanceOf[Element]
      uuidElement.removeChildren()
      uuidElement.appendChild(UUID.randomUUID.toString)


      // TODO look for the amount of memory
      val memory = "1048576"
      val memoryElement: Element = doc.query("/domain/memory").get(0).asInstanceOf[Element]
      memoryElement.removeChildren()
      memoryElement.appendChild(memory)

      val currentMemoryElement: Element = doc.query("/domain/currentMemory").get(0).asInstanceOf[Element]
      currentMemoryElement.removeChildren()
      currentMemoryElement.appendChild(memory)

      // TODO look for the number of CPU
      val nbCPU = "1"
      val vCPUElement: Element = doc.query("/domain/vcpu").get(0).asInstanceOf[Element]
      vCPUElement.removeChildren()
      vCPUElement.appendChild(nbCPU)

      // TODO look for the type of the architecture
      val arch = "i686"
      val typeElement: Element = doc.query("/domain/os/type").get(0).asInstanceOf[Element]
      typeElement.removeAttribute(typeElement.getAttribute("arch"))
      val archAttribute: Attribute = new Attribute("arch", arch)
      typeElement.addAttribute(archAttribute)

      // TODO look for the hard drive disk, check if it is already in use and clone it if needed
      val file = "/home/edaubert/Public/vms/debian_base.toto.qcow2"
      val sourceElement: Element = doc.query("/domain/devices/disk/source").get(0).asInstanceOf[Element]
      sourceElement.removeAttribute(sourceElement.getAttribute("file"))
      val fileAttribute: Attribute = new Attribute("file", file)
      sourceElement.addAttribute(fileAttribute)

      System.out.println(doc.toXML)
      nodeDomain = conn.domainDefineXML(doc.toXML)
      nodeDomain.create()
    }
    catch {
      case e: LibvirtException => {
        logger.debug("Unable to create or start the VM", e)
      }
      case e: ValidityException => {
        logger.debug("Domain definition is not valid", e)
      }
      case e: ParsingException => {
        logger.debug("Domain definition is not valid", e)
      }
      case e: IOException => {
        logger.debug("Unknown exception", e)
      }
    }
    false
  }

  def stopNode () = {
    false
  }
}
