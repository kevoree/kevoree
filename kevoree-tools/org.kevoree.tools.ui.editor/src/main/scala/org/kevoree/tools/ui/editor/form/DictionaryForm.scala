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
package org.kevoree.tools.ui.editor.form

import com.explodingpixels.macwidgets.HudWindow
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudCheckBoxUI, HudTextFieldUI, HudLabelUI}
import org.kevoree.tools.ui.editor.property.SpringUtilities
import java.awt.Dimension
import scala.collection.JavaConversions._
import org.kevoree.tools.ui.framework.data.KevoreeHudComboBoxUI
import org.kevoree.{KevoreeFactory, ComponentType}
import javax.swing._
import event._
import table.{TableModel, DefaultTableModel}
import java.awt.event.{FocusEvent, FocusListener, ActionEvent, ActionListener}
import java.util.regex.Pattern

trait DictionaryForm {

  private val values = new java.util.Vector[String](1)
  private var defaultValue = ""
  private val valuePanel: JPanel = new JPanel

  def createDictionaryPanel (window: HudWindow, kernel: KevoreeUIKernel,
    componentType: ComponentType): Tuple2[JPanel, JButton] = {
    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)

    val portNameTxt = new JTextField()
    portNameTxt.setUI(new HudTextFieldUI())
    val portNameLbl = new JLabel("Name: ", SwingConstants.TRAILING);
    portNameLbl.setUI(new HudLabelUI());
    portNameLbl.setOpaque(false);
    portNameLbl.setLabelFor(portNameTxt);
    layout.add(portNameLbl)
    layout.add(portNameTxt)

    val fragmentDependantCheckBox = new JCheckBox()
    fragmentDependantCheckBox.setUI(new HudCheckBoxUI())
    val fragmentDependantLabel = new JLabel("Fragment dependant:", SwingConstants.TRAILING);
    fragmentDependantLabel.setUI(new HudLabelUI());
    fragmentDependantLabel.setOpaque(false);
    fragmentDependantLabel.setLabelFor(fragmentDependantCheckBox);
    layout.add(fragmentDependantLabel)
    layout.add(fragmentDependantCheckBox)

    /*val definedValuesCheckBox = new JCheckBox()
    definedValuesCheckBox.setUI(new HudCheckBoxUI())
    val DefinedValuesLabel = new JLabel("Predefined value:", SwingConstants.TRAILING);
    DefinedValuesLabel.setUI(new HudLabelUI());
    DefinedValuesLabel.setOpaque(false);
    DefinedValuesLabel.setLabelFor(definedValuesCheckBox);
    definedValuesCheckBox.addChangeListener(new ChangeListener() {
      def stateChanged (e: ChangeEvent) {
        defineValue(valuePanel, e.getSource.asInstanceOf[JCheckBox].isSelected)
      }
    })
    layout.add(DefinedValuesLabel)
    layout.add(definedValuesCheckBox)*/

    /*defineValue(valuePanel, false)
    layout.add(new JLabel(""))
    layout.add(valuePanel)*/
    val datatypeTxt = new JTextField()
    datatypeTxt.setUI(new HudTextFieldUI())
    val datatypeLabel = new JLabel("Datatype: ", SwingConstants.TRAILING);
    datatypeLabel.setUI(new HudLabelUI());
    datatypeLabel.setOpaque(false);
    datatypeLabel.setLabelFor(datatypeTxt);
    layout.add(datatypeLabel)
    layout.add(datatypeTxt)

    val defaultValueTxt = new JTextField()
    defaultValueTxt.setUI(new HudTextFieldUI())
    val defaultValueLabel = new JLabel("Default value: ", SwingConstants.TRAILING);
    defaultValueLabel.setUI(new HudLabelUI());
    defaultValueLabel.setOpaque(false);
    defaultValueLabel.setLabelFor(defaultValueTxt);
    layout.add(defaultValueLabel)
    layout.add(defaultValueTxt)


    val optionalCheckBox = new JCheckBox()
    optionalCheckBox.setUI(new HudCheckBoxUI())
    val optionalLabel = new JLabel("Optional:", SwingConstants.TRAILING);
    optionalLabel.setUI(new HudLabelUI());
    optionalLabel.setOpaque(false);
    optionalLabel.setLabelFor(optionalCheckBox);
    layout.add(optionalLabel)
    layout.add(optionalCheckBox)

