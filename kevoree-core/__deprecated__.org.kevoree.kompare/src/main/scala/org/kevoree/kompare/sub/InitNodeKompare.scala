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
import kompare.JavaSePrimitive
import org.kevoreeAdaptation._
 import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._

trait InitNodeKompare extends AbstractKompare {

  def getInitNodeAdaptationModel (node: ContainerNode): AdaptationModel = {

    val adaptationModelFactory = new org.kevoreeAdaptation.impl.DefaultKevoreeAdaptationFactory
    val adaptationModel = adaptationModelFactory.createAdaptationModel
    logger.info("INIT NODE v2 " + node.getName)
    //UPDATE ALL COMPONENT TYPE

    val root = node.eContainer.asInstanceOf[ContainerRoot]

    /* add type */
    node.getUsedTypeDefinition.foreach {
      ct =>
        val typecmd = adaptationModelFactory.createAdaptationPrimitive
        typecmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddType, root))
        typecmd.setRef(ct)
        adaptationModel.addAdaptations(typecmd)

        /* add all reLib from found deploy Unit*/
        logger.info("Look for deploy unit for type definition " + ct.getName + " on " + node.getName)
        val deployUnitfound: DeployUnit = ct.foundRelevantDeployUnit(node)
        if (deployUnitfound != null) {
          logger.info("DeployUnit found " + deployUnitfound.getUnitName)

          deployUnitfound.getRequiredLibs.foreach {
            rLib =>
              val addcttp = adaptationModelFactory.createAdaptationPrimitive
              addcttp.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddThirdParty, root))
              addcttp.setRef(rLib)
              adaptationModel.addAdaptations(addcttp)
          }

          /* add deploy unit if necessary */
          adaptationModel.getAdaptations
            .filter(adaptation => adaptation.getPrimitiveType.getName == JavaSePrimitive.AddDeployUnit
            || adaptation.getPrimitiveType.getName == JavaSePrimitive.AddThirdParty)
            .find(adaptation => adaptation.getRef.asInstanceOf[DeployUnit].isModelEquals(deployUnitfound)) match {
            case None => {
              val ctcmd = adaptationModelFactory.createAdaptationPrimitive
              ctcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddDeployUnit, root))
              ctcmd.setRef(deployUnitfound)
              adaptationModel.addAdaptations(ctcmd)
            }
            case Some(e) => //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
          }
        } else {
          throw new Exception("Deploy Unit not found for " + node.getName)
        }

    }

    /* add component */
    node.getInstances.foreach({
      c =>
        val addccmd = adaptationModelFactory.createAdaptationPrimitive
        addccmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddInstance, root))
        addccmd.setRef(c)
        adaptationModel.addAdaptations(addccmd)

        val addccmd2 = adaptationModelFactory.createAdaptationPrimitive
        addccmd2.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.StartInstance, root))
        addccmd2.setRef(c)
        adaptationModel.addAdaptations(addccmd2)

        val addccmd3 = adaptationModelFactory.createAdaptationPrimitive
        addccmd3.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.UpdateDictionaryInstance, root))
        addccmd3.setRef(c)
        adaptationModel.addAdaptations(addccmd3)

    })


    /* add FRAGMENT binding */
    root.getHubs.filter(hub => hub.usedByNode(node.getName)).foreach {
      channel =>
        channel.getOtherFragment(node.getName).foreach {
          remoteName =>
            val addccmd = adaptationModelFactory.createAdaptationPrimitive

            addccmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddFragmentBinding, root))
            addccmd.setRef(channel)
            addccmd.setTargetNodeName(remoteName)
            adaptationModel.addAdaptations(addccmd)
        }
    }

    /* add mbinding */
    root.getMBindings.foreach {
      b =>
        if (b.getPort.eContainer.asInstanceOf[KevoreeContainer].eContainer == node) {
          val addcmd = adaptationModelFactory.createAdaptationPrimitive
          addcmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddBinding, root))
          addcmd.setRef(b)
          adaptationModel.addAdaptations(addcmd)
        }
    }

    /* add group */
    /*
    root.getGroups.filter(group=> group.getSubNodes.contains(node)).foreach({
      c =>
        val addgroup = KevoreeAdaptationFactory.$instance.createAddInstance
        addgroup.setRef(c)
        adaptationModel.addAdaptations(addgroup)
    })  */

    adaptationModel
  }
}
