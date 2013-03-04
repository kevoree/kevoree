package org.kevoree.library.javase.webSocketGrp;

public interface WebSocketComponent {

	public String getAddress(String remoteNodeName);

	public int parsePortNumber(String nodeName);

	public String getName();

	public String getNodeName();

	public void localNotification(Object data);
}
