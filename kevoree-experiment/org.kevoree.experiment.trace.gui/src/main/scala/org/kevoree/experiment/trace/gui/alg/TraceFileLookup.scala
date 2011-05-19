package org.kevoree.experiment.trace.gui.alg

import actors.{TIMEOUT, DaemonActor}
import org.jfree.chart.ChartPanel
import org.kevoree.experiment.trace.TraceMessages
import javax.swing.{JPanel, WindowConstants, JFrame}
import java.awt.BorderLayout
import java.io.{FileInputStream, InputStream, File}

class TraceFileLookup (traceFile: File, frame: JFrame, nodeName: String, var maxVal: Int) extends DaemonActor {

  var previousCheck: Long = 0l

  var previousPanel: JPanel = null

  def setMaxVal (maxVal: Int) {
    this.maxVal = maxVal
  }

  def update () {
    val input: InputStream = new FileInputStream(traceFile)
    val traces: TraceMessages.Traces = TraceMessages.Traces.parseFrom(input)
    val linkedTrace = TracePath.getPathFrom(nodeName, maxVal, traces)
    linkedTrace match {
      case Some(ltrace) => {
        println(ltrace.toString)

        val chart = new VectorClockSingleDisseminationChartScala(ltrace);
        val chartPanel = new ChartPanel(chart.buildChart());
        chartPanel.setOpaque(false);

        if (previousPanel != null) {
          frame.remove(previousPanel)
        }

        //frame.removeAll();

        val previousSize = frame.getSize

        frame.add(chartPanel, BorderLayout.CENTER);
        previousPanel = chartPanel;
        frame.pack();
        frame.setSize(previousSize);
        frame.setPreferredSize(previousSize)


      }
      case None => println("Not found")
    }
    App.notifyFromStabilization()
  }


  def act () {
    loop {
      reactWithin(4000) {
        case TIMEOUT => {
          if (traceFile.lastModified() > previousCheck) {
            update()
          }
        }
      }
    }
  }


}