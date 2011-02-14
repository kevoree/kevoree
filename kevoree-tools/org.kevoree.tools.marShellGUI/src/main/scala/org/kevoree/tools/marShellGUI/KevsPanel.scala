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
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import jsyntaxpane.components.Markers
import org.kevoree.tools.marShell.parser.KevsParser

class KevsPanel extends JPanel {

  this.setLayout(new BorderLayout())
  jsyntaxpane.DefaultSyntaxKit.initKit();
  jsyntaxpane.DefaultSyntaxKit.registerContentType("text/kevs", classOf[KevsJSyntaxKit].getName());
  var codeEditor = new JEditorPane();
  var scrPane = new JScrollPane(codeEditor);
  add(scrPane, BorderLayout.CENTER);
  codeEditor.setContentType("text/kevs");
  codeEditor.setText("tblock { /* hehe */ }");

  //codeEditor.setEditorKit(new KermetaSyntaxKit());

  codeEditor.getDocument.addDocumentListener(new DocumentListener(){
      def removeUpdate(e:DocumentEvent) = {updateMarkers(e.getDocument.getText(0, e.getDocument.getLength-1))}
      def insertUpdate(e:DocumentEvent) = {updateMarkers(e.getDocument.getText(0, e.getDocument.getLength-1))}
      def changedUpdate(e:DocumentEvent) {updateMarkers(e.getDocument.getText(0, e.getDocument.getLength-1))}

      def updateMarkers(content:String){
        var parser = new KevsParser();
        var result = parser.parseScript(content);
        Markers.removeMarkers(codeEditor)
        result match {
          case Some(e)=> Markers.removeMarkers(codeEditor);
          case None => {
              var highlighter = codeEditor.getHighlighter()
              highlighter.addHighlight(parser.lastNoSuccess.next.offset, parser.lastNoSuccess.next.rest.offset, new Markers.SimpleMarker(Color.RED));
              parser.lastNoSuccess.next.rest.offset
            }
        }
      }



    })


}
