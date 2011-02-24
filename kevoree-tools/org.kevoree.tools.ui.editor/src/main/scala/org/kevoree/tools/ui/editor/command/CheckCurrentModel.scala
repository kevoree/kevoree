package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import reflect.BeanProperty
import org.kevoree.core.basechecker.RootChecker
import scala.collection.JavaConversions._


class CheckCurrentModel extends Command {

  var kernel : KevoreeUIKernel = null
  def setKernel(k : KevoreeUIKernel) = kernel = k

  var checker = new RootChecker

  def execute(p :Object) {

    var result = checker.check(kernel.getModelHandler.getActualModel)

    result.foreach({ res=>
         println("Violation msg="+res.getMessage)
    })

    if(result.size == 0){
      println("Model checked !")
    }

  }

}