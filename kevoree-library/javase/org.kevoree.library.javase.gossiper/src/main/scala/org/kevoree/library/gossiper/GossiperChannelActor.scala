/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper

import scala.actors.TIMEOUT
import scala.collection.JavaConversions._
import java.util.UUID
import org.kevoree.extra.marshalling.RichString
import org.kevoree.framework.message.Message
import org.kevoree.library.gossiper.version.GossiperMessages.ClockEntry
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock
import org.kevoree.library.gossiper.version.Occured

class GossiperChannelActor(selfNodeName : String,timeout : java.lang.Long,group : GossiperChannel,clocksActor:GossiperUUIDSVectorClockActor) extends actors.DaemonActor {

  /* CONSTRUCTOR */
  private var logger = org.slf4j.LoggerFactory.getLogger(classOf[GossiperActor])
  //private var clocksActor = new GossiperUUIDSVectorClockActor
  this.start
  
  
  /*
   * si newValue => creer un uuid et on stocke
   * on push à tout ceux connu
   * 
   * on attend que les autres viennent chercher
   * 
   * selectPeer => il est orienté
   *  on va faire du gossip avec ceux dont on ne sait pas si ils sont à jour
   */
  
  /* PUBLIC PART */
  case class STOP_GOSSIPER()
  case class DO_GOSSIP(targetNodeName : String)
  case class NOTIFY_PEERS()
  
  def stop(){
    this ! STOP_GOSSIPER()
  }

  
  def scheduleGossip(nodeName : String)={
    this ! DO_GOSSIP(nodeName)
  }
  
  def notifyPeers()={
    this ! NOTIFY_PEERS()
  }
  
  /* PRIVATE PROCESS PART */
  def act(){
    loop {
      reactWithin(timeout.longValue){
        case DO_GOSSIP(targetNodeName) => doGossip(targetNodeName)
        case STOP_GOSSIPER() => this.exit
        case NOTIFY_PEERS() => doNotifyPeer()
        case TIMEOUT => doGossip(selectPeer)
      }
    }
  }
  
  private def doNotifyPeer(){
    group.notifyPeers
  }
  
  private def doGossip(targetNodeName : String)={
    //println("doGossip="+targetNodeName)
    
    if(targetNodeName!= null && targetNodeName != ""){
       
      /* UUIDS synchronisation STEP */
      var remoteUuids = group.getMsgUUIDSFromPeer(targetNodeName)
      
      if(remoteUuids!=null){
        /* check for new uuid values*/
        remoteUuids.foreach{uuid=>
          if(clocksActor.get(uuid)==null){
            clocksActor.swap(uuid, Tuple2[VectorClock,Object](VectorClock.newBuilder.setTimestamp(System.currentTimeMillis).build,null))
          } 
        }
        /* check for deleted uuid values */
        var localUUIDs = clocksActor.getUUIDS
        localUUIDs.foreach{key=>
          if(!remoteUuids.contains(key)){
            if(clocksActor.get(key)._1.getEntiesList.exists(e=> e.getNodeID == targetNodeName)){
              //ALREADY SEEN VECTOR CLOCK - GARBAGE IT
              clocksActor.remove(key)
            } else {
              //NOOP - UNCOMPLETE VECTOR CLOCK
            }
          }
        }
      }
      
      //println("recUUID="+clocksActor.getUUIDS.mkString(","))
      
        
      //FOREACH UUIs
      clocksActor.getUUIDS.foreach{local_UUID=>
        var remoteVectorClock = group.getUUIDVectorClockFromPeer(targetNodeName, local_UUID)
        if(remoteVectorClock != null){
          var occured = VersionUtils.compare(clocksActor.get(local_UUID)._1, remoteVectorClock)
          occured match {
            case Occured.AFTER=>
            case Occured.BEFORE=> updateValue(targetNodeName,local_UUID,remoteVectorClock)
            case Occured.CONCURRENTLY=> updateValue(targetNodeName,local_UUID,remoteVectorClock)
            case _ =>
          }
        }
      }
    }
  }
  
  private def updateValue(remoteNodeName : String,uuid:UUID,remoteVectorClock: VectorClock)={
    
    var finalVectorClock = remoteVectorClock
    if(clocksActor.get(uuid)._2 == null){
      //FULL UPDATE
      var remoteVersionedModel = group.getUUIDDataFromPeer(remoteNodeName, uuid)
      if(remoteVersionedModel != null){
        //UNSERIALZE OBJECT
        var remoteObjectByteString = remoteVersionedModel.getModel
        finalVectorClock = remoteVersionedModel.getVector
        var o = RichString(remoteObjectByteString.toStringUtf8).fromJSON(classOf[Message])
        clocksActor.swap(uuid, Tuple2[VectorClock,Object](finalVectorClock,o))
        group.localDelivery(o) 
      }
    }
    //UPDATE CLOCK
    //println("msg distributed")
    
    finalVectorClock.getEntiesList.find(p=> p.getNodeID == selfNodeName) match {
      case Some(p)=> //NOOP
      case None => {
          var newenties = ClockEntry.newBuilder.setNodeID(selfNodeName).setTimestamp(System.currentTimeMillis).setVersion(1).build
          finalVectorClock = VectorClock.newBuilder(finalVectorClock).addEnties(newenties).setTimestamp(System.currentTimeMillis).build
      }
    }
    
    var newMerged = clocksActor.merge(uuid, finalVectorClock)
    //println("msg merged ")
    implicit def vectorDebug(vc : VectorClock) = VectorClockAspect(vc) 
    newMerged.printDebug
    
    //CHECK FOR GARBAGE
    if(newMerged.getEnties(0).getNodeID.equals(selfNodeName)){
      val allPresent = group.getAllPeers.forall(peer=>{
          newMerged.getEntiesList.exists(e=> e.getNodeID == peer && e.getVersion > 0)
      })
      if(allPresent){
        //THIS NODE IS MASTER ON THE MSG
        //ALL REMOTE NODE IN MY !PRESENT! M@R has rec a copy
        //DELETING
        //
        //println("Garbage ="+uuid)
        clocksActor.remove(uuid)
      }
    }
  }
  
  private def selectPeer() : String = {
    group.selectPeer
  }
  
}
