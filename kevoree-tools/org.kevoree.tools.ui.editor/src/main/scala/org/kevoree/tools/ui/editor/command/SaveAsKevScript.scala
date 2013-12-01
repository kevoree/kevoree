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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.slf4j.LoggerFactory
import java.io.{FileWriter, File}
import org.kevoree.kevscript.KevScriptExporter
import javax.swing.JFileChooser


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/03/12
 * Time: 20:28
 */

class SaveAsKevScript extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  private val filechooser = new JFileChooser();

  def execute(p: AnyRef) {
    val script = KevScriptExporter.export(kernel.getModelHandler.getActualModel)
    if (p.isInstanceOf[String]) {
      val f = new File(p.asInstanceOf[String])
      if (f.exists()) {
        f.delete()
      }
      val fw = new FileWriter(f)
      try {
        fw.write(script)
      } finally {
        fw.close()
      }
    } else {
      if (p.isInstanceOf[StringBuffer]) {
        p.asInstanceOf[StringBuffer].append(script)
      } else {
        val result = filechooser.showSaveDialog(kernel.getModelPanel())
        if (filechooser.getSelectedFile() != null && result == JFileChooser.APPROVE_OPTION) {
          execute(filechooser.getSelectedFile().getPath())
        }
      }
    }
  }

}