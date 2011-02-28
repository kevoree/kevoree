package org.kevoree.tools.marShellGUI

import java.awt.BorderLayout
import javax.swing._
import org.kevoree.ContainerRoot

class KevsFrame extends JFrame {

  protected var kevsPanel = new KevsPanel()

  this.setTitle("Kevoree Script Editor")

  add(kevsPanel, BorderLayout.CENTER)

       /*
  var buttons = new JPanel
  buttons.setLayout(new BoxLayout(buttons,BoxLayout.LINE_AXIS))
  var btExecution = new JButton("execute");

  buttons.add(btExecution)
  add(buttons, BorderLayout.SOUTH)
    */

  setSize(800, 600);
 // setVisible(true);
  setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);


}