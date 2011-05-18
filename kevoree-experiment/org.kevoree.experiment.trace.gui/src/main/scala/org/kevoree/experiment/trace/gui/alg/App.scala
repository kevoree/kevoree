package org.kevoree.experiment.trace.gui.alg

import org.kevoree.experiment.trace.TraceMessages
import org.jfree.chart.ChartPanel
import javax.swing.{WindowConstants, JFrame}
import org.greg.server.ForkedGregServer
import java.io.{File, InputStream}
import scala.collection.JavaConversions._

object App extends Application {

  //LAUNCH TRACE SERVER

  var outTraceFile = new File("trace_out")
  var gregArgs: List[String] = java.util.Arrays.asList("-port", "5676", "-calibrationPort", "5677").toList
  ForkedGregServer.startServer(gregArgs.toArray, outTraceFile)

  println("Greg server started")

  val frame = new JFrame();
  frame.setSize(800, 600);

  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  frame.setVisible(true);


  val lookup = new TraceFileLookup(outTraceFile,frame)
  lookup.start()


}