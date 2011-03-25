/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.util.HashMap
import java.util.UUID
import org.kevoree.framework.message.Message
import org.kevoree.library.version.Version.ClockEntry
import org.kevoree.library.version.Version.VectorClock
import scala.collection.JavaConversions._

class DataManager extends actors.DaemonActor {
  
  private var datas = new HashMap[UUID,Tuple2[VectorClock,Message]]()
  
  this.start
  
  case class GetData(uuid : UUID)
  case class SetData(uuid : UUID, tuple : Tuple2[VectorClock,Message])
  case class RemoveData(uuid : UUID)
  case class GetUUIDVectorClock(uuid : UUID)
  case class GetUUIDVectorClocks()
  case class Stop()
  case class MergeClock(uuid: UUID,newclock : VectorClock)
  
  def stop(){
    this ! Stop()
  }
  
  def getData(uuid : UUID) : Tuple2[VectorClock,Message] ={
	(this !? GetData(uuid)).asInstanceOf[Tuple2[VectorClock,Message]]
  }
  
  def setData(uuid : UUID, tuple : Tuple2[VectorClock,Message]) ={
	this ! SetData(uuid, tuple)
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
  
  def mergeClock(uid: UUID,v : VectorClock):VectorClock = {(this !? MergeClock(uid,v)).asInstanceOf[VectorClock] }
  
  def act(){
	react {
	  case Stop() => this.exit
	  case GetData(uuid) => reply(datas.get(uuid)) // TODO maybe we need to clone the map
	  case SetData(uuid, tuple) => datas.put(uuid, tuple)
	  case RemoveData(uuid) => datas.remove(uuid)
	  case GetUUIDVectorClock(uuid) => reply(getUUIDVectorClockFromUUID(uuid))
	  case GetUUIDVectorClocks() => reply(this.getAllUUIDVectorClocks())
	  case MergeClock(uuid,newClock)=> { 
		  var mergedVC = localMerge(datas.get(uuid)._1,newClock)
		  datas.put(uuid, Tuple2[VectorClock,Message](mergedVC,datas.get(uuid)._2))
		  reply(mergedVC)
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
	  uuidVectorClocks.put(uuid, datas.get(uuid)._1)
	}
	uuidVectorClocks
  }
  
  
  private def localMerge(vc1 : VectorClock,vc2 : VectorClock) : VectorClock = {
    var enties = vc1.getEntiesList.toList
    var timeStamp = System.currentTimeMillis
    //ADD VC2
    vc2.getEntiesList.foreach{clockEntry =>
      enties.find(p=> p.getNodeID == clockEntry.getNodeID) match {
        case Some(toUpdate) => {
			//CHECK MAX VALUE
			var newClock = ClockEntry.newBuilder(toUpdate).setVersion(java.lang.Math.max(toUpdate.getVersion,clockEntry.getVersion)).setTimestamp(timeStamp).build
			enties = ((enties -- List(toUpdate)) ++ List(newClock)) 
		  }
        case None => enties = enties ++ List(clockEntry)
      }
    }
    VectorClock.newBuilder.addAllEnties(enties).setTimestamp(timeStamp).build
  }

}
