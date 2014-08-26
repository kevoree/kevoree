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
package org.kevoree.tools.ui.editor.command


import javax.swing.JPanel

import org.kevoree._
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.modeling.api.util.ModelVisitor
import org.kevoree.tools.ui.editor._

import scala.collection.JavaConversions._


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
        val loadedLib = List[TypeDefinition]()
        val collected = new java.util.ArrayList[TypeDefinition]()
        kernel.getModelHandler.getActualModel.getPackages.foreach(p => {
          p.deepVisitContained(new ModelVisitor {
            override def visit(p1: KMFContainer, p2: String, p3: KMFContainer): Unit = {
              if (p1.isInstanceOf[TypeDefinition]) {
                collected.add(p1.asInstanceOf[TypeDefinition])
              }
            }
          })
        })

        collected.filter(typeDef => !loadedLib.contains(typeDef)).sortWith((x, y) => x.getName().charAt(0).toLower < y.getName().charAt(0).toLower).foreach {
          typeDef =>
            if (!typeDef.getAbstract) {
              typeDefPanelFactory(typeDef).map {
                typeDefPanel =>
                  palette.addTypeDefinitionPanel(typeDefPanel, "default", typeDef.getName)

              }
            }

        }

      }
      case DeployUnitMode => {
        model.deepVisitContained(new ModelVisitor {
          override def visit(p1: KMFContainer, p2: String, p3: KMFContainer): Unit = {
            if (p1.isInstanceOf[DeployUnit]) {
              val deployUnit = p1.asInstanceOf[DeployUnit]
              palette.getCategoryOrAdd(deployUnit.getName)
              val collected = new java.util.ArrayList[TypeDefinition]()
              kernel.getModelHandler.getActualModel.getPackages.foreach(p => {
                p.deepVisitContained(new ModelVisitor {
                  override def visit(p1: KMFContainer, p2: String, p3: KMFContainer): Unit = {
                    if (p1.isInstanceOf[TypeDefinition]) {
                      collected.add(p1.asInstanceOf[TypeDefinition])
                    }
                  }
                })
              })
              collected.sortWith((x, y) => x.getName().charAt(0).toLower < y.getName().charAt(0).toLower).foreach {
                typeDef =>
                  if (!typeDef.getAbstract) {
                    typeDefPanelFactory(typeDef).map {
                      typeDefPanel =>
                        palette.addTypeDefinitionPanel(typeDefPanel, deployUnit.getName, typeDef.getName)

                    }
                  }

              }

            }
          }
        })
      }
    }
    val collected = new java.util.ArrayList[TypeDefinition]()
    kernel.getModelHandler.getActualModel.getPackages.foreach(p => {
      p.deepVisitContained(new ModelVisitor {
        override def visit(p1: KMFContainer, p2: String, p3: KMFContainer): Unit = {
          if (p1.isInstanceOf[TypeDefinition]) {
            collected.add(p1.asInstanceOf[TypeDefinition])
          }
        }
      })
    })


    collected.foreach {
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