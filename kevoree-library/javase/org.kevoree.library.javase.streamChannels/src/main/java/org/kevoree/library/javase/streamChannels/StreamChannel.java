package org.kevoree.library.javase.streamChannels;

import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.message.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/09/11
 * Time: 16:23
 */
public class StreamChannel extends AbstractChannelFragment {
	@Override
	public Object dispatch (Message message) {
		//Local delivery
		localNotification(message);

		// TODO send on remote fragment


		return null;
	}

	@Override
	public ChannelFragmentSender createSender (String s, String s1) {
		return null;
	}

	public void localNotification (Message o) {
		for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
			forward(p, o);
		}
	}

	public List<String> getAllPeers () {
		List<String> peers = new ArrayList<String>();
		for (KevoreeChannelFragment fragment : getOtherFragments()) {
			peers.add(fragment.getNodeName());
		}
		return peers;
	}

	public String getAddress (String remoteNodeName) {
		String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
				org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (ip == null || ip.equals("")) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	private String name = "[A-Za-z0-9_]*";
	private String portNumber = "(65535|5[0-9]{4}|4[0-9]{4}|3[0-9]{4}|2[0-9]{4}|1[0-9]{4}|[0-9]{0,4})";
	private String separator = ",";
	private String affectation = "=";
	private String portPropertyRegex =
			"((" + name + affectation + portNumber + ")" + separator + ")*(" + name + affectation + portNumber + ")";

	public int parsePortNumber (String nodeName) {
		String portProperty = this.getDictionary().get("port").toString();
		if (portProperty.matches(portPropertyRegex)) {
			String[] definitionParts = portProperty.split(separator);
			for (String part : definitionParts) {
				if (part.contains(nodeName + affectation)) {
					//System.out.println(Integer.parseInt(part.substring((nodeName + affectation).length(), part.length())));
					return Integer.parseInt(part.substring((nodeName + affectation).length(), part.length()));
				}
			}
		} else {
			return Integer.parseInt(portProperty);
		}
		return 0;
	}
}
