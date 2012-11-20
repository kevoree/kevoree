package org.daum.library.p2pSock;

import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 13/08/12
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public class QueueMsg  extends Thread {

    private boolean alive = true;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Integer timer;
    private BlockingQueue<Message> queue_node_dead = new LinkedBlockingQueue<Message>();
    private Integer maximum_deadqueue_size;
    private final Semaphore deadMessageSemaphore = new java.util.concurrent.Semaphore(1);
    private P2pSock parentChannel;

    public QueueMsg(P2pSock parentChannel, Integer _timer, Integer _maximum_deadqueue_size){
        this.parentChannel = parentChannel;
        timer = _timer;
        maximum_deadqueue_size = _maximum_deadqueue_size;
    }

    public void stopProcess () {
        alive = false;
        queue_node_dead.clear();
        this.interrupt();
    }


    public void setTimer (Integer timer) {
        this.timer = timer;
    }


    public void enqueue (Message m) {

        logger.debug("addToDeadQueue to {}", m.getDestNodeName());

        if (queue_node_dead.size() < maximum_deadqueue_size) {
            if ("true".equals(parentChannel.getDictionary().get("replay"))) {
                queue_node_dead.add(m);
                deadMessageSemaphore.release();
            }
        } else
        {
            logger.error("The maximum number of message buffer is reached");
        }
    }

    public void run () {

        int size, i;
        Queue<Message> stepFailMsgQueue = new LinkedList<Message>();

        while (alive) {
            size = queue_node_dead.size();

            logger.debug("size queue {}", size);
            if (size > 0) {
                Message current = null;
                for (i = 0; i < size; i++) {

                    try {
                        current = queue_node_dead.peek();
                        queue_node_dead.remove(current);
                        String host = parentChannel.getAddress(current.getDestNodeName());
                        int port = parentChannel.parsePortNumber(current.getDestNodeName());

                        logger.debug("Sending backup message to {}:{}", host, port);

                        /* adding the current node */
                        if (!current.getPassedNodes().contains(parentChannel.getNodeName())) {
                            current.getPassedNodes().add(parentChannel.getNodeName());
                        }
                        P2pClient client = new P2pClient(current.getDestNodeName(),host, port);
                        client.send(current);

                    } catch (Exception e) {
                            stepFailMsgQueue.add(current);
                    }
                }  // for
                if (stepFailMsgQueue.size() > 0) {
                    logger.debug("Undo queue {}", stepFailMsgQueue.size());
                    /* backup message connection refused */
                    queue_node_dead.addAll(stepFailMsgQueue);
                    stepFailMsgQueue.clear();
                }

            } else
            {
                try {
                    logger.debug("acquire");
                    deadMessageSemaphore.acquire();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }// while
        logger.debug("The Queue pool is closed");
    }

}
