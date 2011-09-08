package org.kevoree.tools.ui.editor.listener

import org.kevoree.tools.ui.framework.elements.PortPanel
import java.awt.dnd.{DropTargetDropEvent, DropTarget}
import java.awt.datatransfer.DataFlavor
import com.explodingpixels.macwidgets.{HudWidgetFactory, HudWindow}
import javax.swing.DefaultComboBoxModel
import scala.collection.JavaConversions._
import java.awt.event.{MouseEvent, MouseAdapter}
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.tools.marShell.parser.KevsParser
import util.Random
import org.kevoree.{ComponentInstance, ContainerNode, Port, ChannelType}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import org.kevoree.framework.KevoreeXmiHelper
import java.io.{File, ByteArrayOutputStream}
import org.kevoree.tools.ui.editor.command.LoadModelCommand

/**
 * User: ffouquet
 * Date: 08/09/11
 * Time: 10:52
 */

class PortDragTargetListener(target: PortPanel, kernel: KevoreeUIKernel) extends DropTarget {
  override def drop(p1: DropTargetDropEvent) {
    try {
      val draggedPanel = p1.getTransferable.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
      val targetPort: Port = kernel.getUifactory.getMapping.get(target).asInstanceOf[Port]
      val dropPort: Port = kernel.getUifactory.getMapping.get(draggedPanel).asInstanceOf[Port]
      if (targetPort.isProvidedPort() && dropPort.isProvidedPort() || targetPort.isRequiredPort() && dropPort.isRequiredPort()) {
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
          script.append("bind " + targetPort.eContainer().asInstanceOf[ComponentInstance].getName + "." + targetPort.getPortTypeRef.getName + "@" + targetPort.eContainer().eContainer().asInstanceOf[ContainerNode].getName + " => " + newChannelName + "\n")
          script.append("bind " + dropPort.eContainer().asInstanceOf[ComponentInstance].getName + "." + dropPort.getPortTypeRef.getName + "@" + dropPort.eContainer().eContainer().asInstanceOf[ContainerNode].getName + " => " + newChannelName + "\n")
          script.append("}")
          val parsedScript = parser.parseScript(script.toString())
          parsedScript.map {
            script =>
              import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
              PositionedEMFHelper.updateModelUIMetaData(kernel)
              script.interpret(KevsInterpreterContext(kernel.getModelHandler.getActualModel))
                val file = File.createTempFile("kev", new Random().nextInt + "")
                KevoreeXmiHelper.save("file:///" + file.getAbsolutePath, kernel.getModelHandler.getActualModel);
                val loadCMD = new LoadModelCommand
                loadCMD.setKernel(kernel)
                loadCMD.execute("file:///" + file.getAbsolutePath)
          }
        }
      })
      hud.getContentPane.add(button)

      hud.getJDialog.setVisible(true);
    } catch {
      case _@e => p1.rejectDrop();
    }
  }
}