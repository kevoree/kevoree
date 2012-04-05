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

import javax.swing.JFrame
import org.kevoree.tools.ui.editor.{KevModelTextEditorPanel, KevoreeUIKernel}
import org.kevoree.tools.ui.editor.panel.KevoreeSerialMonitorPanel
import java.awt.event.{WindowEvent, WindowAdapter}

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 05/04/12
 * Time: 11:35
 */

class DisplaySerialMonitor extends Command {

  var j : JFrame = null
  var kernel: KevoreeUIKernel = null
  def setKernel(k: KevoreeUIKernel) = kernel = k

  def execute(p: AnyRef)
  {

    if(j != null){
      j.setVisible(false)
    }
    j = new JFrame("Kevoree Serial Monitor")
    val p = new KevoreeSerialMonitorPanel(kernel)
    j.add(p)
    j.setSize(800,600)
    j.setPreferredSize(j.getPreferredSize)
    j.setVisible(true)
    
    j.addWindowListener(new WindowAdapter() {
      override def windowClosing( e : WindowEvent) {
        p.close()
        j.dispose()
      }
    });
  }

}