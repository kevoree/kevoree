package org.kevoree.library.javase.gossiperNetty

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/09/11
 * Time: 10:28
 */

trait PeerSelector {

  def selectPeer(name: String) : String
  def updateNodeScore (nodeName: String, failure: Boolean)
  def resetNodeFailureManagement (nodeName: String)
  def resetAll()
}