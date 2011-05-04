package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.framework.KevoreeXmiHelper

object BootStrapApp extends Application {

  var model = KevoreeXmiHelper.loadStream(this.getClass.getClassLoader.getResourceAsStream("baseModel.kev"))
  var tscript = new StringBuilder

  tscript append "tblock {"

  val dukeIP = "131.254.15.214"
  val paraisseuxIP = "131.254.12.28"


  tscript append generatePhysicalNodeScript("duke", dukeIP, 8000, 10)
  tscript append generatePhysicalNodeScript("paraisseux", paraisseuxIP, 8000, 10)

  //ADD GLOBAL GROUP
  tscript append "addGroup gossipGroup : LogNettyGossiperGroup {"
  tscript append "port=\"" + generateGroupFragmentPort( List(("duke",10,9000),("paraisseux",10,9000))  )+"\"\n"
  tscript append ",loggerServerIP=\""+dukeIP+"\""


  tscript append "}\n"
  //BIND ALL NODE TO GROUP
  tscript append "addToGroup gossipGroup * \n"

  tscript append "}\n"

  //println(tscript)

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


  def generateGroupFragmentPort(p: List[(String, Int, Int)]): String = {
    val groupPort = new StringBuilder
    p.foreach {
      param =>
        for (i <- 0 until param._2) {
          if (!groupPort.isEmpty) {
            groupPort.append(",")
          }
          groupPort.append(param._1)
          groupPort.append(i)
          groupPort.append("=")
          groupPort.append(param._3 + i)

        }
    }
    groupPort.toString()
  }


  def generatePhysicalNodeScript(prefixeName: String, ip: String, firstPort: Int, subNodesNumber: Int): String = {
    val tscript = new StringBuilder
    for (i <- 0 until subNodesNumber) {
      //ADD NODE
      tscript append "\n"
      tscript append "addNode " + prefixeName
      tscript append i
      tscript append " : "
      tscript append "JavaSENode"
      tscript.append("\n")

      tscript append "network duke"
      tscript append i
      tscript append " { \"KEVOREE.remote.node.modelsynch.port\"= \""
      tscript append firstPort + i
      tscript append "\"}\n"

      tscript append "network "+prefixeName
      tscript append i
      tscript append " { \"KEVOREE.remote.node.ip\"= \""
      tscript append ip
      tscript append "\"}\n"

    }

    tscript.toString()
  }


}