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

        /*       Generate the UUID of the message           */
        /*   Local Node  */
        int bindedSize = getBindedPorts().size() + getOtherFragments().size();
        int rang = random.nextInt(bindedSize);
        if(rang < getBindedPorts().size()){
            forward(getBindedPorts().get(rang),message);
        } else {
            rang = rang - getBindedPorts().size();
            forward(getOtherFragments().get(rang),message);
        }

        return null;
    }

}
