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

package org.kevoree.merger.sub

import org.kevoree.merger.Merger
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree._

trait TypeDefinitionMerger extends Merger with DictionaryMerger with PortTypeMerger with DeployUnitMerger {

  //TYPE DEFINITION MERGER ENTRYPOINT
  def mergeTypeDefinition(actualModel : ContainerRoot,modelToMerge : ContainerRoot) : Unit = {
    val cts : List[TypeDefinition] = List()++modelToMerge.getTypeDefinitions.toList
    cts.foreach{toMergeTypeDef=>
      
    //  println(toMergeTypeDef)
      
      actualModel.getTypeDefinitions.find({actualTypeDef=>actualTypeDef.isModelEquals(toMergeTypeDef)}) match {
        case Some(found_type_definition)=> {
            //println(found_type_definition);
            var root = found_type_definition.eContainer.asInstanceOf[ContainerRoot]
            if(found_type_definition.isUpdated(toMergeTypeDef)){
              updateTypeDefinition(found_type_definition,toMergeTypeDef)
            }
            mergeTypeDefinition(root,found_type_definition,toMergeTypeDef)
          }
          //SIMPLE CASE ? JUST MERGE THE NEW TYPE DEFINITION
        case None => mergeNewTypeDefinition(actualModel,toMergeTypeDef)
      }
    }
  }

  
  private def mergeTypeDefinition(root : ContainerRoot,actuelTypeDefinition:TypeDefinition, newTypeDefinition:TypeDefinition) = {
    //UPDATE DEPLOYS UNIT
    //val root = actuelTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
    var allDeployUnits = List() ++newTypeDefinition.getDeployUnits.toList ++actuelTypeDefinition.getDeployUnits.toList  //CLONE LIST
    actuelTypeDefinition.getDeployUnits.clear
    allDeployUnits.foreach{ndu=>
      var merged = mergeDeployUnit(root,ndu)
      if(!actuelTypeDefinition.getDeployUnits.contains(merged)){ actuelTypeDefinition.getDeployUnits.add(merged) }
    }
    
   // println(actuelTypeDefinition)
    //PARTICULAR CASE - CHECK
    if(actuelTypeDefinition.isInstanceOf[NodeType]){
      root.getDeployUnits.foreach{du=>
    //    println(du)
        
        if(du.getTargetNodeType!= null && du.getTargetNodeType.getName==actuelTypeDefinition.getName){
          println("ok")
          du.setTargetNodeType(actuelTypeDefinition.asInstanceOf[NodeType])
        }
      }
      
    }
    
    
    
  }
  
