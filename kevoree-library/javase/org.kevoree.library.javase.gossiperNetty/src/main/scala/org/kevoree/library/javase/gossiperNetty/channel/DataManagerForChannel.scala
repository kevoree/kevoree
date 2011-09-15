package org.kevoree.library.javase.gossiperNetty.channel

import java.util.HashMap
import java.util.UUID
import org.kevoree.framework.message.Message
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.protocol.version.Version.{ClockEntry, VectorClock}
import org.kevoree.library.javase.gossiperNetty.{GossiperComponent, DataManager}
import scala.Any

class DataManagerForChannel (instance: GossiperComponent, nodeName: String)
  extends DataManager with actors.DaemonActor {

  private val datas = new HashMap[UUID, /*(*/ (VectorClock, Message) /*, Int)*/ ]()
  private val logger = LoggerFactory.getLogger(classOf[DataManagerForChannel])

  this.start()

  case class GetData (uuid: UUID)

  case class SetData (uuid: UUID, tuple: (VectorClock, Any), source: String)

  case class RemoveData (uuid: UUID, tuple: (VectorClock, Any))

  case class GetUUIDVectorClock (uuid: UUID)

  case class GetUUIDVectorClocks ()

  case class Stop ()

  //  case class MergeClock (uuid: UUID, newclock: VectorClock, source: String)

  case class CHECK_FOR_GARBAGE (uuids: List[UUID], source: String)

  def stop () {
    this ! Stop()
  }

  def getData (uuid: UUID): (VectorClock, Any) = {
    (this !? GetData(uuid)).asInstanceOf[(VectorClock, Any)]
  }

  def setData (uuid: UUID, tuple: (VectorClock, Any), source: String) : Boolean = {
    (this !? SetData(uuid, tuple, source)).asInstanceOf[Boolean]
  }

  def removeData (uuid: UUID, tuple: (VectorClock, Any)) {
    this ! RemoveData(uuid, tuple)
  }

  def getUUIDVectorClock (uuid: UUID): VectorClock = {
    (this !? GetUUIDVectorClock(uuid)).asInstanceOf[VectorClock]
  }

  def getUUIDVectorClocks (): java.util.Map[UUID, VectorClock] = {
    (this !? GetUUIDVectorClocks()).asInstanceOf[java.util.Map[UUID, VectorClock]]
  }

  /*def mergeClock (uid: UUID, v: VectorClock, source: String): VectorClock = {
    (this !? MergeClock(uid, v, source)).asInstanceOf[VectorClock]
  }*/

  def checkForGarbage (uuids: List[UUID], source: String) {
    this ! CHECK_FOR_GARBAGE(uuids, source)
  }

  def act () {
    loop {
      react {
        case Stop() => this.exit()
        case GetData(uuid) => reply(datas.get(uuid) /*._1*/)
        case SetData(uuid, tuple, source) => {
          if (tuple._2.isInstanceOf[Message]) {
            if (!garbage(uuid, tuple.asInstanceOf[(VectorClock, Message)])) {
              datas.put(uuid, /*(*/ tuple.asInstanceOf[(VectorClock, Message)])
              reply(true)
            } else {
              reply(false)
            }
          } else {
            reply(false)
          }
        }
        case RemoveData(uuid, tuple) => garbage(uuid, tuple.asInstanceOf[(VectorClock, Message)])
        case GetUUIDVectorClock(uuid) => reply(getUUIDVectorClockFromUUID(uuid))
        case GetUUIDVectorClocks() => reply(getAllUUIDVectorClocks())
        /*case MergeClock(uuid, newClock, source) => {
          //println("clock must be merged")
          val mergedVC = localMerge(datas.get(uuid)._1._1, newClock)
          datas.put(uuid, ((mergedVC, datas.get(uuid)._1._2), if (datas.get(uuid) != null) {
            datas.get(uuid)._2
          } else {
            0
          }))
          //println("clock has been merged")
          reply(mergedVC)
        }*/
        case CHECK_FOR_GARBAGE(uuids, source) => checkForGarbageInternal(uuids, source)
      }
    }
  }

  private def garbage (uuid: UUID, tuple: (VectorClock, Message)): Boolean = {
    //CHECK FOR GARBAGE
    if (tuple._1.getEnties(0).getNodeID.equals(instance.getNodeName)) {
      val allPresent = instance.getAllPeers.forall(peer => {
        tuple._1.getEntiesList.exists(e => e.getNodeID.equals(peer) && e.getVersion > 0)
      })
      if (allPresent) {
        //THIS NODE IS MASTER ON THE MSG
        //ALL REMOTE NODE IN MY !PRESENT! M@R has rec a copy
        //DELETING
        //
        if (datas.contains(uuid)) {
          logger.debug("GARBAGE =" + uuid)
          datas.remove(uuid)
        }
        true
      } else {
        false
      }
    } else {
      false
    }
  }

  private def checkForGarbageInternal (uuids: List[UUID], source: String) {
    logger.debug("checking uuids for garbage")
    datas.keySet().toList.foreach {
      key =>
        if (!uuids.contains(key)) {
          if (datas.get(key)._1 /*._1*/ .getEntiesList.exists(e => e.getNodeID.equals(source))) {
            logger.debug("ALREADY SEEN DATA - GARBAGE IT")
            datas.remove(key)
          }
        }
    }
  }

  private def getUUIDVectorClockFromUUID (uuid: UUID): VectorClock = {
    if (datas.contains(uuid)) {
      updateVectorClock(uuid)
      datas.get(uuid)._1 /*._1*/
    } else {
      null
    }
  }

  private def getAllUUIDVectorClocks (): java.util.Map[UUID, VectorClock] = {
    val uuidVectorClocks: java.util.Map[UUID, VectorClock] = new HashMap[UUID, VectorClock]()
    datas.keySet.foreach {
      uuid: UUID =>
        updateVectorClock(uuid)
        //println(uuid)
        uuidVectorClocks.put(uuid, datas.get(uuid)._1 /*._1*/)
    }
    uuidVectorClocks
  }

  private def updateVectorClock (uuid: UUID) {
    val vectorClock = datas.get(uuid)._1 /*._1*/
    vectorClock.getEntiesList.filter(e => e.getNodeID.equals(nodeName)) match {
      case scala.collection.mutable.Buffer(e) => // we do nothing
      case scala.collection.mutable.Buffer() => {
        logger.debug("add myself on the vectorclock!")
        // we add the current nodeName on the vectorclock
        var enties = vectorClock.getEntiesList.toList
        enties = enties ++ List(ClockEntry.newBuilder()
          .setNodeID(nodeName)
          .setVersion(2) /*.setTimestamp(timeStamp)*/
          .build)
        datas.put(uuid,
                   /*(*/
                   (VectorClock.newBuilder.addAllEnties(enties).setTimestamp(vectorClock.getTimestamp).build, datas
                     .get(uuid) /*._1*/ ._2) /*, if (datas.get(uuid) != null) {
                     datas.get(uuid)._2
                   } else {
                     0
                   })*/)
      }
    }
  }


  private def localMerge (vc1: VectorClock, vc2: VectorClock): VectorClock = {
    var enties = vc1.getEntiesList.toList
    val timeStamp = System.currentTimeMillis
    //ADD VC2
    vc2.getEntiesList.foreach {
      clockEntry =>
        enties.find(p => p.getNodeID == clockEntry.getNodeID) match {
          case Some(toUpdate) => {
            //CHECK MAX VALUE
            val newClock = ClockEntry.newBuilder(toUpdate)
              .setVersion(java.lang.Math.max(toUpdate.getVersion, clockEntry.getVersion)) /*.setTimestamp(timeStamp)*/
              .build
            enties = ((enties -- List(toUpdate)) ++ List(newClock))
          }
          case None => enties = enties ++ List(clockEntry)
        }
    }
    VectorClock.newBuilder.addAllEnties(enties).setTimestamp(timeStamp).build
  }

}
