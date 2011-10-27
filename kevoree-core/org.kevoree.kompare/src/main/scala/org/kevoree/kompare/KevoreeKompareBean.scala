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

package org.kevoree.kompare

import org.kevoree._
import org.kevoree.kompare.sub.AbstractKompare
import org.kevoree.kompare.sub.InitNodeKompare
import org.kevoree.kompare.sub.StopNodeKompare
import org.kevoree.kompare.sub.UpdateNodeKompare
import org.kevoreeAdaptation._
import org.kevoree.framework.aspects.KevoreeAspects._

class KevoreeKompareBean
  extends InitNodeKompare with StopNodeKompare with UpdateNodeKompare with AbstractKompare with KevoreeScheduler {

  def kompare(actualModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): AdaptationModel = {

    val adaptationModel = org.kevoreeAdaptation.KevoreeAdaptationFactory.eINSTANCE.createAdaptationModel
    //STEP 0 - FOUND LOCAL NODE

    val actualLocalNode = actualModel.getNodes.find {
      c => c.getName == nodeName
    }
    val updateLocalNode = targetModel.getNodes.find {
      c => c.getName == nodeName
    }
    val currentAdaptModel = updateLocalNode match {
      case Some(uln) => {

        actualLocalNode match {
          case Some(aln) => getUpdateNodeAdaptationModel(aln, uln) //UPDATE
          case None => getInitNodeAdaptationModel(uln)
        }
      }
      case None => {
        actualLocalNode match {
          case Some(aln) => getStopNodeAdaptationModel(aln)
          case None => {
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
            val rcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveType,
              actualModel.asInstanceOf[ContainerRoot]))
            rcmd.setRef(adaptation.getRef)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(rcmd)
            val acmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddType,
              actualModel.asInstanceOf[ContainerRoot]))
            acmd.setRef(adaptation.getRef)
            currentAdaptModel.addAdaptations(acmd)
          }

          case JavaSePrimitive.UpdateDeployUnit => {
            /*val rcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveDeployUnit, actualModel.asInstanceOf[ContainerRoot]))
            rcmd.setRef(adaptation.getRef)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(rcmd)
            */
            val acmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddDeployUnit,
              actualModel.asInstanceOf[ContainerRoot]))
            acmd.setRef(adaptation.getRef)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(acmd)
          }
          case JavaSePrimitive.UpdateBinding => {
            val rcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveBinding,
              actualModel.asInstanceOf[ContainerRoot]))
            rcmd.setRef(adaptation.getRef)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(rcmd)

            val acmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddBinding,
              actualModel.asInstanceOf[ContainerRoot]))
            acmd.setRef(adaptation.getRef)
            currentAdaptModel.addAdaptations(acmd)
          }

          case JavaSePrimitive.UpdateFragmentBinding => {
            val rcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveFragmentBinding,
              actualModel.asInstanceOf[ContainerRoot]))
            rcmd.setRef(adaptation.getRef)
            rcmd.setTargetNodeName(adaptation.getTargetNodeName)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(rcmd)

            val acmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddFragmentBinding,
              actualModel.asInstanceOf[ContainerRoot]))
            acmd.setRef(adaptation.getRef)
            acmd.setTargetNodeName(adaptation.getTargetNodeName)
            currentAdaptModel.addAdaptations(acmd)
          }


          case JavaSePrimitive.UpdateInstance => {
            val stopcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            stopcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.StopInstance,
              actualModel.asInstanceOf[ContainerRoot]))
            stopcmd.setRef(adaptation.getRef)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(stopcmd)

            val rcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            rcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveInstance,
              actualModel.asInstanceOf[ContainerRoot]))
            rcmd.setRef(adaptation.getRef)
            currentAdaptModel.removeAdaptations(adaptation)
            currentAdaptModel.addAdaptations(rcmd)

            val acmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            acmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddInstance,
              actualModel.asInstanceOf[ContainerRoot]))
            acmd.setRef(adaptation.getRef)
            currentAdaptModel.addAdaptations(acmd)

            val uDiccmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            uDiccmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateDictionaryInstance,
              actualModel.asInstanceOf[ContainerRoot]))
            uDiccmd.setRef(adaptation.getRef)
            currentAdaptModel.addAdaptations(uDiccmd)

            val startcmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive
            startcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.StartInstance,
              actualModel.asInstanceOf[ContainerRoot]))
            startcmd.setRef(adaptation.getRef)
            currentAdaptModel.addAdaptations(startcmd)

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

    plan(currentAdaptModel, nodeName)
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
