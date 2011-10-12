package org.kevoree.tools.ui.editor.form

import com.explodingpixels.macwidgets.HudWindow
import javax.swing._
import com.explodingpixels.macwidgets.plaf.{HudButtonUI, HudComboBoxUI, HudLabelUI, HudTextFieldUI}
import java.awt.event.{ActionEvent, ActionListener}
import org.kevoree.tools.ui.editor.command.KevScriptCommand
import org.kevoree.tools.ui.editor.property.SpringUtilities
import org.kevoree.tools.ui.editor.KevoreeUIKernel

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:31
 * To change this template use File | Settings | File Templates.
 */

trait ComponentTypeForm {

  def createNewComponentTypePanel(window: HudWindow,kernel : KevoreeUIKernel): Tuple2[JPanel, JButton] = {
    val layout = new JPanel(new SpringLayout)
    layout.setOpaque(false)
    val nameTextField = new JTextField()
    nameTextField.setUI(new HudTextFieldUI())
    val componentTypeNameLabel = new JLabel("ComponentType name", SwingConstants.TRAILING);
    componentTypeNameLabel.setUI(new HudLabelUI());
    componentTypeNameLabel.setOpaque(false);
    componentTypeNameLabel.setLabelFor(nameTextField);
    layout.add(componentTypeNameLabel)
    layout.add(nameTextField)

    //MENU LIBRARY
    val libraryModel = new DefaultComboBoxModel
    libraryModel.addElement("no library")
    kernel.getModelHandler.getActualModel.getLibraries.foreach {
      lib =>
        libraryModel.addElement(lib.getName)
    }
    val comboLibrary = new JComboBox(libraryModel)
    comboLibrary.setUI(new HudComboBoxUI())
    val libraryCompoLabel = new JLabel("library : ", SwingConstants.TRAILING)
    libraryCompoLabel.setUI(new HudLabelUI)
    libraryCompoLabel.setLabelFor(comboLibrary)
    layout.add(libraryCompoLabel)
    layout.add(comboLibrary)

    //EXECUTE KEVSCRIPT COMMAND
    val btAdd = new JButton("Add ComponentType")
    btAdd.setUI(new HudButtonUI)
    btAdd.addActionListener(new ActionListener {
      def actionPerformed(p1: ActionEvent) {
        if (nameTextField.getText != "") {
          val cmd = new KevScriptCommand
          cmd.setKernel(kernel)
          if("no library" != comboLibrary.getSelectedItem){
            cmd.execute("tblock { createComponentType " + nameTextField.getText + " @ "+comboLibrary.getSelectedItem+"  } ")
          } else {
            cmd.execute("tblock { createComponentType " + nameTextField.getText + " } ")
          }
          window.getJDialog.dispose()
        }
      }
    })
    window.getJDialog.getRootPane.setDefaultButton(btAdd)
    SpringUtilities.makeCompactGrid(layout, 2, 2, 6, 6, 6, 6)
    Tuple2(layout, btAdd)
  }


}