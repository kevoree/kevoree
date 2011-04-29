package org.kevoree.experiment.trace.gui.alg

import org.kevoree.experiment.trace.TraceMessages.{Trace, Traces}

//import scala.collection.JavaConversions._

object TracePath {

  def stringToVectorClock(content: String): VectorClock = {
    var result = VectorClock(List())
    content.split(',').foreach {
      entry =>
        val values = entry.split(':')
        if (values.size > 2) {
          val nodeID = values(0)
          val nodeVersion = Integer.parseInt(values(1))
          result = VectorClock(result.entries.toList ++ List((nodeID, nodeVersion)))
        }
    }
    result
  }


  def getPathFrom(nodeID: String, nodeVersion: Int, traces: Traces): Option[LinkedTrace] = {
    import scala.collection.JavaConversions._
    val sortedTraces = traces.getTraceList.toList.sortWith((x, y) => x.getTimestamp < y.getTimestamp)
    //SEARCH FOR TRACE OCCURENCE
    sortedTraces.find(trace => stringToVectorClock(trace.getBody).containEntry(nodeID, nodeVersion)) match {
      case Some(traceRoot) => {
        val linkedtraceRoot = buildSuccessorFor(sortedTraces, traceRoot, nodeID, nodeVersion)
        Some(linkedtraceRoot)
      }
      case None => None
    }
  }

  /* Build recursively successor for trace en precise nodeID & Version  */
  def buildSuccessorFor(traces: List[Trace], trace: Trace, nodeID: String, version: Int): LinkedTrace = {
    val successors = lookForSuccessor(traces, nodeID, version, List())
    var result = LinkedTrace(trace, List())
    successors.foreach {
      suc =>
        val optimizedTraces = traces.slice(traces.indexOf(trace), traces.indexOf(traces.last))
        result = LinkedTrace(trace, result.sucessors ++ List(buildSuccessorFor(optimizedTraces, suc._2, suc._1._1, suc._1._2)))
    }
    result
  }

  /* Look for direct successor of a precise version */
  def lookForSuccessor(traces: List[Trace], nodeID: String, version: Int, foundDirectSuccessors: List[(String, Int)]): List[((String, Int), Trace)] = {
    val headVector = stringToVectorClock(traces.head.getBody)
    val containPrevious = headVector.containEntry(nodeID, version)
    val notContainPrevious = foundDirectSuccessors.forall(t => (!headVector.containEntry(t._1, t._2)))
    if (containPrevious && notContainPrevious) {
      val value = ((traces.head.getClientId, headVector.versionForNode(traces.head.getClientId).get), traces.head)
      List(value) ++ lookForSuccessor(traces.tail, nodeID, version, foundDirectSuccessors)
    } else {
      lookForSuccessor(traces.tail, nodeID, version, foundDirectSuccessors)
    }
  }

}