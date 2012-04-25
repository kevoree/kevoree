package org.kevoree.library.rest

import akka.config.Supervision._
import akka.config.Supervision.{SupervisorConfig, Permanent}
import akka.actor._
import cc.spray.can.{ServerConfig, HttpServer}
import org.slf4j.LoggerFactory

class ServerBootstrap (group: RestGroup) {

  var rootService: RootService = _
  val logger = LoggerFactory.getLogger(this.getClass)
  var id = ""
  var supervisorRef: Supervisor = _

  def getRequestTimeout: Long = {
    try {
      java.lang.Long.parseLong(group.getDictionary.get("lock_timeout").toString)
    } catch {
      case _ => 5000
    }
  }

  def startServer (port: Int, ip: String) {

    id = "kevoree.rest.group.spray-service." + group.getName
    val config = ServerConfig(host = ip, port = port, serverActorId = id + "-server", serviceActorId = id, timeoutActorId = id, requestTimeout = getRequestTimeout)

    supervisorRef = Supervisor(
                                SupervisorConfig(
                                                  OneForOneStrategy(List(classOf[Exception]), 3, 100),
                                                  List(
                                                        Supervise(Actor.actorOf(group.getRootService(id)), Permanent),
                                                        Supervise(Actor.actorOf(new HttpServer(config)), Permanent)
                                                      )
                                                )
                              )
  }

  def stop () {
    try {
      Actor.registry.actors.foreach(actor => {
        if (actor.getId().contains(id)) {
          try {
            val result = actor ? PoisonPill
            // wait until the actor is killed
            result.get
          } catch {
            case e: akka.actor.ActorKilledException =>
          }
        }
      })

      try {
        val result = Actor.registry.actorFor(supervisorRef.uuid).get ? PoisonPill
        // wait until the supervisor is killed
        result.get
      } catch {
        case e: akka.actor.ActorKilledException =>
      }

    } catch {
      case _@e => logger.warn("Error while stopping Spray Server ", e)
    }
  }

}