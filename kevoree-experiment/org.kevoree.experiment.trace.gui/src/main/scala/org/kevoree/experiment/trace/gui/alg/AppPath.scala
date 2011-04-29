package org.kevoree.experiment.trace.gui.alg

import java.io.InputStream
import org.kevoree.experiment.trace.TraceMessages

object AppPath extends Application {

  var input: InputStream = this.getClass.getClassLoader.getResourceAsStream("./trace_out")
  var traces: TraceMessages.Traces = TraceMessages.Traces.parseFrom(input)
  var linkedTrace = TracePath.getPathFrom("duke",2,traces)
  linkedTrace match {
    case Some(ltrace)=> println(ltrace.toString)
    case None => println("Not found")
  }



}