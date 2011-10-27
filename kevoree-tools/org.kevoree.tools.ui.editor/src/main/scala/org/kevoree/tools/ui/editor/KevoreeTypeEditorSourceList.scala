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

import javax.swing.JSplitPane
import org.kevoree.TypeDefinition
import com.explodingpixels.macwidgets._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/10/11
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */

class KevoreeTypeEditorSourceList(pane: JSplitPane, kernel: KevoreeUIKernel, typeDef: TypeDefinition) {

  val model = new SourceListModel()
  val sourceList = new SourceList(model)

  def getComponent = {
    sourceList.getComponent
  }


  var controlBar = new SourceListControlBar();
  sourceList.installSourceListControlBar(controlBar)
  controlBar.installDraggableWidgetOnSplitPane(pane)
  sourceList.setColorScheme(new SourceListDarkColorScheme());
  sourceList.useIAppStyleScrollBars()

  def refresh() : Unit = {
    val categoryDic = new SourceListCategory("Dictionary") {
      var itemCount = 0

      def setItemCount(i: Int) {
        itemCount = i
      }

      override def getItemCount: Int = itemCount
    }
    model.addCategory(categoryDic)
    typeDef.getDictionaryType.map {
      dic =>
        dic.getAttributes.foreach {
          dicAtt =>
            val item = new SourceListItem(dicAtt.getName)
            model.addItemToCategory(item, categoryDic);
            categoryDic.setItemCount(categoryDic.getItemCount + 1)
        }
    }


  }


}