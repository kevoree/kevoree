package org.kevoree.library.sky.libvirt

import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.libvirt.Connect
import org.slf4j.LoggerFactory
import org.kevoree.library.sky.api.ProcessStreamFileLogger
import java.io.File

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
