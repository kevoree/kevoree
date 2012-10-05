package org.kevoree.library.socketChannel;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.Message;
import scala.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/11/11
 * Time: 12:09
 */
//@Library(name = "JavaSE", names = {"Android"})
//@ChannelTypeFragment
public class RoundRobinSocketChannel extends SocketChannel {

    private static String currentUUIdMessage = "";
    private Random random = new Random();

    @Override
    public Object dispatch(Message message) {

		// disable broadcast by setting that all fragments have already get the message
		for (KevoreeChannelFragment fragment : this.getOtherFragments()) {
			message.getPassedNodes().add(fragment.getNodeName());
		}
        /*   Local Node  */
        int bindedSize = getBindedPorts().size() + getOtherFragments().size();
        int rang = random.nextInt(bindedSize);
        if(rang < getBindedPorts().size()){

			// remove the future receiver to be sure that the receiver accepts the message
			//message.getPassedNodes().remove(((ContainerNode) ((ComponentInstance) getBindedPorts().get(rang).getModelElement().eContainer()).eContainer()).getName());

			forward(getBindedPorts().get(rang),message);
        } else {
            rang = rang - getBindedPorts().size();

			// remove the future receiver to be sure that the receiver accepts the message
			message.getPassedNodes().remove(getOtherFragments().get(rang).getNodeName());

			forward(getOtherFragments().get(rang),message);
        }

        return null;
    }

}
