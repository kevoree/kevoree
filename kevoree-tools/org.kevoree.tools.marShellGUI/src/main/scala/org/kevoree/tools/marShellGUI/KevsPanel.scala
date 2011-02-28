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
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import jsyntaxpane.components.Markers
import org.kevoree.tools.marShell.parser.KevsParser
import javax.swing.{JSplitPane, JEditorPane, JPanel, JScrollPane}
import org.kevoree.tools.marShell.ast.Script

class KevsPanel extends JPanel {

  def getModel : Script = {
    val parser = new KevsParser();
    val result = parser.parseScript(codeEditor.getText);
    result.get
  }

  this.setLayout(new BorderLayout())
  jsyntaxpane.DefaultSyntaxKit.initKit();
  jsyntaxpane.DefaultSyntaxKit.registerContentType("text/kevs", classOf[KevsJSyntaxKit].getName());
  var codeEditor = new JEditorPane();
  var scrPane = new JScrollPane(codeEditor);

  codeEditor.setContentType("text/kevs");
  codeEditor.setText("tblock { \n //insert Kevoree Script here \n }");

  codeEditor.getDocument.addDocumentListener(new DocumentListener() {
    def removeUpdate(e: DocumentEvent) = {
      updateMarkers(e.getDocument.getText(0, e.getDocument.getLength - 1))
    }

    def insertUpdate(e: DocumentEvent) = {
      updateMarkers(e.getDocument.getText(0, e.getDocument.getLength - 1))
    }

    def changedUpdate(e: DocumentEvent) {
      updateMarkers(e.getDocument.getText(0, e.getDocument.getLength - 1))
    }

    def updateMarkers(content: String) {
      val parser = new KevsParser();
      val result = parser.parseScript(codeEditor.getText);
      Markers.removeMarkers(codeEditor)

      logPanel.clear
      result match {
        case Some(e) => Markers.removeMarkers(codeEditor);
        case None => {

          logPanel.error(parser.lastNoSuccess.toString)

          var highlighter = codeEditor.getHighlighter()
          highlighter.addHighlight(parser.lastNoSuccess.next.offset, parser.lastNoSuccess.next.rest.offset, new Markers.SimpleMarker(Color.ORANGE));
          parser.lastNoSuccess.next.rest.offset
        }
      }
    }
  })

  var logPanel = new LogPanel
  var splitPane: JSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrPane, logPanel)
  splitPane.setOneTouchExpandable(true)
  splitPane.setContinuousLayout(true)
  splitPane.setDividerSize(15)
  splitPane.setDividerLocation(0.99)
  splitPane.setResizeWeight(1.0)
  add(splitPane, BorderLayout.CENTER);

}
