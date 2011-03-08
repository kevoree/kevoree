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
import org.kevoreeAdaptation.AddDeployUnit
import org.kevoreeAdaptation._
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._

trait InitNodeKompare extends AbstractKompare {

  def getInitNodeAdaptationModel(node: ContainerNode): AdaptationModel = {
    var adaptationModel = org.kevoreeAdaptation.KevoreeAdaptationFactory.eINSTANCE.createAdaptationModel
    logger.info("INIT NODE v2 " + node.getName)
    //UPDATE ALL COMPONENT TYPE

    var root = node.eContainer.asInstanceOf[ContainerRoot]

    /* add type */
    node.getUsedTypeDefinition.foreach {
      ct =>
        var typecmd = KevoreeAdaptationFactory.eINSTANCE.createAddType
        typecmd.setRef(ct)
        adaptationModel.getAdaptations.add(typecmd)

        /* add all reLib */
        ct.getRequiredLibs.foreach {
          rLib =>
            var addcttp = KevoreeAdaptationFactory.eINSTANCE.createAddThirdParty
            addcttp.setRef(rLib)
            adaptationModel.getAdaptations.add(addcttp)
        }

        /* add deploy unit if necessary */
        adaptationModel.getAdaptations.filter(adaptation => adaptation.isInstanceOf[AddDeployUnit]).find(adaptation => adaptation.asInstanceOf[AddDeployUnit].getRef.isModelEquals(ct.getDeployUnit)) match {
          case None => {
            var ctcmd = KevoreeAdaptationFactory.eINSTANCE.createAddDeployUnit
            ctcmd.setRef(ct.getDeployUnit)
            adaptationModel.getAdaptations.add(ctcmd)
          }
          case Some(e) => //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
        }


    }

    /* add component */
    node.getInstances.foreach({
      c =>
        var addccmd = KevoreeAdaptationFactory.eINSTANCE.createAddInstance
        addccmd.setRef(c)
        adaptationModel.getAdaptations.add(addccmd)
    })


    /* add FRAGMENT binding */
    root.getHubs.foreach {
      channel =>
        channel.getOtherFragment(node.getName).foreach {
          remoteName =>
            var addccmd = KevoreeAdaptationFactory.eINSTANCE.createAddFragmentBinding
            addccmd.setRef(channel)
            addccmd.setTargetNodeName(remoteName)
            adaptationModel.getAdaptations.add(addccmd)
        }
    }

    /* add mbinding */
    root.getMBindings.foreach {
      b =>
        if (b.getPort.eContainer.eContainer == node) {
          var addcmd = KevoreeAdaptationFactory.eINSTANCE.createAddBinding
          addcmd.setRef(b)
          adaptationModel.getAdaptations.add(addcmd)
        }
    }

    /* add group */
    /*
    root.getGroups.filter(group=> group.getSubNodes.contains(node)).foreach({
      c =>
        val addgroup = KevoreeAdaptationFactory.eINSTANCE.createAddInstance
        addgroup.setRef(c)
        adaptationModel.getAdaptations.add(addgroup)
    })  */

    adaptationModel
  }
}
