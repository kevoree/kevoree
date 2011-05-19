package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.framework.KevoreeXmiHelper
import java.net.URL
import java.io.{InputStreamReader, BufferedReader, OutputStreamWriter, ByteArrayOutputStream}

object BootStrapAppComplex extends Application {

  def bootStrap (packets : List[NodePacket], dukeIP : String, ips : List[String]) {
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

           //Try to push to all
        ips.foreach {
          ip =>
            try {
              val url = new URL("http://" + ip + ":8080");
              println("send to " + url)
              val conn = url.openConnection();
              conn.setConnectTimeout(2000);
              conn.setDoOutput(true);
              val wr = new OutputStreamWriter(conn.getOutputStream())
              wr.write(outStream.toString);
              wr.flush();

              // Get the response
              val rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
              var line: String = rd.readLine;
              while (line != null) {
                println("ipReturn" + line)
                line = rd.readLine
              }
              wr.close();
              rd.close();

            } catch {
              case _@e => e.printStackTrace
            }
        }


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