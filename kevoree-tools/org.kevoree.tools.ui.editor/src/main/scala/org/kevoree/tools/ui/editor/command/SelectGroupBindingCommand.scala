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
import org.kevoree.tools.ui.framework.SelectElement
import org.kevoree.tools.ui.framework.elements.Binding
import org.kevoree.MBinding
import org.kevoree.tools.ui.editor.widget.TempGroupBinding

/**
 * User: ffouquet
 * Date: 02/07/11
 * Time: 14:09
 */

class SelectGroupBindingCommand extends Command {

  @BeanProperty
  var kernel: KevoreeUIKernel = null
  private var alreadySelected: SelectElement = null

  def execute(p: AnyRef) {
    val bObject: AnyRef = kernel.getUifactory.getMapping.get(p)
    if (bObject.isInstanceOf[TempGroupBinding]) {
      var instance: TempGroupBinding = bObject.asInstanceOf[TempGroupBinding]
      val elem: Binding = p.asInstanceOf[Binding]
      if (alreadySelected != null && alreadySelected != elem) {
        alreadySelected.setSelected(false)
      }
      alreadySelected = elem
      elem.setSelected(!elem.getSelected)
      if (elem.getSelected) {
        kernel.getEditorPanel.showPropertyFor(elem)
      }
      else {
        kernel.getEditorPanel.unshowPropertyEditor()
      }
      kernel.getModelPanel.repaint()
      kernel.getModelPanel.revalidate()
    }


  }

}