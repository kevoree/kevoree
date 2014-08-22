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
package org.kevoree.tools.ui.editor.listener

import java.awt.datatransfer.DataFlavor
import java.awt.dnd.{DropTarget, DropTargetDropEvent}
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent}
import java.awt.{BorderLayout, FlowLayout}
import javax.swing._

import com.explodingpixels.macwidgets.plaf.HudLabelUI
import com.explodingpixels.macwidgets.{HudWidgetFactory, HudWindow}
import org.kevoree._
import org.kevoree.factory.DefaultKevoreeFactory
import org.kevoree.kevscript.KevScriptEngine
import org.kevoree.modeling.api.util.ModelVisitor
import org.kevoree.modeling.api.{KMFContainer, ModelCloner}
import org.kevoree.tools.ui.editor.command.LoadModelCommand
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.tools.ui.editor.{KevoreeUIKernel, PositionedEMFHelper, UIHelper}
import org.kevoree.tools.ui.framework.elements.PortPanel

import scala.collection.JavaConversions._
import scala.util.Random

/**
 * User: ffouquet
 * Date: 08/09/11
 * Time: 10:52
 */

class PortDragTargetListener(target: PortPanel, kernel: KevoreeUIKernel) extends DropTarget {

  def checkChannelAvailability = {
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
    collected.filter(td => td.isInstanceOf[ChannelType]).size > 0
  }

  override def drop(p1: DropTargetDropEvent) {
    try {

      if (!checkChannelAvailability) {
        val hud = new HudWindow();
        hud.getJDialog.setTitle("Error !")
        hud.getJDialog.setSize(200, 100);
        hud.getJDialog.setLocationRelativeTo(null);

        val topPanel = new JPanel()
        topPanel.setOpaque(false)
        topPanel.setLayout(new SpringLayout())
        val line1 = new JLabel("No ChannelType is available.", SwingConstants.CENTER)
        line1.setOpaque(false)
        val line2 = new JLabel("Please load a ChannelType library.", SwingConstants.CENTER)
        line2.setOpaque(false)
        line1.setUI(new HudLabelUI)
        line2.setUI(new HudLabelUI)
        topPanel.add(line1)
        topPanel.add(line2)

        SpringUtilities.makeCompactGrid(topPanel, 2, 1, 6, 6, 6, 6)

        val button = HudWidgetFactory.createHudButton("Ok")
        button.addActionListener(new ActionListener {
          def actionPerformed(p1: ActionEvent) {
            hud.getJDialog.dispose()
          }
        })

        val bottomPanel = new JPanel(new FlowLayout())
        bottomPanel.add(button)

        hud.getContentPane.setLayout(new BorderLayout())
        hud.getContentPane.add(topPanel, BorderLayout.NORTH)
        hud.getContentPane.add(bottomPanel, BorderLayout.SOUTH)

        // hud.getJDialog.pack();
        hud.getJDialog.setVisible(true);

      } else {

        val draggedPanel = p1.getTransferable.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
        val targetPort: Port = kernel.getUifactory.getMapping.get(target).asInstanceOf[Port]
        val dropPort: Port = kernel.getUifactory.getMapping.get(draggedPanel).asInstanceOf[Port]
        /*
        if (portAspect.isProvidedPort(targetPort) && portAspect.isProvidedPort(dropPort) || portAspect.isRequiredPort(targetPort) && portAspect.isRequiredPort(dropPort)) {
        throw new Exception("Can't bind Port with same nature (Provided/Required)")
      }  */

        val hud = new HudWindow("Auto channel insertion, choose type definition ?");
        hud.getJDialog.setSize(400, 50);
        hud.getJDialog.setLocationRelativeTo(null);
        //hud.getJDialog.setDefaultCloseOperation();
        val model = new DefaultComboBoxModel()

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


        collected.filter(td => td.isInstanceOf[ChannelType]).foreach {
          c =>
            UIHelper.addItem(model, c.getName)
        }
        val comboBox = HudWidgetFactory.createHudComboBox(model)
        hud.getContentPane.add(comboBox)
        val button = HudWidgetFactory.createHudButton("Ok")
        button.addMouseListener(new MouseAdapter {
          override def mouseClicked(p1: MouseEvent) {


            val collected = new java.util.ArrayList[TypeDefinition]()
            kernel.getModelHandler.getActualModel.getPackages.foreach(p => {
              p.deepVisitContained(new ModelVisitor {
                override def visit(p1: KMFContainer, p2: String, p3: KMFContainer): Unit = {
                  if (p1.isInstanceOf[TypeDefinition]) {
                    if (p1.asInstanceOf[TypeDefinition].getName == comboBox.getSelectedItem) {
                      collected.add(p1.asInstanceOf[TypeDefinition])
                    }
                  }
                }
              })
            })


            if (collected.isEmpty) {
              return;
            }
            //val parser = new KevsParser()
            val newChannelName = collected.get(0).getName.substring(0, scala.math.min(collected.get(0).getName.length, 9)) + "" + scala.math.abs(new Random().nextInt(999))

            val script = new StringBuilder
            script.append("add " + newChannelName + " : " + collected.get(0).getName + "\n")
            script.append("bind " + targetPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName + "." + targetPort.eContainer.asInstanceOf[ComponentInstance].getName + "." + targetPort.getPortTypeRef.getName + " " + newChannelName + "\n")
            script.append("bind " + dropPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName + "." + dropPort.eContainer.asInstanceOf[ComponentInstance].getName + "." + dropPort.getPortTypeRef.getName + " " + newChannelName + "\n")
            PositionedEMFHelper.updateModelUIMetaData(kernel)
            val currentModel = kernel.getModelHandler.getCurrentModel.getModel
            val cloner = new ModelCloner(new DefaultKevoreeFactory())
            val cloned = cloner.clone(currentModel).asInstanceOf[ContainerRoot]
            val kevengine = new KevScriptEngine
            kevengine.execute(script.toString(), cloned)
            val loadCMD = new LoadModelCommand
            loadCMD.setKernel(kernel)
            loadCMD.execute(cloned)


            /*val script = new StringBuilder
            script.append("tblock{")
            script.append("addChannel " + newChannelName + " : " + selectedTypeDef.get.getName + "\n")
            script.append("bind " + targetPort.eContainer.asInstanceOf[ComponentInstance].getName + "." + targetPort.getPortTypeRef.getName + "@" + targetPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName + " => " + newChannelName + "\n")
            script.append("bind " + dropPort.eContainer.asInstanceOf[ComponentInstance].getName + "." + dropPort.getPortTypeRef.getName + "@" + dropPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName + " => " + newChannelName + "\n")
            script.append("}")
            val parsedScript = parser.parseScript(script.toString())
            parsedScript.map {
              script =>
                import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
                PositionedEMFHelper.updateModelUIMetaData(kernel)
                script.interpret(KevsInterpreterContext(kernel.getModelHandler.getActualModel))
                val file = File.createTempFile("kev", new Random().nextInt + "")
                KevoreeXmiHelper.instance$.save(file.getAbsolutePath, kernel.getModelHandler.getActualModel);
                val loadCMD = new LoadModelCommand
                loadCMD.setKernel(kernel)
                loadCMD.execute( file.getAbsolutePath)
            } */

            hud.getJDialog.dispose()


          }
        })
        hud.getContentPane.add(button)

        hud.getJDialog.setVisible(true);
      }
    } catch {
      case _@e => p1.rejectDrop();
    }
  }
}