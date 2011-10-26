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
package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.tools.model2code.Model2Code
import javax.swing.JFileChooser
import org.kevoree.{ContainerRoot, TypeDefinition}

/**
 * User: ffouquet
 * Date: 29/08/11
 * Time: 17:52
 */

class SynchCodeCommand extends Command {
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  def execute(p: Object) {
    if (p != null) {
      val typeDef: TypeDefinition = p.asInstanceOf[TypeDefinition]
      val pomDirectoryHelper: JFileChooser = new JFileChooser
      pomDirectoryHelper.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
      pomDirectoryHelper.setDialogTitle("Please select Home directory for deploy unit code (POM directory)")
      val result: Int = pomDirectoryHelper.showSaveDialog(null)
      if (result == JFileChooser.APPROVE_OPTION) {
        val model2code = new Model2Code
        model2code.modelToCode(typeDef.eContainer.asInstanceOf[ContainerRoot], typeDef, pomDirectoryHelper.getSelectedFile.toURI)

      }
    }
  }
}