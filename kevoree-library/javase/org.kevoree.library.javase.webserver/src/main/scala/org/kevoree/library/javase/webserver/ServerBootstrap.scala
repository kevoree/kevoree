package org.kevoree.library.javase.webserver

import akka.config.Supervision._
import akka.config.Supervision.{SupervisorConfig, Permanent}
import akka.actor._
import cc.spray.can.{ServerConfig, HttpServer}
import org.kevoree.framework.MessagePort

class ServerBootstrap(request : MessagePort) {

  var rootService : RootService = _

  def startServer(port : Int){
    val config = ServerConfig("0.0.0.0",port)
    Supervisor(
        SupervisorConfig(
          OneForOneStrategy(List(classOf[Exception]), 3, 100),
          List(
            Supervise(Actor.actorOf(new RootService("spray-root-service",request,this,config.timeoutTimeout)), Permanent),
            Supervise(Actor.actorOf(new HttpServer(config)), Permanent)
          )
        )
      )
  }

  def stop(){
    responseActor ! PoisonPill
    Actor.registry.actors.foreach(_ ! PoisonPill)
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