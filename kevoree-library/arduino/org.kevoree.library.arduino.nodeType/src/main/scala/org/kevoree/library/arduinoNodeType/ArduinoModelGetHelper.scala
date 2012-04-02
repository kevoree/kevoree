package org.kevoree.library.arduinoNodeType

import org.kevoree.tools.marShell.ast.Script
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShellTransform.KevScriptWrapper
import org.kevoree.extra.kserial.{KevoreeSharedCom, ContentListener}
import org.kevoree.framework.KevoreePropertyHelper


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 30/03/12
 * Time: 14:49
 */
object ArduinoModelGetHelper {

  var scriptRaw = ""




  def getCurrentModel(targetNewModel : ContainerRoot,targetNodeName : String,boardPortName:String) : ContainerRoot = {
    var found : Boolean = false
    var  scriptRaw = new StringBuilder()
    var count : Int =0;
    // val remotePort =  KevoreePropertyHelper.getStringPropertyForChannel(targetNewModel, "", "serialport", true, targetNodeName).get

    KevoreeSharedCom.addObserver(boardPortName, new ContentListener
    {
      def recContent(content: String) {
        scriptRaw.append(content.trim())
        if(content.contains("}")) {   found = true; }
      }
    }
    )
    do
    {
      KevoreeSharedCom.send(boardPortName,"$g")
      Thread.sleep(500)
      count +=1;
    } while(found == false && count < 10)

    if(found){
      val s = scriptRaw.subSequence(scriptRaw.indexOf('{'), scriptRaw.indexOf('}')+1)
      //GET SCRIPT FROM COM PORT
      var script : Script =    KevScriptWrapper.generateKevScriptFromCompressed(s.toString)

      //APPLY TO BUILD A CURRENT MODEL
      import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
      var current = KevoreeFactory.createContainerRoot
      val result = script.interpret(KevsInterpreterContext(current))
      if(result){
        current
      }  else
      {
        null
      }
    }
    else
    {
      null
    }
  }


}
