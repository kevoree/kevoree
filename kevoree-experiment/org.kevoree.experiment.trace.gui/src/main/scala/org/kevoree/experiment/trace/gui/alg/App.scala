package org.kevoree.experiment.trace.gui.alg

import org.kevoree.experiment.trace.TraceMessages
import org.greg.server.ForkedGregServer
import scala.collection.JavaConversions._
import javax.swing.{JOptionPane, WindowConstants, JFrame}
import scala.Some
import org.kevoree.experiment.modelScript.{BootStrapAppComplex, NodePacket, BootStrapApp, ModelEvolutionApp}
import java.net.URL
import java.io._
import org.kevoree.experiment.modelScript.NodePacket._

object App extends Application {


  var outTraceFile = new File("trace_out")
  if (outTraceFile.exists()) {
    outTraceFile.delete();
  }

  //LAUNCH TRACE SERVER
  var gregArgs: List[String] = java.util.Arrays.asList("-port", "5676", "-calibrationPort", "5677").toList
  ForkedGregServer.startServer(gregArgs.toArray, outTraceFile)
  println("Greg server started")

  //BootStrapApp.main(null)
  //Thread.sleep(5000);


  val dukeIP = "131.254.15.214"
  val paraisseuxIP = "131.254.12.28"
  val ips = List(/*dukeIP,*/ paraisseuxIP)
  val packets = List(
                      //NodePacket("duke", dukeIP, 8000, 4),
                      //NodePacket("duke2", dukeIP, 8100, 4),
                      //NodePacket("duke3", dukeIP, 8200, 4),
                      //NodePacket("duke4", dukeIP, 8300, 4),
                      //NodePacket("duke5", dukeIP, 8400, 4),
                      //NodePacket("duke6", dukeIP, 8500, 4),
                      NodePacket("paraisseux", paraisseuxIP, 8000, 4),
                      NodePacket("paraisseux1", paraisseuxIP, 8100, 4)
                    )
  var nbNodes = 0
  packets.foreach {
    p =>
      nbNodes = nbNodes + p.nbElem
  }


  BootStrapAppComplex.bootStrap(packets, paraisseuxIP, ips)


  val inputValue = JOptionPane.showInputDialog("Trace next update from node");

  val input: InputStream = new FileInputStream(outTraceFile)
  val traces: TraceMessages.Traces = TraceMessages.Traces.parseFrom(input)
  var maxVal = 0
  traces.getTraceList.filter(trace => trace.getClientId == inputValue).foreach {
    trace =>
      println("trace=" + TracePath.stringToVectorClock(trace.getBody).toString)

      TracePath.stringToVectorClock(trace.getBody).versionForNode(inputValue) match {
        case None =>
        case Some(vval) => if (vval > maxVal) {
          maxVal = vval
        }
      }
  }

  println(inputValue + " current max value found => " + maxVal)

  val modif = new ModelEvolutionApp(ips)
  // register listener to be notified from stabilization


  // launch the first modification 
  modif.doAction(inputValue)


  val frame = new JFrame();
  frame.setSize(800, 600);

  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  frame.setVisible(true);


  val lookup = new TraceFileLookup(outTraceFile, frame, inputValue, maxVal + 1)
  lookup.start()

  //Run modification
  def notifyFromStabilization () {

    //return

    while (true) {

      Thread.sleep(15000);

      val input: InputStream = new FileInputStream(outTraceFile)
      val traces: TraceMessages.Traces = TraceMessages.Traces.parseFrom(input)
      if (TracePath.isStable(traces.getTraceList.toList, inputValue, maxVal, nbNodes)) {

        // look for the version number which represent the stabilization
        var maxVal = 0
        traces.getTraceList.filter(trace => trace.getClientId == inputValue).foreach {
          trace =>
            println("trace=" + TracePath.stringToVectorClock(trace.getBody).toString)

            TracePath.stringToVectorClock(trace.getBody).versionForNode(inputValue) match {
              case None =>
              case Some(vval) => if (vval > maxVal) {
                maxVal = vval
              }
            }
        }

        // update the TraceFileLookup to look for a new value
        maxVal = maxVal + 1
        lookup.setMaxVal(maxVal)

        // launch a new modification
        modif.doAction(inputValue)
      }
    }
  }

}