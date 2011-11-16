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
package org.kevoree.tools.ui.editor.listener

import org.kevoree.tools.ui.framework.elements.PortPanel
import java.awt.dnd.{DropTargetDropEvent, DropTarget}
import java.awt.datatransfer.DataFlavor
import com.explodingpixels.macwidgets.{HudWidgetFactory, HudWindow}

import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.tools.marShell.parser.KevsParser
import util.Random
import org.kevoree.{ComponentInstance, ContainerNode, Port, ChannelType}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import org.kevoree.framework.KevoreeXmiHelper
import java.io.File
import org.kevoree.tools.ui.editor.command.LoadModelCommand
import java.awt.event.{ActionEvent, ActionListener, MouseEvent, MouseAdapter}
import java.awt.{FlowLayout, BorderLayout}
import org.kevoree.tools.ui.editor.property.SpringUtilities
import javax.swing._
import com.explodingpixels.macwidgets.plaf.HudLabelUI

/**
 * User: ffouquet
 * Date: 08/09/11
 * Time: 10:52
 */

class PortDragTargetListener(target: PortPanel, kernel: KevoreeUIKernel) extends DropTarget {

  def checkChannelAvailability = {
    kernel.getModelHandler.getActualModel.getTypeDefinitions.filter(td => td.isInstanceOf[ChannelType]).size > 0
  }

  override def drop(p1: DropTargetDropEvent) {
    try {

      if(!checkChannelAvailability){
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
          def actionPerformed(p1: ActionEvent) {hud.getJDialog.dispose()}
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
      if (targetPort.isProvidedPort && dropPort.isProvidedPort || targetPort.isRequiredPort && dropPort.isRequiredPort) {
        throw new Exception("Can't bind Port with same nature (Provided/Required)")
      }

      val hud = new HudWindow("Auto channel insertion, choose type definition ?");
      hud.getJDialog.setSize(400, 50);
      hud.getJDialog.setLocationRelativeTo(null);
      //hud.getJDialog.setDefaultCloseOperation();
      val model = new DefaultComboBoxModel()
      kernel.getModelHandler.getActualModel.getTypeDefinitions.filter(td => td.isInstanceOf[ChannelType]).foreach {
        c =>
          model.addElement(c.getName)
      }
      val comboBox = HudWidgetFactory.createHudComboBox(model)
      hud.getContentPane.add(comboBox)
      val button = HudWidgetFactory.createHudButton("Ok")
      button.addMouseListener(new MouseAdapter {
        override def mouseClicked(p1: MouseEvent) {

          val selectedTypeDef = kernel.getModelHandler.getActualModel.getTypeDefinitions.find(td => td.getName == comboBox.getSelectedItem)
          if (selectedTypeDef.isEmpty) {
            return;
          }
          val parser = new KevsParser()
          val newChannelName = selectedTypeDef.get.getName.substring(0, scala.math.min(selectedTypeDef.get.getName.length, 9)) + "" + scala.math.abs(new Random().nextInt(999))
          val script = new StringBuilder
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
              KevoreeXmiHelper.save(file.getAbsolutePath, kernel.getModelHandler.getActualModel);
              val loadCMD = new LoadModelCommand
              loadCMD.setKernel(kernel)
              loadCMD.execute( file.getAbsolutePath)
              hud.getJDialog.dispose()
          }
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