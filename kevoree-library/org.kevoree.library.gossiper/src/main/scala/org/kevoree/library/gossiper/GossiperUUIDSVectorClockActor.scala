/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper

import java.util.HashMap
import java.util.UUID
import org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock
import scala.collection.JavaConversions._

class GossiperUUIDSVectorClockActor extends actors.DaemonActor {

  private var uuids = new HashMap[UUID,Tuple2[VectorClock,Object]]
  private var logger = org.slf4j.LoggerFactory.getLogger(classOf[GossiperActor])
  this.start
  
  case class STOP_GOSSIPER()
  case class GET_UUIDS()
  case class GET(uuid: UUID)
  case class SET(uuid: UUID,value : Tuple2[VectorClock,Object])
  case class REMOVE(uuid: UUID)
  case class MERGE(uuid: UUID,newclock : VectorClock)
  
  
  def getUUIDS() = { {this !? GET_UUIDS()}.asInstanceOf[List[UUID]] }
  def stop() ={this ! STOP_GOSSIPER()}
  def get(uuid: UUID):Tuple2[VectorClock,Object] = {(this !? GET(uuid)).asInstanceOf[Tuple2[VectorClock,Object]] }
  def swap(uuid: UUID,value : Tuple2[VectorClock,Object]):Tuple2[VectorClock,Object] = {(this !? SET(uuid,value)).asInstanceOf[Tuple2[VectorClock,Object]] }
  def remove(uuid: UUID) = { this ! REMOVE(uuid) }
  def merge(uid: UUID,v : VectorClock):VectorClock = {(this !? MERGE(uid,v)).asInstanceOf[VectorClock] }
  
  def act(){
    loop {
      react{
        case GET_UUIDS() => {reply(uuids.keySet.toList)}
        case STOP_GOSSIPER()=> exit
        case GET(uuid) => { reply(uuids.get(uuid)) }
        case SET(uuid,value) => { uuids.put(uuid, value); reply(value) }
        case REMOVE(uuid)=> uuids.remove(uuid)
        case MERGE(uuid,newClock)=> { 
            var mergedVC = localMerge(uuids.get(uuid)._1,newClock)
            uuids.put(uuid, Tuple2[VectorClock,Object](mergedVC,uuids.get(uuid)._2))
            reply(mergedVC)
          }
      }
    }
  }
  
  def localMerge(vc1 : VectorClock,vc2 : VectorClock) : VectorClock = {
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
