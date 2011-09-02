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

package org.kevoree.kompare.sub

import org.kevoree._
import org.kevoreeAdaptation._
import org.kevoreeAdaptation._
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._

trait UpdateNodeKompare extends AbstractKompare with UpdateChannelKompare {

  def getUpdateNodeAdaptationModel(actualNode: ContainerNode, updateNode: ContainerNode): AdaptationModel = {
    val adaptationModel = org.kevoreeAdaptation.KevoreeAdaptationFactory.eINSTANCE.createAdaptationModel
    logger.info("UPDATE NODE " + actualNode.getName)


    val actualRoot = actualNode.eContainer.asInstanceOf[ContainerRoot]
    val updateRoot = updateNode.eContainer.asInstanceOf[ContainerRoot]

    //Update Type Step
    updateNode.getUsedTypeDefinition.foreach {
      uct =>
      actualNode.getUsedTypeDefinition.find({
          act => act.isModelEquals(uct)
        }) match {
        case Some(ct) => {
            //CHECK IF TYPE IS UPDATE
            if (ct.isUpdated(uct)) {
              val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createUpdateType
              adaptcmd.setRef(uct)
              adaptationModel.getAdaptations.add(adaptcmd)

              //ADD UPDATE DEPLOY UNIT IF NECESSARY
              var uctDeployUnit = uct.foundRelevantDeployUnit(updateNode)
              
              adaptationModel.getAdaptations.filter(adaptation => adaptation.isInstanceOf[UpdateDeployUnit]).find(adaptation => adaptation.asInstanceOf[UpdateDeployUnit].getRef.isModelEquals(uctDeployUnit)) match {
                case None => {
                    val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createUpdateDeployUnit
                    ctcmd.setRef(uctDeployUnit)
                    adaptationModel.getAdaptations.add(ctcmd)
                  }
                case Some(e) => //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
              }

            }
          }
        case None => {
            //ADD TYPE
            val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAddType
            ctcmd.setRef(uct)
            adaptationModel.getAdaptations.add(ctcmd)

            /* add deploy unit if necessary */
            //CHECK IF A PREVIOUS INSTALLED TYPE DEFINITION USE THIS DEPLOY UNIT
            
            var uctDeployUnit = uct.foundRelevantDeployUnit(updateNode)
            
            actualNode.getUsedTypeDefinition.find(typeDef => typeDef.foundRelevantDeployUnit(actualNode).isModelEquals(uctDeployUnit)) match {
              case None => {
                  //CHECK IF THIS DEPLOY UNIT IS ALREADY MARK AS TO BE INSTALLED
                  adaptationModel.getAdaptations.filter(adaptation => adaptation.isInstanceOf[AddDeployUnit]).find(adaptation => adaptation.asInstanceOf[AddDeployUnit].getRef.isModelEquals(uctDeployUnit)) match {
                    case None => {
                        var ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAddDeployUnit
                        ctcmd.setRef(uctDeployUnit)
                        adaptationModel.getAdaptations.add(ctcmd)
                      }
                    case Some(e) => //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
                  }
                }
              case Some(_) => // TYPE DEFINITION ALREADY USE DEPLOY UNIT IN PREVIOUS MODEL
            }


            //ADD USED THIRDPARTY
            uctDeployUnit.getRequiredLibs.foreach {
              tp =>
              var adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createAddThirdParty
              adaptcmd.setRef(tp)
              adaptationModel.getAdaptations.add(adaptcmd)
            }
          }
      }
    }
    actualNode.getUsedTypeDefinition.foreach {
      act =>
      updateNode.getUsedTypeDefinition.find({
          uct => uct.isModelEquals(act)
        }) match {
        case Some(ct) => //OK CHECK ALEADY DONE IN PREVIOUS STEP
        case None => {

            //ADD TYPE
            var ctcmd = KevoreeAdaptationFactory.eINSTANCE.createRemoveType
            ctcmd.setRef(act)
            adaptationModel.getAdaptations.add(ctcmd)

            var actualDeployU = act.foundRelevantDeployUnit(actualNode)
            
            /* remove deploy unit if necessary */
            updateNode.getUsedTypeDefinition.find(typeDef => (typeDef != act) && typeDef.foundRelevantDeployUnit(updateNode).isModelEquals(actualDeployU)) match {
              case Some(_) => // DO NOT UNINSTALL DEPLOY UNIT
              case None => {
                  adaptationModel.getAdaptations.filter(adaptation => adaptation.isInstanceOf[RemoveDeployUnit]).find(adaptation => adaptation.asInstanceOf[RemoveDeployUnit].getRef.isModelEquals(actualDeployU)) match {
                    case None => {
                        var ctcmd = KevoreeAdaptationFactory.eINSTANCE.createRemoveDeployUnit
                        ctcmd.setRef(actualDeployU)
                        adaptationModel.getAdaptations.add(ctcmd)
                      }
                    case Some(e) => //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
                  }

                }
            }



            //TODO REMOVE TIRDPARTY

          }
      }
    }

    //INSTANCE STEP
    updateNode.getInstances.foreach {
      uc =>
      actualNode.getInstances.find({
          ac => ac.isModelEquals(uc)
        }) match {
        case Some(c) => {

         // println("check instance "+c)
            //CHECK IF INSTANCE TYPE DEFINITION IS NOT UPDATED
            if (c.getTypeDefinition.isUpdated(uc.getTypeDefinition)) {

           //   println("is update")
              val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createUpdateInstance
              adaptcmd.setRef(uc)
              adaptationModel.getAdaptations.add(adaptcmd)

              //UPDATE BINDING IF NECESSARY
              uc match {
                case i: ComponentInstance => {
                    i.getRelatedBindings.foreach(b => {
                        adaptationModel.getAdaptations.filter(p => p.isInstanceOf[UpdateBinding]).find(adaptation => adaptation.asInstanceOf[UpdateBinding].getRef.isModelEquals(b)) match {
                          case None => {
                              val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createUpdateBinding
                              adaptcmd.setRef(b)
                              adaptationModel.getAdaptations.add(adaptcmd)
                            }
                          case Some(e) => //UPDATE BINDING ALREADY RESGISTERED
                        }
                      })
                  }
                case i: Channel => {
                    i.getRelatedBindings.foreach {
                      b =>
                      adaptationModel.getAdaptations.filter(p => p.isInstanceOf[UpdateBinding]).find(adaptation => adaptation.asInstanceOf[UpdateBinding].getRef.isModelEquals(b)) match {
                        case None => {
                            val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createUpdateBinding
                            adaptcmd.setRef(b)
                            adaptationModel.getAdaptations.add(adaptcmd)
                          }
                        case Some(e) => //UPDATE BINDING ALREADY RESGISTERED
                      }
                    }
                  }
                case _ =>
              }


            } else {
              //CHECK IS DICTIONARY IS UPDATED
              if (uc.getDictionary.isUpdated(c.getDictionary)) {
                val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createUpdateDictionaryInstance
                adaptcmd.setRef(uc)
                adaptationModel.getAdaptations.add(adaptcmd)
              } else {
                /* SPECIAL CASE UPDATE GROUP BINDING AS DICTIONARY */
                if (uc.isInstanceOf[Group] && c.isInstanceOf[Group]) {
                  val previousGroup = c.asInstanceOf[Group]
                  val updateGroup = uc.asInstanceOf[Group]

                  var modified = false
                  if (previousGroup.getSubNodes.size != updateGroup.getSubNodes.size) {
                    modified = true
                  }
                  //DEEP CHECK
                  if (!modified) {
                    if (!previousGroup.getSubNodes.forall(sub => updateGroup.getSubNodes.exists(usub => usub.getName == sub.getName))) {
                      modified = true
                    }
                    if (!updateGroup.getSubNodes.forall(sub => previousGroup.getSubNodes.exists(usub => usub.getName == sub.getName))) {
                      modified = true
                    }
                  }

                  if (modified) {
                    val adaptcmd = KevoreeAdaptationFactory.eINSTANCE.createUpdateDictionaryInstance
                    adaptcmd.setRef(uc)
                    adaptationModel.getAdaptations.add(adaptcmd)
                  }


                }

              }

            }
          }
        case None => {
            val ccmd = KevoreeAdaptationFactory.eINSTANCE.createAddInstance
            ccmd.setRef(uc)
            adaptationModel.getAdaptations.add(ccmd)
          }
      }
    }
    actualNode.getInstances.foreach {
      ac =>
      updateNode.getInstances.find({
          uc => uc.isModelEquals(ac)
        }) match {
        case Some(c) => //OK , CASE ALREADY PROCESS BY PREVIOUS STEP
        case None => {
            val ccmd = KevoreeAdaptationFactory.eINSTANCE.createRemoveInstance
            ccmd.setRef(ac)
            adaptationModel.getAdaptations.add(ccmd)
          }
      }
    }

    //Binding Step
    updateRoot.getMBindings.filter(mb => mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == actualNode.getName).foreach {
      uct =>
      actualRoot.getMBindings.filter(mb => mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == updateNode.getName).find({
          act => act.isModelEquals(uct)
        }) match {
        case Some(ct) => //OK
        case None => {
            val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAddBinding
            ctcmd.setRef(uct)
            adaptationModel.getAdaptations.add(ctcmd)
          }
      }
    }
    actualRoot.getMBindings.filter(mb => mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == actualNode.getName).foreach {
      act =>
      updateRoot.getMBindings.filter(mb => mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == updateNode.getName).find({
          uct => uct.isModelEquals(act)
        }) match {
        case Some(ct) => //OK
        case None => {
            val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createRemoveBinding
            ctcmd.setRef(act)
            adaptationModel.getAdaptations.add(ctcmd)
          }
      }
    }




    //FRAGMENT BINDING STEP
    //ONLY CHECK FOR HUB NO UNINSTALL
    updateRoot.getHubs.filter(hub => hub.usedByNode(updateNode.getName)).foreach {
      newhub =>
      actualRoot.getHubs.filter(hub => hub.usedByNode(updateNode.getName)).find(hub => newhub.getName == hub.getName) match {
        case None => {
            //NEW HUB INIT BINDING
            newhub.getOtherFragment(updateNode.getName).foreach {
              remoteName =>
              val addccmd = KevoreeAdaptationFactory.eINSTANCE.createAddFragmentBinding
              addccmd.setRef(newhub)
              addccmd.setTargetNodeName(remoteName)
              adaptationModel.getAdaptations.add(addccmd)
            }
          }

        case Some(previousHub) => {
            //adaptationModel.getAdaptations.addAll(getUpdateChannelAdaptationModel(previousHub, newhub, updateNode.getName).getAdaptations)
          }
      }
    }
    actualRoot.getHubs.filter(hub => hub.usedByNode(updateNode.getName)).foreach {
      actualHub =>
      updateRoot.getHubs.filter(hub => hub.usedByNode(updateNode.getName)).find(hub => actualHub.getName == hub.getName) match {
        case None => // NOTHING TO DO HUB WILL BE UNINSTALL, NO UNBIND IS NECESSARY
        case Some(updateHub) => {
            //CHECK AND UPDATE MBINDING
            adaptationModel.getAdaptations.addAll(getUpdateChannelAdaptationModel(actualHub, updateHub, updateNode.getName).getAdaptations)
          }
      }
    }


    //GROUP STEP
    /*
     updateRoot.getGroups.filter(group => group.getSubNodes.exists(node => node.getName == updateNode.getName)).foreach {
     newgroup =>
     actualRoot.getGroups.filter(group => group.getSubNodes.exists(node => node.getName == updateNode.getName)).find(group => newgroup.getName == group.getName) match {
     case None => {
     //NEW GROUP INIT BINDING
     val addgroup = KevoreeAdaptationFactory.eINSTANCE.createAddInstance
     addgroup.setRef(newgroup)
     adaptationModel.getAdaptations.add(addgroup)
     }

     case Some(previousGroup) => {
     //SEARCH SOME MODIFICATION

     // adaptationModel.getAdaptations.addAll(getUpdateChannelAdaptationModel(previousHub, newhub, updateNode.getName).getAdaptations)
     }
     }
     }
     actualRoot.getGroups.filter(group => group.getSubNodes.exists(node => node.getName == updateNode.getName)).foreach {
     newGroup =>
     updateRoot.getGroups.filter(group => group.getSubNodes.exists(node => node.getName == updateNode.getName)).find(group => newGroup.getName == group.getName) match {
     case None => // NOTHING TO DO HUB WILL BE UNINSTALL, NO UNBIND IS NECESSARY
     case Some(previousHub) => {
     //CHECK AND UPDATE MBINDING
     adaptationModel.getAdaptations.addAll(getUpdateChannelAdaptationModel(previousHub, newhub, updateNode.getName).getAdaptations)
     }
     }
     } */



    adaptationModel
  }

}
