package org.kevoree.experiment.smartForest.dpa

import org.kevoree.library.tools.dpa.DPA
import org.kevoree.{ContainerRoot, NamedElement, ComponentType}
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.ast.Script._
import org.kevoree.tools.marShell.ast.TransactionalBloc._
import org.kevoree.tools.marShell.ast.RemoveComponentInstanceStatment._
import org.kevoree.tools.marShell.ast.ComponentInstanceID._
import org.kevoree.tools.marShell.ast.{ComponentInstanceID, AddComponentInstanceStatment, TransactionalBloc, Script}

/**
 * User: ffouquet
 * Date: 18/07/11
 * Time: 17:22
 */

class AddForestMonitoringComponentDPA extends DPA {
  final val componentName: String = "component"
  def getComponentName = componentName

  final val typeDefinition: String = "type"
  final val nodeName: String = "node"
  def getNodeName = nodeName

  final val templateScript: String = "tblock { \n" + "  addComponent ${component}@${node} : ${type}\n" + "}"
  private var increment: Int = 0
  var componentTypes: java.util.HashMap[String, NamedElement] = null

  def applyPointcut(myModel: ContainerRoot): java.util.List[java.util.Map[String, NamedElement]] = {
    if (componentTypes == null) {
      componentTypes = new java.util.HashMap[String, NamedElement]
      for (typeDef <- myModel.getTypeDefinitions) {
        if (typeDef.isInstanceOf[ComponentType]) {
          componentTypes.put((typeDef.asInstanceOf[NamedElement]).getName, typeDef.asInstanceOf[NamedElement])
        }
      }
    }
    val results: java.util.List[java.util.Map[String, NamedElement]] = new java.util.ArrayList[java.util.Map[String, NamedElement]]
    for (containerNode <- myModel.getNodes) {
      var existTempSensor: Boolean = false
      var existSmokeSensor: Boolean = false
      var existHumiditySensor: Boolean = false
      for (myInstance <- containerNode.getComponents) {
        if ((myInstance.getTypeDefinition.asInstanceOf[NamedElement]).getName.equalsIgnoreCase("TempSensor")) {
          existTempSensor = true
        }
        if ((myInstance.getTypeDefinition.asInstanceOf[NamedElement]).getName.equalsIgnoreCase("SmokeSensor")) {
          existSmokeSensor = true
        }
        if ((myInstance.getTypeDefinition.asInstanceOf[NamedElement]).getName.equalsIgnoreCase("HumiditySensor")) {
          existHumiditySensor = true
        }
      }
      if (!existHumiditySensor) {
        val myMap: java.util.Map[String, NamedElement] = new java.util.HashMap[String, NamedElement]
        myMap.put(this.typeDefinition, componentTypes.get("HumiditySensor"))
        myMap.put(this.nodeName, containerNode.asInstanceOf[NamedElement])
        results.add(myMap)
      }
      if (!existSmokeSensor) {
        val myMap: java.util.Map[String, NamedElement] = new java.util.HashMap[String, NamedElement]
        myMap.put(this.typeDefinition, componentTypes.get("SmokeSensor"))
        myMap.put(this.nodeName, containerNode.asInstanceOf[NamedElement])
        results.add(myMap)
      }
      if (!existTempSensor) {
        val myMap: java.util.Map[String, NamedElement] = new java.util.HashMap[String, NamedElement]
        myMap.put(this.typeDefinition, componentTypes.get("TempSensor"))
        myMap.put(this.nodeName, containerNode.asInstanceOf[NamedElement])
        results.add(myMap)
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
    script = script.replace("${" + this.componentName + "}", myMap.get(this.typeDefinition).getName + ({
      increment += 1;
      increment
    }))
    return script
  }

  def getASTScript(myMap: java.util.Map[String, NamedElement]): Script = {
    Script(
      List(
        TransactionalBloc(
          List(
            AddComponentInstanceStatment(
              ComponentInstanceID(myMap.get(componentName).getName, Some(myMap.get(nodeName).getName)),
              myMap.get(typeDefinition).getName,
              new java.util.Properties()
            )
          )
        )
      )
    )
  }


}