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

@Library(name = "JavaSE")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name="port",defaultValue="9000",optional=true, fragmentDependant = true),
        @DictionaryAttribute(name="number_max_queue",defaultValue="1000",optional=false),
        @DictionaryAttribute(name="refresh_thread_loop",defaultValue="1000",optional=false)
}
)
public class SocketChannel extends  AbstractChannelFragment implements  Runnable{

    private  ServerSocket server = null;
    private  Socket client=null;

    private  Thread reception_messages;                                                   /* thread in charge for receiving messages */
    private  ThreadPool sending_messages;                                                 /* thread in charge for sending messages */
    private  boolean alive=false;

    private  Queue<SocketMessage> queue;                                                 /* Saves the messages */
    private Queue<SocketMessage> queue_tmp;                                              /* Saves the messages in case the message being sent are lost*/
    private static String currentUUIdMessage;                                                   /* Current ID of the message */
    public static HashMap<String, Integer> fragment = new HashMap<String, Integer>();                   /* autosave UUID already received to not broadcast again*/

    private Integer number_max_queue=1000;
    private Integer refresh_thread_loop=1000;

    private     HashMap<String, Integer> fragments;


    private  Logger logger = LoggerFactory.getLogger(SocketChannel.class);

    @Override
    public Object dispatch(Message msg) {

        SocketMessage current;
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
        logger.debug("look for port on " + nodeName);
        return KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "port",
                nodeName);
    }


    @Start
    public void startChannel()
    {
        logger.debug("Socket channel is starting ");

        try
        {
            number_max_queue= Integer.parseInt(getDictionary().get("number_max_queue").toString());
            refresh_thread_loop =    Integer.parseInt(getDictionary().get("refresh_thread_loop").toString());
        } catch (Exception e) {
            logger.error(" number_max_queue : "+e);
        }


        queue =new LinkedList<SocketMessage>();

        reception_messages = new Thread(this);
        sending_messages = new ThreadPool();
        alive =true;
        reception_messages.start();
        sending_messages.start();
        fragments = new HashMap<String, Integer>();
    }

    @Stop
    public void stopChannel() {

        logger.debug("Socket channel is closing ");
        alive =false;
        queue = null;
        try{
            if(!server.isClosed())
                server.close();
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

    public void send(SocketMessage data,String ip,Integer port) throws IOException {

        client = new Socket(ip,port);
        OutputStream os=client.getOutputStream();
        ObjectOutputStream oos=new ObjectOutputStream(os);
        oos.writeObject(data.getUuid());
        oos.writeObject(data.getDestNodeName());
        oos.writeObject(data.getDestChannelName()) ;
        oos.writeObject(data.getResponseTag());
        oos.writeObject(data.getInOut());
        oos.writeObject(data.getTimeout());
        oos.writeObject(data.getPassedNodes());
        oos.writeObject(data.getContent());

    }



    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, String remoteChannelName) {


        return new ChannelFragmentSender() {
            @Override
            public Object sendMessageToRemote(Message message) {


                SocketMessage sockMsg=null;
                String ip = getAddress(remoteNodeName);
                int port = parsePortNumber(remoteNodeName);

                SocketMessage current=null;

                try
                {

                    // logger.debug("enqueue message  ip <"+ip+">"+ " port <"+port+"> remoteNodeName "+remoteNodeName+"  "+ message.getPassedNodes()+" "+queue.size());

                    if(queue.size() <  number_max_queue ){

                        message.getPassedNodes().add(getNodeName());

                        if(message instanceof SocketMessage)
                        {
                            sockMsg =(SocketMessage)message;
                            logger.debug("UUID exist :"+sockMsg.getUuid());

                        }else
                        {
                            sockMsg = new SocketMessage();
                            sockMsg.setUuid(currentUUIdMessage);
                            sockMsg.setContent(message.getContent());
                            sockMsg.setPassedNodes(message.getPassedNodes());
                            sockMsg.setDestNodeName(remoteNodeName);

                            logger.debug("Create UUID :"+sockMsg.getUuid());
                        }
                        queue.add(sockMsg);

                    }else
                    {
                        logger.error("The maximum number of enqueue message is reached");
                    }

                } catch (Exception e)
                {
                    logger.error("createSender "+e);

                }
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        } ;

    }
    public void receive(SocketMessage data) throws IOException {



    }

    @Override
    public void run() {

        InputStream data = null;
        int port;


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
                client=null;
                client = server.accept();

                data = client.getInputStream();
                ObjectInputStream ois=new ObjectInputStream(data);

                try {


                    SocketMessage msg = new SocketMessage();
                    msg.setUuid((String)  ois.readObject());

                    if(getOtherFragments().size() > 1){


                        if (fragments.containsKey(msg.getUuid()))
                        {
                            fragments.put(msg.getUuid(), fragments.get((msg.getUuid()) + 1));
                        }
                        else
                        {
                            fragments.put(msg.getUuid(), 1);
                        }


                        if((fragments.get(msg.getUuid())  < (getOtherFragments().size()-1)) )
                        {
                            msg.setDestNodeName((String)ois.readObject());
                            msg.setDestChannelName((String)ois.readObject());
                            msg.setResponseTag((String)ois.readObject());
                            msg.setInOut((Boolean)ois.readObject());
                            msg.setTimeout((Long)ois.readObject());
                            msg.setPassedNodes((List<String>)ois.readObject());
                            msg.setContent(ois.readObject());
                            msg.getPassedNodes().add(getNodeName());


                            remoteDispatch(msg);

                        }else
                        {
                            fragments.remove(msg.getUuid());
                            msg =null;
                        }
                    }else
                    {
                        /* only one fragment */
                        msg.setDestNodeName((String)ois.readObject());
                        msg.setDestChannelName((String)ois.readObject());
                        msg.setResponseTag((String)ois.readObject());
                        msg.setInOut((Boolean)ois.readObject());
                        msg.setTimeout((Long)ois.readObject());
                        msg.setPassedNodes((List<String>)ois.readObject());
                        msg.setContent(ois.readObject());
                        msg.getPassedNodes().add(getNodeName());
                        remoteDispatch(msg);
                    }


                } catch (Exception e) {
                  //  logger.error("Failed to read object "+e);
                }
                client.close();
            } catch (IOException e)
            {
                logger.error("Failure to accept client "+e);
            }
        }
        logger.debug("The ServerSocket pool is closing");
        // while
        try {

            server.close();
            logger.debug("ServerSocket is closed");

        } catch (IOException e) {
            logger.error("Failure to close the ServerSocket"+e);
        }
    }


    private class ThreadPool extends Thread
    {
        public void run() {

            SocketMessage current=null;
            int qsize=0;
            while (alive)
            {
                /*pool */
                qsize =   queue.size();
                if(qsize > 0){

                    logger.debug("Queue pool size "+queue.size());
                    queue_tmp       = new LinkedList<SocketMessage>();

                    for(int i=0;i <qsize;i++){
                        current =     queue.peek();
                        queue.remove(current);
                        try
                        {
                            /* try to send  message  if the message is not send is backup in the queue queue_tmp */
                            send(current,getAddress(current.getDestNodeName()),parsePortNumber(current.getDestNodeName()));
                            client.close();
                        } catch (Exception e)
                        {
                            queue_tmp.add(current);
                            if(client != null)
                                try {
                                    client.close();
                                } catch (IOException e1) {

                                }
                        }
                    }
                    if(queue_tmp.size() > 0)
                    {
                        /* backup message connection refused */
                        queue.addAll(queue_tmp);
                        queue_tmp.clear();
                    }
                }

                try
                {
                    Thread.sleep(refresh_thread_loop);
                } catch (Exception e)
                {
                    logger.error("Pool "+e);

                }

            }// while
            logger.debug("The Queue pool is closed ");

        }
    }
}
