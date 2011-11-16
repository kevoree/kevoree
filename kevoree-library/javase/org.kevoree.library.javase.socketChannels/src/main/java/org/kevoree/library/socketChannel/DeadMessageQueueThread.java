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

    public void stopProcess() {
        alive = false;
    }

    private BlockingQueue<Message> queue_node_dead = null;

    public void addToDeadQueue(Message m ){
        queue_node_dead.add(m);
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeadMessageQueueThread(SocketChannel parent){
        parentChannel =   parent;
    }


    public void run() {
        Message current = null;
        Socket client_consumer = null;
        int size, i;
        Queue<Message> queue_tmp;
        while (alive) {
            size = queue_node_dead.size();
            if (size > 0) {
                queue_tmp = new LinkedList<Message>();
                for (i = 0; i < size; i++) {
                    current = queue_node_dead.peek();
                    queue_node_dead.remove(current);
                    try {
                        logger.debug("Sending backup message to " + current.getDestNodeName() + " port <"
                                + parsePortNumber(current.getDestNodeName()) + "> ");

                        String host = getAddress(current.getDestNodeName());
                        int port = parsePortNumber(current.getDestNodeName());
                        if (clientSockets.containsKey(host)) {
                            client_consumer = clientSockets.get(host);
                        } else {
                            client_consumer = new Socket(host, port);
                            clientSockets.put(host, client_consumer);
                        }
                        /* adding the current node */
                        if (!current.getPassedNodes().contains(getNodeName())) {
                            current.getPassedNodes().add(getNodeName());
                        }
                        OutputStream os = client_consumer.getOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(os);


                        // RichJSONObject obj = new RichJSONObject(current);
                        oos.writeObject(current);
                        oos.flush();
//							client_consumer.close();
                        queue_node_dead.remove(current);
                    } catch (Exception e) {
                        queue_tmp.add(current);
                        if (alive) {
                            logger.warn("Unable to send message to  " + current.getDestNodeName() + e);
                        }

                        // remove the cache socket because the destination node maybe broken and the pipe need to create again
                        if (client_consumer != null) {
                            clientSockets.remove(getAddress(current.getDestNodeName()));
                        }

                        try {
                            Thread.sleep(timer);
                        } catch (Exception e2) {
                        }
                    }
                }  // for
                if (queue_tmp.size() > 0) {
                    logger.debug("Undo queue " + queue_tmp.size());
                    /* backup message connection refused */
                    queue_node_dead.addAll(queue_tmp);
                    queue_tmp.clear();
                }
            } else {
                try {
                    logger.debug("acquire");
                    sem2.acquire();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }// while
        logger.debug("The Queue pool is closed ");
    }
}
