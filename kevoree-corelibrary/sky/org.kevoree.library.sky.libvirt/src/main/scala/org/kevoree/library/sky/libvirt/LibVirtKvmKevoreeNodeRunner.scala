package org.kevoree.library.sky.libvirt

import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.libvirt.Connect
import org.slf4j.LoggerFactory
import org.kevoree.library.sky.api.ProcessStreamFileLogger
import java.io.File
import nu.xom.{Document, Attribute, Element, Builder}
import org.kevoree.framework.KevoreePropertyHelper
import org.kevoree.{ContainerNode, ContainerRoot}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/10/12
 * Time: 18:27
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class LibVirtKvmKevoreeNodeRunner(nodeName: String, iaasNode: AbstractHostNode, conn: Connect) extends LibVirtKevoreeNodeRunner(nodeName, iaasNode, conn) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def createXMLDomain(iaasModel: ContainerRoot, childBootStrapModel: ContainerRoot): Document = {

    val domain = conn.domainLookupByName(iaasNode.getDictionary.get("defaultdomain").toString)
    val parser: Builder = new Builder
    val doc = parser.build(domain.getXMLDesc(0), null)
    iaasModel.findByQuery("nodes[" + iaasNode.getName + "]/hosts[" + nodeName + "]", classOf[ContainerNode]) match {
      case node:ContainerNode => {
        // look for the hard drive disk, check if it is already in use and clone it if needed
        val diskOption = KevoreePropertyHelper.getProperty(node, "DISK")
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
        val copyModeOption = KevoreePropertyHelper.getProperty(node, "COPY_MODE")
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
          true
        } else {
          logger.error("Unable to start the VM because the disk cannot be defined correctly")
          false
        }
      }
      case null =>
    }
    doc
  }

  // maybe this must be defined before to be sent to the node (by the manager)
  def cloneDisk(diskPath: String, newDiskPath: String, copyMode: String): Boolean = {
    if (new File(newDiskPath).exists()) {
      new File(newDiskPath).delete()
    }
    if (copyMode == "base") {
      val process = Runtime.getRuntime.exec(Array[String]("qemu-img", "create", "-f", "qcow2", "-b", diskPath, newDiskPath))
      // FIXME maybe we need to have a better thread management
      val outFile = File.createTempFile("qemu-img-" + diskPath.substring(diskPath.lastIndexOf("/") + 1) + "-" + newDiskPath.substring(newDiskPath.lastIndexOf("/") + 1), "clone.out")
      outFile.deleteOnExit()
      val errFile = File.createTempFile("qemu-img-" + diskPath.substring(diskPath.lastIndexOf("/") + 1) + "-" + newDiskPath.substring(newDiskPath.lastIndexOf("/") + 1), "clone.err")
      errFile.deleteOnExit()
      new Thread(new ProcessStreamFileLogger(process.getInputStream, outFile)).start()
      new Thread(new ProcessStreamFileLogger(process.getErrorStream, errFile)).start()
      if (process.waitFor() != 0) {
        logger.error("Unable to create a disk at '{}' based on '{}'", newDiskPath, diskPath)
        false
      } else {
        true
      }
    } else if (copyMode == "clone") {
      val process = Runtime.getRuntime.exec(Array[String]("cp", diskPath, newDiskPath))
      val outFile = File.createTempFile("cp-" + diskPath.substring(diskPath.lastIndexOf("/") + 1) + "-" + newDiskPath.substring(newDiskPath.lastIndexOf("/") + 1), "clone.out")
      outFile.deleteOnExit()
      val errFile = File.createTempFile("cp-" + diskPath.substring(diskPath.lastIndexOf("/") + 1) + "-" + newDiskPath.substring(newDiskPath.lastIndexOf("/") + 1), "clone.err")
      errFile.deleteOnExit()
      new Thread(new ProcessStreamFileLogger(process.getInputStream, outFile)).start()
      new Thread(new ProcessStreamFileLogger(process.getErrorStream, errFile)).start()
      if (process.waitFor() != 0) {
        logger.error("Unable to create a disk at '{}' as a clone of '{}'", newDiskPath, diskPath)
        false
      } else {
        true
      }
    } else if (copyMode == "as_is") {
      true
    } else {
      logger.error("Unknown copy mode: {}", copyMode)
      false
    }

  }
}
