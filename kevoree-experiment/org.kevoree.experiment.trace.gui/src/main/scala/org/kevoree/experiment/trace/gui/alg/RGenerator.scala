package org.kevoree.experiment.trace.gui.alg

import java.io.{File, FileWriter}

/**
 * User: ffouquet
 * Date: 18/07/11
 * Time: 09:07
 */

object RGenerator {

  val scriptEnd = "\nlibrary(Hmisc)\nbpplot(propDelais,main=\"Downtime propagation delay\")"


   def generatePropagationTimeScript(trace:LinkedTrace) : String = {

     var nodes : List[String] = List()
     var diff : List[Long] = List()

     //INIT
     val firstTime = trace.trace.getTimestamp
     nodes = nodes ++ List(trace.trace.getClientId)
     diff = diff ++ List(0l)

     def recusiveCall(traces:List[LinkedTrace]){
        traces.foreach{ trace =>
          if(!nodes.contains(trace.trace.getClientId)){
            nodes = nodes ++ List(trace.trace.getClientId)
            val mili = ((trace.trace.getTimestamp-firstTime)/1000000)
            diff = diff ++ List(mili)
          }
          recusiveCall(trace.sucessors)
        }
     }

     recusiveCall(trace.sucessors)
     "nodeNames <- c(\"" + nodes.mkString("\",\"") +"\")" + "\npropDelais <- c(" + diff.mkString(",") +")"+scriptEnd

   }


   def generateFile(content:String){

     val fwr = new FileWriter(new File("out.r"))
     fwr.write(content)
     fwr.close()

   }




}