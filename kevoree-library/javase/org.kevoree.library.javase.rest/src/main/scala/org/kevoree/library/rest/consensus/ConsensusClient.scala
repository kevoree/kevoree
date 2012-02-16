package org.kevoree.library.rest.consensus

import org.slf4j.LoggerFactory
import org.kevoree.{ContainerRoot, Group}
import cc.spray.can._
import akka.config.Supervision._
import HttpClient._
import akka.config.Supervision.SupervisorConfig
import akka.actor.{PoisonPill, Supervisor, Actor}
import org.kevoree.framework.{KevoreeXmiHelper, Constants, KevoreePropertyHelper}
import util.matching.Regex


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/02/12
 * Time: 19:09
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class ConsensusClient (groupName: String, timeout : Long) {
  private val logger = LoggerFactory.getLogger(getClass)
  private var alreadyInitialize = 0
  var supervisorRef: Supervisor = _
  var id = ""

  def initialize () {
    if (alreadyInitialize == 0) {
      id = "kevoree.rest.group.spray-service.consensus." + groupName
      val config = ClientConfig(clientActorId = id, requestTimeout = timeout)
      // start and supervise the HttpClient actor
      supervisorRef = Supervisor(SupervisorConfig(
                                                   OneForOneStrategy(List(classOf[Exception]), 3, 100),
                                                   List(Supervise(Actor.actorOf(new HttpClient(config)), Permanent))
                                                 )
                                )
    }
    alreadyInitialize += 1
  }

  def kill () {
    alreadyInitialize -= 1
    if (alreadyInitialize == 0) {
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
        case _@e => logger.warn("Error while stopping Spray client ", e)
      }
    }
  }

  def acquireRemoteLocks (group: Group, nodeName: String, currentModel: ContainerRoot, hash: Long, futureModel: ContainerRoot): Int = {
    logger.debug("Try to acquire a global lock")
    var nbLocked = 1

    group.getSubNodes.filter(n => n.getName != nodeName).foreach {
      node => {
        logger.debug("try to ask for lock to {}", node.getName)
        val remoteHashedModels = sendHash(group.getName, node.getName, currentModel, hash, "/model/consensus/lock")
        // if remote hashes are equivalent to local hash, we consider the remote node is locked
        if (hash == remoteHashedModels) {
          nbLocked += 1
        }
      }
    }

    nbLocked
  }

  def sendModel (group: Group, nodeName: String, currentModel: ContainerRoot, futureModel: ContainerRoot) {
    // send model
    group.getSubNodes.filter(n => n.getName != nodeName).foreach {
      node => {
        sendModel(group.getName, nodeName, node.getName, currentModel, futureModel)
      }
    }
  }

  def unlock (group: Group, nodeName: String, model: ContainerRoot, newHash : Long) {
    // unlock all nodes of the group
    group.getSubNodes.filter(n => n.getName != nodeName).foreach {
      node => {
        unlock(group.getName, node.getName, model, newHash)
      }
    }
  }

  def pull (group: Group, nodeName: String, model: ContainerRoot, hash: Long): Option[ContainerRoot] = {
    var hashes = List[(Long, List[String])]()

    group.getSubNodes.filter(n => n.getName != nodeName).foreach {
      node => {
        val remoteHashedModels = sendHash(group.getName, node.getName, model, hash, "/model/consensus/hash")
        if (hash != remoteHashedModels) {
          hashes.find(t => t._1 == remoteHashedModels) match {
            case None => hashes = hashes ++ List[(Long, List[String])]((remoteHashedModels, List[String](node.getName)))
            case Some(tuple) => {
              val names = tuple._2 ++ List[String](node.getName)
              hashes = hashes -- List[(Long, List[String])](tuple) ++ List[(Long, List[String])]((tuple._1, names))
            }
          }
        }
      }
    }

    hashes.find(t => hashes.forall(t2 => t2._1 != t._1 && t._2.size > t2._2.size)) match {
      case None => None
      case Some(tuple) => {
        // check if the global model is the current one in the local node
        if (tuple._1 != hash) {
          // if the current model is not the global model
          pull(group.getName, tuple._2(0), model)
        } else {
          None
        }
      }
    }
  }

  private def sendHash (groupName: String, nodeName: String, currentModel: ContainerRoot, hash: Long, path: String): Long = {
    logger.debug("send hash of the current model to know if {} is agreed with the current configuration and if an update can be done.", nodeName)
    var ipOption = KevoreePropertyHelper.getStringNetworkProperty(currentModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    var portOption = KevoreePropertyHelper.getIntPropertyForGroup(currentModel, groupName, "port", true, nodeName)

    if (!ipOption.isDefined) {
      ipOption = Some("127.0.0.1")
    }
    if (!portOption.isDefined) {
      portOption = Some(8000)
    }

    // if there is a error about implicit concat, check the import package order
    val dialog = HttpClient.HttpDialog(ipOption.get, portOption.get, id)
      .send(HttpRequest(method = HttpMethods.GET, uri = path + "?hash=" + hash)).end

    dialog.get
    dialog.value match {
      case Some(Right(r: HttpResponse)) => {
        logger.debug("Response received:\n{}", r.bodyAsString)

        val lockRegex = new Regex("<lock nodeName=\"([A-Za-z0-9_]*)\" hash" + "=\"([0-9]*)\" />")
        val hashRegex = new Regex("<hash nodeName=\"([A-Za-z0-9_]*)\">([0-9]*)</hash>")
        r.bodyAsString match {
          case lockRegex(remoteNodeName, remoteHash) => {
            try {
              java.lang.Long.parseLong(remoteHash)
            } catch {
              case e: NumberFormatException => 0
            }
          }
          case hashRegex(remoteNodeName, remoteHash) => {
            try {
              java.lang.Long.parseLong(remoteHash)
            } catch {
              case e: NumberFormatException => 0
            }
          }
          case _ => 0
        }
      }
      case Some(Left(error)) =>
        logger.debug("Response received:\n{}", error.getMessage); 0
      case _@e => logger.debug("Unable to send hash\n{}", e); 0
    }
  }

  private def sendModel (groupName: String, localNodeName: String, remoteNodeName: String, currentModel: ContainerRoot, futureModel: ContainerRoot): Boolean = {
    var ipOption = KevoreePropertyHelper.getStringNetworkProperty(currentModel, remoteNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    var portOption = KevoreePropertyHelper.getIntPropertyForGroup(currentModel, groupName, "port", true, remoteNodeName)

    if (!ipOption.isDefined) {
      ipOption = Some("127.0.0.1")
    }
    if (!portOption.isDefined) {
      portOption = Some(8000)
    }

    val modelString = KevoreeXmiHelper.saveToString(futureModel, false)
    // if there is a error about implicit concat, check the import package order
    val dialog = HttpClient.HttpDialog(ipOption.get, portOption.get, id)
      .send(HttpRequest(method = HttpMethods.POST, uri = "/model/current?sender=" + localNodeName).withBody(modelString)).end

    dialog.get
    dialog.value match {
      case Some(Right(r: HttpResponse)) => {
        logger.debug("Response received:\n{}", r.bodyAsString)
        r.bodyAsString == "<ack nodeName=\"" + remoteNodeName + "\" />"
      }
      case Some(Left(error)) => logger.debug("Response received:\n{}", error.getMessage); false
      case _@e => logger.debug("Unable to send model\n{}", e); false
    }
  }

  private def unlock (groupName: String, nodeName: String, currentModel: ContainerRoot, newHash : Long): Boolean = {
    var ipOption = KevoreePropertyHelper.getStringNetworkProperty(currentModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    var portOption = KevoreePropertyHelper.getIntPropertyForGroup(currentModel, groupName, "port", true, nodeName)
    if (!ipOption.isDefined) {
      ipOption = Some("127.0.0.1")
    }
    if (!portOption.isDefined) {
      portOption = Some(8000)
    }
    // if there is a error about implicit concat, check the import package order
    val dialog = HttpClient.HttpDialog(ipOption.get, portOption.get, id)
      .send(HttpRequest(method = HttpMethods.GET, uri = "/model/consensus/unlock?hash=" + newHash)).end

    dialog.get
    dialog.value match {
      case Some(Right(r: HttpResponse)) => {
        logger.debug("Response received:\n{}", r.bodyAsString)
        r.bodyAsString == "<unlock nodeName=\"" + nodeName + "\" hash=\"" + newHash + "\" />"
      }
      case Some(Left(error)) => logger.debug("Response received:\n{}", error.getMessage); false
      case _@e => logger.debug("Unable to send unlock message\n{}", e); false
    }
  }

  private def pull (groupName: String, nodeName: String, currentModel: ContainerRoot): Option[ContainerRoot] = {
    var ipOption = KevoreePropertyHelper.getStringNetworkProperty(currentModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
    var portOption = KevoreePropertyHelper.getIntPropertyForGroup(currentModel, groupName, "port", true, nodeName)
    if (!ipOption.isDefined) {
      ipOption = Some("127.0.0.1")
    }
    if (!portOption.isDefined) {
      portOption = Some(8000)
    }
    // if there is a error about implicit concat, check the import package order
    val dialog = HttpClient.HttpDialog(ipOption.get, portOption.get).send(HttpRequest(method = HttpMethods.GET, uri = "/model/current")).end

    dialog.get
    dialog.value match {
      case Some(Right(r: HttpResponse)) => {
        logger.debug("Response received:\n{}", r.bodyAsString)
        Some(KevoreeXmiHelper.loadString(r.bodyAsString))
      }
      case Some(Left(error)) => logger.debug("Response received:\n{}", error.getMessage); None
      case _@e => logger.debug("Unable to send a pull request\n{}", e); None
    }
  }
}
