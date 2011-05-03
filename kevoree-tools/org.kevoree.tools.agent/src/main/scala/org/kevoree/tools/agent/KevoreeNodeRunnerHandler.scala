package org.kevoree.tools.agent


object KevoreeNodeRunnerHandler {

  private var runnners : List[KevoreeNodeRunner] = List()

  def closeAllRunners() {
     runnners.foreach{ runner =>
         runner.stopKillNode()
     }
  }

  def addRunner( nodeName:String, port : Int){
     val newRunner = new KevoreeNodeRunner(nodeName,port)
    newRunner.startNode()
    runnners = runnners :+ newRunner
  }


}