package org.kevoree.library.socketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 07/10/11
 * Time: 09:30
 * To change this template use File | Settings | File Templates.
 */
/*
import com.esotericsoftware.kryo.serialize.*;
import com.esotericsoftware.kryo.*;
import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;
*/
import org.kevoree.annotation.*;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kevoree.extra.marshalling.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;



@Library(name = "JavaSE", names = {"Android"})
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name="port",defaultValue="9000",optional=true, fragmentDependant = true),
        @DictionaryAttribute(name="number_max_queue",defaultValue="50",optional=false),
        @DictionaryAttribute(name="timer",defaultValue="2000",optional=false)
}
)
public class SocketChannel extends  AbstractChannelFragment implements  Runnable{

    private ServerSocket server = null;
    private  BlockingQueue<SocketMessage> queue_node_dead=null;
    private Thread reception_messages=null;                                                                /* thread in charge for receiving messages   PRODUCTEUR  */
    private  ThreadNodeDead sending_messages_node_dead;
    private boolean alive=false;
    private static String currentUUIdMessage = "";                                                             /* Current ID of the message */
    private Integer number_max_queue=50;
    private Integer timer=50;
    private HashMap<String,Integer> fragments=null;

    /*  Kryo kryo = new KryoReflectionFactorySupport();     */
    final java.util.concurrent.Semaphore sem = new java.util.concurrent.Semaphore(1);
    final java.util.concurrent.Semaphore sem2 = new java.util.concurrent.Semaphore(1);
    private  Logger logger = LoggerFactory.getLogger(SocketChannel.class);

