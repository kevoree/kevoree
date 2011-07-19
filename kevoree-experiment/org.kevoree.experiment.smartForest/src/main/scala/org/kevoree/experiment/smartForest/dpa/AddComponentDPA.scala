package org.kevoree.experiment.smartForest.dpa

import org.kevoree.library.tools.dpa.DPA
import java.util.ArrayList
import org.kevoree.{ContainerRoot, NamedElement, ComponentType}
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.ast.TransactionalBloc._
import org.kevoree.tools.marShell.ast.AddComponentInstanceStatment._
import org.kevoree.tools.marShell.ast.ComponentInstanceID._
import org.kevoree.tools.marShell.ast.{ComponentInstanceID, AddComponentInstanceStatment, TransactionalBloc, Script}

/**
 * User: ffouquet
 * Date: 18/07/11
 * Time: 17:43
 */

object AddComponentDPAO {
  val componentName: String = "component"
  def getComponentName = componentName
  val typeDefinition: String = "type"
  def getTypeDefinition = typeDefinition
  val nodeName: String = "node"
  def getNodeName = nodeName
}

class AddComponentDPA extends DPA {


  final val templateScript: String = "tblock { \n" + "  addComponent ${component}@${node} : ${type}\n" + "}"
  private var increment: Int = 0

  def applyPointcut(myModel: ContainerRoot): java.util.List[java.util.Map[String, NamedElement]] = {
    val results: java.util.List[java.util.Map[String, NamedElement]] = new java.util.ArrayList[java.util.Map[String, NamedElement]]
    for (containerNode <- myModel.getNodes) {
      for (typeDef <- myModel.getTypeDefinitions) {
        if (typeDef.isInstanceOf[ComponentType]) {
          val myMap: java.util.Map[String, NamedElement] = new java.util.HashMap[String, NamedElement]
          myMap.put(AddComponentDPAO.typeDefinition, typeDef.asInstanceOf[NamedElement])
          myMap.put(AddComponentDPAO.nodeName, containerNode.asInstanceOf[NamedElement])
          results.add(myMap)
        }
      }
    }
    return results
  }

  def getScript(myMap: java.util.Map[String, NamedElement]): String = {
    var script: String = templateScript
    for (name <- myMap.keySet) {
      val replacedString: String = "${" + name + "}"
      script = script.replace(replacedString, myMap.get(name).getName)
    }
    script = script.replace("${" + AddComponentDPAO.componentName + "}", myMap.get(AddComponentDPAO.typeDefinition).getName + ({
      increment += 1;
      increment
    }))
    script
  }

  def getASTScript(stringNamedElementMap: java.util.Map[String, NamedElement]): Script = {
    Script(
      List(
        TransactionalBloc(
          List(
            AddComponentInstanceStatment(
              ComponentInstanceID(stringNamedElementMap.get(AddComponentDPAO.typeDefinition).getName+ ({increment += 1;increment}), Some(stringNamedElementMap.get(AddComponentDPAO.nodeName).getName)),
              stringNamedElementMap.get(AddComponentDPAO.typeDefinition).getName,
              new java.util.Properties()
            )
          )
        )
      )
    )
  }

}