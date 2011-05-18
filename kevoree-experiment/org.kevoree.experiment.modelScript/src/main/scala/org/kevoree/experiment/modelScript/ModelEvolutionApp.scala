package org.kevoree.experiment.modelScript

import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import java.lang.Math
import scala.collection.JavaConversions._
import org.kevoree.{ComponentInstance, ContainerNode, ContainerRoot}
import java.net.URL
import java.io._

object ModelEvolutionApp extends Application {

  var model: ContainerRoot = null
  var kevScript: StringBuilder = null
  var ip: String = null
  var port: Int = 0

  //val addresses: Array[String] = new Array[String] ()

  val dukeIP = "131.254.15.214"
  val paraisseuxIP = "131.254.12.28"
  val ips = List (dukeIP, paraisseuxIP)


  try {
    loadCurrentModel ()
    initKevScript ()
    //doAddComponent(model)
    doOneMove (model)
    finalizeKevScript ()
    println(kevScript)
    updateModelFromKevScript (kevScript.toString ())
  } catch {
    case _@e => e.printStackTrace ()
  }


  def initKevScript () {
    kevScript = new StringBuilder
    kevScript append "tblock {\n"
  }

  def finalizeKevScript () {
    kevScript append "}\n"
  }

  def loadCurrentModel () {
    port = 8000
    ip = selectRandomlyIntoList (ips).asInstanceOf[String]

    val url = "http://" + ip + ":" + port + "/model/current"
    println ("ask model to " + url)

    model = KevoreeXmiHelper.load (url)
    
  }

  def updateModelFromKevScript (kevScript: String) {
    val parser = new KevsParser ();
    val script = parser.parseScript (kevScript.toString)
    script match {
      case Some (validScript) => {
        if (validScript.interpret (KevsInterpreterContext (model))) {
          ParserUtil.save ("modelEvolution.kev", model)
          val outStream = new ByteArrayOutputStream

          KevoreeXmiHelper.saveStream (outStream, model)
          outStream.flush ()

          val url = new URL ("http://" + ip + ":" + port + "/model/current");
          println ("send new model to " + url)
          val conn = url.openConnection ();
          conn.setConnectTimeout (2000);
          conn.setDoOutput (true);
          val wr = new OutputStreamWriter (conn.getOutputStream)
          wr.write (outStream.toString);
          wr.flush ();

          // Get the response
          val rd = new BufferedReader (new InputStreamReader (conn.getInputStream));
          var line: String = rd.readLine;
          while (line != null) {
            println ("ipReturn" + line)
            line = rd.readLine
          }
          wr.close ();
          rd.close ();

        } else {
          println ("Interpreter Error")
        }

      }
      case None => {
        println ("DTC Error !")
        println (parser.lastNoSuccess)
      }
    }
  }

  def getModel (address: String, port: Int): ContainerRoot = {
    KevoreeXmiHelper.loadStream (this.getClass.getClassLoader.getResourceAsStream ("baseModelEvolution.kev"))
  }

  def doAddComponent (model: ContainerRoot) {
    val node: ContainerNode = selectRandomlyIntoList (model.getNodes.toList).asInstanceOf[ContainerNode]

    //addComponent myFakeLight1@myJavaNode : FakeSimpleLight { param1="hello",param2="helloP2"}
    kevScript append "addComponent "
    kevScript append "myFakeLight1"
    kevScript append "@"
    kevScript append node.getName
    kevScript append ":"
    kevScript append "FakeSimpleLight"
  }

  def doOneMove (model: ContainerRoot) {
    val instance: ComponentInstance = selectComponentRandomly (model)
    if (instance != null) {
      kevScript append "moveComponent "
      kevScript append instance.getName
      kevScript append "@"
      kevScript append instance.eContainer ().asInstanceOf[ContainerNode].getName
      kevScript append " => "
      kevScript append selectRandomlyIntoList (model.getNodes.toList).asInstanceOf[ContainerNode].getName
    }
  }

  def selectComponentRandomly (model: ContainerRoot): ComponentInstance = {
    val nodes = model.getNodes.filter ((node => node.getComponents.size () > 0))
    if (nodes.size > 0) {
      val node: ContainerNode = selectRandomlyIntoList (nodes.toList).asInstanceOf[ContainerNode]
      selectRandomlyIntoList (node.getComponents.toList).asInstanceOf[ComponentInstance]
    } else {
      null
    }
  }

  private def selectRandomlyIntoList[A] (elements: List[A]): A = {
    val i: Int = (Math.random () * elements.size).asInstanceOf[Int]
    elements (i)
  }
}