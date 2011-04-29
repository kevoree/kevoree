package org.kevoree.experiment.trace.gui.alg

import org.kevoree.experiment.trace.TraceMessages.{Trace}

case class LinkedTrace(trace:Trace, sucessors:List[LinkedTrace]) {

  val lineSeparator = System.getProperty("line.separator").toString

  override def toString : java.lang.String = {
    toString(0)
  }

  def toString(indice:Int):String ={
    val result = new StringBuffer

    for(i <- 0 until indice){
      result append " "
    }
    result.append(trace.getClientId)
    result.append("[")
    result.append(TracePath.stringToVectorClock(trace.getBody).toString)
    result.append("]=>"+sucessors.size)
    result.append(lineSeparator)
    sucessors.foreach{ successor=>
         result.append(successor.toString(indice+1))
    }
    result.toString
  }





}