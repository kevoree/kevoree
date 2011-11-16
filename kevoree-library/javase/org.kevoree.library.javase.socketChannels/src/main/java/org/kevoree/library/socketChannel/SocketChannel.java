package org.kevoree.library.socketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 07/10/11
 * Time: 09:30
 * To change this template use File | Settings | File Templates.
 */

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;

@Library(name = "JavaSE", names = {"Android"})
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "9000", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "maximum_size_messaging", defaultValue = "50", optional = true),
        @DictionaryAttribute(name = "timer", defaultValue = "2000", optional = true),
        @DictionaryAttribute(name = "replay", defaultValue = "true", optional = true, vals = {"true", "false"})
}
)
public class SocketChannel extends AbstractChannelFragment implements Runnable {

    private ServerSocket server = null;
    private List<Socket> localServerSockets = new ArrayList<Socket>();

    /* thread in charge for receiving messages   PRODUCTEUR  */
    private Thread reception_messages = null;
    private DeadMessageQueueThread sending_messages_node_dead;
    private boolean alive = false;
    /* Current ID of the message */


    private HashMap<String, Integer> fragments = null;
    final Semaphore sem = new java.util.concurrent.Semaphore(1);

    private Logger logger = LoggerFactory.getLogger(SocketChannel.class);

    public Map<String, Socket> getClientSockets() {
        return clientSockets;
    }

    private Map<String, Socket> clientSockets = new HashMap<String, Socket>();


