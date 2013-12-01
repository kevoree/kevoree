package org.kevoree.tools.ui.editor

import javax.swing.{JFileChooser, BoxLayout, JPanel}
import java.awt.{Color, BorderLayout}
import org.kevoree.tools.ui.kevscript.KevScriptEditorPanel
import java.awt.event.{MouseEvent, MouseAdapter}
import javax.swing.filechooser.FileFilter
import java.io.{FileOutputStream, PrintWriter, File}
import org.kevoree.tools.ui.editor.command.{KevScriptCommand, SaveAsKevScript}
import com.explodingpixels.macwidgets.HudWidgetFactory

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/12/2013
 * Time: 11:18
 */
class ExtKevScriptEditor(kernel: KevoreeUIKernel) extends JPanel {

  setLayout(new BorderLayout())

  var kevsPanel = new KevScriptEditorPanel(kernel.getModelHandler)
  add(kevsPanel, BorderLayout.CENTER)
  var buttons = new JPanel
  buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS))
  buttons.setOpaque(true);
  buttons.setBorder(null);
  buttons.setBackground(new Color(57, 57, 57));
  add(buttons, BorderLayout.SOUTH)

  var btSave = HudWidgetFactory.createHudButton("Save Script")
  btSave.addMouseListener(new MouseAdapter() {
    override def mouseClicked(p1: MouseEvent) = {
      val fileChooser = new JFileChooser
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
      fileChooser.setMultiSelectionEnabled(false)
      fileChooser.setAcceptAllFileFilterUsed(true)
      fileChooser.setFileHidingEnabled(true)
      fileChooser.addChoosableFileFilter(new FileFilter() {
        override def getDescription(): String = "KevScript Files"

        override def accept(f: File): Boolean = {
          if (f.isDirectory()) {
            return false;
          }
          val extension = f.getName.substring(f.getName.lastIndexOf(".") + 1)
          if (extension != null) {
            if (extension.equals("kevs")) {
              return true;
            } else {
              return false;
            }
          }
          return false;
        }
      })
      val result = fileChooser.showSaveDialog(kevsPanel);
      if (result == JFileChooser.APPROVE_OPTION) {
        val destFile = fileChooser.getSelectedFile
        if (!destFile.exists) {
          destFile.createNewFile
        }

        val pr = new PrintWriter(new FileOutputStream(destFile))
        pr.append(kevsPanel.getContent)
        pr.flush()
        pr.close()
      }

    }
  })
  buttons.add(btSave)

  var btLoad = HudWidgetFactory.createHudButton("Load KevScript");
  btLoad.addMouseListener(new MouseAdapter() {
    override def mouseClicked(p1: MouseEvent) = {
      //Save Script
      val fileChooser = new JFileChooser
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
      fileChooser.setMultiSelectionEnabled(false)
      fileChooser.setAcceptAllFileFilterUsed(true)
      fileChooser.setFileHidingEnabled(true)
      fileChooser.addChoosableFileFilter(new FileFilter() {
        override def getDescription(): String = "KevScript Files"

        override def accept(f: File): Boolean = {
          if (f.isDirectory()) {
            return false;
          }
          val extension = f.getName.substring(f.getName.lastIndexOf(".") + 1)
          if (extension != null) {
            if (extension.equals("kevs")) {
              return true;
            } else {
              return false;
            }
          }

          return false;
        }
      })
      val result = fileChooser.showOpenDialog(kevsPanel);
      if (result == JFileChooser.APPROVE_OPTION) {
        val loadFilePath = fileChooser.getSelectedFile.getAbsolutePath
        val res = new StringBuilder
        scala.io.Source.fromFile(loadFilePath).getLines.foreach {
          l => res.append(l); res.append('\n')
        }
        kevsPanel.setContent(null)
        kevsPanel.setContent(res.toString().trim())
        kevsPanel.repaint()
      }
    }
  })
  buttons.add(btLoad)

  var bTCurrent = HudWidgetFactory.createHudButton("Load Current Model")
  bTCurrent.addMouseListener(new MouseAdapter() {
    override def mouseClicked(p1: MouseEvent) = {
      val cmdSave = new SaveAsKevScript()
      cmdSave.setKernel(kernel)
      val buffer = new StringBuffer()
      cmdSave.execute(buffer)
      kevsPanel.setContent(null)
      kevsPanel.setContent(buffer.toString)
      kevsPanel.repaint()

    }
  })
  buttons.add(bTCurrent)

  var btExecution = HudWidgetFactory.createHudButton("Execute")
  btExecution.addMouseListener(new MouseAdapter() {
    override def mouseClicked(p1: MouseEvent) = {
      val script = kevsPanel.getContent
      if (script != null) {
        PositionedEMFHelper.updateModelUIMetaData(kernel)
        val kevSCommand = new KevScriptCommand
        kevSCommand.setKernel(kernel)
        kevSCommand.execute(kevsPanel.getContent)
      }
    }
  })
  buttons.add(btExecution)


}
