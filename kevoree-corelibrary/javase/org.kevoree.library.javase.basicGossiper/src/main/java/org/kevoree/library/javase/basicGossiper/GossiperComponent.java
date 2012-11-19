package org.kevoree.library.javase.basicGossiper;

import java.util.List;

public interface GossiperComponent {

	public String getAddress(String remoteNodeName);

	public int parsePortNumber(String nodeName);

	public String getName();

	public String getNodeName();

	public void localNotification(Object data);
}
