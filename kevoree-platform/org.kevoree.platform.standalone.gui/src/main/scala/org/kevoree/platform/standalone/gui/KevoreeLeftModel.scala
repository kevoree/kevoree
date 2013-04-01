/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.platform.standalone.gui

import com.explodingpixels.macwidgets._
import javax.swing.{SwingUtilities, JPanel}
import org.kevoree.{ContainerRoot, ContainerNode}
import scala.collection.JavaConversions._
import org.kevoree.framework.kaspects.ChannelAspect

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/03/12
 * Time: 20:38
 */

class KevoreeLeftModel extends JPanel {

  val model = new SourceListModel()
  val sourceList = new SourceList(model)
  private val channelAspect = new ChannelAspect()

  def getSourceList = sourceList


  // sourceList.setColorScheme(new SourceListDarkColorScheme())


  def reload(kmodel: ContainerNode) {
    if (model.getCategories.size() > 0) {
      model.removeCategoryAt(0)
    }

    val category = new SourceListCategory(kmodel.getName)
    val componentItem = new SourceListItem("Components")
    val channelItem = new SourceListItem("Channels")
    val groupItem = new SourceListItem("Groups")
    val childItem = new SourceListItem("ChildNodes")
    model.addCategory(category)
    model.addItemToCategory(componentItem, category)
    model.addItemToCategory(channelItem, category)
    model.addItemToCategory(groupItem, category)
    model.addItemToCategory(childItem, category)

    kmodel.getComponents.foreach {
      c =>
        val itc = new SourceListItem(c.getName + ":" + c.getTypeDefinition.getName)
        model.addItemToItem(itc, componentItem)
    }

    kmodel.eContainer.asInstanceOf[ContainerRoot].getHubs.filter(h => channelAspect.getRelatedNodes(h).exists(c => c.getName == kmodel.getName)).foreach {
      c =>
        val itc = new SourceListItem(c.getName + ":" + c.getTypeDefinition.getName)
        model.addItemToItem(itc, channelItem)
    }
    kmodel.eContainer.asInstanceOf[ContainerRoot].getGroups.filter(g => g.getSubNodes.exists(c => c.getName == kmodel.getName)).foreach{g =>
      val itc = new SourceListItem(g.getName + ":" + g.getTypeDefinition.getName)
      model.addItemToItem(itc, groupItem)
    }
    
    kmodel.eContainer.asInstanceOf[ContainerRoot].getNodes.find(n => n.getName == kmodel.getName) match {
      case None =>
      case Some(n) => {
        n.getHosts.foreach{
          child =>  {
            val itc = new SourceListItem(child.getName + ":" + child.getTypeDefinition.getName)
            model.addItemToItem(itc, childItem)
          }
        }
      }
    }



    sourceList.useIAppStyleScrollBars()
    //sourceList.setExpanded(category, false)

    SwingUtilities.invokeLater(new Runnable() {
      def run() {
        doLayout()
        repaint()
        revalidate()
      }
    })
  }


}
