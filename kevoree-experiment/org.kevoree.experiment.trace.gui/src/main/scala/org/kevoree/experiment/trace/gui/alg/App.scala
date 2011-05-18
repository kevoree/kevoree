package org.kevoree.experiment.trace.gui.alg

import org.kevoree.experiment.trace.TraceMessages
import org.jfree.chart.ChartPanel
import org.greg.server.ForkedGregServer
import scala.collection.JavaConversions._
import javax.swing.{JOptionPane, WindowConstants, JFrame}
import scala.Some
import java.io.{FileInputStream, File, InputStream}
import org.kevoree.experiment.modelScript.{BootStrapApp, ModelEvolutionApp}

object App extends Application {



  //LAUNCH TRACE SERVER






  var outTraceFile = new File("trace_out")
  if(outTraceFile.exists()){
    outTraceFile.delete();
  }


  var gregArgs: List[String] = java.util.Arrays.asList("-port", "5676", "-calibrationPort", "5677").toList
  ForkedGregServer.startServer(gregArgs.toArray, outTraceFile)
  println("Greg server started")


  BootStrapApp.main(null)

  Thread.sleep(5000);



  val inputValue = JOptionPane.showInputDialog("Trace next update from node");

  val input: InputStream = new FileInputStream(outTraceFile)
  val traces: TraceMessages.Traces = TraceMessages.Traces.parseFrom(input)
  var maxVal = 0
  traces.getTraceList.filter(trace => trace.getClientId == inputValue).foreach{trace =>
    println("trace="+TracePath.stringToVectorClock(trace.getBody).toString)

    TracePath.stringToVectorClock(trace.getBody).versionForNode(inputValue) match {
        case None =>
        case Some(vval)=>  if(vval > maxVal){ maxVal = vval}
      }
  }

  println(inputValue+" current max value found => "+maxVal)


  val modif = new ModelEvolutionApp
  modif.doAction(inputValue)



  val frame = new JFrame();
  frame.setSize(800, 600);

  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  frame.setVisible(true);


  val lookup = new TraceFileLookup(outTraceFile, frame, inputValue,maxVal+1)
  lookup.start()

  //Run modification





}