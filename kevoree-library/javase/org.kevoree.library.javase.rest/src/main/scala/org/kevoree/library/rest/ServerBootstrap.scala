package org.kevoree.library.rest

import akka.config.Supervision._
import akka.config.Supervision.{SupervisorConfig, Permanent}
import akka.actor._
import cc.spray.can.{ServerConfig, HttpServer}
import org.kevoree.framework.MessagePort
import org.slf4j.LoggerFactory

class ServerBootstrap(group : RestGroup) {

  var rootService : RootService = _
  val logger = LoggerFactory.getLogger(this.getClass)

  def startServer(port : Int){
    val id = "kevoree.rest.group.spray-service"
    val config = ServerConfig("0.0.0.0",port,id+"-server",id,id)

    Supervisor(
        SupervisorConfig(
          OneForOneStrategy(List(classOf[Exception]), 3, 100),
          List(
            Supervise(Actor.actorOf(new RootService(id,group)), Permanent),
            Supervise(Actor.actorOf(new HttpServer(config)), Permanent)
          )
        )
      )
  }

  def stop(){
    try {
      Actor.registry.actors.foreach(_ ! PoisonPill)
    } catch {
      case _ @ e => logger.warn("Error while stopping Spray Server")
    }
  }

}