package org.kevoree.library.socketChannel;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.Message;
import scala.util.Random;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/11/11
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
@Library(name = "JavaSE", names = {"Android"})
@ChannelTypeFragment
public class RoundRobinSocketChannel extends SocketChannel {

    private static String currentUUIdMessage = "";
    private Random random = new Random();

    @Override
    public Object dispatch(Message message) {


        SocketMessage msgTOqueue=null;
        /*       Generate the UUID of the message           */
        String   currentUUIdMessage = UUID.randomUUID().toString();

        if (message instanceof SocketMessage) {
            msgTOqueue = (SocketMessage) message;
            //  logger.debug("Use an existing UUID"+msgTOqueue.getUuid());
        } else {
            msgTOqueue = new SocketMessage();
            msgTOqueue.setUuid(currentUUIdMessage);
            // logger.debug("Create a UUID :"+msgTOqueue.getUuid());
        }
        msgTOqueue.setContent(message.getContent());
        msgTOqueue.setPassedNodes(message.getPassedNodes());
        msgTOqueue.setDestChannelName(message.getDestChannelName());
        msgTOqueue.setInOut(message.getInOut());
        msgTOqueue.setTimeout(message.getTimeout());

        /*       Generate the UUID of the message           */
        /*   Local Node  */
        int bindedSize = getBindedPorts().size() + getOtherFragments().size();
        int rang = random.nextInt(bindedSize);
        if(rang < getBindedPorts().size()){
            forward(getBindedPorts().get(rang),msgTOqueue);
        } else {
            rang = rang - getBindedPorts().size();
            forward(getOtherFragments().get(rang),msgTOqueue);
        }

        return null;
    }

}
