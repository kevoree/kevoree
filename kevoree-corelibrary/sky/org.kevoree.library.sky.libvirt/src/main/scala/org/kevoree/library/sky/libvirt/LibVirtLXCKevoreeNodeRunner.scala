package org.kevoree.library.sky.libvirt

import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.libvirt.Connect
import org.slf4j.LoggerFactory
import org.kevoree.ContainerRoot
import nu.xom.{Document, Builder}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 14/11/12
 * Time: 18:13
 */
class LibVirtLXCKevoreeNodeRunner (nodeName: String, iaasNode: AbstractHostNode, conn: Connect) extends LibVirtKevoreeNodeRunner(nodeName, iaasNode, conn) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def createXMLDomain(iaasModel: ContainerRoot, childBootStrapModel: ContainerRoot) = {
    val parser: Builder = new Builder
    val doc : Document = parser.build(this.getClass.getClassLoader.getResourceAsStream("lxc.xml"));
    doc
  }

}
