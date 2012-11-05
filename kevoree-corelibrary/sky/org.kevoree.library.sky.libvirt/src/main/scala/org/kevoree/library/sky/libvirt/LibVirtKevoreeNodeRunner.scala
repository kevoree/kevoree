package org.kevoree.library.sky.libvirt

import org.kevoree.library.sky.api.KevoreeNodeRunner
import org.kevoree.ContainerRoot
import org.kevoree.library.sky.api.nodeType.AbstractHostNode
import org.libvirt.{LibvirtException, Domain, Network, Connect}
import scala.xml._
import org.jsoup._
import select.Elements
import java.util.UUID

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/10/12
 * Time: 18:27
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class LibVirtKevoreeNodeRunner(nodeName : String, iaasNode: AbstractHostNode) extends KevoreeNodeRunner (nodeName)  {
  def startNode (iaasModel: ContainerRoot, childBootStrapModel: ContainerRoot) = {
    // Create the connection
    var conn: Connect = null
    val testNetwork: Network = null

    try {
      conn = new Connect("qemu:///system", false)
    } catch {
      case e: LibvirtException => println(e.getError)
    }

    try {
      val domain: Domain = conn.domainLookupByName(conn.getHostName())
      XML.loadString(domain.getXMLDesc(0))

      /* Change attributes */
      val doc = Jsoup.parse(domain.getXMLDesc(0))

      var content: Elements = doc.getElementsByTag("name")
      content.html("Name")

      content = doc.getElementsByTag("uuid")
      content.html(UUID.randomUUID().toString)

      content = doc.getElementsByTag("memory")
      content.html("1024")

      content = doc.getElementsByTag("currentMemory")
      content.html("1024")

      content = doc.getElementsByTag("vCPU")
      content.html("1")

      content = doc.getElementsByTag("os>type")
      content.attr("arch", "i386")

      content = doc.getElementsByTag("devices>disk>source")
      content.attr("file", "/tmp")

      /* Save new configuration */
      conn.domainDefineXML(doc.outerHtml())
      println("Fini!")
    } catch {
      case e: LibvirtException => println(e.getError)
    }
    false
  }

  def stopNode () = {
    false
  }
}
