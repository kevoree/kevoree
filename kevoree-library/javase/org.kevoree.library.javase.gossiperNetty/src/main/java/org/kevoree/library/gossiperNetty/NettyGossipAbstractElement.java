/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.gossiperNetty;

import java.util.List;

/**
 *
 * @author edaubert
 */
public interface NettyGossipAbstractElement {

	public List<String> getAllPeers();

	public String getAddress(String remoteNodeName);

	public int parsePortNumber(String nodeName);

	public Boolean parseFullUDPParameter();
	
	//public String selectPeer();
	
	public String getName();
	
	public String getNodeName();
	
	public void localNotification(Object data);
	
}
