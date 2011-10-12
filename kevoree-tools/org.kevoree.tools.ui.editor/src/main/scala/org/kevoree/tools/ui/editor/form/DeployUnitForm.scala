package org.kevoree.tools.ui.editor.form

import com.explodingpixels.macwidgets.HudWindow
import javax.swing._
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudLabelUI, HudTextFieldUI}
import java.awt.event.{ActionEvent, ActionListener}
import org.kevoree.tools.ui.editor.command.KevScriptCommand
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.tools.ui.editor.KevoreeUIKernel

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */

trait DeployUnitForm {
  def createNewDeployUnitPanel(window: HudWindow,kernel : KevoreeUIKernel): Tuple2[JPanel, JButton] = {
      val layout = new JPanel(new SpringLayout)
      layout.setOpaque(false)

      //UnitName
      val nameTextField = new JTextField()
      nameTextField.setUI(new HudTextFieldUI())
      val nodeNameLabel = new JLabel("UnitName", SwingConstants.TRAILING);
      nodeNameLabel.setUI(new HudLabelUI());
      nodeNameLabel.setOpaque(false);
      nodeNameLabel.setLabelFor(nameTextField);
      layout.add(nodeNameLabel)
      layout.add(nameTextField)
      //GroupName
      val groupnameTextField = new JTextField()
      groupnameTextField.setUI(new HudTextFieldUI())
      val groupNameLabel = new JLabel("GroupName", SwingConstants.TRAILING);
      groupNameLabel.setUI(new HudLabelUI());
      groupNameLabel.setOpaque(false);
      groupNameLabel.setLabelFor(groupnameTextField);
      layout.add(groupNameLabel)
      layout.add(groupnameTextField)
      //version
      val versionTextField = new JTextField()
      versionTextField.setUI(new HudTextFieldUI())
      val versionTextLabel = new JLabel("Version", SwingConstants.TRAILING);
      versionTextLabel.setUI(new HudLabelUI());
      versionTextLabel.setOpaque(false);
      versionTextLabel.setLabelFor(versionTextField);
      layout.add(versionTextLabel)
      layout.add(versionTextField)

      //EXECUTE KEVSCRIPT COMMAND
      val btAdd = new JButton("Add DeployUnit")
      btAdd.setUI(new HudButtonUI)
      btAdd.addActionListener(new ActionListener {
        def actionPerformed(p1: ActionEvent) {
          if (nameTextField.getText != "") {
            val cmd = new KevScriptCommand
            cmd.setKernel(kernel)
            cmd.execute("tblock { addDeployUnit \"" + nameTextField.getText + "\" \"" + groupnameTextField.getText + "\" \"" + versionTextField.getText + "\" } ")
            window.getJDialog.dispose()
          }
        }
      })
      window.getJDialog.getRootPane.setDefaultButton(btAdd)
      SpringUtilities.makeCompactGrid(layout, 3, 2, 6, 6, 6, 6)
      Tuple2(layout, btAdd)
    }
}