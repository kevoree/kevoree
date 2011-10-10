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
package org.kevoree.tools.ui.editor.command


import org.kevoree.tools.ui.framework.elements.{GroupTypePanel, NodeTypePanel, ChannelTypePanel, ComponentTypePanel}
import org.kevoree._
import javax.swing.JPanel
import tools.ui.editor._

/**
 * User: ffouquet
 * Date: 28/07/11
 * Time: 22:49
 */

class ReloadTypePalette extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  def execute(p: Object) {

    val model = kernel.getModelHandler.getActualModel
    val palette = kernel.getEditorPanel.getPalette
    palette.clear


    TypeDefinitionPaletteMode.getCurrentMode match {
      case LibraryMode => {
        var loadedLib = List[TypeDefinition]()
        model.getLibraries.foreach {
          library =>
            palette.getCategoryOrAdd(library.getName)
            library.getSubTypes.foreach {
              subTypeDef =>
                loadedLib = loadedLib ++ List(subTypeDef)
                typeDefPanelFactory(subTypeDef).map {
                  typeDefPanel =>
                    palette.addTypeDefinitionPanel(typeDefPanel, library.getName, subTypeDef.getName)

                }
            }

        }
        model.getTypeDefinitions.filter(typeDef => !loadedLib.contains(typeDef)).foreach {
          typeDef =>
            typeDefPanelFactory(typeDef).map {
              typeDefPanel =>
                palette.addTypeDefinitionPanel(typeDefPanel, "default", typeDef.getName)

            }

        }

      }
      case DeployUnitMode => {
        model.getDeployUnits.foreach {
          deployUnit =>
            model.getTypeDefinitions.filter(t => t.getDeployUnits.exists(du => du == deployUnit)).foreach {
              typeDef =>
                typeDefPanelFactory(typeDef).map {
                  typeDefPanel =>
                    palette.addTypeDefinitionPanel(typeDefPanel, deployUnit.getUnitName, typeDef.getName)

                }

            }


        }
      }


    }

    model.getTypeDefinitions.foreach {
      ct =>
        palette.updateTypeValue(ModelHelper.getTypeNbInstance(model, ct), ct.getName)
    }

  }

  def typeDefPanelFactory(typeDef: TypeDefinition): Option[JPanel] = {
    typeDef match {
      case ct: ComponentType => Some(kernel.getUifactory.createComponentTypeUI(ct))
      case ct: ChannelType => Some(kernel.getUifactory.createChannelTypeUI(ct))
      case nt: NodeType => Some(kernel.getUifactory.createNodeTypeUI(nt))
      case gt: GroupType => Some(kernel.getUifactory.createGroupTypeUI(gt))
      case _ => None
    }
  }


}