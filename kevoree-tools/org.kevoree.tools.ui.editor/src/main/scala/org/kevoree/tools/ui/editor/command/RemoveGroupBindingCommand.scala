package org.kevoree.tools.ui.editor.command

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
import reflect.BeanProperty
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.ui.editor.aspects.Art2UIAspects._
import org.kevoree.tools.ui.editor.widget.TempGroupBinding
import org.kevoree.{ContainerRoot, MBinding}
import org.kevoree.tools.ui.framework.elements.{Binding, ModelPanel}

/**
 * User: ffouquet
 * Date: 04/07/11
 * Time: 10:28
 */

class RemoveGroupBindingCommand(mb: TempGroupBinding) extends Command {

  @BeanProperty
  var kernel: KevoreeUIKernel = null

  def execute(p: AnyRef) {

    //REMOVE UI
    val modelPanel = kernel.getUifactory.getMapping.get(kernel.getModelHandler.getActualModel).asInstanceOf[ModelPanel]
    val bui = kernel.getUifactory.getMapping.get(mb).asInstanceOf[Binding]
    modelPanel.removeBinding(bui)

    //REMOVE INSTANCE
    mb.getOriginGroup.removeSubNodes(mb.getTargetNode)

    //UNBIND UI
    kernel.getUifactory.getMapping.unbind(bui, mb);
    
    
    
    kernel.getEditorPanel.unshowPropertyEditor()

    kernel.getModelHandler.EMFListener.notifyChanged()


  }

}