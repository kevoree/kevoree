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

import scala.collection.JavaConversions._
import com.explodingpixels.macwidgets._
import javax.swing._
import java.awt.datatransfer.{DataFlavor, Transferable}
import java.util.HashMap
import org.kevoree.tools.ui.framework.elements.{GroupTypePanel, NodeTypePanel, ChannelTypePanel, ComponentTypePanel}
import java.awt.event.InputEvent
import java.awt.{Cursor, Color, Graphics, Component}
;

/**
 * User: ffouquet
 * Date: 16/06/11
 * Time: 22:38
 */

class TypeIcon(c: Color) extends Icon {
  def getIconHeight = 12

  def getIconWidth = 12

  def paintIcon(p1: Component, p2: Graphics, p3: Int, p4: Int) {
    p2.setColor(c)
    p2.fillRect(0, 4, 12, 12)
  }
}

object channelIcon extends TypeIcon(new Color(255, 127, 36, 200))

object componentIcon extends TypeIcon(new Color(0, 0, 0, 200))

object nodeIcon extends TypeIcon(new Color(100, 100, 100, 200))

object groupIcon extends TypeIcon(new Color(56, 171, 67, 200))

class TypeDefinitionSourceList(pane: JSplitPane, kernel: KevoreeUIKernel) {

  val model = new SourceListModel()
  val sourceList = new SourceList(model);

  def getTypeIcon(p: JPanel): TypeIcon = {
    p match {
      case c: ComponentTypePanel => componentIcon
      case c: ChannelTypePanel => channelIcon
      case c: NodeTypePanel => nodeIcon
      case c: GroupTypePanel => groupIcon
      case _ => null
    }
  }

  sourceList.setTransferHandler(new TransferHandler() {


    override def exportAsDrag(p1: JComponent, p2: InputEvent, p3: Int) {
      kernel.getModelPanel.setFlightObject(map.get(sourceList.getSelectedItem))
      kernel.getModelPanel.repaint()
      kernel.getModelPanel.revalidate()
      super.exportAsDrag(p1, p2, p3)
    }

    override def exportDone(p1: JComponent, p2: Transferable, p3: Int) {
      kernel.getModelPanel.unsetFlightObject(map.get(sourceList.getSelectedItem))
      kernel.getModelPanel.repaint()
      kernel.getModelPanel.revalidate()
      super.exportDone(p1, p2, p3)
    }

    protected override def createTransferable(c: JComponent): Transferable = {
      return new Transferable {
        def getTransferDataFlavors: Array[DataFlavor] = {
          return new Array[DataFlavor](0)
        }

        def isDataFlavorSupported(dataFlavor: DataFlavor): Boolean = {
          return true
        }

        def getTransferData(dataFlavor: DataFlavor): AnyRef = {
          return map.get(sourceList.getSelectedItem)
        }
      }
    }

    override def getSourceActions(c: JComponent): Int = {
      return TransferHandler.COPY
    }
  })

  var controlBar = new SourceListControlBar();
  sourceList.installSourceListControlBar(controlBar)
  controlBar.installDraggableWidgetOnSplitPane(pane)
  sourceList.setColorScheme(new SourceListDarkColorScheme());
  sourceList.useIAppStyleScrollBars()

  def getComponent = {
    sourceList.getComponent
  }

  var map = new java.util.HashMap[SourceListItem, JPanel]()

  def clear {
    map.clear()
    model.getCategories.toList.foreach {
      categ =>
        model.removeCategory(categ)
    }
  }


  def getCategoryOrAdd(libName: String): SourceListCategory = {
    model.getCategories.find(categ => categ.getText == libName) match {
      case Some(e) => e
      case None => {
        val category = new SourceListCategory(libName)
        model.addCategory(category)
        sourceList.setExpanded(category, false)
        category
      }
    }
  }



  def updateTypeValue(value:Int,typeName : String){
      model.getCategories.foreach{ categ =>
          categ.getItems.foreach{ item =>
              if(item.getText == typeName){
                item.setCounterValue(value)
              }
          }
      }

  }
  def addTypeDefinitionPanel(ctp: JPanel, libName: String, typeName: String) {
    val categ = getCategoryOrAdd(libName)
    categ.getItems.find(item => item.getText == typeName) match {
      case Some(item) =>
      case None => {
        if (typeName != null) {
          val item = new SourceListItem(typeName)
          map.put(item, ctp)
          //item.setCounterValue(3)
          item.setIcon(getTypeIcon(ctp))
          model.addItemToCategory(item, categ);
        }

      }
    }

  }

}
