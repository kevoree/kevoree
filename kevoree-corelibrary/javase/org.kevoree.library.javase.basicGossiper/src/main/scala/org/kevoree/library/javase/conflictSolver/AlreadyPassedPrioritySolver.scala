package org.kevoree.library.javase.conflictSolver

import org.kevoree.library.basicGossiper.protocol.version.Version.VectorClock
import org.kevoree.ContainerRoot
import org.kevoree.merger.KevoreeMergerComponent
import java.util
import org.kevoree.api.service.core.script.KevScriptEngineFactory

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/11/12
 * Time: 13:28
 */
class AlreadyPassedPrioritySolver(kevsFactory : KevScriptEngineFactory) extends ConflictSolver {

  private val mergerComponent = new KevoreeMergerComponent();

  def resolve(current: (VectorClock, ContainerRoot), proposed: (VectorClock, ContainerRoot), sourceNodeName : String, currentNodeName : String): ContainerRoot = {

    //found current version
    var currentNodeVersion = new util.HashMap[String,Int]()
    for (i <- 0 until current._1.getEntiesCount()-1){
      currentNodeVersion.put(current._1.getEnties(i).getNodeID,current._1.getEnties(i).getVersion)
    }
    //collect of before Node
    val beforeNodeNames = new util.ArrayList[String]()
    for (i <- 0 until proposed._1.getEntiesCount()-1){
       if(currentNodeVersion.containsKey(proposed._1.getEnties(i).getNodeID)){
         if(proposed._1.getEnties(i).getVersion > currentNodeVersion.get(proposed._1.getEnties(i).getNodeID)){
           beforeNodeNames.add(proposed._1.getEnties(i).getNodeID)
         }
       }
    }
    currentNodeVersion.clear()
    val kevengine = kevsFactory.createKevScriptEngine(current._2)
    for(i <- 0 until beforeNodeNames.size()-1){
      kevengine.addVariable("nodeName",beforeNodeNames.get(i))
      kevengine.append("removeNode {nodeName}")
    }
    val cleanedModel = kevengine.interpret()
    mergerComponent.merge(cleanedModel,proposed._2)
  }


}