  /* This method try to update */
  private def updateTypeDefinition(actuelTypeDefinition:TypeDefinition, newTypeDefinition:TypeDefinition) = {
    val root = actuelTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
    val otherRoot = newTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
    
   // org.eclipse.emf.ecore.util.EcoreUtil.replace(actuelTypeDefinition, newTypeDefinition)
    
    
    //REMOVE OLD AND ADD NEW TYPE
    root.getTypeDefinitions.remove(actuelTypeDefinition)
    mergeNewTypeDefinition(root,newTypeDefinition)

    //UPDATE LIBRARIES
    root.getLibraries.filter(p=> p.getSubTypes.contains(actuelTypeDefinition) ).foreach{lib=>
      lib.getSubTypes.remove(actuelTypeDefinition);lib.getSubTypes.add(newTypeDefinition)
    }
    
    //PARTICULAR CASE - CHECK
    if(actuelTypeDefinition.isInstanceOf[NodeType]){
      root.getDeployUnits.foreach{du=>
        if(du.getTargetNodeType!= null && du.getTargetNodeType.getName==actuelTypeDefinition.getName){
          du.setTargetNodeType(newTypeDefinition.asInstanceOf[NodeType])
        }
      }
    }

    //PROCESS INSTANCE
    val listInstance = root.eAllContents.filter(p=>{
        p match {
          case i : Instance => i.getTypeDefinition == actuelTypeDefinition
          case _ => false
        }
      }).toList ++ List()
    listInstance.foreach{instance=>

      val art2instance = instance.asInstanceOf[Instance]
      art2instance.setTypeDefinition(newTypeDefinition)

      //MERGE DICTIONARY
      mergeDictionary(art2instance.getDictionary,newTypeDefinition.getDictionaryType)

      //SPECIFIC PROCESS
      art2instance match {
        
        
        
        case c : ComponentInstance => {
            val ct = newTypeDefinition.asInstanceOf[ComponentType]

            //MERGE PORT
            val providedPort = c.getProvided.toList ++ List()
            providedPort.foreach{pport=>
              ct.getProvided.find(p=> p.getName == pport.getPortTypeRef.getName) match {
                case None => pport.removeAndUnbind
                case Some(ptref)=> pport.setPortTypeRef(ptref)
              }
            }
            val requiredPort = c.getRequired.toList ++ List()
            requiredPort.foreach{rport=>
              ct.getRequired.find(p=> p.getName == rport.getPortTypeRef.getName) match {
                case None => rport.removeAndUnbind
                case Some(ptref)=> rport.setPortTypeRef(ptref)
              }
            }

            //CREATE NEW PORT
            ct.getProvided.foreach{newpt=> 
              c.getProvided.find(p=>p.getPortTypeRef == newpt) match {
                case None => {
                    val newport = KevoreeFactory.eINSTANCE.createPort();
                    newport.setPortTypeRef(newpt)
                    c.getProvided.add(newport)
                  }
                case Some(p)=> //OK PORT ALREADY EXIST
              }
            }
            ct.getRequired.foreach{newpt=>
              c.getRequired.find(p=>{p.getPortTypeRef == newpt}) match {
                case None => {
                    val newport = KevoreeFactory.eINSTANCE.createPort();
                    newport.setPortTypeRef(newpt)
                    c.getRequired.add(newport)
                  }
                case Some(p)=> //OK PORT ALREADY EXIST
              }
            }

          }
        case _ => //NO SPECIFIC PROCESS FOR OTHER INSTANCE
      }

    }

  }

  /* MERGE A SIMPLE NEW TYPE DEFINITION */
  private def mergeNewTypeDefinition(actualModel : ContainerRoot, newTypeDefinition:TypeDefinition) = {

    // println("Merge new type Case =>"+newTypeDefinition)

    //MERGE TYPE REQUIRED LIB DEPRECATED 
    /*
     val etp : List[DeployUnit] = List() ++ newTypeDefinition.getRequiredLibs
     newTypeDefinition.getRequiredLibs.clear
     etp.foreach{loopTP=>
     newTypeDefinition.getRequiredLibs.add(mergeDeployUnit(actualModel,loopTP))
     }*/
    
    //MERGE TYPE DEPLOY UNITS
    var newTypeDefinitionDeployUnits = List() ++newTypeDefinition.getDeployUnits.toList //CLONE LIST
    newTypeDefinition.getDeployUnits.clear
    newTypeDefinitionDeployUnits.foreach{ndu=>
      newTypeDefinition.getDeployUnits.add(mergeDeployUnit(actualModel,ndu))
    }
    
    //MERGE TARGET NODE TYPE
    
    
 //   println("ok"+newTypeDefinition)
    
    
    /* DEPRECATED BEFORE DEPLOYTS UNITS TO N
     if(newTypeDefinition.getDeployUnit != null){
     newTypeDefinition.setDeployUnit(mergeDeployUnit(actualModel,newTypeDefinition.getDeployUnit))
     }*/

    //ADD RECUSIVE DEFINITON TO ROOT
    newTypeDefinition match {
      case ct : ChannelType => { actualModel.getTypeDefinitions.add(ct) }
      case ct : ComponentType => {
          actualModel.getTypeDefinitions.add(ct)
          ct.getProvided.foreach{ptref=>ptref.setRef(mergePortType(actualModel,ptref.getRef))}
          ct.getRequired.foreach{ptref=>ptref.setRef(mergePortType(actualModel,ptref.getRef))}
        }
      case nt : NodeType => { actualModel.getTypeDefinitions.add(nt) }
      case gt : GroupType => { actualModel.getTypeDefinitions.add(gt) }
      case pt : PortType => { /*println("PORTTYPE M ?? "+pt.toString)*//* MERGE BY COMPONENT TYPE */ }
      case _ @ msg => println("Error uncatch type") // NO RECURSIVE FOR OTHER TYPE
    }



  }

}
