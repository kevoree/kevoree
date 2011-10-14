package org.kevoree.library.javase.webserver

import akka.config.Supervision._
import akka.config.Supervision.{SupervisorConfig, Permanent}
import akka.actor._
import cc.spray.can.{ServerConfig, HttpServer}

class ServerBootstrap {

  var rootService : RootService = _

  def startServer(port : Int){

    var config = ServerConfig("0.0.0.0",port)

    Supervisor(
        SupervisorConfig(
          OneForOneStrategy(List(classOf[Exception]), 3, 100),
          List(
            Supervise(Actor.actorOf(new RootService("spray-root-service")), Permanent),
            Supervise(Actor.actorOf(new HttpServer(config)), Permanent)
          )
        )
      )
  }

  def stop(){
    Actor.registry.actors.foreach(_ ! PoisonPill)
  }

}