package org.kevoree.library.javase.basicGossiper;

import java.util.List;

public interface GossiperComponent {

	public List<String> getAllPeers();

	public String getAddress(String remoteNodeName);

	public int parsePortNumber(String nodeName);

	public Boolean parseBooleanProperty(String name);

	public String getName();

	public String getNodeName();

	public void localNotification(Object data);
}
