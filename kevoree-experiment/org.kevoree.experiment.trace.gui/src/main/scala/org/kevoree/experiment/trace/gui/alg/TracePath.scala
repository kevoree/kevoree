package org.kevoree.experiment.trace.gui.alg

import org.kevoree.experiment.trace.TraceMessages.{Trace, Traces}
import org.kevoree.experiment.modelScript.NodePacket

//import scala.collection.JavaConversions._

object TracePath {

  //HELPER
  def stringToVectorClock(content: String): VectorClock = {
    var tmps = content.split("!")
    var result = VectorClock(List(), tmps(0))
    content.split(',').foreach {
      entry =>
      val values = entry.split(':')
      if (values.size >= 2) {
        val nodeID = values(0)
        val nodeVersion = Integer.parseInt(values(1).trim)
        result = VectorClock(result.entries.toList ++ List((nodeID, nodeVersion)), tmps(0))
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
    /*println("currentTrace contains " + nodeID + " and " + version)
    println("trace => " + traces.head.getBody)*/
   // val notContainPrevious = foundDirectSuccessors.forall(t => (!headVector.containEntry(t._1, t._2)))
    val previousTrace = findPreviousTrace(traces.head, traces, nodeID, version)
    val alreadyNew  = isAlreadyNew(previousTrace, traces.head, nodeID, version)
    println("previous trace is also an updated trace so the current trace is not a direct successor")
    println("trace => " + previousTrace.getBody)
    var lvalue : List[((String, Int), Trace)] = List()
    var foundDirectSuccessors2 = foundDirectSuccessors
    if (containPrevious /*&& notContainPrevious*/ && alreadyNew) {
      foundDirectSuccessors2 = foundDirectSuccessors2 ++ List((traces.head.getClientId, headVector.versionForNode(traces.head.getClientId).get))
      lvalue = List(((traces.head.getClientId, headVector.versionForNode(traces.head.getClientId).get), traces.head))
    }
    if (!traces.tail.isEmpty) {
      lvalue ++ lookForSuccessor(traces.tail, nodeID, version, foundDirectSuccessors2)
    } else {
      lvalue
    }
  }

  /**
   *
   * @return true if there is only one difference between previous and current trace. This difference is about version of nodeId (equals to version -1 for the previous trace), false else
   */
  private def isAlreadyNew(previousTrace : Trace, currentTrace : Trace, nodeId : String, version : Int) : Boolean ={
    val currentTraceVectorClock = stringToVectorClock(currentTrace.getBody)
    val previousTraceVectorClock = stringToVectorClock(previousTrace.getBody)
    currentTraceVectorClock.entries.forall{
      t =>
        (t._1.equals(nodeId) && t._2.equals(version - 1)) || previousTraceVectorClock.containEntry(t._1, t._2)
    }
  }


  /**
   * look for the previous trace where the version for nodeId is equals to version -1 compared to the current trace
   */
  private def findPreviousTrace(trace : Trace, traces : List[Trace], nodeId : String, version : Int) : Trace = {
    var reverseTraces = traces.reverse
    var t = reverseTraces.head
    while (!t.getClientId.equals(trace.getClientId) && !stringToVectorClock(t.getBody).containEntry(nodeId, version -1)) {
      reverseTraces = reverseTraces.tail
      t = reverseTraces.head
    }
    t
  }



  /**
   * the state of the system is stable when we find into the traces at least one trace for each node where
   * the version for nodeId is equals to version
   *
   */
  def isStable (traces: List[Trace], nodeId: String, version: Int, nbNodes : Int): Boolean = {
    val reverseTraces = traces.reverse
    var found : List[String] = List[String]()
    var i = 0
    while (found.size <= nbNodes && i < traces.size) {
      if (TracePath.stringToVectorClock(reverseTraces(i).getBody).containEntry(nodeId, version) && !found.contains(reverseTraces(i).getClientId)) {
        found = found ++ List(reverseTraces(i).getClientId)
      }
      i = i + 1
    }
    found.size == nbNodes
  }


}