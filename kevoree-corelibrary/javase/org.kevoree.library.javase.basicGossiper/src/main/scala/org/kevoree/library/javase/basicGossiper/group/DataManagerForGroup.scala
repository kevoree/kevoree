package org.kevoree.library.javase.basicGossiper.group

import java.util.Date
import java.util.HashMap
import java.util.UUID
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import java.lang.Math
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import org.kevoree.library.javase.basicGossiper.{Occured, VersionUtils, VectorClockAspect, DataManager}
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.framework.{KevoreeXmiHelper, FileNIOHelper}
import java.io.{File, ByteArrayOutputStream, ByteArrayInputStream}
import org.kevoree.library.basicGossiper.protocol.version.Version.{ClockEntry, VectorClock}
import org.kevoree.library.javase.conflictSolver.ConflictSolver

class DataManagerForGroup(nameInstance: String, selfNodeName: String, modelService: KevoreeModelHandlerService, merge: Boolean, solver : ConflictSolver)
  extends DataManager {

  private var lastCheckedTimeStamp = new Date(0l)
  private val uuid: UUID = UUID.nameUUIDFromBytes(nameInstance.getBytes)
  private var vectorClock: VectorClock = VectorClock.newBuilder
    .setTimestamp(System.currentTimeMillis)
    /*.addEnties(
                ClockEntry.newBuilder.setNodeID(selfNodeName).setVersion(1)
                  .setTimestamp(System.currentTimeMillis()).build)*/
    .build
  private val logger = LoggerFactory.getLogger(classOf[DataManagerForGroup])

  def getData(uuid: UUID): (VectorClock, Any) = {
    logger.debug("getData")
    val result = if (uuid.equals(this.uuid)) {
      if ((vectorClock.getEntiesCount == 0) || modelService.getLastModification.after(lastCheckedTimeStamp)) {
        setVectorClock(increment())
      }
      new Tuple2[VectorClock, ContainerRoot](vectorClock, modelService.getLastModel)
    } else {
      null
    }
    logger.debug("getData end")
    result
  }

  def setData(uuid: UUID, tuple: (VectorClock, Any), source: String): Boolean = {
    if (uuid.equals(this.uuid) && tuple._2 != null && tuple._2.isInstanceOf[ContainerRoot]) {
      updateOrResolve(tuple.asInstanceOf[(VectorClock, ContainerRoot)], source)
      true
    } else {
      false
    }
  }

  def checkForGarbage(uuids: List[UUID], source: String) {}

  def removeData(uuid: UUID, tuple: (VectorClock, Any)) {}

  def getUUIDVectorClock(uuid: UUID): VectorClock = {
    logger.debug("getUUIDVectorClock")
    val result = {
      if ((vectorClock.getEntiesCount == 0) || modelService.getLastModification.after(lastCheckedTimeStamp)) {
        setVectorClock(increment())
      }
      getUUIDVectorClockFromUUID(uuid)
    }
    logger.debug("getUUIDVectorClock end")
    result
  }

  def getUUIDVectorClocks: java.util.Map[UUID, VectorClock] = {
    logger.debug("getUUIDVectorClocks")
    val result = {if ((vectorClock.getEntiesCount == 0) || modelService.getLastModification.after(lastCheckedTimeStamp)) {
      setVectorClock(increment())
    }
    getAllUUIDVectorClocks }
    logger.debug("getUUIDVectorClocks end")
    result
  }

  protected var lastNodeSynchronization: String = selfNodeName

  protected def setVectorClock(vc: VectorClock) {
    vectorClock = vc
  }

  implicit def vectorDebug(vc: VectorClock) = VectorClockAspect(vc)

  private def updateOrResolve(tuple: (VectorClock, ContainerRoot), source: String) {
    vectorClock.printDebug()
    tuple._1.printDebug()
    val occured = VersionUtils.compare(vectorClock, tuple._1)
    occured match {
      case Occured.AFTER => {
        logger.debug("VectorClocks comparison into DataManager give us: AFTER")
      }
      case Occured.BEFORE => {
        logger.debug("VectorClocks comparison into DataManager give us: BEFORE")
        updateModelOrHaraKiri(tuple._2)
        lastNodeSynchronization = source
        setVectorClock(localMerge(tuple._1))
      }
      case Occured.CONCURRENTLY => {
        logger.debug("VectorClocks comparison into DataManager give us: CONCURRENTLY")
        logger.debug("merge local and remote model due to concurrency")
        val solvedModel = solver.resolve((vectorClock,modelService.getLastModel),(tuple),source,selfNodeName)
        updateModelOrHaraKiri(solvedModel)
        lastNodeSynchronization = source
        // update local vectorclock according to both local and remote vectorclocks
        logger.debug("BEFORE MERGE CONCURENCY")
        vectorClock.printDebug()
        setVectorClock(localMerge(tuple._1))
        logger.debug("AFTER MERGE CONCURENCY")
        increment()
        logger.debug("AFTER INCREMENT CONCURENCY")

        /*
        val localDate = new Date(vectorClock.getTimestamp)
        val remoteDate = new Date(tuple._1.getTimestamp)
        if (merge) {
          // TODO need to be tested
          logger.debug("merge local and remote model due to concurrency")
          updateModelOrHaraKiri(mergerComponent.merge(modelService.getLastModel, tuple._2))
          lastNodeSynchronization = source
          // update local vectorclock according to both local and remote vectorclocks
          logger.debug("BEFORE MERGE CONCURENCY")
          vectorClock.printDebug()
          setVectorClock(localMerge(tuple._1))
          logger.debug("AFTER MERGE CONCURENCY")
          increment()
          logger.debug("AFTER INCREMENT CONCURENCY")
        } else {
          if (localDate.before(remoteDate)) {
            updateModelOrHaraKiri(tuple._2)
            lastNodeSynchronization = source
            setVectorClock(localMerge(tuple._1))
          }
        } */

        logger.debug("Local date is after, do nothing")
      }
      case _ =>
    }
  }

  private def updateModelOrHaraKiri(newmodel: ContainerRoot) {
    if (GroupUtils.detectHaraKiri(newmodel, modelService.getLastModel, nameInstance, selfNodeName)) {
      modelService.updateModel(newmodel)
      lastCheckedTimeStamp = modelService.getLastModification
    } else {
      lastCheckedTimeStamp = modelService.atomicUpdateModel(newmodel)
    }
  }

  private def increment(): VectorClock = {
    logger.debug("Increment")
    val currentTimeStamp = System.currentTimeMillis
    val incrementedEntries = new java.util.ArrayList[ClockEntry]
    var selfFound = false;
    vectorClock.getEntiesList.foreach {
      clock =>
        if (clock.getNodeID.equals(selfNodeName)) {
          selfFound = true;
          if (lastCheckedTimeStamp.before(modelService.getLastModification)) {
            incrementedEntries
              .add(ClockEntry.newBuilder(clock).setVersion(clock.getVersion + 1) /*.setTimestamp (currentTimeStamp)*/
              .build());
            lastCheckedTimeStamp = modelService.getLastModification
          } else {
            incrementedEntries.add(clock);
          }
        } else {
          incrementedEntries.add(clock);
        }
    }
    if (!selfFound) {
      incrementedEntries
        .add(ClockEntry.newBuilder().setNodeID(selfNodeName).setVersion(1) /*.setTimestamp (currentTimeStamp)*/
        .build());
      //lastCheckedTimeStamp = modelService.getLastModification
    }
    logger.debug("End increment")
    VectorClock.newBuilder().addAllEnties(incrementedEntries).setTimestamp(currentTimeStamp).build()
  }

  private def getUUIDVectorClockFromUUID(uuid: UUID): VectorClock = {
    if (uuid.equals(this.uuid)) {
      vectorClock
    } else {
      null
    }
  }

  private def getAllUUIDVectorClocks: java.util.Map[UUID, VectorClock] = {
    val uuidVectorClocks: java.util.Map[UUID, VectorClock] = new HashMap[UUID, VectorClock]
    uuidVectorClocks.put(uuid, vectorClock)
    uuidVectorClocks
  }

  private def localMerge(clock2: VectorClock): VectorClock = {

    val newClockBuilder = VectorClock.newBuilder();
    val clock = vectorClock
    val orderedNodeID = new java.util.ArrayList[String]();
    val values = new java.util.HashMap[String, Int]();
    //val timestamps = new java.util.HashMap[String, Long] ();

    val currentTimeMillis = System.currentTimeMillis();

    var i: Int = 0;
    var j: Int = 0;
    while (i < clock.getEntiesCount || j < clock2.getEntiesCount) {

      clock match {
        case _ if (i >= clock.getEntiesCount) => {
          addOrUpdate(orderedNodeID, values /*, timestamps*/ , clock2.getEnties(j), currentTimeMillis)
          j = j + 1;
        }
        case _ if (j >= clock2.getEntiesCount) => {
          addOrUpdate(orderedNodeID, values /*, timestamps*/ , clock.getEnties(i), currentTimeMillis)
          i = i + 1;
        }
        case _ => {
          val v1 = clock.getEnties(i);
          val v2 = clock2.getEnties(j);
          if (v1.getNodeID.equals(v2.getNodeID)) {
            values.put(v1.getNodeID, Math.max(v1.getVersion, v2.getVersion));
            //timestamps.put (v1.getNodeID, currentTimeMillis);
            if (!orderedNodeID.contains(v1.getNodeID)) {
              orderedNodeID.add(v1.getNodeID);
            }
            i = i + 1;
            j = j + 1;
          } else {
            if (j < i) {
              if (!orderedNodeID.contains(v2.getNodeID)) {
                orderedNodeID.add(v2.getNodeID);
                values.put(v2.getNodeID, v2.getVersion);
                //timestamps.put (v2.getNodeID, v2.getTimestamp);
              } else {
                values.put(v2.getNodeID, Math.max(v2.getVersion, values.get(v2.getNodeID)));
                //timestamps.put (v2.getNodeID, currentTimeMillis);
              }
              j = j + 1;
            } else {
              if (!orderedNodeID.contains(v1.getNodeID)) {
                orderedNodeID.add(v1.getNodeID);
                values.put(v1.getNodeID, v1.getVersion);
                //timestamps.put (v1.getNodeID, v1.getTimestamp);
              } else {
                values.put(v1.getNodeID, Math.max(v1.getVersion, values.get(v1.getNodeID)));
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
        val entry = ClockEntry.newBuilder().
          setNodeID(nodeId).
          setVersion(values.get(nodeId)) //.
        //setTimestamp (timestamps.get (nodeId)).build ();
        newClockBuilder.addEnties(entry);
    }
    newClockBuilder.setTimestamp(currentTimeMillis).build();
  }

  private def addOrUpdate(orderedNodeID: java.util.ArrayList[String], values: java.util.HashMap[String, Int],
                          /*timestamps: java.util.HashMap[String, Long],*/ clockEntry: ClockEntry, currentTimeMillis: Long) {
    if (!orderedNodeID.contains(clockEntry.getNodeID)) {
      orderedNodeID.add(clockEntry.getNodeID);
      values.put(clockEntry.getNodeID, clockEntry.getVersion);
      //timestamps.put (clockEntry.getNodeID, clockEntry.getTimestamp);
    } else {
      values.put(clockEntry.getNodeID, Math.max(clockEntry.getVersion, values.get(clockEntry.getNodeID)));
      //timestamps.put (clockEntry.getNodeID, currentTimeMillis);
    }
  }
}
