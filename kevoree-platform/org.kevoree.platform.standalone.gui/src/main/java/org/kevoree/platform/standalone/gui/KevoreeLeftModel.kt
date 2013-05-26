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

import org.kevoree.framework.kaspects.ChannelAspect
import javax.swing.JPanel
import com.explodingpixels.macwidgets.SourceListModel
import com.explodingpixels.macwidgets.SourceList
import org.kevoree.ContainerNode
import com.explodingpixels.macwidgets.SourceListCategory
import com.explodingpixels.macwidgets.SourceListItem
import org.kevoree.ContainerRoot
import javax.swing.SwingUtilities

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/03/12
 * Time: 20:38
 */

class KevoreeLeftModel: JPanel() {

    val model = SourceListModel()
    val _sourceList = SourceList(model)
    private val channelAspect = ChannelAspect()

    fun getSourceList(): SourceList {
        return _sourceList
    }

    fun reload(kmodel: ContainerNode) {
        if (model.getCategories()!!.size() > 0) {
            model.removeCategoryAt(0)
        }

        val category = SourceListCategory(kmodel.getName())
        val componentItem = SourceListItem("Components")
        val channelItem = SourceListItem("Channels")
        val groupItem = SourceListItem("Groups")
        val childItem = SourceListItem("ChildNodes")
        model.addCategory(category)
        model.addItemToCategory(componentItem, category)
        model.addItemToCategory(channelItem, category)
        model.addItemToCategory(groupItem, category)
        model.addItemToCategory(childItem, category)
        kmodel.getComponents().forEach {
            c ->
            val itc = SourceListItem(c.getName() + ":" + c.getTypeDefinition()!!.getName())
            model.addItemToItem(itc, componentItem)
        }
        (kmodel.eContainer() as ContainerRoot).getHubs().filter{ h ->
            channelAspect.getRelatedNodes(h).any{ c -> c.getName() == kmodel.getName() }
        }.forEach {
            c ->
            val itc = SourceListItem(c.getName() + ":" + c.getTypeDefinition()!!.getName())
            model.addItemToItem(itc, channelItem)
        }
        (kmodel.eContainer() as ContainerRoot).getGroups().filter{ g -> g.getSubNodes().any{ c -> c.getName() == kmodel.getName() } }.forEach{ g ->
            val itc = SourceListItem(g.getName() + ":" + g.getTypeDefinition()!!.getName())
            model.addItemToItem(itc, groupItem)
        }

        val nRes = (kmodel.eContainer() as ContainerRoot).getNodes().find{ n -> n.getName() == kmodel.getName() }
        if(nRes != null){
            for(child in nRes.getHosts()){
                val itc = SourceListItem(child.getName() + ":" + child.getTypeDefinition()!!.getName())
                model.addItemToItem(itc, childItem)
            }
        }

        try {
            _sourceList.useIAppStyleScrollBars()

        } catch(e: Exception){

        }

        SwingUtilities.invokeLater(object : Runnable {
            override fun run() {
                doLayout()
                repaint()
                revalidate()
            }
        })
    }
}



