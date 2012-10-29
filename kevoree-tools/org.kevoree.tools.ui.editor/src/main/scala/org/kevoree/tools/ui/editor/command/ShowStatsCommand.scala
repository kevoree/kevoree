package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.{GroupType, NodeType, ChannelType, ComponentType}
import javax.swing.{JDialog, SwingConstants, JLabel, JFrame}
import org.jdesktop.swingx.JXDialog
import java.awt.Dimension

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/10/12
 * Time: 18:11
 */
class ShowStatsCommand extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  def execute(p: Any) {

    val buffer = new StringBuffer();
    val model = kernel.getModelHandler.getActualModel

    buffer.append("Stats====<br />")
    buffer.append("ComponentType=")
    buffer.append(model.getTypeDefinitions.filter(td => td.isInstanceOf[ComponentType]).size+"<br />")
    buffer.append("ChannelType=")
    buffer.append(model.getTypeDefinitions.filter(td => td.isInstanceOf[ChannelType]).size+"<br />")
    buffer.append("NodeType=")
    buffer.append(model.getTypeDefinitions.filter(td => td.isInstanceOf[NodeType]).size+"<br />")
    buffer.append("GroupType=")
    buffer.append(model.getTypeDefinitions.filter(td => td.isInstanceOf[GroupType]).size+"<br />")
    buffer.append("=========<br />")

    val label = new JLabel("<html>"+buffer.toString+"</html>", SwingConstants.CENTER);

    val dia = new JDialog()
    dia.getContentPane.add(label)
    dia.setPreferredSize(new Dimension(800,600))
    dia.setSize(dia.getPreferredSize)
    dia.setVisible(true)



  }
}
