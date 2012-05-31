package org.kevoree.library.nioChannel;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/11/11
 * Time: 22:54
 */
@Library(name = "JavaSE", names = {"Android"})
@ChannelTypeFragment
public class NioRoundRobin extends NioChannel {
	protected Logger logger = LoggerFactory.getLogger(NioRoundRobin.class);
	private Random random = new Random();

	@Override
	public Object dispatch (Message message) {
		int rang;
		if (message.getDestNodeName().equals(this.getNodeName())) {
			rang = random.nextInt(getBindedPorts().size());
		} else {
			rang = random.nextInt(getBindedPorts().size() + getOtherFragments().size());
		}

		if (rang < getBindedPorts().size()) {
			logger.debug("select rang: {} for channel {} with DestNodeName={}", new Object[]{rang, this.getName(), message.getDestNodeName()});
			logger.debug("send message to {}", getBindedPorts().get(rang).getName());
			forward(getBindedPorts().get(rang), message);
		} else {
			// FIXME the otherFragment must be a provided port else an exception will thrown
			rang = rang - getBindedPorts().size();
			logger.debug("select rang: {} for channel {} with DestNodeName={}", new Object[]{rang, this.getName(), message.getDestNodeName()});
			logger.debug("send message to {} on {}", getOtherFragments().get(rang).getName(), getOtherFragments().get(rang).getNodeName());
			forward(getOtherFragments().get(rang), message);
		}

		return null;
	}


}
