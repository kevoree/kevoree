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

package org.kevoree.tools.ui.editor

import org.kevoree.ContainerRoot
import org.kevoree.KevoreeFactory
import org.kevoree.merger.KevoreeMergerComponent
import scala.collection.JavaConversions._
import org.eclipse.emf.common.notify.impl.AdapterImpl
import org.eclipse.emf.common.notify.Notification

class Art2Handler {

  private var merger = new KevoreeMergerComponent
  private var actualModel : ContainerRoot = KevoreeFactory.eINSTANCE.createContainerRoot
  actualModel.eAdapters.add(POC)

  def merge(modelToMerge : ContainerRoot) : Unit = {
    actualModel = merger.merge(actualModel, modelToMerge)
  }

  /* ACESSOR TO MODEL */
  def getActualModel : ContainerRoot = {actualModel}

  def setActualModel(c : ContainerRoot) = {
    actualModel = c
    actualModel.eAdapters.add(POC)
  }


  object POC extends  AdapterImpl {
    override def  notifyChanged(notification:Notification) = {
       println("Model changed !")
    }
  }

}
