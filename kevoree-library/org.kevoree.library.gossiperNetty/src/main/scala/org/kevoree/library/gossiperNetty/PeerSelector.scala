package org.kevoree.library.gossiperNetty

import org.kevoree.ContainerRoot

/**
 * User: Erwan Daubert
 * Date: 06/04/11
 * Time: 13:10
 */

trait PeerSelector {
	def selectPeer(groupName: String) : String
}