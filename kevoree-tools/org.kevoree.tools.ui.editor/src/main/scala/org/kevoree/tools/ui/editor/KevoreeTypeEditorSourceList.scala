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

import com.explodingpixels.macwidgets._
import com.explodingpixels.widgets.PopupMenuCustomizer
import command.{AddComponentTypeElementUICommand}
import javax.swing.{JMenuItem, JPopupMenu, JSplitPane}
import java.awt.event.{ActionEvent, ActionListener}
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.tools.ui.framework.data.KevoreeSourceListItem
import org.kevoree.{PortTypeRef, ComponentType, TypeDefinition}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/10/11
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */

class KevoreeTypeEditorSourceList(pane: JSplitPane, kernel: KevoreeUIKernel, typeDef: TypeDefinition) {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val dictionaryCategoryLabel = "Dictionary"
  private val portsCategoryLabel = "Ports"
  private val model: SourceListModel = new SourceListModel()
  private val sourceList: SourceList = new SourceList(model)
  private val controlBar: SourceListControlBar = new SourceListControlBar();

  //INIT Procedure

  sourceList.installSourceListControlBar(controlBar)
  controlBar.installDraggableWidgetOnSplitPane(pane)
  sourceList.setColorScheme(new SourceListDarkColorScheme());
  sourceList.useIAppStyleScrollBars()
  initializeControlBar()
  initializeCategories()

  //END INIT Procedure


  def getComponent = {
    sourceList.getComponent
  }

  private def initializeControlBar() {

    //Creation of the Plus button
    controlBar.createAndAddPopdownButton(MacIcons.PLUS,
      new PopupMenuCustomizer() {
        def customizePopup(popup: JPopupMenu) {
          popup.removeAll();
          val cmd = new AddComponentTypeElementUICommand
          cmd.setKernel(kernel)
          logger.debug("Setting typeDef of AddComponentTypeElementUICommand to " + typeDef.asInstanceOf[ComponentType])
          cmd.setComponentType(typeDef.asInstanceOf[ComponentType])

          val libMenuItem = new JMenuItem("Add Port");
          libMenuItem.addActionListener(new ActionListener {
            def actionPerformed(p1: ActionEvent) {
              cmd.execute(cmd.portLabel)
            }
          });
          popup.add(libMenuItem);

          val deployUnitMenuItem = new JMenuItem("Add Dictionary");
          deployUnitMenuItem.addActionListener(new ActionListener {
            def actionPerformed(p1: ActionEvent) {
              cmd.execute(cmd.dictionaryLabel)
            }
          });
          popup.add(deployUnitMenuItem);
        }
      }
    )

    //Creation of the Minus button
    controlBar.createAndAddButton(MacIcons.MINUS,
      new ActionListener {
        def actionPerformed(p1: ActionEvent) {
          logger.debug("Remove action required")
          val kevObject = sourceList.getSelectedItem.asInstanceOf[KevoreeSourceListItem].getKevoreeObject
          if(kevObject != null) {
            logger.debug("KevoreeObject:" + kevObject)
            kevObject match {
              case ptRef : PortTypeRef => {
                ptRef.setEContainer(null,None)
                kernel.getEditorPanel.getTypeEditorPanel.refresh()
              }
              case _@e => logger.warn("KevoreeObject matches no entry")
            }
          }
        }
      }
    )

  }

  private def initializeCategories() {
    logger.debug("Initializing categories...")

    val categoryDic = new SourceListCategory(dictionaryCategoryLabel) {
      var itemCount = 0

      def setItemCount(i: Int) {
        itemCount = i
      }

      override def getItemCount: Int = itemCount
    }

    model.addCategory(categoryDic)
    sourceList.setExpanded(categoryDic, false)

    val categoryPorts = new SourceListCategory(portsCategoryLabel) {
      var itemCount = 0

      def setItemCount(i: Int) {
        itemCount = i
      }

      override def getItemCount: Int = itemCount
    }
    model.addCategory(categoryPorts)
    sourceList.setExpanded(categoryPorts, false)

    logger.debug("Categories initialization done. " + model.getCategories.size() + " categories in model.")
  }


  def refresh() {
    logger.debug("Refreshing SourceList items")
    updateDictionary()
    updatePorts()
    logger.debug("Done refreshing")

  }

  private def updateDictionary() {
    import scala.collection.JavaConversions._

    model.getCategories.toList.find(cat => cat.getText.equals(dictionaryCategoryLabel)).map {
      categoryDic =>
        while(categoryDic.getItems.size>0) {
          model.removeItemFromCategoryAtIndex(categoryDic,0)
        }
        typeDef.getDictionaryType.map {
          dic =>
            dic.getAttributes.foreach {
              dicAtt =>
                if (!categoryDic.getItems.toList.exists(cat => cat.getText.equals(dicAtt.getName))) {
                  val item = new KevoreeSourceListItem(dicAtt)
                  model.addItemToCategory(item, categoryDic)
                }
              //categoryDic.setItemCount(categoryDic.getItemCount + 1)
            }
        }
    }
  }

  private def updatePorts() {
    import scala.collection.JavaConversions._

    model.getCategories.toList.find(cat => cat.getText.equals(portsCategoryLabel)).map {
      portsCategory =>
        while(portsCategory.getItems.size>0) {
          model.removeItemFromCategoryAtIndex(portsCategory,0)
        }
        typeDef match {
          case componentType: ComponentType => {
            if (componentType.getProvided.size > 0) {
              val providedItem = getOrAddItemInCategory(portsCategory, "Provided")
              componentType.getProvided.foreach {
                pp =>
                  if (!providedItem.getChildItems.toList.exists(provPort => provPort.getText.equals(pp.getName))) {
                    val portItem = new KevoreeSourceListItem(pp)
                    model.addItemToItem(portItem, providedItem)
                  }
              }
            }
            if (componentType.getRequired.size > 0) {
              val requiredItem = getOrAddItemInCategory(portsCategory, "Required")
              componentType.getRequired.foreach {
                rp =>
                  if (!requiredItem.getChildItems.toList.exists(reqPort => reqPort.getText.equals(rp.getName))) {
                    val portItem = new KevoreeSourceListItem(rp)
                    model.addItemToItem(portItem, requiredItem)
                  }
              }
            }
          }
          case _@e => logger.warn("No update implemented for type definition:" + e.getClass)
        }
    }
  }

  private def getOrAddItemInCategory(category: SourceListCategory, itemTitle: String): SourceListItem = {
    import scala.collection.JavaConversions._
    category.getItems.toList.find(it => it.getText.equals(itemTitle)) match {
      case None => {
        val item = new KevoreeSourceListItem(itemTitle)
        model.addItemToCategory(item, category)
        item
      }
      case Some(item) => item
    }
  }

}