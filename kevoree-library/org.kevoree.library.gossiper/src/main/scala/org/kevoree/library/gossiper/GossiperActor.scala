/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper

import org.kevoree.library.gossiper.version.GossiperMessages.VersionedModel
import org.kevoree.library.gossiper.version.Occured
import org.kevoree.library.gossiper.version.VersionUtils
import scala.actors.TIMEOUT

class GossiperActor(timeout : Long,group : GossiperGroup[VersionedModel]) extends actors.DaemonActor {

  this.start
  
  case class STOP_GOSSIPER()
  case class DO_GOSSIP(targetNodeName : String)
  case class NOTIFY_PEERS()
  
  def stop(){
    this ! STOP_GOSSIPER()
  }
  
  
  
  def act(){
    loop {
      reactWithin(timeout){
        case DO_GOSSIP(targetNodeName : String) => doGossip(targetNodeName)
        case STOP_GOSSIPER() => this.exit
        case NOTIFY_PEERS() =>
        case TIMEOUT => doGossip(group.selectPeer)
      }
    }
  }
  
  private def doGossip(targetNodeName : String)={
    try {
      var clock = group.getVectorFromPeer(targetNodeName)
      var compareResult = VersionUtils.compare(group.currentClock, clock)
      compareResult match {
        case Occured.AFTER =>
          // we do nothing because VectorClocks are equals (and so models are equals)
          // or local VectorClock is more recent (and so local model is more recent)
        case Occured.BEFORE => {
            // we update our local model because the selected peer has a more recent VectorClock (and so a more recent model)
            println("Update detected by Gossip Group")
            var versionedModel = group.getVersionnedModelToPeer(targetNodeName)
            var newClock = group.update(versionedModel)
            group.setCurrentClock(newClock)
          }
        case Occured.CONCURRENTLY => {
            println("Concurrency detected by Gossip Group")
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
