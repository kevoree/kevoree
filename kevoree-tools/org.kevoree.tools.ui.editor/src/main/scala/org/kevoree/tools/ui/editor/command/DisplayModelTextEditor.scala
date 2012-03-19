package org.kevoree.tools.ui.editor.command

import javax.swing.JFrame
import org.kevoree.tools.ui.editor.{KevoreeUIKernel, KevModelTextEditorPanel}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 18/03/12
 * Time: 22:05
 */

class DisplayModelTextEditor extends Command {

  var j : JFrame = null 
  
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k
  
  def execute(p: AnyRef) {
    if(j != null){
      j.setVisible(false)
    }
    
    j = new JFrame("Kevoree Model Text Editor")
    val p = new KevModelTextEditorPanel(kernel)
    p.reload()
    j.add(p)
    j.setSize(800,600)
    j.setPreferredSize(j.getPreferredSize)
    j.setVisible(true)
    
    
  }
  
}
