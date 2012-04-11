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
package org.kevoree.tools.ui.editor

import javax.swing.JFrame
import panel.KevoreeSerialMonitorPanel
import java.awt.event.{WindowEvent, WindowAdapter}

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 05/04/12
 * Time: 12:19
 */

object Tester extends  App {


  val j = new JFrame("Kevoree Serial Monitor")
      var p = new KevoreeSerialMonitorPanel(null)

      j.add(p)
      j.setSize(800,600)
      j.setPreferredSize(j.getPreferredSize)
      j.setVisible(true)

  j.addWindowListener(new WindowAdapter() {
    override def windowClosing( e : WindowEvent) {
      p.close()
      p = null
      j.dispose()
    }
  });
}