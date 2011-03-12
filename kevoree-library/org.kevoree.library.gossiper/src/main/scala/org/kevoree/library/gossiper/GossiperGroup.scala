/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper

import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock

trait GossiperGroup[A] {

  def getVectorFromPeer(targetNodeName:String) : VectorClock
  def getVersionnedModelToPeer(targetNodeName: String) : A
  def selectPeer(): String
  def currentClock : VectorClock
  def setCurrentClock(clock : VectorClock)
  def resolve(model : A) : VectorClock
  def update(model : A) : VectorClock
  
}
