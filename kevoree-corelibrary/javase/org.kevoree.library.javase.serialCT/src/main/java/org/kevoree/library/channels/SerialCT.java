package org.kevoree.library.channels;

import org.kevoree.annotation.*;
import org.kevoree.extra.kserial.KevoreeSharedCom;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.HashMap;


/**
 * @author ffouquet
 */
@Library(name = "JavaSE")
@DictionaryType({
		@DictionaryAttribute(name = "serialport", fragmentDependant = true)
})
@ChannelTypeFragment
public class SerialCT extends AbstractChannelFragment {
	private static final Logger logger = LoggerFactory.getLogger(SerialCT.class);

	KContentListener cl = new KContentListener(this);
//    protected ServiceReference sr;
//    protected KevoreeModelHandlerService modelHandlerService = null;

	protected HashMap<String, String> nodePortCache = new HashMap<String, String>();

	protected String getPortFromNode (String remoteNodeName) {

		if (!nodePortCache.containsKey(remoteNodeName)) {
			String remotePort = "/dev/tty_unknown";
			Option<String> remotePortOption = KevoreePropertyHelper.getStringPropertyForChannel(this.getModelService().getLastModel(), this.getName(), "serialport", true, remoteNodeName);
			if (remotePortOption.isDefined()) {
                remotePort = remotePortOption.get();
				nodePortCache.put(remoteNodeName, remotePort);
				//logger.warn(this.getName() + ":SerailCT on node " + this.getNodeName() + " using port " + nodePortCache.get(remoteNodeName));
			} else {
                nodePortCache.put(remoteNodeName, remotePort);
				logger.error("unable to find the given dictionary attribute \"serialport\"!");
			}
		}
		return nodePortCache.get(remoteNodeName);
	}

	@Start
	public void startRxTxChannel () {

		new Thread() {
			@Override
			public void run () {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
				for (KevoreeChannelFragment cf : getOtherFragments()) {
					String port = getPortFromNode(cf.getNodeName());
					if (port != null && !port.equals("")) {
						KevoreeSharedCom.addObserver(port, cl);
					} else {
						logger.error("Com Port Not Found ");
					}
				}
			}
		}.start();
	}

	@Update
	public void updateRxTxChannel () {
		stopRxTxChannel();
		startRxTxChannel();
	}

	@Stop
	public void stopRxTxChannel () {
		for (String port : nodePortCache.values()) {
			KevoreeSharedCom.removeObserver(port, cl);
		}
		nodePortCache.clear();
	}

	@Override
	public Object dispatch (Message msg) {

		for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
			forward(p, msg);
		}
		for (KevoreeChannelFragment cf : getOtherFragments()) {
			if (msg.getPassedNodes().isEmpty()) {
				forward(cf, msg);
			}
		}
		return null;
	}

	@Override
	public ChannelFragmentSender createSender (final String remoteNodeName, final String remoteChannelName) {
		return new ChannelFragmentSender() {

			@Override
			public Object sendMessageToRemote (Message message) {
				String messageTosSend = "#" + getName() + /*":" + getNodeName() +*/ "[" + message.getContent().toString() + "]";

				logger.debug("Send Message");
				logger.debug(getPortFromNode(remoteNodeName));
				logger.debug(messageTosSend);

				KevoreeSharedCom.send(getPortFromNode(remoteNodeName), messageTosSend);
				return null;
			}
		};
	}


}
