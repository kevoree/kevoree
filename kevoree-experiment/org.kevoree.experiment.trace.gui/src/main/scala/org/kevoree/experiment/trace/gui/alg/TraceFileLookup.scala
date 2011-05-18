package org.kevoree.experiment.trace.gui.alg

import actors.{TIMEOUT, DaemonActor}
import org.jfree.chart.ChartPanel
import java.io.{InputStream, File}
import org.kevoree.experiment.trace.TraceMessages
import javax.swing.{JPanel, WindowConstants, JFrame}

class TraceFileLookup(traceFile: File, frame: JFrame) extends DaemonActor {

  var previousCheck: Long = 0l

  var previousPanel: JPanel = null

  def act() {
    loop {
      reactWithin(3000) {
        case TIMEOUT => {
          if (traceFile.lastModified() > previousCheck) {


            val input: InputStream = this.getClass.getClassLoader.getResourceAsStream("./trace_out")
            val traces: TraceMessages.Traces = TraceMessages.Traces.parseFrom(input)
            val linkedTrace = TracePath.getPathFrom("duke0", 3, traces)
            linkedTrace match {
              case Some(ltrace) => {
                println(ltrace.toString)
                val frame = new JFrame();
                frame.setSize(800, 600);

                val chart = new VectorClockSingleDisseminationChartScala(ltrace);
                val chartPanel = new ChartPanel(chart.buildChart());
                chartPanel.setOpaque(false);

                if (previousPanel != null) {
                  frame.remove(previousPanel)
                }

                //frame.removeAll();
                frame.add(chartPanel);
                previousPanel = chartPanel;

              }
              case None => println("Not found")
            }


          }
        }
      }
    }
  }


}