    //EXECUTE KEVSCRIPT COMMAND
    val btAdd = new JButton("Add Dictionary Attribute")
    btAdd.setUI(new HudButtonUI)
    btAdd.addActionListener(new ActionListener {
      def actionPerformed (p1: ActionEvent) {

        val dictionaryAttribute = KevoreeFactory.createDictionaryAttribute
        dictionaryAttribute.setName(portNameTxt.getText)
        dictionaryAttribute.setFragmentDependant(fragmentDependantCheckBox.isSelected)
        dictionaryAttribute.setOptional(optionalCheckBox.isSelected)
        // build enum values
        //(?:vals|enum)=\{\"([^\"]*)\"(?:,\"([^\"]*)\")*\}
        var pattern = Pattern.compile("(?:vals|enum)=\\{((\\\"[^\\\"]*\\\")(?:,(\\\"[^\\\"]*\\\"))*)\\}")
        var matcher = pattern.matcher(datatypeTxt.getText.toLowerCase)
        if (matcher.find()) {
          val stringBuilder = new StringBuilder
          stringBuilder append "enum="
          pattern = Pattern.compile("\\\"([^\\\"]*)\\\"")
          matcher = pattern.matcher(datatypeTxt.getText.toLowerCase)
          while (matcher.find()) {
            val value = matcher.group(1)
             if (stringBuilder.last != '=') {
                stringBuilder append ","
              }
              stringBuilder append value
          }


          /*values.foreach {
            value =>
              if (stringBuilder.last != '=') {
                stringBuilder append ","
              }
              stringBuilder append value
          }*/
          dictionaryAttribute.setDatatype(stringBuilder.toString())
        }

        val defaultDictionaryValue = KevoreeFactory.createDictionaryValue
        defaultDictionaryValue.setAttribute(dictionaryAttribute)
        defaultDictionaryValue.setValue(defaultValue)

        if (componentType.getDictionaryType.isEmpty) {
          componentType.setDictionaryType(Some(KevoreeFactory.createDictionaryType))
        }

        componentType.getDictionaryType.get.addAttributes(dictionaryAttribute)
        componentType.getDictionaryType.get.addDefaultValues(defaultDictionaryValue)

        kernel.getEditorPanel.getTypeEditorPanel.refresh()
      }
    })
    window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 5, 2, 6, 6, 6, 6)
    Tuple2(layout, btAdd)
  }

  private def defineValue (panel: JPanel, defaultValues: Boolean) {
    panel.removeAll()
    if (defaultValues) {
      val dm: DefaultTableModel = new DefaultTableModel
      updateValuesToModel(dm)
      val table: JTable = new JTable(dm)
      table.setShowGrid(true)
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
      table.setMinimumSize(new Dimension(100, 100))
      table.setSize(new Dimension(100, 100))
      val scrollTable: JScrollPane = new JScrollPane(table)
      scrollTable.setColumnHeader(null)
      scrollTable.setMinimumSize(new Dimension(100, 80))
      val tableBox: Box = new Box(BoxLayout.Y_AXIS)
      tableBox.add(scrollTable)
      table.getModel.addTableModelListener(new TableModelListener {
        def tableChanged (e: TableModelEvent) {
          val row: Int = e.getFirstRow
          val column: Int = e.getColumn
          if (row >= 0 && column >= 0) {
            val model: TableModel = e.getSource.asInstanceOf[TableModel]
            val data: String = model.getValueAt(row, column).toString
            if (row == values.size) {
              if (!(data == "")) {
                values.add(data)
                updateValuesToModel(model.asInstanceOf[DefaultTableModel])
              }
            }
            else {
              if (data == "") {
                values.remove(row)
                updateValuesToModel(model.asInstanceOf[DefaultTableModel])
              }
            }
          }
        }
      })

      val setOfValuesComboxBoxLabel = new JLabel("Values:", SwingConstants.TRAILING);
      setOfValuesComboxBoxLabel.setUI(new HudLabelUI());
      setOfValuesComboxBoxLabel.setOpaque(false);
      setOfValuesComboxBoxLabel.setLabelFor(table);

      val defaultValueComboxBoxModel = new DefaultComboBoxModel
      updateValuesToModel(defaultValueComboxBoxModel)
      val defaultValueComboxBox = new JComboBox(defaultValueComboxBoxModel)
      defaultValueComboxBox.setUI(new KevoreeHudComboBoxUI())

      val defaultValueComboxBoxLabel = new JLabel("Default value:", SwingConstants.TRAILING);
      defaultValueComboxBoxLabel.setUI(new HudLabelUI());
      defaultValueComboxBoxLabel.setOpaque(false);
      defaultValueComboxBoxLabel.setLabelFor(defaultValueComboxBox);
      defaultValueComboxBox.getModel.addListDataListener(new ListDataListener() {
        def intervalAdded (e: ListDataEvent) {}

        def intervalRemoved (e: ListDataEvent) {}

        def contentsChanged (e: ListDataEvent) {
          defaultValue = e.getSource.asInstanceOf[ComboBoxModel].getSelectedItem.toString
        }
      })

      panel.add(defaultValueComboxBoxLabel)
      panel.add(table)
      panel.add(setOfValuesComboxBoxLabel)
      panel.add(defaultValueComboxBox)
    } else {
      val portNameTxt = new JTextField()
      portNameTxt.setUI(new HudTextFieldUI())
      val portNameLbl = new JLabel("Default Value: ", SwingConstants.TRAILING);
      portNameLbl.setUI(new HudLabelUI());
      portNameLbl.setOpaque(false);
      portNameLbl.setLabelFor(portNameTxt);

      portNameTxt.addFocusListener(new FocusListener() {
        def focusGained (e: FocusEvent) {}

        def focusLost (e: FocusEvent) {
          defaultValue = e.getSource.asInstanceOf[JTextField].getText
        }
      })

      panel.add(portNameLbl)
      panel.add(portNameTxt)
    }
    panel.repaint()
  }

  private def updateValuesToModel (model: DefaultComboBoxModel) {
    model.removeAllElements()
    values.foreach {
      value => model.addElement(value)
    }
  }

  private def updateValuesToModel (model: DefaultTableModel) {
    val dummyHeader = new java.util.Vector[String](1)
    dummyHeader.addElement("")
    model.setDataVector(getVectorFromValues, dummyHeader)
  }

  private def getVectorFromValues: java.util.Vector[_] = {
    val vector = new java.util.Vector[java.util.Vector[String]](1)
    for (value <- values) {
      val v = new java.util.Vector[String](1)
      v.addElement(value)
      vector.addElement(v)
    }
    val v = new java.util.Vector[String](1)
    v.addElement("")
    vector.addElement(v)
    vector
  }
}