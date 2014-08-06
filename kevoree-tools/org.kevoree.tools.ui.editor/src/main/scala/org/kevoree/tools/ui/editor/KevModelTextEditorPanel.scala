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
package org.kevoree.tools.ui.editor

import command.LoadModelCommand
import javax.swing._
import java.awt.event.{MouseEvent, MouseAdapter}
import org.kevoree.factory.DefaultKevoreeFactory
import org.kevoree.modeling.api.json.{JSONModelLoader, JSONModelSerializer}
import org.slf4j.LoggerFactory
import java.awt.BorderLayout

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 18/03/12
 * Time: 21:43
 */

class KevModelTextEditorPanel(kernel: KevoreeUIKernel) extends JPanel {

  private val saver = new JSONModelSerializer();
  private val loader = new JSONModelLoader(new DefaultKevoreeFactory());

  def reload() {
    PositionedEMFHelper.updateModelUIMetaData(kernel)
    codeEditor.setText(saver.serialize(kernel.getModelHandler.getActualModel))
  }

  this.setLayout(new BorderLayout())
  jsyntaxpane.DefaultSyntaxKit.initKit();
  jsyntaxpane.DefaultSyntaxKit.registerContentType("text/json", classOf[jsyntaxpane.syntaxkits.XHTMLSyntaxKit].getName());
  var codeEditor = new JEditorPane();

  var scrPane = new JScrollPane(codeEditor);

  codeEditor.setContentType("text/xml; charset=UTF-8");
  codeEditor.setText("\n");

  add(scrPane, BorderLayout.CENTER);

  val btApply = new JButton
  btApply.setText("Apply")
  add(btApply, BorderLayout.SOUTH);

  btApply.addMouseListener(new MouseAdapter() {
    override def mouseClicked(p1: MouseEvent) {
      try {
        val newModel = loader.loadModelFromString(codeEditor.getText).get(0)
        val loadCMD = new LoadModelCommand
        loadCMD.setKernel(kernel)
        loadCMD.execute(newModel)

      } catch {
        case _@e => {
          LoggerFactory.getLogger(this.getClass).error("error while apply model")
        }
      }
    }
  })


}
