package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import java.io.ByteArrayOutputStream
import org.kevoree.framework.KevoreeXmiHelper

object BootStrapAppComplex extends Application {

  def bootStrap (packets : List[NodePacket]) {
    var model = KevoreeXmiHelper.loadStream(this.getClass.getClassLoader.getResourceAsStream("baseModelEvolution.kev"))
    var tscript = new StringBuilder

    tscript append "tblock {"

    //val dukeIP = "192.168.1.123"



    tscript.append(TopologyGeneratorScript.generate(packets, dukeIP))

    tscript append "addComponent "
    tscript append "myFakeLight1"
    tscript append "@"
    tscript append "duke0"
    tscript append ":"
    tscript append "FakeSimpleLight"

    tscript append "}\n"

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


}