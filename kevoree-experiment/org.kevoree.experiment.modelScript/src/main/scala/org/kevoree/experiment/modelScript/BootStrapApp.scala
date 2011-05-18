package org.kevoree.experiment.modelScript

import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.framework.KevoreeXmiHelper
import java.net.URL
import java.io.{ByteArrayOutputStream, BufferedReader, InputStreamReader, OutputStreamWriter}

object BootStrapApp extends Application {

  var model = KevoreeXmiHelper.loadStream(this.getClass.getClassLoader.getResourceAsStream("baseModelEvolution.kev"))
  var tscript = new StringBuilder

  tscript append "tblock {"

  //val dukeIP = "192.168.1.123"


  val dukeIP = "131.254.15.214"
  val paraisseuxIP = "131.254.12.28"
  val ips = List(dukeIP, paraisseuxIP)


  tscript append generatePhysicalNodeScript("duke", dukeIP, 8000, 4)
  tscript append generatePhysicalNodeScript("paraisseux", paraisseuxIP, 8000, 4)



  //ADD GLOBAL GROUP
  tscript append "addGroup gossipGroup : LogNettyGossiperGroup {"
  tscript append "port=\"" + generateGroupFragmentPort(List(("duke", 4, 9000), ("paraisseux", 4, 9000))) + "\"\n"
  tscript append ",loggerServerIP=\"" + dukeIP + "\""


  tscript append "}\n"
  //BIND ALL NODE TO GROUP
  tscript append "addToGroup gossipGroup * \n"


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
        ParserUtil.save("bootStrap.kev", model)
        val outStream = new ByteArrayOutputStream

        KevoreeXmiHelper.saveStream(outStream, model)
        outStream.flush


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
        /*
       var i = 0
       while (true) {
         try {
           val url = new URL("http://"+dukeIP+":8000/model/current")
           val conn = url.openConnection();

           conn.setConnectTimeout(2000);
           conn.setDoOutput(true);
           var wr = new OutputStreamWriter(conn.getOutputStream())
           wr.write(outStream.toString);
           wr.flush();

           // Get the response
           var rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           var line: String = rd.readLine;
           while (line != null) {
             println(line)
             println(i)
             line = rd.readLine
           }
           wr.close();
           rd.close();

           i = i +1
           Thread.sleep(1000)

         } catch {
           case _@e => e.printStackTrace()
         }


       } */


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

      tscript append "network " + prefixeName
      tscript append i
      tscript append " { \"KEVOREE.remote.node.modelsynch.port\"= \""
      tscript append firstPort + i
      tscript append "\"}\n"

      tscript append "network " + prefixeName
      tscript append i
      tscript append " { \"KEVOREE.remote.node.ip\"= \""
      tscript append ip
      tscript append "\"}\n"

    }

    tscript.toString()
  }


}