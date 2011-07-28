package org.kevoree.library.gossiperNetty.channel

import java.util.HashMap
import java.util.UUID
import org.kevoree.framework.message.Message
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.DataManager
import org.kevoree.library.gossiperNetty.version.Version.{ClockEntry, VectorClock}

class DataManagerForChannel extends DataManager with actors.DaemonActor  {
  
  private var datas = new HashMap[UUID,Tuple2[VectorClock,Message]]()
  private var logger = LoggerFactory.getLogger(classOf[DataManagerForChannel])
  
  this.start
  
  case class GetData(uuid : UUID)
  case class SetData(uuid : UUID, tuple : Tuple2[VectorClock,Any], source : String)
  case class RemoveData(uuid : UUID)
  case class GetUUIDVectorClock(uuid : UUID)
  case class GetUUIDVectorClocks()
  case class Stop()
  case class MergeClock(uuid: UUID,newclock : VectorClock, source : String)
  
  def stop(){
    this ! Stop()
  }
  
  def getData(uuid : UUID) : Tuple2[VectorClock,Any] ={
	(this !? GetData(uuid)).asInstanceOf[Tuple2[VectorClock,Any]]
  }
  
  def setData(uuid : UUID, tuple : Tuple2[VectorClock,Any], source : String) ={
	this ! SetData(uuid, tuple, source)
  }
  
  def removeData(uuid : UUID) ={
	this ! RemoveData(uuid)
  }
  
  def getUUIDVectorClock(uuid : UUID) : VectorClock ={
	(this !? GetUUIDVectorClock(uuid)).asInstanceOf[VectorClock]
  }
  
  def getUUIDVectorClocks() : java.util.Map[UUID, VectorClock] ={
	(this !? GetUUIDVectorClocks()).asInstanceOf[java.util.Map[UUID, VectorClock]]
  }
  
  def mergeClock(uid: UUID,v : VectorClock, source : String):VectorClock = {(this !? MergeClock(uid,v, source)).asInstanceOf[VectorClock] }
  
  def act(){
	loop {
	  react {
		case Stop() => this.exit
		case GetData(uuid) => reply(datas.get(uuid)) // TODO maybe we need to clone the map
		case SetData(uuid, tuple, source) => {
			if (tuple._2.isInstanceOf[Message]) {
			  datas.put(uuid, tuple.asInstanceOf[Tuple2[VectorClock,Message]])
			}
		}
		case RemoveData(uuid) => datas.remove(uuid)
		case GetUUIDVectorClock(uuid) => reply(getUUIDVectorClockFromUUID(uuid))
		case GetUUIDVectorClocks() => reply(getAllUUIDVectorClocks())
		case MergeClock(uuid,newClock, source)=> {
			//println("clock must be merged")
			val mergedVC = localMerge(datas.get(uuid)._1,newClock)
			datas.put(uuid, Tuple2[VectorClock,Message](mergedVC,datas.get(uuid)._2))
			//println("clock has been merged")
			reply(mergedVC)
		  }
	  }
	}
  }
  
  private def getUUIDVectorClockFromUUID(uuid : UUID) : VectorClock ={
	if (datas.contains(uuid)) {
	  datas.get(uuid)._1
	} else {
	  null
	}
  }
  
  private def getAllUUIDVectorClocks() : java.util.Map[UUID, VectorClock] ={
	var uuidVectorClocks : java.util.Map[UUID, VectorClock] = new HashMap[UUID, VectorClock]()
	datas.keySet.foreach { uuid : UUID =>
	  //println(uuid)
	  uuidVectorClocks.put(uuid, datas.get(uuid)._1)
	}
	uuidVectorClocks
  }
  
  
  private def localMerge(vc1 : VectorClock,vc2 : VectorClock) : VectorClock = {
    var enties = vc1.getEntiesList.toList
    val timeStamp = System.currentTimeMillis
    //ADD VC2
    vc2.getEntiesList.foreach{clockEntry =>
      enties.find(p=> p.getNodeID == clockEntry.getNodeID) match {
        case Some(toUpdate) => {
			//CHECK MAX VALUE
			val newClock = ClockEntry.newBuilder(toUpdate).setVersion(java.lang.Math.max(toUpdate.getVersion,clockEntry.getVersion)).setTimestamp(timeStamp).build
			enties = ((enties -- List(toUpdate)) ++ List(newClock)) 
		  }
        case None => enties = enties ++ List(clockEntry)
      }
    }
    VectorClock.newBuilder.addAllEnties(enties).setTimestamp(timeStamp).build
  }

}
