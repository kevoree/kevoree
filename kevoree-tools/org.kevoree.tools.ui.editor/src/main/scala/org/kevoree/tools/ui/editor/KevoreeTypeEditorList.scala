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

import javax.swing.table.DefaultTableModel
import com.explodingpixels.macwidgets.IAppWidgetFactory
import java.awt.BorderLayout

import org.kevoree.{ComponentType, TypeDefinition}
import com.explodingpixels.macwidgets.plaf.ITunesTableUI
import javax.swing.{JTable, BorderFactory, JScrollPane, JPanel}

/**
 * User: ffouquet
 * Date: 26/08/11
 * Time: 10:55
 */

class KevoreeTypeEditorList(typeDefinition: TypeDefinition, uikernel: KevoreeUIKernel) extends JPanel {

  val model = new DefaultTableModel()
  model.addColumn("Name")
  model.addColumn("Nature")
  model.addColumn("Type")
  model.addColumn("DefValue")

  var table = new JTable(model) {
    override def isCellEditable(p1: Int, p2: Int) = {
      p2 match {
        case 1 => false
        case _ => true
      }
    }
  }
  table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
  table.setUI(new ITunesTableUI());
  table.getColumnModel.getColumn(0).setPreferredWidth(150)
  table.getColumnModel.getColumn(1).setPreferredWidth(120)
  table.getColumnModel.getColumn(2).setPreferredWidth(200)
  table.getColumnModel.getColumn(3).setPreferredWidth(100)
  // val table = MacWidgetFactory.createITunesTable(model);
  //table.set
  val scrollPane = new JScrollPane(table);
  scrollPane.setBorder(BorderFactory.createEmptyBorder());
  IAppWidgetFactory.makeIAppScrollPane(scrollPane);
  this.setLayout(new BorderLayout())
  this.add(scrollPane, BorderLayout.CENTER)
  reload()

  /* END Constructor */

  /* RELOAD ALL TYPE INFORMATIONS */
  def reload() {
    if (typeDefinition != null) {
      if (typeDefinition.getDictionaryType != null) {
        typeDefinition.getDictionaryType.getAttributes.foreach {
          at =>
            val defValue: String = typeDefinition.getDictionaryType.getDefaultValues.find(dv => dv.getAttribute == at).map(dv => dv.getValue).getOrElse("")
            model.addRow(Array[AnyRef](at.getName, "Property", at.getDatatype, defValue))
        }
      }
      typeDefinition match {
        case c: ComponentType => {
          c.getProvided.foreach {
            ppref =>
              model.addRow(Array[AnyRef](ppref.getName, "ProvidedPortType", "", ""))
          }
          c.getRequired.foreach {
            rpref =>
              model.addRow(Array[AnyRef](rpref.getName, "RequiredPortType", "", ""))
          }
        }
        case _ => // NO OTHER SPECIAL CASE
      }

    }

  }


}