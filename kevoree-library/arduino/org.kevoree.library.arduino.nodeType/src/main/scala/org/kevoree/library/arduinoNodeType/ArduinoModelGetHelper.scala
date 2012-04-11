package org.kevoree.library.arduinoNodeType

import org.kevoree.tools.marShell.ast.Script
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShellTransform.KevScriptWrapper
import org.kevoree.extra.kserial.{KevoreeSharedCom, ContentListener}
import org.kevoree.framework.KevoreePropertyHelper
import org.slf4j.LoggerFactory
import org.kevoree.cloner.ModelCloner


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 30/03/12
 * Time: 14:49
 */
object ArduinoModelGetHelper {

  var scriptRaw = ""
  var logger = LoggerFactory.getLogger(this.getClass);


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

    if(found)
    {
      val s = scriptRaw.subSequence(scriptRaw.indexOf('$')+1, scriptRaw.indexOf('}')+1)
      logger.info("Compressed script from arduino node : "+s)
      //GET SCRIPT FROM COM PORT
      var script : Script =  KevScriptWrapper.miniPlanKevScript(KevScriptWrapper.generateKevScriptFromCompressed(s.toString,targetNewModel))
      logger.info("The plan script : "+script)
      //APPLY TO BUILD A CURRENT MODEL
      import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._

      val cc = new ModelCloner
      var current = cc.clone(targetNewModel)

      current.removeAllGroups()
      current.removeAllHubs()
      current.removeAllMBindings()
      current.getNodes.foreach {
        node =>
          current.removeNodes(node)
      }

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
      logger.error("The node '"+targetNodeName+"' did not respond in time or is not present on the port "+boardPortName+". The firmware have to be flashed with a kevoree runtime")
      null
    }
  }


}
