package org.kevoree.library.javase.gossiperNetty;

import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/09/11
 * Time: 10:51
 */
public interface GossiperComponent {

	public List<String> getAllPeers();

	public String getAddress(String remoteNodeName);

	public int parsePortNumber(String nodeName);

	public Boolean parseBooleanProperty(String name);

	public String getName();

	public String getNodeName();

	public void localNotification(Object data);
}
