package socketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 07/10/11
 * Time: 09:30
 * To change this template use File | Settings | File Templates.
 */


import org.kevoree.annotation.*;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

@Library(name = "socketChannel")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name="port",defaultValue="9000",optional=true, fragmentDependant = true),
        @DictionaryAttribute(name="number_max_queue",defaultValue="50",optional=false)
}
)
public class SocketChannel extends  AbstractChannelFragment implements  Runnable{

    private ServerSocket server = null;
    private  BlockingQueue<SocketMessage> queue=null;
    private  BlockingQueue<SocketMessage> queue_node_dead=null;
    private Thread reception_messages=null;                                                                /* thread in charge for receiving messages   PRODUCTEUR  */
    private ThreadConsumer sending_messages=null;                                                             /* thread in charge for sending messages  CONSOMMATEUR  */
    private  ThreadNodeDead sending_messages_node_dead;
    private boolean alive=false;
    private static String currentUUIdMessage = "";                                                             /* Current ID of the message */
    private Socket client_server=null;

    private Integer number_max_queue=50;
    private HashMap<String, List<String>> fragments=null;

    private  Logger logger = LoggerFactory.getLogger(SocketChannel.class);

    @Override
    public Object dispatch(Message msg) {

        SocketMessage curr;
        /*
        Generate the UUID of the message
         */
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
        number_max_queue= Integer.parseInt(getDictionary().get("number_max_queue").toString());

        queue =new LinkedBlockingQueue<SocketMessage>();
        queue_node_dead    =new LinkedBlockingQueue<SocketMessage>();
        reception_messages = new Thread(this);
        sending_messages = new ThreadConsumer();
        sending_messages_node_dead  = new ThreadNodeDead();
        alive =true;
        reception_messages.start();
        sending_messages.start();
        sending_messages_node_dead.start();
        fragments = new HashMap<String, List <String>>();
    }

