package org.kevoree.experiment.smartForest.dpa

import org.kevoree.library.tools.dpa.DPA
import org.kevoree.{ContainerRoot, NamedElement}
import scala.collection.JavaConversions._
import java.util.{HashMap, ArrayList, Map}
import org.kevoree.tools.marShell.ast.{ComponentInstanceID, RemoveComponentInstanceStatment, TransactionalBloc, Script}

/**
 * User: ffouquet
 * Date: 18/07/11
 * Time: 17:11
 */

object RemoveComponentDPAO {
   val componentName: String = "component"
   val nodeName: String = "node"
}

class RemoveComponentDPA extends DPA {

  final val templateScript: String = "tblock { \n" + "  removeComponent ${component}@${node}\n" + "}"

  def applyPointcut(myModel: ContainerRoot): java.util.List[Map[String, NamedElement]] = {
    val results: java.util.List[Map[String, NamedElement]] = new java.util.ArrayList[Map[String, NamedElement]]()
    for (containerNode <- myModel.getNodes) {
      for (componentInstance <- containerNode.getComponents) {
        val myMap: Map[String, NamedElement] = new HashMap[String, NamedElement]
        myMap.put(RemoveComponentDPAO.componentName, componentInstance.asInstanceOf[NamedElement])
        myMap.put(RemoveComponentDPAO.nodeName, containerNode.asInstanceOf[NamedElement])
        results.add(myMap)
      }
    }
    results
  }

  def getScript(myMap: java.util.Map[String, NamedElement]): String = {
    var script: String = templateScript
    for (name <- myMap.keySet) {
      script = script.replace("${" + name + "}", myMap.get(name).getName)
    }
    return script
  }

  def getASTScript(myMap: java.util.Map[String, NamedElement]): Script = {
    Script(
      List(
        TransactionalBloc(
          List(
            RemoveComponentInstanceStatment(
              ComponentInstanceID(myMap.get(RemoveComponentDPAO.componentName).getName, Some(myMap.get(RemoveComponentDPAO.nodeName).getName))
            )
          )
        )
      ))

  }


}