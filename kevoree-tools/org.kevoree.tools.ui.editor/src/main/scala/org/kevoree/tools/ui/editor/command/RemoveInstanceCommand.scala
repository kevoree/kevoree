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

package org.kevoree.tools.ui.editor.command

import scala.reflect.BeanProperty
import scala.collection.JavaConversions._
import org.kevoree.tools.ui.editor.aspects.Art2UIAspects._
import org.kevoree.tools.ui.editor.{ModelHelper, KevoreeUIKernel}
import org.kevoree._

class RemoveInstanceCommand(elem: org.kevoree.NamedElement) extends Command {

  @BeanProperty
  var kernel: KevoreeUIKernel = null

  def execute(p: Object) {

    elem match {
      case inst: Channel => {inst.removeModelAndUI(kernel); updateType(inst) }
      case inst: ComponentInstance => {inst.removeModelAndUI(kernel) ; updateType(inst) }
      case inst: ContainerNode => {inst.removeModelAndUI(kernel); updateType(inst)    }
      case inst: Group => {inst.removeModelAndUI(kernel) ; updateType(inst)      }
    }

    kernel.getEditorPanel.unshowPropertyEditor()


  }

  def updateType(i: Instance) {
    kernel.getEditorPanel.getPalette.updateTypeValue(ModelHelper.getTypeNbInstance(kernel.getModelHandler.getActualModel, i.getTypeDefinition), i.getTypeDefinition.getName)
  }


}
