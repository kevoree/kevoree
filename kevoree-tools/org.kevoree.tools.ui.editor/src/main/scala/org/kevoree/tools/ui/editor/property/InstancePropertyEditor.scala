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
package org.kevoree.tools.ui.editor.property

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree._
import com.explodingpixels.macwidgets.HudWidgetFactory
import com.explodingpixels.macwidgets.plaf.{HudTextFieldUI, HudLabelUI}
import javax.swing._
import event.{DocumentEvent, DocumentListener}
import java.awt.Dimension
import java.awt.event.{ActionListener, ActionEvent}
import text.BadLocationException

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */

class InstancePropertyEditor(elem:org.kevoree.Instance, kernel: KevoreeUIKernel) extends NamedElementPropertyEditor(elem,kernel) {

  def getValue(instance: Instance, att: DictionaryAttribute): String = {
      var value: DictionaryValue = null
      if (instance.getDictionary.isEmpty) {
        instance.setDictionary(new Some[Dictionary](KevoreeFactory.createDictionary))
      }
      import scala.collection.JavaConversions._
      for (v <- instance.getDictionary.get.getValuesForJ) {
        if (v.getAttribute == att) {
          return v.getValue
        }
      }
      import scala.collection.JavaConversions._
      for (v <- instance.getTypeDefinition.getDictionaryType.get.getDefaultValuesForJ) {
        if (v.getAttribute == att) {
          return v.getValue
        }
      }
      return ""
    }

    def setValue(aValue: AnyRef, instance: Instance, att: DictionaryAttribute): Unit = {
      var value: DictionaryValue = null
      import scala.collection.JavaConversions._
      for (v <- instance.getDictionary.get.getValuesForJ) {
        if (v.getAttribute == att) {
          value = v
        }
      }
      if (value == null) {
        value = KevoreeFactory.createDictionaryValue
        value.setAttribute(att)
        instance.getDictionary.get.addValues(value)
      }
      value.setValue(aValue.toString)
    }

    //CONSTRUCTOR CODE
  var p: JPanel = new JPanel(new SpringLayout)
    p.setBorder(null)


    if (elem.getTypeDefinition.getDictionaryType.isDefined) {
      import scala.collection.JavaConversions._
      for (att <- elem.getTypeDefinition.getDictionaryType.get.getAttributesForJ) {
        val l: JLabel = new JLabel(att.getName, SwingConstants.TRAILING)
        l.setUI(new HudLabelUI)
        p.add(l)
        if (att.getDatatype ne "") {
          if (att.getDatatype.startsWith("enum=")) {
            val values: String = att.getDatatype.replaceFirst("enum=", "")
            val model = new DefaultComboBoxModel
            values.split(",").foreach{value => model.addElement(value)}
            val comboBox: JComboBox = HudWidgetFactory.createHudComboBox(model)
            l.setLabelFor(comboBox)
            p.add(comboBox)
            comboBox.setSelectedItem(getValue(elem, att))
            comboBox.addActionListener(new ActionListener {
              def actionPerformed(actionEvent: ActionEvent): Unit = {
                setValue(comboBox.getSelectedItem.toString, elem, att)
              }
            })
          }
        }
        else {
          var textField: JTextField = new JTextField(10)
          textField.setUI(new HudTextFieldUI)
          textField.getDocument.addDocumentListener(new DocumentListener {
            def insertUpdate(documentEvent: DocumentEvent): Unit = {
              try {
                setValue(documentEvent.getDocument.getText(0, documentEvent.getDocument.getLength), elem, att)
              }
              catch {
                case e: BadLocationException => {
                  e.printStackTrace
                }
              }
            }

            def removeUpdate(documentEvent: DocumentEvent): Unit = {
              try {
                setValue(documentEvent.getDocument.getText(0, documentEvent.getDocument.getLength), elem, att)
              }
              catch {
                case e: BadLocationException => {
                  e.printStackTrace
                }
              }
            }

            def changedUpdate(documentEvent: DocumentEvent): Unit = {
              try {
                setValue(documentEvent.getDocument.getText(0, documentEvent.getDocument.getLength), elem, att)
              }
              catch {
                case e: BadLocationException => {
                  e.printStackTrace
                }
              }
            }
          })
          l.setLabelFor(textField)
          p.add(textField)
          textField.setText(getValue(elem, att))
        }
      }
      SpringUtilities.makeCompactGrid(p, elem.getTypeDefinition.getDictionaryType.get.getAttributesForJ.size, 2, 6, 6, 6, 6)
    }


    p.setOpaque(false)
    var scrollPane: JScrollPane = new JScrollPane(p)
    scrollPane.getViewport.setOpaque(false)


    scrollPane.setOpaque(false)


    scrollPane.setBorder(null)


    scrollPane.setPreferredSize(new Dimension(250, 150))


    this.addCenter(scrollPane)


}