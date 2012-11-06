package org.kevoree.library.sky.libvirt

import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.libvirt.Connect
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/10/12
 * Time: 18:27
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class LibVirtKvmKevoreeNodeRunner (nodeName: String, iaasNode: AbstractHostNode, conn: Connect) extends LibVirtKevoreeNodeRunner(nodeName, iaasNode, conn) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  // maybe this must be defined before to be sent to the node (by the manager)
  def cloneDisk (diskPath: String, newDiskPath: String, copyMode: String): Boolean = {
    if (copyMode == "base") {
      val process = Runtime.getRuntime.exec(Array[String]("qemu-img", "-f", "qcow2", "-b", diskPath, newDiskPath))
      if (process.waitFor() != 0) {
        logger.error("Unable to create a disk at '{}' based on '{}'", newDiskPath, diskPath)
        false
      } else {
        true
      }
    } else if (copyMode == "clone") {
      val process = Runtime.getRuntime.exec(Array[String]("cp", diskPath, newDiskPath))
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
