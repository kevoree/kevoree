package socketChannel;

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
import org.codehaus.jackson.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
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
    private  BlockingQueue<SocketMessage> queue_node_dead=null;
    private Thread reception_messages=null;                                                                /* thread in charge for receiving messages   PRODUCTEUR  */
    private  ThreadNodeDead sending_messages_node_dead;
    private boolean alive=false;
    private static String currentUUIdMessage = "";                                                             /* Current ID of the message */
    private Socket client_server=null;
    private Integer number_max_queue=50;
    private HashMap<String,Integer> fragments=null;
    Semaphore sem1 = new Semaphore();
    Semaphore sem2 = new Semaphore();
    /*  Kryo kryo = new KryoReflectionFactorySupport();     */


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
        number_max_queue= Integer.parseInt(getDictionary().get("number_max_queue").toString());
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

        logger.debug("Socket channel is closing ");
        alive =false;
        try{
            if(!server.isClosed())
                server.close();
            reception_messages.interrupt();
            sending_messages_node_dead.interrupt();
        } catch (Exception e) {
            logger.error(""+e);
        }
        /* wait */
        try
        {
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

                SocketMessage msgTOqueue;
                Socket client_consumer;
                SocketMessage current;
                int port;
                String host;

                sem1.P();
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
                    logger.warn("Sending message to " + msgTOqueue.getDestNodeName() + " " + parsePortNumber(msgTOqueue.getDestNodeName()));

                    host =      getAddress(msgTOqueue.getDestNodeName());
                    port =        parsePortNumber(msgTOqueue.getDestNodeName());

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
                        sem2.V();
                    }else
                    {
                        logger.error("The maximum number of message buffer is reached");
                    }
                }
                sem1.V();

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
        while(alive)
        {
            try
            {
                client_server=null;

                client_server = server.accept();
                data = client_server.getInputStream();
                ObjectInputStream ois=new ObjectInputStream(data);
                SocketMessage  msg =null;

                try
                {

                    /*
                    ByteBuffer buffer;
                    buffer = (ByteBuffer) ois.readObject();
                    buffer.flip();
                    msg = kryo.readObject(buffer, SocketMessage.class);
                    */

                    String jsonPacket =  (String)ois.readObject();
                    RichString c = new RichString(jsonPacket);
                    msg = (SocketMessage) c.fromJSON(SocketMessage.class);

                    if(!msg.getPassedNodes().contains(getNodeName()))
                        msg.getPassedNodes().add(getNodeName());
                    logger.warn("Reading message from  " + msg.getPassedNodes());
                } catch (Exception e)
                {
                    logger.error("Unable to read object "+e);
                }



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
                        logger.warn("remove fragment " + msg.getUuid());
                        fragments.remove(msg.getUuid());
                    }else
                    {
                        logger.debug(fragments.get(msg.getUuid())+"/"+getOtherFragments().size());
                    }

                }else
                {
                    /* only two nodes */
                    remoteDispatch(msg);
                }

                client_server.close();
            } catch (Exception e)
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



    private class ThreadNodeDead extends Thread
    {
        public void run() {

            SocketMessage current=null;
            Socket client_consumer=null;
            while (alive)
            {

                if(queue_node_dead.size() > 0){


                    current =     queue_node_dead.peek();

                    try
                    {
                        logger.warn("Sending backup message to " + current.getDestNodeName() + " port <" + parsePortNumber(current.getDestNodeName()) + "> ");
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
                        logger.warn("Unable to send message to  " + current.getDestNodeName()+e);
                    }
                }else
                {
                    sem2.P();
                }

                try
                {

                    Thread.sleep(2000);
                } catch (Exception e2)
                {
                    logger.error("Unable "+e2);
                }

            }// while
            logger.debug("The Queue pool is closed ");

        }
    }


}
