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

package org.kevoree.tools.marShellGUI

import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JEditorPane
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.WindowConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import jsyntaxpane.components.Markers
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.framework.KevoreeXmiHelper

object MainRunner {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    println("Hello, world!")

    val model = KevoreeXmiHelper.load("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-tools/org.kevoree.tools.marShellGUI/src/main/resources/baseModel.kev")
    KevsModelHandlers.put(1,model)



    val f = new KevsFrame();
    /*
    var c = f.getContentPane();
    c.setLayout(new BorderLayout());

    var p = new KevsPanel()
    c.add(p, BorderLayout.CENTER)





    f.setSize(800, 600);  */
    f.setVisible(true);
    //f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


  }

}
