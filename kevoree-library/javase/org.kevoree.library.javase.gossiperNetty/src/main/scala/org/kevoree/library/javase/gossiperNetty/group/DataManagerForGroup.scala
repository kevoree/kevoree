package org.kevoree.library.javase.gossiperNetty.group

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Date
import java.util.HashMap
import java.util.UUID
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import java.lang.Math
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import org.kevoree.library.javase.gossiperNetty.{Occured, VersionUtils, VectorClockAspect, DataManager}
import org.kevoree.library.gossiperNetty.protocol.version.Version.{ClockEntry, VectorClock}

class DataManagerForGroup (nameInstance: String, selfNodeName: String, modelService: KevoreeModelHandlerService)
  extends DataManager with actors.DaemonActor {

  private var lastCheckedTimeStamp = new Date (0l)
  private val uuid: UUID = UUID.nameUUIDFromBytes (nameInstance.getBytes)
  private var vectorClock: VectorClock = VectorClock.newBuilder
    .setTimestamp (System.currentTimeMillis)
    /*.addEnties(
                ClockEntry.newBuilder.setNodeID(selfNodeName).setVersion(1)
                  .setTimestamp(System.currentTimeMillis()).build)*/
    .build
  private val logger = LoggerFactory.getLogger (classOf[DataManagerForGroup])

  //private var model: ContainerRoot = null


  case class GetData (uuid: UUID)

  case class SetData (uuid: UUID, tuple: (VectorClock, Any), source : String)

  case class RemoveData (uuid: UUID, tuple: (VectorClock, Any))

  case class GetUUIDVectorClock (uuid: UUID)

  case class GetUUIDVectorClocks ()

  case class Stop ()

//  case class MergeClock (uuid: UUID, newclock: VectorClock, source : String)

  def stop () {
    this ! Stop ()
  }

  def getData (uuid: UUID): (VectorClock, Any) = {
    logger.debug ("getData")
    val result = (this !? GetData (uuid)).asInstanceOf[(VectorClock, Any)]
    logger.debug ("getData end")
    result
  }

  def setData (uuid: UUID, tuple: (VectorClock, Any), source : String) : Boolean = {
    (this !? SetData (uuid, tuple, source)).asInstanceOf[Boolean]
  }

  def checkForGarbage (uuids: List[UUID], source : String) {}

  def removeData (uuid: UUID, tuple: (VectorClock, Any)) {}

  def getUUIDVectorClock (uuid: UUID): VectorClock = {
    logger.debug ("getUUIDVectorClock")
    val result = (this !? GetUUIDVectorClock (uuid)).asInstanceOf[VectorClock]
    logger.debug ("getUUIDVectorClock end")
    result
  }

  def getUUIDVectorClocks() : java.util.Map[UUID, VectorClock] = {
    logger.debug ("getUUIDVectorClocks")
    val result = (this !? GetUUIDVectorClocks ()).asInstanceOf[java.util.Map[UUID, VectorClock]]
    logger.debug ("getUUIDVectorClocks end")
    result
  }

  protected var lastNodeSynchronization : String = selfNodeName
  /*def mergeClock (uid: UUID, v: VectorClock, source : String): VectorClock = {
    logger.debug ("mergeClock")
    val result = (this !? MergeClock (uid, v, source)).asInstanceOf[VectorClock]
    logger.debug ("mergeClock end")
    result
  }*/

  protected def setVectorClock (vc: VectorClock) {
    vectorClock = vc
  }

  def act () {
    loop {
      react {
        case Stop () => this.exit()
        case GetData (uuid) => {
          if (uuid.equals (this.uuid)) {
            if ((vectorClock.getEntiesCount == 0) || modelService.getLastModification.after (lastCheckedTimeStamp)) {
              setVectorClock (increment ())
            }
            val tuple = new Tuple2[VectorClock, ContainerRoot](vectorClock, modelService.getLastModel)
            reply (tuple)
          } else {
            reply (null)
          }
        }
        case SetData (uuid, tuple, source) => {
          if (uuid.equals (this.uuid) && tuple._2 != null && tuple._2.isInstanceOf[ContainerRoot]) {
            updateOrResolve (tuple.asInstanceOf[(VectorClock, ContainerRoot)], source)
            reply(true)
          } else {
            reply(false)
          }
        }
        case GetUUIDVectorClock (uuid) => {
          if ((vectorClock.getEntiesCount == 0) || modelService.getLastModification.after (lastCheckedTimeStamp)) {
            setVectorClock (increment ())
          }
          //setVectorClock(increment())
          reply (getUUIDVectorClockFromUUID (uuid))
        }
        case GetUUIDVectorClocks () => {
          if ((vectorClock.getEntiesCount == 0) || modelService.getLastModification.after (lastCheckedTimeStamp)) {
            setVectorClock (increment ())
          }
          //setVectorClock(increment())
          reply (getAllUUIDVectorClocks)
        }
        /*case MergeClock (uuid, newClock, source) => {
          /*println("localClock")
          vectorClock.getEntiesList.foreach {
            v => println(v.getNodeID + "\t" + v.getVersion + "\t" + v.getTimestamp)
          }
          println("newClock")
          newClock.getEntiesList.foreach {
            v => println(v.getNodeID + "\t" + v.getVersion + "\t" + v.getTimestamp)
          }*/
          lastNodeSynchronization = source
          val mergedVC = localMerge (newClock)
          /*println("merged clock")
          mergedVC.getEntiesList.foreach {
            v => println(v.getNodeID + "\t" + v.getVersion + "\t" + v.getTimestamp)
          }*/
          setVectorClock (mergedVC)
          reply (mergedVC)
        }*/
//        case RemoveData (uuid) => // We do nothing because the model cannot be deleted
      }
    }
  }

  /*private def stringFromModel(): Array[Byte] = {
   val out = new ByteArrayOutputStream
   KevoreeXmiHelper.saveStream(out, modelService.getLastModel)
   out.flush
   val bytes = out.toByteArray
   out.close
   bytes
 }

 private def modelFromString(model: Array[Byte]): ContainerRoot = {
   val stream = new ByteArrayInputStream(model)
   KevoreeXmiHelper.loadStream(stream)
 }*/
  implicit def vectorDebug (vc: VectorClock) = VectorClockAspect (vc)

  private def updateOrResolve (tuple: (VectorClock, ContainerRoot), source : String) {
    vectorClock.printDebug()
    tuple._1.printDebug()
    val occured = VersionUtils.compare (vectorClock, tuple._1)
    occured match {
      case Occured.AFTER => {
        logger.debug ("VectorClocks comparison into DataManager give us: AFTER")
      }
      case Occured.BEFORE => {
        logger.debug ("VectorClocks comparison into DataManager give us: BEFORE")
        updateModelOrHaraKiri (tuple._2)
        lastNodeSynchronization = source
        setVectorClock (localMerge (tuple._1))
      }
      case Occured.CONCURRENTLY => {
        logger.debug ("VectorClocks comparison into DataManager give us: CONCURRENTLY")
        val localDate = new Date (vectorClock.getTimestamp);
        val remoteDate = new Date (tuple._1.getTimestamp);
        //TODO TO IMPROVE
        if (localDate.before (remoteDate)) {
          updateModelOrHaraKiri (tuple._2)
          lastNodeSynchronization = source
          setVectorClock (localMerge (tuple._1))
        }

        logger.debug ("Local date is after, do nothing")
      }
      case _ =>
    }
  }

  private def updateModelOrHaraKiri (newmodel: ContainerRoot) {
    if (GroupUtils.detectHaraKiri (newmodel, modelService.getLastModel, nameInstance, selfNodeName)) {
      modelService.updateModel (newmodel)
      lastCheckedTimeStamp = modelService.getLastModification
    } else {
      lastCheckedTimeStamp = modelService.atomicUpdateModel (newmodel)
    }
  }

  private def increment (): VectorClock = {
    logger.debug ("Increment")
    val currentTimeStamp = System.currentTimeMillis
    val incrementedEntries = new java.util.ArrayList[ClockEntry]
    var selfFound = false;
    vectorClock.getEntiesList.foreach {
      clock =>
        if (clock.getNodeID.equals (selfNodeName)) {
          selfFound = true;
          if (lastCheckedTimeStamp.before (modelService.getLastModification)) {
            incrementedEntries
              .add (ClockEntry.newBuilder (clock).setVersion (clock.getVersion + 1)/*.setTimestamp (currentTimeStamp)*/
              .build ());
            lastCheckedTimeStamp = modelService.getLastModification
          } else {
            incrementedEntries.add (clock);
          }
        } else {
          incrementedEntries.add (clock);
        }
    }
    if (!selfFound) {
      incrementedEntries
        .add (ClockEntry.newBuilder ().setNodeID (selfNodeName).setVersion (1)/*.setTimestamp (currentTimeStamp)*/
        .build ());
      //lastCheckedTimeStamp = modelService.getLastModification
    }
    logger.debug ("End increment")
    VectorClock.newBuilder ().addAllEnties (incrementedEntries).setTimestamp (currentTimeStamp).build ()
  }

  private def getUUIDVectorClockFromUUID (uuid: UUID): VectorClock = {
    if (uuid.equals (this.uuid)) {
      vectorClock
    } else {
      null
    }
  }

  private def getAllUUIDVectorClocks: java.util.Map[UUID, VectorClock] = {
    val uuidVectorClocks: java.util.Map[UUID, VectorClock] = new HashMap[UUID, VectorClock]
    uuidVectorClocks.put (uuid, vectorClock)
    uuidVectorClocks
  }

  private def localMerge (clock2: VectorClock): VectorClock = {

    val newClockBuilder = VectorClock.newBuilder ();
    val clock = vectorClock
    val orderedNodeID = new java.util.ArrayList[String] ();
    val values = new java.util.HashMap[String, Int] ();
    //val timestamps = new java.util.HashMap[String, Long] ();

    val currentTimeMillis = System.currentTimeMillis ();

    var i: Int = 0;
    var j: Int = 0;
    while (i < clock.getEntiesCount || j < clock2.getEntiesCount) {

      clock match {
        case _ if (i >= clock.getEntiesCount) => {
          addOrUpdate (orderedNodeID, values/*, timestamps*/, clock2.getEnties (j), currentTimeMillis)
          j = j + 1;
        }
        case _ if (j >= clock2.getEntiesCount) => {
          addOrUpdate (orderedNodeID, values/*, timestamps*/, clock.getEnties (i), currentTimeMillis)
          i = i + 1;
        }
        case _ => {
          val v1 = clock.getEnties (i);
          val v2 = clock2.getEnties (j);
          if (v1.getNodeID.equals (v2.getNodeID)) {
            values.put (v1.getNodeID, Math.max (v1.getVersion, v2.getVersion));
            //timestamps.put (v1.getNodeID, currentTimeMillis);
            if (!orderedNodeID.contains (v1.getNodeID)) {
              orderedNodeID.add (v1.getNodeID);
            }
            i = i + 1;
            j = j + 1;
          } else {
            if (j < i) {
              if (!orderedNodeID.contains (v2.getNodeID)) {
                orderedNodeID.add (v2.getNodeID);
                values.put (v2.getNodeID, v2.getVersion);
                //timestamps.put (v2.getNodeID, v2.getTimestamp);
              } else {
                values.put (v2.getNodeID, Math.max (v2.getVersion, values.get (v2.getNodeID)));
                //timestamps.put (v2.getNodeID, currentTimeMillis);
              }
              j = j + 1;
            } else {
              if (!orderedNodeID.contains (v1.getNodeID)) {
                orderedNodeID.add (v1.getNodeID);
                values.put (v1.getNodeID, v1.getVersion);
                //timestamps.put (v1.getNodeID, v1.getTimestamp);
              } else {
                values.put (v1.getNodeID, Math.max (v1.getVersion, values.get (v1.getNodeID)));
                //timestamps.put (v1.getNodeID, currentTimeMillis);
              }
              i = i + 1;
            }
          }
        }
      }
    }
    // int index = 0;
    orderedNodeID.foreach {
      nodeId =>
        val entry = ClockEntry.newBuilder ().
          setNodeID (nodeId).
          setVersion (values.get (nodeId))//.
          //setTimestamp (timestamps.get (nodeId)).build ();
        newClockBuilder.addEnties (entry);
    }
    newClockBuilder.setTimestamp (currentTimeMillis).build ();
  }

  private def addOrUpdate (orderedNodeID: java.util.ArrayList[String], values: java.util.HashMap[String, Int],
    /*timestamps: java.util.HashMap[String, Long],*/ clockEntry: ClockEntry, currentTimeMillis: Long) {
    if (!orderedNodeID.contains (clockEntry.getNodeID)) {
      orderedNodeID.add (clockEntry.getNodeID);
      values.put (clockEntry.getNodeID, clockEntry.getVersion);
      //timestamps.put (clockEntry.getNodeID, clockEntry.getTimestamp);
    } else {
      values.put (clockEntry.getNodeID, Math.max (clockEntry.getVersion, values.get (clockEntry.getNodeID)));
      //timestamps.put (clockEntry.getNodeID, currentTimeMillis);
    }
  }
}
