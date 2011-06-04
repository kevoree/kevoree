package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.framework.KevoreeXmiHelper
import java.net.URL
import java.io.{InputStreamReader, BufferedReader, OutputStreamWriter, ByteArrayOutputStream}

object BootStrapAppComplex {

  def bootStrap (packets: List[NodePacket], ip: String, ips: List[String], sendNotification: Boolean,
    alwaysAskModel: Boolean, delay: java.lang.Integer) {
    if (!packets.isEmpty) {
      val model = KevoreeXmiHelper
        .loadStream(this.getClass.getClassLoader.getResourceAsStream("baseModelEvolution.kev"))
      val tscript = new StringBuilder

      tscript append "tblock {"

      tscript.append(TopologyGeneratorScript.generate(packets, ip, sendNotification, alwaysAskModel, delay))

      tscript append "addComponent "
      tscript append "myFakeConsole1"
      tscript append "@"
      tscript append findNodeName(tscript) //"duke0"
      tscript append ":"
      tscript append "FakeConsole"

      tscript append "}\n"

      //println(tscript.toString())

      val parser = new KevsParser();
      val script = parser.parseScript(tscript.toString())
      script match {
        case Some(validScript) => {
          import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
          if (validScript.interpret(KevsInterpreterContext(model))) {
            ParserUtil.save("bootStrapComplex.kev", model)
            val outStream = new ByteArrayOutputStream

            KevoreeXmiHelper.saveStream(outStream, model)
            outStream.flush()

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
                  val wr = new OutputStreamWriter(conn.getOutputStream)
                  wr.write(outStream.toString);
                  wr.flush();

                  // Get the response
                  val rd = new BufferedReader(new InputStreamReader(conn.getInputStream));
                  var line: String = rd.readLine;
                  while (line != null) {
                    println("ipReturn" + line)
                    line = rd.readLine
                  }
                  wr.close();
                  rd.close();

                } catch {
                  case _@e => e.printStackTrace()
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

  def findNodeName (script: StringBuilder): String = {
    val index = script.indexOf("addNode ")
    if (index > -1) {
      //println(script.substring(index + "addNode ".length(), script.indexOf("\n", index + 1) - " : JavaSENode".length()))
      script.substring(index + "addNode ".length(), script.indexOf("\n", index + 1) - " : JavaSENode".length())
    } else {
      println("there is no node available so we cannot add a component")
      "duke0"
    }
  }


}