package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import java.io.ByteArrayOutputStream
import org.kevoree.framework.KevoreeXmiHelper

object BootStrapAppComplex extends Application {

  var model = KevoreeXmiHelper.loadStream(this.getClass.getClassLoader.getResourceAsStream("baseModelEvolution.kev"))
  var tscript = new StringBuilder

  tscript append "tblock {"

  //val dukeIP = "192.168.1.123"


  val dukeIP = "131.254.15.214"
  val paraisseuxIP = "131.254.12.28"
  val packets = List(
    NodePacket("duke",dukeIP,8000,4), 
    NodePacket("paraisseux",paraisseuxIP,8000,4)
  )

  tscript.append(TopologyGeneratorScript.generate(packets, dukeIP))
  
  tscript append "addComponent "
  tscript append "myFakeLight1"
  tscript append "@"
  tscript append "duke0"
  tscript append ":"
  tscript append "FakeSimpleLight"

  tscript append "}\n"

  println(tscript)

  val parser = new KevsParser();
  val script = parser.parseScript(tscript.toString())
  script match {
    case Some(validScript) => {
        import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
        if (validScript.interpret(KevsInterpreterContext(model))) {
          ParserUtil.save("bootStrapComplex.kev", model)
          val outStream = new ByteArrayOutputStream

          KevoreeXmiHelper.saveStream(outStream, model)
          outStream.flush

          Kev2GraphML.toGraphMLFile("bootStrapComplex", model)


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