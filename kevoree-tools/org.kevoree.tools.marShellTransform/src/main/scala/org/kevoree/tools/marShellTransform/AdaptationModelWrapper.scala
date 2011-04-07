package org.kevoree.tools.marShellTransform

import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.ast._
import org.kevoree.{ComponentInstance, ContainerNode}
import org.kevoreeAdaptation._

object AdaptationModelWrapper {

  def generateScriptFromAdaptModel(model: AdaptationModel): Script = {
    val statments = new java.util.ArrayList[Statment]()
    model.getAdaptations.foreach {
      adapt =>
        adapt match {
          case statement: AddBinding => statments.add(AddBindingStatment(ComponentInstanceID(statement.getRef.getPort.eContainer.asInstanceOf[ComponentInstance].getName, Some(statement.getRef.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)), statement.getRef.getPort.getPortTypeRef.getName, statement.getRef.getHub.getName))
          case statement: AddDeployUnit => //NOOP
          case statement: AddFragmentBinding => //NOOP
          case statement: AddInstance => {
             statement.getRef match {
               case c : ComponentInstance =>{
                 val cid =  ComponentInstanceID(c.getName, Some(c.eContainer.eContainer.asInstanceOf[ContainerNode].getName))
                 val props = new java.util.Properties
                 if(c.getDictionary != null){
                   c.getDictionary.getValues.foreach{value=>
                        props.put(value.getAttribute.getName,value.getValue)
                   }
                 }
                 statments.add(AddComponentInstanceStatment(cid,c.getTypeDefinition.getName,props))
               }
                 //TODO
               case _ @ uncatchInstance => println("warning : uncatched=" + uncatchInstance)
             }
          }
          case statement: AddThirdParty => //NOOP
          case statement: AddType => //NOOP
          case statement: RemoveBinding => statments.add(RemoveBindingStatment(ComponentInstanceID(statement.getRef.getPort.eContainer.asInstanceOf[ComponentInstance].getName, Some(statement.getRef.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)), statement.getRef.getPort.getPortTypeRef.getName, statement.getRef.getHub.getName))
          case statement: RemoveInstance => {
            statement.getRef match {
              //TODO

              case _ @ uncatchInstance => println("warning : uncatched=" + uncatchInstance)
            }
          }
          case _@unCatched => println("warning : uncatched=" + unCatched)
        }

    }
    Script(List(Block(statments.toList)))
  }

}
