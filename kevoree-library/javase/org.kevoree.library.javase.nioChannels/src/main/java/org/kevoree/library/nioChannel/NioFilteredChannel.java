package org.kevoree.library.nioChannel;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreePort;
import org.kevoree.framework.message.Message;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/04/12
 * Time: 10:33
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@ChannelTypeFragment
public class NioFilteredChannel extends NioChannel {

	private static final Logger logger = LoggerFactory.getLogger(NioFilteredChannel.class);

	@Override
	public Object dispatch (Message message) {
		logger.debug("TOTO");
		if (message.getDestNodeName().equals(this.getNodeName())) {
			if (message.getContent() instanceof StdKevoreeMessage
					&& ((StdKevoreeMessage) message.getContent()).getValue("destComponentName").isDefined()
					&& ((StdKevoreeMessage) message.getContent()).getValue("destNodeName").isDefined()) {
				localFilteredForward(message);
			} else {
				localForward(message);
			}
		} else {
			if (message.getContent() instanceof StdKevoreeMessage
					&& ((StdKevoreeMessage) message.getContent()).getValue("destComponentName").isDefined()
					&& ((StdKevoreeMessage) message.getContent()).getValue("destNodeName").isDefined()) {
				logger.debug("message to {}@{}", ((StdKevoreeMessage) message.getContent()).getValue("destComponentName").get(),
						((StdKevoreeMessage) message.getContent()).getValue("destNodeName").get());
				if (!((StdKevoreeMessage) message.getContent()).getValue("destNodeName").get().equals(this.getNodeName())) {
					logger.debug("send message to remote node");
					remoteFilteredForward(message);
				} else if (((StdKevoreeMessage) message.getContent()).getValue("destNodeName").get().equals(this.getNodeName())) {
					logger.debug("send message to local component");
					localFilteredForward(message);
				} else {
					logger.debug("send message to all bound components");
					localForward(message);
					remoteForward(message);
				}
			} else {
				logger.debug("send message to all bound components");
				localForward(message);
				remoteForward(message);
			}
		}
		return null;
	}

	private void localForward (Message message) {
		for (KevoreePort port : getBindedPorts()) {
			forward(port, message);
		}
	}

	private void localFilteredForward (Message message) {
		String destComponentName = ((StdKevoreeMessage) message.getContent()).getValue("destComponentName").get().toString();
		for (KevoreePort port : getBindedPorts()) {
			if (destComponentName.equals(port.getComponentName())) {
				forward(port, message);
				break;
			}
		}
	}

	private void remoteForward (Message message) {
		for (KevoreeChannelFragment fragment : getOtherFragments()) {
			forward(fragment, message);
		}
	}

	private void remoteFilteredForward (Message message) {
		String destNodeName = ((StdKevoreeMessage) message.getContent()).getValue("destNodeName").get().toString();
		for (KevoreeChannelFragment fragment : getOtherFragments()) {
			if (destNodeName.equals(fragment.getNodeName())) {
				forward(fragment, message);
				break;
			}
		}
	}

}