    @Stop
    public void stopChannel() {

        logger.debug("Socket channel is closing ");
        alive =false;

        try{
            if(!server.isClosed())
                server.close();

            reception_messages.interrupt();
            sending_messages.interrupt();
            sending_messages_node_dead.interrupt();
        } catch (Exception e) {
            logger.error(""+e);
        }

        /* wait */
        try
        {
            sending_messages.join();
            reception_messages.join();
        } catch (Exception e) {
            logger.error(""+e);
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

                SocketMessage msgTOqueue=null;
                String ip = getAddress(remoteNodeName);
                int port = parsePortNumber(remoteNodeName);

                if(queue.size() <  number_max_queue )
                {

                    if(message instanceof SocketMessage)
                    {
                        msgTOqueue =(SocketMessage)message;
                        logger.debug("Use an existing UUID"+msgTOqueue.getUuid()+ " "+remoteNodeName+ " "+msgTOqueue.getDestNodeName());

                    }else
                    {
                        msgTOqueue = new SocketMessage();
                        msgTOqueue.setUuid(currentUUIdMessage);

                        logger.debug("Create a UUID :"+msgTOqueue.getUuid()+ " "+remoteNodeName+ " "+msgTOqueue.getDestNodeName());
                    }

                    /* adding the current node */
                    if(!message.getPassedNodes().contains(getNodeName()))
                        message.getPassedNodes().add(getNodeName());

                    msgTOqueue.setContent(message.getContent());
                    msgTOqueue.setPassedNodes(message.getPassedNodes());
                    msgTOqueue.setDestChannelName(message.getDestChannelName());
                    msgTOqueue.setInOut(message.getInOut());
                    msgTOqueue.setTimeout(message.getTimeout());
                    msgTOqueue.setDestNodeName(remoteNodeName);


                    try
                    {
                        if(!message.getPassedNodes().contains(message.getDestNodeName()))
                        {
                            queue.put(msgTOqueue);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }


                }else
                {
                    logger.error("The maximum number of message buffer is reached");
                }

                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        } ;

    }


    @Override
    public void run() {

        InputStream data = null;
        int port;
        Integer v;
        SocketMessage msg;
        try
        {
            port = parsePortNumber(getNodeName());
            logger.debug("Running Socket server node <"+getNodeName()+"> port <"+port+">");
            server = new ServerSocket(port);
        }
        catch (IOException e)
        {
            logger.error("Fail to create ServerSocket "+e);
        }
        while(alive)
        {
            try
            {
                client_server=null;
                client_server = server.accept();

                data = client_server.getInputStream();
                ObjectInputStream ois=new ObjectInputStream(data);
                msg = new SocketMessage();

                try
                {
                    msg.setUuid((String)  ois.readObject());
                    msg.setDestNodeName((String)ois.readObject());
                    msg.setDestChannelName((String) ois.readObject());
                    msg.setResponseTag((String) ois.readObject());
                    msg.setInOut((Boolean) ois.readObject());
                    msg.setTimeout((Long) ois.readObject());
                    msg.setPassedNodes((List<String>) ois.readObject());
                    msg.setContent(ois.readObject());
                } catch (Exception e)
                {
                    logger.error("Failed to read object "+e);
                }


                   // msg.getPassedNodes().add(getNodeName());

                if(getOtherFragments().size() > 1){

                    if (fragments.containsKey(msg.getUuid()))
                    {

                        logger.debug("fragment exist "+msg.getUuid()+" "+   fragments.get(msg.getUuid())+" "+msg.getPassedNodes());

                        for(String node :  msg.getPassedNodes())
                        {
                              if(!fragments.get(msg.getUuid()).contains(node)){
                                     fragments.get(msg.getUuid()).add(node);
                              }

                        }

                        msg.setPassedNodes(fragments.get(msg.getUuid()));


                    }
                    else
                    {

                        fragments.put(msg.getUuid(), msg.getPassedNodes());
                                         msg.setPassedNodes(fragments.get(msg.getUuid()));
                        logger.debug("new fragment "+ msg.getUuid()+" "+   fragments.get(msg.getUuid())+" "+msg.getPassedNodes());
                        remoteDispatch(msg);

                    }

                    if((fragments.get(msg.getUuid()).size() == (getOtherFragments().size())) )
                    {
                        logger.debug("remove fragment "+msg.getUuid());
                        fragments.remove(msg.getUuid());
                    }

                }else
                {
                    /* only two nodes */
                    remoteDispatch(msg);
                }

                client_server.close();
            } catch (IOException e)
            {
                logger.debug("Failure to accept client "+e);
            }
        }   // while



        logger.debug("The ServerSocket pool is closing");

        try {

            server.close();
            logger.debug("ServerSocket is closed");
        } catch (IOException e)
        {

        }
    }


    private class ThreadConsumer extends Thread
    {
        public void run() {

            SocketMessage current=null;
            Socket client_consumer=null;
            while (alive)
            {

                try {
                    current =null;
                    current =     queue.take();
                } catch (InterruptedException e) {
                    logger.error("" + e);
                }

                try
                {


                    logger.debug("Sending message "+ current.getUuid()+" port <" + parsePortNumber(current.getDestNodeName()) + "> ");
                    /* try to send  message  if the message is not send is backup in the queue queue_tmp */


                    client_consumer = new Socket(getAddress(current.getDestNodeName()),parsePortNumber(current.getDestNodeName()));

                    OutputStream os=client_consumer.getOutputStream();
                    ObjectOutputStream oos=new ObjectOutputStream(os);

                    oos.writeObject(current.getUuid());
                    oos.writeObject(current.getDestNodeName());
                    oos.writeObject(current.getDestChannelName()) ;
                    oos.writeObject(current.getResponseTag());
                    oos.writeObject(current.getInOut());
                    oos.writeObject(current.getTimeout());
                    oos.writeObject(current.getPassedNodes());
                    oos.writeObject(current.getContent());

                    client_consumer.close();


                } catch (Exception e)
                {

                    try {
                        logger.debug("Fail to reach "+current.getDestNodeName());
                        queue_node_dead.put(current);
                        if(client_consumer !=null)
                            client_consumer.close();
                    } catch (Exception e1) {

                    }
                }



            }// while
            logger.debug("The Queue pool is closed ");

        }
    }


    private class ThreadNodeDead extends Thread
    {
        public void run() {

            SocketMessage current=null;
            Socket client_consumer=null;
            while (alive)
            {

                try {
                    current =null;
                    current =     queue_node_dead.take();
                } catch (InterruptedException e) {
                    logger.error("" + e);
                }

                try
                {
                    logger.debug("Sending message to node dead "+ current.getUuid()+" port <" + parsePortNumber(current.getDestNodeName()) + "> ");
                    /* try to send  message  if the message is not send is backup in the queue queue_tmp */
                    client_consumer = new Socket(getAddress(current.getDestNodeName()),parsePortNumber(current.getDestNodeName()));

                    OutputStream os=client_consumer.getOutputStream();
                    ObjectOutputStream oos=new ObjectOutputStream(os);

                    oos.writeObject(current.getUuid());
                    oos.writeObject(current.getDestNodeName());
                    oos.writeObject(current.getDestChannelName()) ;
                    oos.writeObject(current.getResponseTag());
                    oos.writeObject(current.getInOut());
                    oos.writeObject(current.getTimeout());
                    oos.writeObject(current.getPassedNodes());
                    oos.writeObject(current.getContent());

                    client_consumer.close();

                } catch (Exception e)
                {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e2) {

                    }
                    try {
                        logger.debug("Fail to reach "+current.getDestNodeName());
                        queue_node_dead.put(current);
                        if(client_consumer !=null)
                            client_consumer.close();
                    } catch (Exception e1) {

                    }
                }



            }// while
            logger.debug("The Queue pool is closed ");

        }
    }


}
