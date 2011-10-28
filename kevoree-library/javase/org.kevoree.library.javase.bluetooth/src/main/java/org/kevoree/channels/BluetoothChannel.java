package org.kevoree.channels;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreeFragmentPropertyHelper;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.Semaphore;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/09/11
 * Time: 14:12
 * To change this template use File | Settings | File Templates.
 * sudo hciconfig hci0 reset
 */

@Library(name = "JavaSE")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name="addressMAC",defaultValue="jed-0",optional=false, fragmentDependant = true)
}
)
public class BluetoothChannel extends AbstractChannelFragment implements  Runnable {


    private Logger logger = LoggerFactory.getLogger(BluetoothChannel.class);
    private boolean alive=false;
    private   RFClientActor client;

    public String getnodeMAC (String nodeName) {
        //logger.debug("look for port on " + nodeName);
        return KevoreeFragmentPropertyHelper.getPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "addressMAC", nodeName);
    }

    public static String getlocalMAC() throws BluetoothStateException {
        LocalDevice  localDevice = LocalDevice.getLocalDevice();
        return localDevice.getBluetoothAddress();
    }

    @Start
    public void startChannel(){

        try {
            getDictionary().put("addressMAC",getlocalMAC());
            client = new RFClientActor();
            client.start();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }
    @Stop
    public  void stopChannel(){
        if(client != null)
            client.stop();

    }
    public void dummy() {}

    @Override
    public Object dispatch(Message msg) {
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

    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender() {
            @Override
            public Object sendMessageToRemote(Message message) {

                logger.debug("Sending message to " + remoteNodeName);
                client.sendMessage(getnodeMAC(remoteNodeName),message);

                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        } ;

    }

    @Override
    public void run() {

        StreamConnectionNotifier service = null;
        StreamConnection conn;
        try {
            service = (StreamConnectionNotifier) Connector.open("btspp://localhost:" +
                    new UUID(0x1101).toString() +
                    ";name=service");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        int maxConcurrentClients = 16;
        final Semaphore sem = new Semaphore(maxConcurrentClients);

        while (true)
        {
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                continue;
            }
            StreamConnection connection=null;
            final ObjectInputStream in;
            try {
                // Wait for client connection
                connection = service.acceptAndOpen();
                RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);

                logger.debug("Reading message from " + dev.getBluetoothAddress());
                in = new ObjectInputStream(connection.openInputStream());
                Message obj = null;
                try
                {
                    obj = (Message)  in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                connection.close();

                remoteDispatch(obj);

            }catch (IOException e){
                logger.warn("Failed to accept And Open Client", e);
            }
            sem.release();

        }


    }
}
