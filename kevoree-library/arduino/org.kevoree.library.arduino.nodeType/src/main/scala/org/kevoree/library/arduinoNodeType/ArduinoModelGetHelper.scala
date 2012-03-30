package org.kevoree.library.arduinoNodeType

import org.kevoree.tools.marShell.ast.Script
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 30/03/12
 * Time: 13:55
 */

object ArduinoModelGetHelper {

  def getCurrentModel(targetNewModel : ContainerRoot,targetNodeName : String,boardPortName:String) : ContainerRoot = {

    //FOUND COM PORT IN TARGET MODEL


    //GET SCRIPT FROM COM PORT
    var script : Script = null


    //APPLY TO BUILD A CURRENT MODEL
    import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
    var current = KevoreeFactory.createContainerRoot
    val result = script.interpret(KevsInterpreterContext(current))
    if(result){
      current
    }  else {
      null
    }
  }


}
