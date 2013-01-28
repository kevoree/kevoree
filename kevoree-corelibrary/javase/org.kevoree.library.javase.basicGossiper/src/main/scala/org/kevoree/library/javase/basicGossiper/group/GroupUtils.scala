package org.kevoree.library.javase.basicGossiper.group

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.kevoree.ContainerRoot
import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._

object GroupUtils {

  def detectHaraKiri(newModel:ContainerRoot,oldModel:ContainerRoot,instanceGroupName:String,nodeName:String) : Boolean={
    //SEARCH FOR NEW GROUP INSTANCE IN NEW MODEL
    newModel.getGroups.find(group => group.getName==instanceGroupName && group.getSubNodes.exists(sub=>sub.getName == nodeName)) match {
      case Some(newGroup)=> {
          //SEARCH FOR ACTUAL GROUP
          oldModel.getGroups.find(group => group.getName==instanceGroupName && group.getSubNodes.exists(sub=>sub.getName == nodeName)) match {
            case Some(currentGroup)=> {
                //TYPE DEF HASHCODE COMPARE
                val node = newModel.getNodes.find(node=>node.getName==nodeName).get
                
                
                newGroup.getTypeDefinition.foundRelevantDeployUnit(node).getHashcode != currentGroup.getTypeDefinition.foundRelevantDeployUnit(node).getHashcode
            }
            case None => true//STRANGE ERROR wTf  - HaraKiri best effort
          }
      }
      case None => true //INSTANCE WILL BE UNINSTALL
    }
  }
  
}
