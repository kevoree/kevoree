package org.kevoree.experiment.trace.gui.alg

import org.kevoree.experiment.trace.TraceMessages.{Trace, Traces}

//import scala.collection.JavaConversions._

object TracePath {

  //HELPER
  def stringToVectorClock(content: String): VectorClock = {
    var result = VectorClock(List())
    content.split(',').foreach {
      entry =>
      val values = entry.split(':')
      if (values.size >= 2) {
        val nodeID = values(0)
        val nodeVersion = Integer.parseInt(values(1).trim)
        result = VectorClock(result.entries.toList ++ List((nodeID, nodeVersion)))
      }
    }
    result
  }


  //INIT SEARCH PATH
  def getPathFrom(nodeID: String, nodeVersion: Int, traces: Traces): Option[LinkedTrace] = {
    import scala.collection.JavaConversions._
    val sortedTraces = traces.getTraceList.toList.sortWith((x, y) => x.getTimestamp < y.getTimestamp)

    //SEARCH FOR FIRST TRACE OCCURENCE
    sortedTraces.find(trace => trace.getClientId == nodeID && stringToVectorClock(trace.getBody).containEntry(nodeID, nodeVersion)) match {
      case Some(traceRoot) => {
          val linkedtraceRoot = buildLinkedFor(sortedTraces, traceRoot, nodeID, nodeVersion)
          Some(linkedtraceRoot)
        }
      case None => None
    }
  }

  /* Build recursively successor for trace with precise nodeID & Version  */
  protected def buildLinkedFor(traces: List[Trace], trace: Trace, nodeID: String, version: Int): LinkedTrace = {

    val tracesWithoutTrace = traces.slice(traces.indexOf(trace)+1, traces.size+1)

    val successors = lookForSuccessor(tracesWithoutTrace, nodeID, version, List())
    var result = LinkedTrace(trace, List())
    successors.foreach{
      suc =>
      val optimizedTraces = traces.slice(traces.indexOf(suc._2)+1, traces.size+1)
      result = LinkedTrace(trace, result.sucessors ++ List(buildLinkedFor(optimizedTraces, suc._2, suc._1._1, suc._1._2)))
    }
    result
  }

  /* Look for direct successor of a precise version */
  protected def lookForSuccessor(traces: List[Trace], nodeID: String, version: Int, foundDirectSuccessors: List[(String, Int)]): List[((String, Int), Trace)] = {
    if(traces.isEmpty){
      return List()
    }
    val headVector = stringToVectorClock(traces.head.getBody)
    val containPrevious = headVector.containEntry(nodeID, version)
    val notContainPrevious = foundDirectSuccessors.forall(t => (!headVector.containEntry(t._1, t._2)))
    var lvalue : List[((String, Int), Trace)] = List()
    var foundDirectSuccessors2 = foundDirectSuccessors
    if (containPrevious && notContainPrevious ) {
      foundDirectSuccessors2 = foundDirectSuccessors2 ++ List((traces.head.getClientId, headVector.versionForNode(traces.head.getClientId).get))
      lvalue = List(((traces.head.getClientId, headVector.versionForNode(traces.head.getClientId).get), traces.head))
    }
    if (!traces.tail.isEmpty) {
      lvalue ++ lookForSuccessor(traces.tail, nodeID, version, foundDirectSuccessors2)
    } else {
      lvalue
    }
  }




}