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
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._

trait StopNodeKompare extends AbstractKompare {

  def getStopNodeAdaptationModel(node:ContainerNode):AdaptationModel={
    val adaptationModel = org.kevoreeAdaptation.KevoreeAdaptationFactory.eINSTANCE.createAdaptationModel
    logger.info("STOP NODE "+node.getName)

    val root = node.eContainer.asInstanceOf[ContainerRoot]

    /* add remove FRAGMENT binding */
    root.getHubs.filter(hub=> hub.usedByNode(node.getName)).foreach{channel =>
      channel.getOtherFragment(node.getName).foreach{remoteName =>
        val addccmd = KevoreeAdaptationFactory.eINSTANCE.createRemoveFragmentBinding
        addccmd.setRef(channel)
        addccmd.setTargetNodeName(remoteName)
        adaptationModel.getAdaptations.add(addccmd)
      }
    }


    /* remove mbinding */
    root.getMBindings.foreach{b=>
      if(b.getPort.eContainer.eContainer == node){
        val addcmd = KevoreeAdaptationFactory.eINSTANCE.createRemoveBinding
        addcmd.setRef(b)
        adaptationModel.getAdaptations.add(addcmd)
      }
    }

    /* add component */
    node.getInstances.foreach({c =>
        val cmd = KevoreeAdaptationFactory.eINSTANCE.createRemoveInstance
        cmd.setRef(c)
        adaptationModel.getAdaptations.add(cmd)
      })


    /* remove type */
    node.getUsedTypeDefinition.foreach{ct=>
      val rmctcmd = KevoreeAdaptationFactory.eINSTANCE.createRemoveType
      rmctcmd.setRef(ct)
      adaptationModel.getAdaptations.add(rmctcmd)
      
      /* add all reLib from found deploy Unit*/
      val deployUnitfound : DeployUnit = ct.foundRelevantDeployUnit(node)
      

      /* remove all reLib */
      //TODO

      /* add deploy unit if necessary */
      adaptationModel.getAdaptations.filter(adaptation => adaptation.isInstanceOf[RemoveDeployUnit]).find(adaptation=> adaptation.asInstanceOf[RemoveDeployUnit].getRef.isModelEquals(deployUnitfound) ) match {
        case None => {
            val ctcmd = KevoreeAdaptationFactory.eINSTANCE.createRemoveDeployUnit
            ctcmd.setRef(deployUnitfound)
            adaptationModel.getAdaptations.add(ctcmd)
          }
        case Some(e)=> //SIMILAR DEPLOY UNIT PRIMITIVE ALREADY REGISTERED
      }


    }



    adaptationModel
  }

}
