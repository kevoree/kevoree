package org.kevoree.tools.ui.editor.command

import javax.swing.{JButton, BoxLayout, JPanel}
import java.awt.BorderLayout
import org.kevoree.tools.marShellGUI.KevsFrame
import org.kevoree.tools.ui.editor.KevoreeUIKernel
import java.awt.event.{MouseEvent, MouseAdapter}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.framework.KevoreeXmiHelper
import java.io.File
import java.util.Random

class OpenKevsShell extends Command {

  private var current: KevsEditorFrame = null;
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  class KevsEditorFrame extends KevsFrame {


    var buttons = new JPanel
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS))
    var btExecution = new JButton("execute");
    btExecution.addMouseListener(new MouseAdapter() {
      override def mouseClicked(p1: MouseEvent) = {
        //TODO SAVE CURRENT MODEL
        var script = kevsPanel.getModel
        if (script != null) {
          import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._

          var result = script.interpret(KevsInterpreterContext(kernel.getModelHandler.getActualModel))
          println("Interpreter Result : " + result)
          if(result){
            //reload
            val file = File.createTempFile("kev", new Random().nextInt+"")

            KevoreeXmiHelper.save(file.getAbsolutePath, kernel.getModelHandler().getActualModel);

            var loadCMD = new LoadModelCommand
             loadCMD.setKernel(kernel)
            loadCMD.execute(file.getAbsolutePath)



          }
        }
      }
    })


    buttons.add(btExecution)
    add(buttons, BorderLayout.SOUTH)


  }

  def execute(p: Object) = {
    if (current == null) {
      current = new KevsEditorFrame
      current.setVisible(true)
    }
    current.toFront
  }
}