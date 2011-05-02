package org.kevoree.experiment.trace.gui.alg

import java.io.InputStream
import org.kevoree.experiment.trace.TraceMessages
import org.jfree.chart.ChartPanel
import javax.swing.{WindowConstants, JFrame}

object AppPath extends Application {

  var input: InputStream = this.getClass.getClassLoader.getResourceAsStream("./trace_out")
  var traces: TraceMessages.Traces = TraceMessages.Traces.parseFrom(input)
  var linkedTrace = TracePath.getPathFrom("duke", 2, traces)
  linkedTrace match {
    case Some(ltrace) => {
      println(ltrace.toString)
      val frame = new JFrame();
      frame.setSize(400, 400);

      val chart = new VectorClockSingleDisseminationChartScala(ltrace);
      val chartPanel = new ChartPanel(chart.buildChart());
      chartPanel.setOpaque(false);
      frame.add(chartPanel);
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);

    }
    case None => println("Not found")
  }


}