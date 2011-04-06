package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import reflect.BeanProperty

class SynchNodeTypeCommand extends Command {

  @BeanProperty
  var kernel: KevoreeUIKernel = null

  def execute(p: AnyRef) {

  }


}