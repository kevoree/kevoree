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

import javax.swing.JPanel
import org.kevoree.api.service.core.checker.CheckerViolation
import com.explodingpixels.macwidgets._
import java.awt.{BorderLayout, Dimension}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 28/09/11
 * Time: 18:18
 * To change this template use File | Settings | File Templates.
 */

object ErrorPanel {

  var layout = new JPanel()
  layout.setLayout(new BorderLayout())

  var model: SourceListModel = null
  var sourceList: SourceList = null
  var errorCateg: SourceListCategory = null
  //var warnCateg: SourceListCategory = null
  init()

  def getPanel: JPanel= {
    layout
  }

  def init() {
    model = new SourceListModel()
    sourceList = new SourceList(model)
    sourceList.setColorScheme(new SourceListDarkColorScheme());
    sourceList.useIAppStyleScrollBars()
    errorCateg = new SourceListCategory("Error")
    model.addCategory(errorCateg)
    layout.add(sourceList.getComponent,BorderLayout.CENTER)
  }

  def clear() {
    model.removeCategory(errorCateg)
    errorCateg = new SourceListCategory("Error")
    model.addCategory(errorCateg)
  }

  def displayError(cv: CheckerViolation) {
    val item = new SourceListItem(cv.getMessage)
    model.addItemToCategory(item, errorCateg)

    layout.repaint()
    layout.revalidate()


  }


}