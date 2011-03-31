/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.framework.aspects

import org.kevoree._
import scala.collection.JavaConversions._
import KevoreeAspects._

case class TypeDefinitionAspect(selfTD : TypeDefinition) {

  def isModelEquals(pct : TypeDefinition) : Boolean = {
    pct.getName == selfTD.getName
    /* deep compare */
  }
  
  /* Check if the new type definition define new deploy unit than self */
  def contractChanged(pTD : TypeDefinition) : Boolean = {
    if(pTD.getName != selfTD.getName){return true}
    if(pTD.getFactoryBean != selfTD.getFactoryBean){return true}
    //DICTIONARY TYPE CHECK  
    if(pTD.getDictionaryType != null){
      if(!pTD.getDictionaryType.isModelEquals(selfTD.getDictionaryType)){return true}
    }

    //SPECIAL CONSISTENCY CHECK
    pTD match {
      case otherTD : ComponentType => {
          var selfCT = selfTD.asInstanceOf[ComponentType]
          if(otherTD.getProvided.size != selfCT.getProvided.size){return true}
          if(otherTD.getRequired.size != selfCT.getRequired.size){return true}
      }
      case _ =>
    }
    false
  }

  def isUpdated(pTD : TypeDefinition) : Boolean = {
    if(selfTD.getDeployUnits != null){
      if(pTD.getDeployUnits != null){
        if(pTD.getDeployUnits.size == 0){
          return false
        }
        
        if(selfTD.getDeployUnits.size != pTD.getDeployUnits.size){
          return true
        }
        var allNotUpdate = selfTD.getDeployUnits.forall(selfDU=>{
            pTD.getDeployUnits.find(p=> p.isModelEquals(selfDU)) match {
              case Some(pDU)=> {
                  try{
                    var pDUInteger = java.lang.Long.parseLong(pDU.getHashcode)
                    var selfDUInteger = java.lang.Long.parseLong(selfDU.getHashcode)
                    selfDUInteger > pDUInteger                 
                  } catch {
                    case _@ e => {
                        e.printStackTrace
                        println("Bad HashCode - equiality verification - " +pDU.getHashcode + " - " +selfDU.getHashcode)
                        pDU.getHashcode == selfDU.getHashcode
                        
                    }
                  }
                } 
              case _ => false
            }
          })
        !allNotUpdate   
      } else {
        true
      }
    } else {
      pTD.getDeployUnits != null
    }
  }
  
  
  
  def foundRelevantDeployUnit(node : ContainerNode) = {
    
    /* add all reLib from found deploy Unit*/
    var deployUnitfound : DeployUnit = null
    if(node.getTypeDefinition != null){
      selfTD.getDeployUnits.find(du => du.getTargetNodeType != null && du.getTargetNodeType.getName == node.getTypeDefinition.getName) match {
        case Some(e)=> deployUnitfound = e
        case _ =>
      }
    }
    /*
     if(deployUnitfound == null){
     deployUnitfound = selfTD.getDeployUnits.find(du => du.getTargetNodeType.getName == null).get
     }*/
    if(deployUnitfound == null){
      deployUnitfound = selfTD.getDeployUnits.get(0)
    }
    
    deployUnitfound
    
  }
  

}
