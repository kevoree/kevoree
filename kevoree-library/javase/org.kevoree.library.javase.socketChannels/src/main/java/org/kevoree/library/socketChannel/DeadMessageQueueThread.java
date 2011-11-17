package org.kevoree.library.socketChannel;

import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/11/11
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
public class DeadMessageQueueThread extends Thread {

    private boolean alive = true;
    private SocketChannel parentChannel;

    public void setTimer(Integer timer) {
        this.timer = timer;
    }

    private Integer timer;
    private BlockingQueue<Message> queue_node_dead = new LinkedBlockingQueue<Message>();
    private Integer maximum_deadqueue_size;
    private final Semaphore deadMessageSemaphore = new java.util.concurrent.Semaphore(1);

    public void stopProcess() {
        alive = false;
        queue_node_dead.clear();
    }

    public void addToDeadQueue(Message m) {

        logger.debug("addToDeadQueue to "+m.getDestNodeName());

        if (queue_node_dead.size() < maximum_deadqueue_size) {
            if ("true".equals(parentChannel.getDictionary().get("replay"))) {
                queue_node_dead.add(m);
                deadMessageSemaphore.release();
            }

        } else {
            logger.error("The maximum number of message buffer is reached");
        }
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeadMessageQueueThread(SocketChannel parent, Integer _timer, Integer _maximum_deadqueue_size) {
        parentChannel = parent;
        timer = _timer;
        maximum_deadqueue_size = _maximum_deadqueue_size;
    }


    public void run() {

        int size, i;
        Queue<Message> stepFailMsgQueue   = new LinkedList<Message>();

        while (alive)
        {
            size = queue_node_dead.size();

            logger.debug("size queue " + size);
            if (size > 0)
            {
                Message current=null;
                for (i = 0; i < size; i++) {

                    try
                    {
                        current = queue_node_dead.peek();
                        queue_node_dead.remove(current);
                        String host = parentChannel.getAddress(current.getDestNodeName());
                        int port = parentChannel.parsePortNumber(current.getDestNodeName());

                        logger.debug("Sending backup message to " + host + " port <"   + port);

                        /* adding the current node */
                        if (!current.getPassedNodes().contains(parentChannel.getNodeName())) {
                            current.getPassedNodes().add(parentChannel.getNodeName());
                        }
                        Socket client_consumer = parentChannel.getOrCreateSocket(host,port);

                        OutputStream os = client_consumer.getOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(os);
                        oos.writeObject(current);
                        oos.flush();

                        client_consumer = null;
                        os = null;
                        oos = null;


                    } catch (Exception e) {

                        try {
                            Thread.sleep(timer);
                            logger.warn("Unable to send message to  " + current.getDestNodeName());

                            String host = parentChannel.getAddress(current.getDestNodeName());
                            int port = parentChannel.parsePortNumber(current.getDestNodeName());

                            parentChannel.delete_link(host,port);
                            stepFailMsgQueue.add(current);

                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }  // for
                if (stepFailMsgQueue.size() > 0) {
                    logger.debug("Undo queue " + stepFailMsgQueue.size());
                    /* backup message connection refused */
                    queue_node_dead.addAll(stepFailMsgQueue);
                    stepFailMsgQueue.clear();
                }

            } else {
                try {
                    logger.debug("acquire");
                    deadMessageSemaphore.acquire();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }// while
        logger.debug("The Queue pool is closed ");
    }
}
