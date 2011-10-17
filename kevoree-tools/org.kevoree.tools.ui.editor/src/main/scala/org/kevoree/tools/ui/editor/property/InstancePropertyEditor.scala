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

class InstancePropertyEditor(elem: org.kevoree.Instance, kernel: KevoreeUIKernel) extends NamedElementPropertyEditor(elem, kernel) {

  def getValue(instance: Instance, att: DictionaryAttribute, targetNode: Option[String]): String = {
    var value: DictionaryValue = null
    for (v <- instance.getDictionary.get.getValues) {
      targetNode match {
        case Some(targetNodeSearch) => {
          if (v.getAttribute == att && v.getTargetNode == targetNodeSearch) {
            return v.getValue
          }
        }
        case None => {
          if (v.getAttribute == att) {
            return v.getValue
          }
        }
      }
    }
    for (v <- instance.getTypeDefinition.getDictionaryType.get.getDefaultValues) {
      if (v.getAttribute == att) {
        return v.getValue
      }
    }
    return "" //DEFAULT CASE RETURN EMPTY VALUE
  }

  def setValue(aValue: AnyRef, instance: Instance, att: DictionaryAttribute, targetNode: Option[String]): Unit = {
    var value: DictionaryValue = null
    for (v <- instance.getDictionary.get.getValues) {
      targetNode match {
        case Some(targetNodeSearch) => {
          if (v.getAttribute.getName == att.getName && v.getTargetNode == targetNodeSearch) {
            value = v
          }
        }
        case None => {
          if (v.getAttribute.getName == att.getName) {
            value = v
          }
        }
      }
    }
    if (value == null) {
      value = KevoreeFactory.createDictionaryValue
      value.setAttribute(att)
      targetNode.map(t => value.setTargetNode(t))
      instance.getDictionary.get.addValues(value)
    }
    value.setValue(aValue.toString)
    kernel.getModelHandler.notifyChanged()
  }

  //CONSTRUCTOR CODE
  if (elem.getDictionary.isEmpty) {
    elem.setDictionary(new Some[Dictionary](KevoreeFactory.createDictionary))
  }
  var p: JPanel = new JPanel(new SpringLayout)
  p.setBorder(null)

  var nbLigne = 0
  if (elem.getTypeDefinition.getDictionaryType.isDefined) {
    for (att <- elem.getTypeDefinition.getDictionaryType.get.getAttributes) {

      att.getDatatype match {
        case _ if (att.getDatatype != "" && att.getDatatype.startsWith("enum=") && !att.getFragmentDependant) => {
          val l: JLabel = new JLabel(att.getName, SwingConstants.TRAILING)
          l.setUI(new HudLabelUI)
          p.add(l)
          p.add(getEnumBox(att, l, None))
          nbLigne = nbLigne + 1
        }
        case _ if (att.getDatatype != "" && att.getDatatype.startsWith("enum=") && att.getFragmentDependant) => {
          getNodesLinked(elem).foreach {
            nodeName =>
              val l: JLabel = new JLabel(att.getName + "->" + nodeName, SwingConstants.TRAILING)
              l.setUI(new HudLabelUI)
              p.add(l)
              p.add(getEnumBox(att, l, Some(nodeName)))
              nbLigne = nbLigne + 1
          }
        }
        case _ if (att.getFragmentDependant) => {
          getNodesLinked(elem).foreach {
            nodeName =>
              val l: JLabel = new JLabel(att.getName + "->" + nodeName, SwingConstants.TRAILING)
              l.setUI(new HudLabelUI)
              p.add(l)
              p.add(getTextField(att, l, Some(nodeName)))
              nbLigne = nbLigne + 1
          }
        }
        case _ => {
          val l: JLabel = new JLabel(att.getName, SwingConstants.TRAILING)
          l.setUI(new HudLabelUI)
          p.add(l)
          p.add(getTextField(att, l, None))
          nbLigne = nbLigne + 1
        }
      }
    }
    SpringUtilities.makeCompactGrid(p, nbLigne, 2, 6, 6, 6, 6)
  }


  p.setOpaque(false)
  var scrollPane: JScrollPane = new JScrollPane(p)
  scrollPane.getViewport.setOpaque(false)
  scrollPane.setOpaque(false)
  scrollPane.setBorder(null)
  scrollPane.setPreferredSize(new Dimension(250, 150))
  this.addCenter(scrollPane)

  //END CONSTRUCTOR CODE


  def getNodesLinked(i: Instance): List[String] = {
    i match {
      case g: Group => {
        g.getSubNodes.map(s => s.getName)
      }
      case c: Channel => {
        import org.kevoree.framework.aspects.KevoreeAspects._
        c.getRelatedNodes.map(s => s.getName)
      }
      case _ => List()
    }
  }


  def getEnumBox(att: DictionaryAttribute, label: JLabel, targetNode: Option[String]): JComponent = {
    val values: String = att.getDatatype.replaceFirst("enum=", "")
    val model = new DefaultComboBoxModel
    values.split(",").foreach {
      value => model.addElement(value)
    }
    val comboBox: JComboBox = HudWidgetFactory.createHudComboBox(model)
    label.setLabelFor(comboBox)
    p.add(comboBox)
    comboBox.setSelectedItem(getValue(elem, att, targetNode))
    comboBox.addActionListener(new ActionListener {
      def actionPerformed(actionEvent: ActionEvent): Unit = {
        setValue(comboBox.getSelectedItem.toString, elem, att, targetNode)
      }
    })
    comboBox
  }


  //get Default TextField
  def getTextField(att: DictionaryAttribute, label: JLabel, targetNode: Option[String]): JComponent = {
    val textField: JTextField = new JTextField(10)
    textField.setUI(new HudTextFieldUI)
    textField.getDocument.addDocumentListener(new DocumentListener {
      def insertUpdate(documentEvent: DocumentEvent): Unit = {
        try {
          setValue(documentEvent.getDocument.getText(0, documentEvent.getDocument.getLength), elem, att, targetNode)
        }
        catch {
          case e: BadLocationException => {
            e.printStackTrace
          }
        }
      }

      def removeUpdate(documentEvent: DocumentEvent): Unit = {
        try {
          setValue(documentEvent.getDocument.getText(0, documentEvent.getDocument.getLength), elem, att, targetNode)
        }
        catch {
          case e: BadLocationException => {
            e.printStackTrace
          }
        }
      }

      def changedUpdate(documentEvent: DocumentEvent): Unit = {
        try {
          setValue(documentEvent.getDocument.getText(0, documentEvent.getDocument.getLength), elem, att, targetNode)
        }
        catch {
          case e: BadLocationException => {
            e.printStackTrace
          }
        }
      }
    })
    label.setLabelFor(textField)
    textField.setText(getValue(elem, att, targetNode))
    textField
  }


}