    @Override
    public Object dispatch(Message msg) {

        /*       Generate the UUID of the message           */
        currentUUIdMessage =UUID.randomUUID().toString();

        /*   Local Node  */
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, msg) ;
        }

        /*   Remote Node */
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!msg.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, msg);
            }
        }

        return null;
    }

    public String getAddress (String remoteNodeName) {
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        return ip;
    }


    public int parsePortNumber (String nodeName) {
        //logger.debug("look for port on " + nodeName);
        return KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "port",
                nodeName);
    }


    @Start
    public void startChannel()
    {
        logger.debug("Socket channel is starting ");
        number_max_queue= Integer.parseInt(getDictionary().get("timer").toString());
        timer =Integer.parseInt(getDictionary().get("timer").toString());
        queue_node_dead    =new LinkedBlockingQueue<SocketMessage>();
        reception_messages = new Thread(this);
        sending_messages_node_dead  = new ThreadNodeDead();
        alive =true;
        reception_messages.start();
        sending_messages_node_dead.start();
        fragments = new HashMap<String, Integer>();
        /*  kryo.register(SocketMessage.class);  */
    }

    @Stop
    public void stopChannel() {
        alive =false;
        logger.debug("Socket channel is closing ");

        try{
            if(!server.isClosed())
                server.close();
            reception_messages.interrupt();
            sending_messages_node_dead.interrupt();
        } catch (Exception e) {
            //logger.error(""+e);
        }
        /* wait */
        try
        {
            reception_messages.join();
        } catch (Exception e) {
            // ignore
        }
        logger.debug("Socket channel is closed ");

    }

    @Update
    public void updateChannel() {
        stopChannel();
        startChannel();
    }


    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender() {
            @Override
            public Object sendMessageToRemote(Message message) {

                final   SocketMessage msgTOqueue;
                final Socket client_consumer;
                final   SocketMessage current;
                int port;
                String host;


                try {
                    sem.acquire();
                } catch (InterruptedException e) {
                    // ignore
                }
                if(message instanceof SocketMessage)
                {
                    msgTOqueue =(SocketMessage)message;
                    //  logger.debug("Use an existing UUID"+msgTOqueue.getUuid());

                }else
                {
                    msgTOqueue = new SocketMessage();
                    msgTOqueue.setUuid(currentUUIdMessage);

                    // logger.debug("Create a UUID :"+msgTOqueue.getUuid());
                }

                msgTOqueue.setContent(message.getContent());
                msgTOqueue.setPassedNodes(message.getPassedNodes());
                msgTOqueue.setDestChannelName(message.getDestChannelName());
                msgTOqueue.setInOut(message.getInOut());
                msgTOqueue.setTimeout(message.getTimeout());
                msgTOqueue.setDestNodeName(remoteNodeName);


                try
                {

                    host =      getAddress(msgTOqueue.getDestNodeName());
                    port =        parsePortNumber(msgTOqueue.getDestNodeName());

                    logger.debug("Sending message to " + msgTOqueue.getDestNodeName() + " host < "+host+" >"+" port <"+ +port+"> "+parsePortNumber(msgTOqueue.getDestNodeName()));


                    client_consumer = new Socket(host,port);


                    /* adding the current node */
                    if(!msgTOqueue.getPassedNodes().contains(getNodeName()))
                        msgTOqueue.getPassedNodes().add(getNodeName());

                    OutputStream os=client_consumer.getOutputStream();
                    ObjectOutputStream oos=new ObjectOutputStream(os);

                    /*       KRYO
                    ByteBuffer buffer = ByteBuffer.allocateDirect(256);
                    kryo.writeObject(buffer, msgTOqueue);
                    oos.writeObject(buffer);
                    */


                    /* JSON */
                    RichJSONObject obj = new RichJSONObject(msgTOqueue);
                    oos.writeObject(obj.toJSON());

                    client_consumer.close();
                } catch (Exception e)
                {
                    logger.warn("Unable to send message to " + msgTOqueue.getDestNodeName() + " " + parsePortNumber(msgTOqueue.getDestNodeName())+e);

                    SocketMessage backup = new SocketMessage();

                    backup.setUuid(msgTOqueue.getUuid());
                    backup.setDestNodeName(msgTOqueue.getDestNodeName());
                    backup.setDestChannelName(msgTOqueue.getDestChannelName());
                    backup.setResponseTag(msgTOqueue.getResponseTag());
                    backup.setInOut(msgTOqueue.getInOut());
                    backup.setTimeout(msgTOqueue.getTimeout());
                    backup.setPassedNodes(msgTOqueue.getPassedNodes());
                    backup.setContent(msgTOqueue.getContent());

                    if(queue_node_dead.size() <  number_max_queue )
                    {
                        queue_node_dead.add(backup);
                        sem2.release();
                    }else
                    {
                        logger.error("The maximum number of message buffer is reached");
                    }
                }

                sem.release();


                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        } ;

    }


    @Override
    public void run() {

        InputStream data = null;
        int port;

        try
        {
            port = parsePortNumber(getNodeName());
            logger.debug("Running Socket server <"+getNodeName()+"> port <"+port+">");
            server = new ServerSocket(port);
        }
        catch (IOException e)
        {
            logger.error("Unable to create ServerSocket "+e);
        }
        int maxConcurrentClients = 16;
        final Semaphore sem = new Semaphore(maxConcurrentClients);
        Executor pool = Executors.newFixedThreadPool(16);
        while(alive)
        {
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                continue;
            }

            final Socket client;
            final InputStream stream;
            try {
                client = server.accept();
                stream = client.getInputStream();
            } catch (Exception e) {
                if(alive)
                    logger.warn("Failed to accept client or get its input stream", e);
                continue;
            }

            pool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        SocketMessage  msg =null;
                        try {
                            ObjectInputStream ois=new ObjectInputStream(stream);
                            String jsonPacket =  (String)ois.readObject();
                            RichString c = new RichString(jsonPacket);
                            msg = (SocketMessage) c.fromJSON(SocketMessage.class);
                        } catch (Exception e) {
                            if(alive)
                                logger.warn("Failed to accept client or get its input stream", e);

                        }

                        if(!msg.getPassedNodes().contains(getNodeName()))
                            msg.getPassedNodes().add(getNodeName());

                        logger.debug("Reading message from  " + msg.getPassedNodes());

                        if(getOtherFragments().size() > 1){

                            if (fragments.containsKey(msg.getUuid()))
                            {
                                // logger.debug("fragment exist "+msg.getUuid()+" "+   fragments.get(msg.getUuid())+" "+msg.getPassedNodes()+" port "+client_server.getPort());
                                fragments.put(msg.getUuid(),((fragments.get(msg.getUuid()))+new Integer(1)));
                            }
                            else
                            {
                                fragments.put(msg.getUuid(), new Integer(1));
                                //  logger.debug("new fragment "+ msg.getUuid()+" "+msg.getPassedNodes()+" port "+client_server.getPort());
                                remoteDispatch(msg);
                            }

                            if((fragments.get(msg.getUuid()) == (getOtherFragments().size())) )
                            {
                                try  {  Thread.sleep(500); } catch (Exception e2)  {}
                                logger.debug("Remove fragment " + msg.getUuid()+" "+fragments.size());
                                fragments.remove(msg.getUuid());
                            }

                        }else
                        {
                            /* only two nodes */
                            remoteDispatch(msg);
                        }

                    } finally {
                        sem.release();
                        try {
                            client.close();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                }
            });


        }   // while



        logger.debug("The ServerSocket pool is closing");

        try {
            if(server !=null)
                server.close();
            logger.debug("ServerSocket is closed");
        } catch (IOException e)
        {

        }
    }



    private class ThreadNodeDead extends Thread
    {
        public void run() {

            SocketMessage current=null;
            Socket client_consumer=null;
            int size,i;
            Queue<SocketMessage> queue_tmp;
            while (alive)
            {

                size =  queue_node_dead.size();

                if( size > 0){
                    queue_tmp       = new LinkedList<SocketMessage>();
                    for(i=0;i <size;i++){

                        current =     queue_node_dead.peek();
                        queue_node_dead.remove(current);
                        try
                        {
                            logger.debug("Sending backup message to " + current.getDestNodeName() + " port <" + parsePortNumber(current.getDestNodeName()) + "> ");
                            /* try to send  message  if the message is not send is backup in the queue queue_tmp */
                            client_consumer = new Socket(getAddress(current.getDestNodeName()),parsePortNumber(current.getDestNodeName()));

                            /* adding the current node */
                            if(!current.getPassedNodes().contains(getNodeName()))
                                current.getPassedNodes().add(getNodeName());

                            OutputStream os=client_consumer.getOutputStream();
                            ObjectOutputStream oos=new ObjectOutputStream(os);
                            RichJSONObject obj = new RichJSONObject(current);
                            oos.writeObject(obj.toJSON());
                            client_consumer.close();
                            queue_node_dead.remove(current);

                        } catch (Exception e)
                        {
                            if(alive)
                                logger.warn("Unable to send message to  " + current.getDestNodeName()+" "+e);
                            queue_tmp.add(current);
                            try  {  Thread.sleep(timer); } catch (Exception e2)  {}
                        }

                    }  // for
                    if(queue_tmp.size() > 0)
                    {
                        logger.debug("Undo queue "+queue_tmp.size());
                        /* backup message connection refused */
                        queue_node_dead.addAll(queue_tmp);
                        queue_tmp.clear();
                    }

                }else
                {
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


}
