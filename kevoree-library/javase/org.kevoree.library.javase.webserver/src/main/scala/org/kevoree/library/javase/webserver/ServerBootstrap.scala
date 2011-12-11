package org.kevoree.library.javase.webserver

import akka.config.Supervision._
import akka.config.Supervision.{SupervisorConfig, Permanent}
import akka.actor._
import org.slf4j.LoggerFactory
import org.kevoree.framework.{AbstractComponentType, MessagePort}
import cc.spray.can.{ServerConfig, HttpServer}

class ServerBootstrap(request : MessagePort,compo : AbstractComponentType) {

  var rootService : RootService = _
  val logger = LoggerFactory.getLogger(this.getClass)
  var id = ""
  var supervisorRef : Supervisor = _
  var httpServer : ActorRef= _

  def startServer(port : Int){
    id = "kevoree.javase.webserver.spray-service."+compo.getName
    val config = ServerConfig("0.0.0.0",port,id+"-server",id,id)
    httpServer = Actor.actorOf(new HttpServer(config))
    supervisorRef = Supervisor(
        SupervisorConfig(
          OneForOneStrategy(List(classOf[Exception]), 3, 100),
          List(
            Supervise(Actor.actorOf(new RootService(id,request,this,config.timeoutTimeout)), Permanent),
            Supervise(httpServer, Permanent)
          )
        )
      )
  }

  def stop(){
    if(responseActor!= null){
      if(responseActor.isRunning){
        responseActor.stop()
      }
    }
    try {
      Actor.registry.actors.foreach(actor=> {
        if(actor.getId().contains(id))
        actor ! PoisonPill
      })
      supervisorRef.shutdown()
      //httpServer.dispatcher.asInstanceOf[SelectorWakingDispatcher].killDispatcher()
    } catch {
      case _ @ e => logger.warn("Error while stopping Spray Server")
    }
  }

  private var responseActor : akka.actor.ActorRef = _
  
  def setResponseActor(a : akka.actor.ActorRef) {
    responseActor = a
  }
  
  def responseHandler(param : AnyRef){
    if (responseActor != null ){
      responseActor ! param
    }
  }

}