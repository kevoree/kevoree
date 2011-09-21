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
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoreeAdaptation._

trait UpdateChannelKompare extends AbstractKompare {

  def getUpdateChannelAdaptationModel (actualChannel: Channel, updateChannel: Channel,nodeName: String): AdaptationModel = {
    val adaptationModel = org.kevoreeAdaptation.KevoreeAdaptationFactory.eINSTANCE.createAdaptationModel

    updateChannel.getOtherFragment(nodeName).foreach {
      newhubBindingNodeName =>
        actualChannel.getOtherFragment(nodeName).find(b => b == newhubBindingNodeName) match {
          case None => {
            //NEW BINDING TODO
            val addccmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive()
            addccmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.AddFragmentBinding,actualChannel.eContainer().asInstanceOf[ContainerRoot]))


            addccmd.setRef(updateChannel)
            addccmd.setTargetNodeName(newhubBindingNodeName)
            adaptationModel.getAdaptations.add(addccmd)
          }
          case Some(bname) => //OK ALREADY BINDED
        }
    }
    actualChannel.getOtherFragment(nodeName).foreach {
      previousHubBindingNodeName =>
        updateChannel.getOtherFragment(nodeName).find(b => b == previousHubBindingNodeName) match {
          case None => {
            //REMOVE BINDING TODO
            val addccmd = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive()
            addccmd.setPrimitiveType(getAdaptationPrimitive(JavaSePrimitive.RemoveFragmentBinding,actualChannel.eContainer().asInstanceOf[ContainerRoot]))
            addccmd.setRef(updateChannel)
            addccmd.setTargetNodeName(previousHubBindingNodeName)
            adaptationModel.getAdaptations.add(addccmd)
          }
          case Some(bname) => //OK ALREADY BINDED
        }
    }
    adaptationModel
  }

}
