/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper

import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock
import org.kevoree.library.gossiper.version.GossiperMessages.VersionedModel
import org.kevoree.library.gossiper.version.Occured
import scala.actors.TIMEOUT

class GossiperActor(timeout : Long,group : GossiperGroup[VersionedModel]) extends actors.DaemonActor {

  private var logger = org.slf4j.LoggerFactory.getLogger(classOf[GossiperActor])
  
  this.start
  
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
      reactWithin(timeout){
        case DO_GOSSIP(targetNodeName) => doGossip(targetNodeName)
        case STOP_GOSSIPER() => this.exit
        case NOTIFY_PEERS() => doNotifyPeer()
        case TIMEOUT => doGossip(group.selectPeer)
      }
    }
  }
  
  private def doNotifyPeer(){
    group.notifyPeers
  }
  
  implicit def vectorDebug(vc : VectorClock) = VectorClockAspect(vc) 
  private def doGossip(targetNodeName : String)={
    if(targetNodeName != ""){
      try {
        var clock = group.getVectorFromPeer(targetNodeName)

        group.currentClock.printDebug
        clock.printDebug
      
        var compareResult = VersionUtils.compare(group.currentClock, clock)
      
        logger.debug(compareResult.toString)
      
        compareResult match {
          case Occured.AFTER =>
            // we do nothing because VectorClocks are equals (and so models are equals)
            // or local VectorClock is more recent (and so local model is more recent)
          case Occured.BEFORE => {
              // we update our local model because the selected peer has a more recent VectorClock (and so a more recent model)
              logger.debug("Update detected by Gossip Group")
              var versionedModel = group.getVersionnedModelToPeer(targetNodeName)
              var newClock = group.update(versionedModel)
              group.setCurrentClock(newClock)
            }
          case Occured.CONCURRENTLY => {
              logger.debug("Concurrency detected by Gossip Group")
              var versionedModel = group.getVersionnedModelToPeer(targetNodeName)
              group.setCurrentClock(group.resolve(versionedModel))
              // Other possibility not implemented :
              // It is not possible to find the most recent VectorClock (and so the more recent model)
              // That's why we choose to keep all local information about local node, component, ...
              // We also choose to keep all remote information describe into the model of the selected peer.
            }
        }
      } catch {
        case _ @ e => //TODO ERROR AS DEBUG
      }
    }
  }
  
  
}
