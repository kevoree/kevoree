/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty.group

import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._

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
                
                
                return (newGroup.getTypeDefinition.foundRelevantDeployUnit(node).getHashcode != currentGroup.getTypeDefinition.foundRelevantDeployUnit(node).getHashcode)
            }
            case None => true//STRANGE ERROR wTf  - HaraKiri best effort
          }
      }
      case None => true //INSTANCE WILL BE UNINSTALL
    }
  }
  
}
