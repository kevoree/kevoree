package org.kevoree.library.sky.jails

import nodeType.JailNode
import org.kevoree.api.service.core.script.KevScriptEngine

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/11/12
 * Time: 23:45
 *
 * @author Erwan Daubert
 * @version 1.0
 */
object JailsReasoner {

  def createNodes(kengine : KevScriptEngine, iaasNode : JailNode) {

  }

  def createNode (kengine: KevScriptEngine, iaasNode: JailNode) {
    // TODO use ezjail-admin list and parse the result to get the IP and the name of the jail and the id
    // TODO list the available constraints for each jail's id to get CPU, RAM
      /*val parser: Builder = new Builder
      val doc: Document = parser.build(domain.getXMLDesc(0), null)
      val nodeName = doc.query("/domain/name").get(0).getValue
      val ram = doc.query("/domain/memory").get(0).getValue
      val cpuCore = doc.query("/domain/vcpu").get(0).getValue
      val disk = doc.query("/domain/disk/source").get(0).getValue
      kengine addVariable("nodeName", nodeName)
      kengine addVariable("ram", ram)
      kengine addVariable("cpuCore", cpuCore)
      kengine addVariable("disk", disk)
      kengine addVariable("parentNodeName", iaasNode.getName)
      kengine append "addNode {nodeName} : PLibVirtNode { RAM = '{ram}', CPU_CORE = '{cpuCore}', DISK = '{disk}' }"
      kengine append "addChild {nodeName}@{parentName}"*/
    }

}