    @Override
    public Object dispatch(Message message) {
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, message);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!message.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, message);
            }
        }
        return null;
    }

    public String getAddress(String remoteNodeName) {
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    public int parsePortNumber(String nodeName) throws IOException {
        try {
            //logger.debug("look for port on " + nodeName);
            return KevoreeFragmentPropertyHelper
                    .getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "port",
                            nodeName);
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Start
    public void startChannel() {
        logger.debug("Socket channel is starting ");
        Integer maximum_size_messaging = Integer.parseInt(getDictionary().get("maximum_size_messaging").toString());
        Integer timer = Integer.parseInt(getDictionary().get("timer").toString());
        sending_messages_node_dead = new DeadMessageQueueThread(this,timer,maximum_size_messaging);
        reception_messages = new Thread(this);
        alive = true;
        reception_messages.start();
        sending_messages_node_dead.start();
        fragments = new HashMap<String, Integer>();
    }

    @Stop
    public void stopChannel() {

        sending_messages_node_dead.stopProcess();
        alive = false;
        logger.debug("Socket channel is closing ");
        try {
            reception_messages.interrupt();
            sending_messages_node_dead.interrupt();
            if (!server.isClosed()) {
                server.close();
            }
        } catch (Exception e) {
            //logger.error(""+e);
        }
        for (Socket socket : clientSockets.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                //logger.error("Error while trying to close socket", e);
            }
        }
        for (Socket socket : localServerSockets) {
            try {
                socket.close();
            } catch (IOException e) {
                // logger.error("Error while trying to close socket", e);
            }
        }
        // clean cache sockets
        clientSockets.clear();
        localServerSockets.clear();
        logger.debug("Socket channel is closed ");
    }

    @Update
    public void updateChannel() {
        stopChannel();
        startChannel(); // TODO CHECK MSG IN QUEUE
    }


    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender() {
            @Override
            public Object sendMessageToRemote(Message message) {
                int port;
                String host;
                try {
                    sem.acquire();
                } catch (InterruptedException e) {
                    // ignore
                }
                try {
                    host = getAddress(message.getDestNodeName());
                    port = parsePortNumber(message.getDestNodeName());
                    // adding the current node  to passedNodes
                    if (!message.getPassedNodes().contains(getNodeName())) {
                        message.getPassedNodes().add(getNodeName());
                    }
                    logger.debug("Sending message to " + message.getDestNodeName() + " host <" + host + ">" + " port <"
                            + +port + "> " + parsePortNumber(message.getDestNodeName()) + "\t" + message
                            .getContent());
                    // create the link if not exist
                    Socket client_consumer = getOrCreateSocket(host,port);
                    OutputStream os = client_consumer.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(os);
                    oos.writeObject(message);
                    oos.flush();
                } catch (Exception e) {
                    try {
                        logger.warn("Unable to send message to " + message.getDestNodeName() + " " + parsePortNumber(
                                message.getDestNodeName()), e);

                        sending_messages_node_dead.addToDeadQueue(message);
                    } catch (IOException e1) {
                        logger.warn("", e1);
                    }
                }
                sem.release();
                return null;
            }
        };
    }


    @Override
    public void run() {
        int port;
        try {
            port = parsePortNumber(getNodeName());
            logger.debug("Running Socket server <" + getNodeName() + "> port <" + port + ">");
            server = new ServerSocket(port);
        } catch (IOException e) {
            logger.error("Unable to create ServerSocket", e);
        }
        int maxConcurrentClients = 16;
        final Semaphore sem = new Semaphore(maxConcurrentClients);
        Executor pool = Executors.newFixedThreadPool(16);
        while (alive) {
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                continue;
            }
            final Socket client;
            final InputStream stream;
            try {
                client = server.accept();
                localServerSockets.add(client);
                stream = client.getInputStream();
            } catch (Exception e) {
                if (alive) {
                    logger.warn("Failed to accept client or get its input stream", e);
                }
                continue;
            }
            pool.execute(new Runnable() {
                Message msg = null;
                boolean _alive = true;
                @Override
                public void run() {
                    while (_alive) {
                        if (!client.isConnected()) {
                            _alive = false;
                            break;
                        }
                        try {
                            if (stream != null) {
                                try {
                                    ObjectInputStream ois = new ObjectInputStream(stream);
                                    msg = (Message) ois.readObject();
                                } catch (Exception e) {
                                    if (alive) {
                                        //  logger.warn("Failed to accept client or get its input stream");
                                        _alive = false;
                                        msg = null;
                                        localServerSockets.remove(client);
                                    }
                                }
                            } else {
                                // the remote node close the channel (update, down )
                                _alive = false;
                                msg = null;
                                localServerSockets.remove(client);
                            }
                            if (msg != null) {
                                if (!msg.getPassedNodes().contains(getNodeName())) {
                                    msg.getPassedNodes().add(getNodeName());
                                }
                                logger.debug(
                                        "Reading message from  " + msg.getPassedNodes() + "\t" + msg.getContent() + "\t"
                                                + msg.getDestNodeName());

                                /* if (getOtherFragments().size() > 1) {
                                  //  if (fragments.containsKey(msg.getUuid())) {
                                        // logger.debug("fragment exist "+msg.getUuid()+" "+   fragments.get(msg.getUuid())+" "+msg.getPassedNodes()+" port "+client_server.getPort());
                                  //      fragments.put(msg.getUuid(), ((fragments.get(msg.getUuid())) + 1));
                                   // } else {
                                     //   fragments.put(msg.getUuid(), 1);
                                        //  logger.debug("new fragment "+ msg.getUuid()+" "+msg.getPassedNodes()+" port "+client_server.getPort());
                                        remoteDispatch(msg);
                                    //}
                                    if ((fragments.get(msg.getUuid()) == (getOtherFragments().size()))) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (Exception e2) {
                                        }
                                        logger.debug("Remove fragment " + msg.getUuid() + " " + fragments.size());
                                        fragments.remove(msg.getUuid());
                                    }
                                } else {
                                    remoteDispatch(msg);
                                }*/

                                remoteDispatch(msg);
                            }
                            msg = null;
                        } finally {
                            sem.release();
                        }
                    }
                    try {
                        client.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            });
        }   // while
        logger.debug("The ServerSocket pool is closing");
        try {
            if (server != null) {
                server.close();
            }
            logger.debug("ServerSocket is closed");
        } catch (IOException e) {
        }
    }


    public Socket getOrCreateSocket(String host,Integer port) throws IOException {
        Socket client_consumer = null;
        if (clientSockets.containsKey(host)) {
            // the link exist
            client_consumer = clientSockets.get(host);
            if (client_consumer.isClosed()) {
                // the link exist but is broken
                clientSockets.remove(host);
                client_consumer = new Socket(host, port);
                clientSockets.put(host, client_consumer);
            }
        } else {
            /// no link in cache
            client_consumer = new Socket(host, port);
            client_consumer.setSoTimeout(0);
            clientSockets.put(host, client_consumer);
        }
        return client_consumer;
    }

}
