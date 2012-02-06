package org.kevoree.library.sky.manager.http

import org.slf4j.LoggerFactory
import cc.spray.can.{ServerConfig, HttpServer}
import org.kevoree.framework.AbstractNodeType
import akka.actor._
import akka.config.Supervision._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 06/02/12
 * Time: 16:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class IaaSHTTPServer (node: AbstractNodeType) {

  val logger = LoggerFactory.getLogger(this.getClass)
  var id = ""
  var supervisorRef: Supervisor = _

  def startServer (port: Int) {

    id = "iaas.spray-service." + node.getNodeName
    val config = ServerConfig("0.0.0.0", port, id + "-server", id, id)

    supervisorRef = Supervisor(SupervisorConfig(OneForOneStrategy(List(classOf[Exception]), 3, 100),
                                                 List(Supervise(Actor.actorOf(new HTTPServerRoot(id, node)), Permanent), Supervise(Actor.actorOf(new HttpServer(config)), Permanent))))
  }

  def stop () {
    try {
      Actor.registry.actors.foreach(actor => {
        if (actor.getId().contains(id)) {
          try {
            val result = actor ? PoisonPill
            result.get
          } catch {
            case e: akka.actor.ActorKilledException =>
          }
        }
      })

      try {
        val result = Actor.registry.actorFor(supervisorRef.uuid).get ? PoisonPill
        result.get
      } catch {
        case e: akka.actor.ActorKilledException =>
      }

    } catch {
      case _@e => logger.warn("Error while stopping Spray Server ", e)
    }
  }


}
