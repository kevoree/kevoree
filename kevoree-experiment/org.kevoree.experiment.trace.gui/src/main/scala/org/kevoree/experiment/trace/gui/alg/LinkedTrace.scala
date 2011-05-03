package org.kevoree.experiment.trace.gui.alg

import org.kevoree.experiment.trace.TraceMessages.{Trace}
import java.util.Calendar

case class LinkedTrace(trace:Trace, sucessors:List[LinkedTrace]) {

  val lineSeparator = System.getProperty("line.separator").toString

  override def toString : java.lang.String = {
    toString(0,trace.getTimestamp)
  }

  def toString(indice:Int,beginTime:Long):String ={
    val result = new StringBuffer

    for(i <- 0 until indice){
      result append " "
    }
    result.append(trace.getClientId)
    result.append("[")
    result.append(TracePath.stringToVectorClock(trace.getBody).toString)
    result.append("]")
    result.append("")



    val calendar: Calendar = Calendar.getInstance
    calendar.setTimeInMillis( (trace.getTimestamp - beginTime ) / 1000000 )
    val timeRepresentation = "" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND)

    result.append(timeRepresentation)
    result.append("=>"+sucessors.size)
    result.append(lineSeparator)
    sucessors.foreach{ successor=>
         result.append(successor.toString(indice+1,beginTime))
    }
    result.toString
  }

}
