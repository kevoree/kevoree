package org.kevoree.tools.ui.editor

import command.LoadModelCommand
import jsyntaxpane.syntaxkits.XmlSyntaxKit
import javax.swing._
import java.awt.event.{MouseEvent, MouseAdapter}
import org.slf4j.LoggerFactory
import org.kevoree.framework.KevoreeXmiHelper
import java.awt.{Color, BorderLayout}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 18/03/12
 * Time: 21:43
 */

class KevModelTextEditorPanel(kernel: KevoreeUIKernel) extends JPanel {

  def reload(){
    PositionedEMFHelper.updateModelUIMetaData(kernel)
    codeEditor.setText(KevoreeXmiHelper.saveToString(kernel.getModelHandler.getActualModel,true))
  }

  /*
    def getModel: Script = {
      val parser = new KevsParser();
      val result = parser.parseScript(codeEditor.getText);
      result.get
    }
  */
  this.setLayout(new BorderLayout())
  jsyntaxpane.DefaultSyntaxKit.initKit();
  jsyntaxpane.DefaultSyntaxKit.registerContentType("text/xml", classOf[XmlSyntaxKit].getName());
  var codeEditor = new JEditorPane();

  var scrPane = new JScrollPane(codeEditor);

  codeEditor.setContentType("text/xml; charset=UTF-8");


//  codeEditor.setBackground(new Color(80, 80, 80))
  // codeEditor.setBackground(Color.DARK_GRAY)

  codeEditor.setText("<wtf></wtf>\n");

/*
  var editorKit = codeEditor.getEditorKit
  var toolPane = new JToolBar
  editorKit.asInstanceOf[XmlSyntaxKit].addToolBarActions(codeEditor,toolPane)
*/
  add(scrPane, BorderLayout.CENTER);

  val btApply = new JButton
  btApply.setText("Apply")
  add(btApply, BorderLayout.SOUTH);

  btApply.addMouseListener(new MouseAdapter() {
    override def mouseClicked(p1: MouseEvent) {
      try {
        val newModel = KevoreeXmiHelper.loadString(codeEditor.getText)
        val loadCMD = new LoadModelCommand
        loadCMD.setKernel(kernel)
        loadCMD.execute(newModel)

      } catch {
        case _@e => LoggerFactory.getLogger(this.getClass).error("error while apply model")
      }
    }
  })


}
