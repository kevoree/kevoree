package org.kevoree.library.javase.basicGossiper


trait PeerSelector {

  def selectPeer(name: String) : String
  def updateNodeScore (nodeName: String, failure: Boolean)
  def resetNodeFailureManagement (nodeName: String)
  def resetAll()
}