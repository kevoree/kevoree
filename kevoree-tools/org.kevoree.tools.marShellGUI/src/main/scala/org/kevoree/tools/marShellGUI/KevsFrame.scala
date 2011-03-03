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
package org.kevoree.tools.marShellGUI

import java.awt.BorderLayout
import javax.swing._
import org.kevoree.ContainerRoot

class KevsFrame extends JFrame {

  protected var kevsPanel = new KevsPanel()

  this.setTitle("Kevoree Script Editor")

  add(kevsPanel, BorderLayout.CENTER)

       /*
  var buttons = new JPanel
  buttons.setLayout(new BoxLayout(buttons,BoxLayout.LINE_AXIS))
  var btExecution = new JButton("execute");

  buttons.add(btExecution)
  add(buttons, BorderLayout.SOUTH)
    */

  setSize(800, 600);
 // setVisible(true);
  setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);


}