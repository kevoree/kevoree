package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.framework.KevoreeXmiHelper

object BootStrapApp extends Application {

  var model = KevoreeXmiHelper.loadStream(this.getClass.getClassLoader.getResourceAsStream("baseModel.kev"))
  var tscript = new StringBuilder

  tscript append "tblock {"

  //Add 20 node duke
  for (i <- 0 until 20) {
    //ADD NODE
    tscript append "\n"
    tscript append "addNode duke"
    tscript append i
    tscript append " : "
    tscript append "JavaSENode"
    tscript.append("\n")

    tscript append "network duke"
    tscript append i
    tscript append " { \"KEVOREE.remote.node.modelsynch.port\"= \""
    tscript append 8000+i
    tscript append "\"}\n"
  }

  //COMPUT GGROUP FRAGEMENT PORT
  var groupPort = new StringBuilder
  for (i <- 0 until 20) {
    if (!groupPort.isEmpty) {
      groupPort.append(",")
    }
    groupPort.append("duke")
    groupPort.append(i)
    groupPort.append("=")
    groupPort.append(9080+i)

  }

  //ADD GLOBAL GROUP
  tscript append "addGroup gossipGroup : LogNettyGossiperGroup {"
  tscript append "port=\""+groupPort.toString
  tscript append "\"}\n"
  //BIND ALL NODE TO GROUP
  tscript append "addToGroup gossipGroup * \n"

  tscript append "}\n"

  val parser = new KevsParser();
  val script = parser.parseScript(tscript.toString())
  script match {
    case Some(validScript) => {
      import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
      if (validScript.interpret(KevsInterpreterContext(model))) {
        ParserUtil.save("bootStrap.kev", model)
      } else {
        println("Interpreter Error")
      }

    }
    case None => {
      println("DTC Error !")
      println(parser.lastNoSuccess)
    }
  }


}