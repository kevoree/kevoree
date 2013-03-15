/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
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

package org.kevoree.kompare

import org.kevoree._
import org.kevoree.kompare.sub.AbstractKompare
import org.kevoree.kompare.sub.InitNodeKompare
import org.kevoree.kompare.sub.StopNodeKompare
import org.kevoree.kompare.sub.UpdateNodeKompare
import org.kevoreeAdaptation._
import org.kevoree.framework.aspects.KevoreeAspects._

import scala.collection.JavaConversions._

class KevoreeKompareBean
  extends InitNodeKompare with StopNodeKompare with UpdateNodeKompare with AbstractKompare with KevoreeScheduler {

  def kompare(actualModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel = {

    val adaptationModelFactory = new org.kevoreeAdaptation.impl.DefaultKevoreeAdaptationFactory
    val adaptationModel = adaptationModelFactory.createAdaptationModel
    //STEP 0 - FOUND LOCAL NODE
    val actualLocalNode = actualModel.findByPath("nodes[" + nodeName + "]", classOf[ContainerNode])
    val updateLocalNode = targetModel.findByPath("nodes[" + nodeName + "]", classOf[ContainerNode])
    val currentAdaptModel = updateLocalNode match {
      case uln: ContainerNode => {
        actualLocalNode match {
          case aln: ContainerNode => getUpdateNodeAdaptationModel(aln, uln) //UPDATE
          case null => getInitNodeAdaptationModel(uln)
        }
      }
      case null => {
        actualLocalNode match {
          case aln: ContainerNode => getStopNodeAdaptationModel(aln)
          case null => {
            adaptationModel
            /* BEST EFFORT PREPARE PLATEFORM */
            //updateAllThirdParties(actualModel,targetModel)
          }
        }
      }
    }

    //TRANSFORME UPDATE
    currentAdaptModel.getAdaptations.toList.foreach {
      adaptation =>
        adaptation.getPrimitiveType.getName match {
          case JavaSePrimitive.UpdateType => {
            val rcmd = adaptationModelFactory.createAdaptationPrimitive
            rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveType,
              actualModel))
            rcmd.setRef(adaptation.getRef)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(rcmd)
            val acmd = adaptationModelFactory.createAdaptationPrimitive
            acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddType,
              actualModel))
            acmd.setRef(adaptation.getRef)
            currentAdaptModel.addAdaptations(acmd)
          }
          /*
                    case JavaSePrimitive.UpdateDeployUnit => {
                      /*val rcmd = KevoreeAdaptationFactory.$instance.createAdaptationPrimitive
                      rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveDeployUnit, actualModel.asInstanceOf[ContainerRoot]))
                      rcmd.setRef(adaptation.getRef)
                      currentAdaptModel.removeAdaptations(adaptation)
                      currentAdaptModel.addAdaptations(rcmd)
                      */
                      val acmd = KevoreeAdaptationFactory.$instance.createAdaptationPrimitive
                      acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddDeployUnit,
                        actualModel.asInstanceOf[ContainerRoot]))
                      acmd.setRef(adaptation.getRef)
                      currentAdaptModel.removeAdaptations(adaptation)
                      currentAdaptModel.addAdaptations(acmd)
                    }  */
          case JavaSePrimitive.UpdateBinding => {
            val rcmd = adaptationModelFactory.createAdaptationPrimitive
            rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveBinding, actualModel))
            rcmd.setRef(adaptation.getRef)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(rcmd)

            val acmd = adaptationModelFactory.createAdaptationPrimitive
            acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddBinding, actualModel))
            acmd.setRef(adaptation.getRef)
            currentAdaptModel.addAdaptations(acmd)
          }

          case JavaSePrimitive.UpdateFragmentBinding => {
            val rcmd = adaptationModelFactory.createAdaptationPrimitive
            rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveFragmentBinding, actualModel))
            rcmd.setRef(adaptation.getRef)
            rcmd.setTargetNodeName(adaptation.getTargetNodeName)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(rcmd)

            val acmd = adaptationModelFactory.createAdaptationPrimitive
            acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddFragmentBinding, actualModel))
            acmd.setRef(adaptation.getRef)
            acmd.setTargetNodeName(adaptation.getTargetNodeName)
            currentAdaptModel.addAdaptations(acmd)
          }


          case JavaSePrimitive.UpdateInstance => {
            val stopcmd = adaptationModelFactory.createAdaptationPrimitive
            stopcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.StopInstance, actualModel))
            stopcmd.setRef(adaptation.getRef.asInstanceOf[(Object, Object)]._1)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(stopcmd)

            val rcmd = adaptationModelFactory.createAdaptationPrimitive
            rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveInstance,
              actualModel))
            rcmd.setRef(adaptation.getRef.asInstanceOf[(Object, Object)]._1)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(rcmd)

            val acmd = adaptationModelFactory.createAdaptationPrimitive
            acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddInstance,
              actualModel))
            acmd.setRef(adaptation.getRef.asInstanceOf[(Object, Object)]._2)
            currentAdaptModel.addAdaptations(acmd)

            val uDiccmd = adaptationModelFactory.createAdaptationPrimitive
            uDiccmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateDictionaryInstance,
              actualModel))
            uDiccmd.setRef(adaptation.getRef.asInstanceOf[(Object, Object)]._2)
            currentAdaptModel.addAdaptations(uDiccmd)

            val startcmd = adaptationModelFactory.createAdaptationPrimitive
            startcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.StartInstance,
              actualModel))
            startcmd.setRef(adaptation.getRef.asInstanceOf[(Object, Object)]._2)
            currentAdaptModel.addAdaptations(startcmd)

          }

          case JavaSePrimitive.AddThirdParty => {
            val startCmd = adaptationModelFactory.createAdaptationPrimitive
            startCmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.StartThirdParty,
              actualModel))
            startCmd.setRef(adaptation.getRef)
            currentAdaptModel.addAdaptations(startCmd)
          }

          case _ =>
        }
    }

    //CHECK HARAH KIRI PROBLEM
    /*
    currentAdaptModel.getAdaptations.filter(ad => ad.getPrimitiveType.getName == JavaSePrimitive.AddDeployUnit || ad.getPrimitiveType.getName == JavaSePrimitive.RemoveDeployUnit || ad.getPrimitiveType.getName == JavaSePrimitive.AddType || ad.getPrimitiveType.getName == JavaSePrimitive.RemoveType).foreach {
      ad =>
        ad.getRef match {
          case deployUnit: DeployUnit => {
            val root = deployUnit.eContainer.asInstanceOf[ContainerRoot]
            root.getNodes.find(n => n.getName == nodeName).map {
              node =>
                if (detectHaraKiriDeployUnit(node.getTypeDefinition, deployUnit)) {
                  logger.warn("HaraKiri ignore from Kompare for deployUnit => " + deployUnit.getUnitName)
                  currentAdaptModel.removeAdaptations(ad)
                } else {
                  logger.debug("Sucessfully checked " + deployUnit.getUnitName)
                }

            }
          }
          case typeDef: TypeDefinition => {
            val root = typeDef.eContainer.asInstanceOf[ContainerRoot]
            root.getNodes.find(n => n.getName == nodeName).map {
              node =>
                if(detectHaraKiriTypeDefinition(node.getTypeDefinition,typeDef)){
                  logger.warn("HaraKiri ignore from Kompare for type => " + typeDef.getName)
                  currentAdaptModel.removeAdaptations(ad)
                }
            }
          }

        }
    }
        */

    //logger.debug("after Hara Kiri detect")
    val afterPlan = plan(currentAdaptModel, nodeName)
    return afterPlan
  }


  /*
    def detectHaraKiriTypeDefinition(nodeType: TypeDefinition, fnodeType: TypeDefinition): Boolean = {
      if (
        (nodeType.getName == fnodeType.getName) || nodeType.getSuperTypes.exists(superT => detectHaraKiriTypeDefinition(superT, fnodeType))
      ) {
        true
      } else {
        false
      }
    }

    def detectHaraKiriDeployUnit(nodeType: TypeDefinition, deployUnit: DeployUnit): Boolean = {
      import org.kevoree.framework.aspects.KevoreeAspects._
      if (
        nodeType.getDeployUnits.exists(du => du.isModelEquals(deployUnit)) || nodeType.getSuperTypes.exists(superT => detectHaraKiriDeployUnit(superT, deployUnit))
      ) {
        true
      } else {
        false
      }
    }  */

}
