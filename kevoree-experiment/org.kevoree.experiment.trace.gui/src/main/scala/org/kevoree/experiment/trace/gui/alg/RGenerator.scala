package org.kevoree.experiment.trace.gui.alg

/**
 * User: ffouquet
 * Date: 18/07/11
 * Time: 09:07
 */

object RGenerator {

   def generatePropagationTimeScript(trace:LinkedTrace) : String = {

     var nodes : List[String] = List()
     var diff : List[Long] = List()

     //INIT
     val firstTime = trace.trace.getTimestamp
     nodes = nodes ++ List(trace.trace.getClientId)
     diff = diff ++ List(0l)

     def recusiveCall(traces:List[LinkedTrace]){
        traces.foreach{ trace =>

        }
     }

     recusiveCall(trace.sucessors)
     "nodeNames <- c(" + nodes.mkString(",") +")"



   }




}